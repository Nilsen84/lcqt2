import renderer from './renderer-inject.js'

module.exports = function() {
    const cp = require('child_process'), originalSpawn = cp.spawn
    const path = require('path')
    const fs = require('fs')
    const https = require('https')
    const { app, dialog, shell, BrowserWindow, ipcMain } = require('electron')
    const semver = require('semver')
    const parse = require('shell-quote/parse')

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

    ipcMain.on('LCQT_READ_CONFIG', event => {
        event.returnValue = readConfigSync()
    })

    ipcMain.on('LCQT_WRITE_CONFIG', async (event, config) => {
        await fs.promises.writeFile(
            configPath,
            JSON.stringify(config, null, 4),
            'utf8'
        )
    })

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
            },
            autoHideMenuBar: true
        })

        window.on('close', () => {
            mainWin.focus()
        });

        window.loadURL(`file://${__dirname}/index.html`)
    })

    cp.spawn = (cmd, args, opts) => {
        if(!['javaw', 'java'].includes(path.basename(cmd, '.exe'))) {
            return originalSpawn(cmd, args, opts)
        }

        let config = readConfigSync()

        args = args.filter(e => e !== '-XX:+DisableAttachMechanism');
        delete opts.env['_JAVA_OPTIONS'];
        delete opts.env['JAVA_TOOL_OPTIONS'];
        delete opts.env['JDK_JAVA_OPTIONS'];

        args.splice(
            Math.max(0, args.indexOf('-cp')),
            0,
            ...config.agents ? config.agents.filter(a => a.enabled).map(a => `-javaagent:${a.path}=${a.option}`) : [],
            `-javaagent:${path.join(installDir, 'agent.jar')}`,
            ...config.jvmArgsEnabled ? parse(config.jvmArgs) : []
        )

        if(config.crackedEnabled && config.crackedUsername) {
            args.push('--username', config.crackedUsername)
        }

        return originalSpawn(
            config.customJvmEnabled && config.customJvm || cmd,
            args,
            opts
        )
    }

    app.on('web-contents-created', (event, webContents) => {
        webContents.on('dom-ready', () => {
            if(webContents.getURL().includes(__dirname)) return
            webContents.executeJavaScript(renderer)
        })
    })
}