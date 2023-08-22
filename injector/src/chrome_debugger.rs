use serde::Deserialize;
use serde_json::json;
use std::error::Error;
use std::net::TcpStream;
use std::str::FromStr;
use tungstenite::http::Uri;
use tungstenite::{Message, WebSocket};

#[derive(Deserialize, Debug)]
pub struct CDPError {
    pub code: i64,
    pub message: String,
}

#[derive(Deserialize, Debug)]
#[serde(untagged)]
pub enum Response {
    Event {
        method: String,
        params: serde_json::Value,
    },
    Response {
        id: i32,
        result: Option<serde_json::Value>,
        error: Option<CDPError>,
    },
}

pub struct ChromeDebugger {
    ws: WebSocket<TcpStream>,
}

impl ChromeDebugger {
    pub fn connect_url(uri: impl AsRef<str>) -> Result<ChromeDebugger, Box<dyn Error>> {
        let url = Uri::from_str(uri.as_ref())?;
        let host = url.host().ok_or("url doesn't contain a host name")?;
        let stream = TcpStream::connect((host, url.port_u16().unwrap_or(80)))?;

        Ok(Self {
            ws: tungstenite::client(&url, stream)?.0,
        })
    }

    pub fn send(&mut self, id: i32, method: &str, params: serde_json::Value) -> Result<(), String> {
        self.ws
            .write(Message::Text(
                serde_json::to_string(&json!({
                    "id": id,
                    "method": method,
                    "params": params
                }))
                .map_err(|e| format!("write {method}: json: {e}"))?,
            ))
            .map_err(|e| format!("write {method}: {e}"))
    }

    pub fn flush(&mut self) -> Result<(), String> {
        self.ws.flush().map_err(|e| format!("flush: {e}"))
    }

    pub fn read(&mut self) -> Result<Response, String> {
        let txt = match self.ws.read().map_err(|e| format!("read: {e}"))? {
            Message::Text(txt) => txt,
            _ => unreachable!(),
        };
        serde_json::from_str(&txt).map_err(|e| format!("read: json: {e}"))
    }
}
