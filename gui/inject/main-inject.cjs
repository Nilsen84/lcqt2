import renderer from './renderer-inject.js'

module.exports = function() {
    const { app, ipcMain, BrowserWindow, webContents, dialog, shell } = require('electron')
    const path = require('path')
    const fs = require('fs')
    const parse = require('shell-quote/parse')
    const semver = require('semver')
    const https = require('https')

    const installDir = path.dirname(__dirname)

    https.get(
        {
            hostname: 'api.github.com',
            path: '/repos/Nilsen84/lcqt2/releases/latest',
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
                    let res = dialog.showMessageBoxSync({
                        title: 'LCQT Update',
                        message: 'A new version of Lunar Client Qt is available: ' +
                            `${__APP_VERSION__} -> ${json.tag_name}`,
                        buttons: ['Open URL', 'OK'],
                        noLink: true
                    })

                    if(res === 0) {
                        shell.openExternal(json.html_url)
                    }
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
            width: 900,
            height: 600,
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
            customJvm: config.customJvmEnabled && config.customJvm,
            jvmArgs: [
                `-javaagent:${path.join(installDir, 'agent.jar')}=${configPath}`,
                ...config.jvmArgsEnabled ? parse(config.jvmArgs) : []
            ],
            minecraftArgs: config.crackedEnabled && config.crackedUsername ? [
                '--username', config.crackedUsername
            ] : []
        }
    })

    for(const contents of webContents.getAllWebContents()) {
        contents.removeAllListeners('devtools-opened')
        contents.executeJavaScript(renderer)
    }

    app.on('web-contents-created', (event, webContents) => {
        webContents.on('dom-ready', () => {
            if(webContents.getURL().includes(__dirname)) return

            webContents.removeAllListeners('devtools-opened')
            webContents.executeJavaScript(renderer)
        })
    })
}