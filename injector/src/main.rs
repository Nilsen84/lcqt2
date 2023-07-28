#![feature(try_blocks)]

use std::{env, io};
use std::error::Error;
use std::net::{Ipv4Addr, TcpListener};
use std::path::{Path};
use std::process::{Command, Stdio};
use std::string::String;

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
                localappdata.clone() + r"\Programs\launcher\Lunar Client.exe",
                localappdata + r"\Programs\lunarclient\Lunar Client.exe"
            ]
        }
        "macos" => vec!["/Applications/Lunar Client.app/Contents/MacOS/Lunar Client".into()],
        "linux" => vec!["/usr/bin/lunarclient".into()],
        _ => Err("unsupported os")?
    };

    paths.iter()
        .find(|p| Path::new(p).exists())
        .ok_or(format!("searched in the following locations: [{}]", paths.join(", ")))
        .map(|p| p.clone())
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
    cmd.arg(format!("--inspect={}", port));
    #[cfg(not(debug_assertions))]
    cmd.stdout(Stdio::null())
        .stderr(Stdio::null())
        .stdin(Stdio::null());
    let mut cp = cmd.spawn().map_err(|e| format!("failed to start lunar: {}", e))?;

    let res = try {
        let mut debugger = ChromeDebugger::connect(port).map_err(|e| format!("failed to connect debugger: {}", e))?;

        let payload = format!(
            "require(`${{{}}}/gui.asar/main-inject.js`)()",
            serde_json::to_string(env::current_exe()?.parent().unwrap())?
        );

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
        std::process::exit(1);
    }
}