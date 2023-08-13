#![feature(try_blocks)]
#![allow(dead_code)]

use std::{env, io, thread};
use std::error::Error;
use std::io::{BufRead, BufReader, ErrorKind};
use std::net::{Ipv4Addr, TcpListener};
use std::path::{Path};
use std::process::{Child, Command, Stdio};
use std::string::String;
use std::time::Duration;

use serde_json::json;

use crate::chrome_debugger::ChromeDebugger;

mod chrome_debugger;

fn free_port() -> io::Result<u16> {
    Ok(TcpListener::bind((Ipv4Addr::LOCALHOST, 0))?.local_addr()?.port())
}

fn find_lunar_executable() -> Result<String, String> {
    let paths: Vec<String> = match env::consts::OS {
        "windows" => {
            let localappdata = env::var("localappdata").or(Err("%localappdata% not defined"))?;

            vec![
                format!(r"{localappdata}\Programs\launcher\Lunar Client.exe"),
                format!(r"{localappdata}\Programs\lunarclient\Lunar Client.exe")
            ]
        }
        "macos" => vec![
            "/Applications/Lunar Client.app/Contents/MacOS/Lunar Client".into(),
            format!(
                "{}/Applications/Lunar Client.app/Contents/MacOS/Lunar Client",
                env::var("HOME").or(Err("$HOME not defined"))?
            )
        ],
        "linux" => vec!["/usr/bin/lunarclient".into()],
        _ => Err("unsupported os")?
    };

    paths.iter()
        .find(|p| Path::new(p).exists())
        .ok_or(format!("searched in the following locations: [{}]", paths.join(", ")))
        .map(|p| p.clone())
}

fn wait_for_websocket_url(child: &mut Child) -> io::Result<String> {
    let reader = BufReader::new(child.stderr.take().unwrap());
    for line in reader.lines() {
        if let Some(url) = line?.strip_prefix("Debugger listening on ") {
            return Ok(url.into())
        }
    }
    Err(io::Error::new(ErrorKind::UnexpectedEof, "'Debugger listening on ' was never printed"))
}

fn run() -> Result<(), Box<dyn Error>> {
    let lunar_exe = match env::args().nth(1) {
        Some(arg) => arg,
        _ => find_lunar_executable().map_err(|e|
            format!("failed to locate lunars launcher: {}", e)
        )?
    };

    let port = free_port()?;

    let mut cmd = Command::new(&lunar_exe);
    cmd.env("__GL_THREADED_OPTIMIZATIONS", "0");
    cmd.arg(format!("--inspect={}", port));
    cmd.stdin(Stdio::null());
    cmd.stderr(Stdio::piped());
    #[cfg(not(debug_assertions))]
    cmd.stdout(Stdio::null());
    let mut cp = cmd.spawn().map_err(|e| format!("failed to start lunar: {}", e))?;

    let res = try {
        let url = wait_for_websocket_url(&mut cp)?;
        println!("[LCQT] Connecting to {url}");
        let mut debugger = ChromeDebugger::connect_url(url).map_err(|e| format!("failed to connect debugger: {}", e))?;

        // otherwise you get "ReferenceError: require is not defined"
        thread::sleep(Duration::from_millis(1000));

        let payload = format!(
            "require(`${{{}}}/gui.asar/main-inject.js`)()",
            serde_json::to_string(env::current_exe()?.parent().unwrap())?
        );

        println!("[LCQT] Sending payload");

        debugger.send("Runtime.evaluate", json!({
            "expression": payload,
            "includeCommandLineAPI": true
        }))?;
    };

    if let Err(_) = res {
        let _ = cp.kill();
    }

    res
}

fn main() {
    if let Err(e) = run() {
        eprintln!("[error] {}", e);
        if cfg!(windows) {
            Command::new("cmd.exe").arg("/c").arg("pause").status().unwrap();
        }
        std::process::exit(1);
    }
}
