#![feature(try_blocks)]
#![allow(dead_code)]

use std::{env, io};
use std::error::Error;
use std::io::{BufRead, BufReader, ErrorKind, Read};
use std::path::{Path};
use std::process::{Command, Stdio};
use std::string::String;

use serde_json::json;

use crate::chrome_debugger::{ChromeDebugger, Response};

mod chrome_debugger;

fn find_lunar_executable() -> Result<String, String> {
    let paths: Vec<String> = match env::consts::OS {
        "windows" => {
            let localappdata = env::var("localappdata").or(Err("%localappdata% not defined"))?;

            vec![
                format!(r"{localappdata}\Programs\launcher\Lunar Client.exe"),
                format!(r"{localappdata}\Programs\lunarclient\Lunar Client.exe"),
            ]
        }
        "macos" => vec![
            "/Applications/Lunar Client.app/Contents/MacOS/Lunar Client".into(),
            format!(
                "{}/Applications/Lunar Client.app/Contents/MacOS/Lunar Client",
                env::var("HOME").or(Err("$HOME not defined"))?
            ),
        ],
        _ => vec!["/usr/bin/lunarclient".into()],
    };

    paths.iter()
        .find(|p| Path::new(p).exists())
        .ok_or_else(|| {
            let mut err = String::from("searched in the following locations:");
            for path in &paths {
                err += &format!("\n - {}", path)
            }
            err
        })
        .map(|p| p.clone())
}

fn wait_for_websocket_url(stream: impl Read) -> io::Result<String> {
    let reader = BufReader::new(stream);
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

    let mut cp = Command::new(&lunar_exe)
        .arg("--inspect-brk=0")
        .stdin(Stdio::null())
        .stdout(if cfg!(debug_assertions) { Stdio::inherit() } else { Stdio::null() })
        .stderr(Stdio::piped())
        .spawn().map_err(|e| format!("failed to start lunar: {}", e))?;

    let res = try {
        let url = wait_for_websocket_url(cp.stderr.take().unwrap())
            .map_err(|e| format!("failed to extract websocket url: {e}"))?;

        println!("[LCQT] Connecting to {url}");

        let mut debugger = ChromeDebugger::connect_url(url)
            .map_err(|e| format!("failed to connect debugger: {}", e))?;

        let payload = format!(
            "require(`${{{}}}/gui.asar/main-inject.js`)()",
            serde_json::to_string(env::current_exe()?.parent().unwrap())?
        );

        debugger.send(1, "Debugger.enable", json!({}))?;
        debugger.send(2, "Runtime.runIfWaitingForDebugger", json!({}))?;
        debugger.flush()?;

        loop {
            match debugger.read()? {
                Response::Event { method, params } if method == "Debugger.paused" => {
                    let call_frame = params.pointer("/callFrames/0/callFrameId").unwrap();

                    debugger.send(3, "Debugger.evaluateOnCallFrame", json!({
                        "callFrameId": call_frame,
                        "expression": payload
                    }))?;
                    debugger.send(4, "Debugger.disable", json!({}))?;
                    debugger.flush()?;
                }
                ref r @ Response::Response{ id, ref error, .. } => {
                    println!("{:?}", r);
                    if error.is_some() {
                        Err("CDP Error")?
                    }
                    if id == 4 {
                        break;
                    }
                }
                _ => {}
            }
        }
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
