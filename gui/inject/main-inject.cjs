import renderer from './renderer-inject.js'

module.exports = function() {
    const { app, ipcMain, BrowserWindow, webContents, dialog } = require('electron')
    const path = require('path')
    const fs = require('fs')
    const parse = require('shell-quote/parse')
    const semver = require('semver')
    const https = require('https')

    const installDir = path.dirname(__dirname)

    https.get(
        {
            hostname: 'api.github.com',
            path: '/repos/Nilsen84/lunar-client-qt/releases/latest',
            headers: {
                'User-Agent': 'Mozilla/5.0'
            }
        },
        res => {
            if(res.statusCode !== 200) {
                return console.error(
                    '[LCQT] Failed to check for updates -',
                    res.statusCode,
                    res.statusMessage
                )
            }

            let body = []
            res.on('data', chunk => body.push(chunk))
            res.on('end', () => {
                let json = JSON.parse(Buffer.concat(body).toString())
                if (semver.lt(__APP_VERSION__, json.tag_name)) {
                    dialog.showMessageBoxSync({
                        title: 'LCQT Update',
                        message: 'A new version of Lunar Client Qt is available: ' +
                            `${__APP_VERSION__} -> ${json.tag_name}\n` +
                            `${json.html_url}`
                    })
                }
            })
        }
    )

    let configDir = path.join(app.getPath('appData'), 'lcqt2')
    if(!fs.existsSync(configDir)) fs.mkdirSync(configDir)
    let configPath = path.join(configDir, 'config.json')

    function readConfigSync() {
        try {
            return JSON.parse(fs.readFileSync(configPath, 'utf8'))
        } catch {
            return {}
        }
    }

    ipcMain.on('LCQT_OPEN_WINDOW', event => {
        let mainWin = BrowserWindow.fromWebContents(event.sender)

        let window = new BrowserWindow({
            parent: mainWin,
            modal: true,
            center: true,
            width: 650,
            height: 500,
            resizable: true,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false,
                additionalArguments: [configPath]
            },
            autoHideMenuBar: true
        })

        window.on('close', e => {
            mainWin.focus()
        });
        window.loadURL(`file://${__dirname}/index.html`)
    })

    ipcMain.on('LCQT_GET_LAUNCH_OPTIONS', (event) => {
        let config = readConfigSync()

        event.returnValue = {
            configPath,
            installDir,
            jvmArgs: parse(config.jvmArgs)
        }
    })

    for(const contents of webContents.getAllWebContents()) {
        contents.removeAllListeners('devtools-opened')
        contents.executeJavaScript(renderer)
    }
}