(function (lcqtDirectory) {
    function rendererInject(lcqtDirectory) {
        const { ipcRenderer } = require('electron')
        const path = require('path')
        const cp = require('child_process'), originalSpawn = cp.spawn

        cp.spawn = function (cmd, args, opts) {
            if (!['javaw', 'java'].includes(path.basename(cmd, '.exe'))) {
                return originalSpawn(cmd, args, opts)
            }

            args = args.filter(e => e !== '-XX:+DisableAttachMechanism');

            delete opts.env['_JAVA_OPTIONS'];
            delete opts.env['JAVA_TOOL_OPTIONS'];
            delete opts.env['JDK_JAVA_OPTIONS'];

            return originalSpawn(
                cmd,
                [`-javaagent:${path.join(lcqtDirectory, 'patcher.jar')}`, ...args],
                opts
            );
        }

        function waitForElement(selector) {
            return new Promise(resolve => {
                if(document.querySelector(selector)) {
                    return resolve(document.querySelector(selector))
                }

                const observer = new MutationObserver(() => {
                    if(document.querySelector(selector)) {
                        resolve(document.querySelector(selector))
                        observer.disconnect()
                    }
                })

                observer.observe(document.body, {
                    subtree: true,
                    childList: true
                })
            })
        }

        waitForElement("#exit-button").then(exitButton => {
            let syringe = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAJrSURBVFhHzZgxTtxAFIbXUEQJpKFiuxSpwg1oqOjTpcoBotQ5RKQcIGWKSLkAJ6ChQIITUFFApCSKlIAUhHC+N35vZWZnvGN7Znc/6Zc9Y/u9n3/HNvJk1dR1/Rldoqc6tTxoWqFdHc7BsZfIuEXLNUnDL651Xb/WqTk49q05xfEDlTdJE0nOzBmHengOjrVNlk+SBr45I5gk86/QgzujoWySFD92bcI8SpKxmAtRLkkKb6BT6RLBJclWzN25mYZ91P65v7uCpaBBl8m3uhXu0b5eJteZya86lQ+K7uhuSpLGzJzB3J7u5oOiU/QPnaFNnZb5LpPv9LSy0GgHiTnjTA9ZkmI6RvQ5mQUaWHI+fpJdJqPPyVFQ2E/Op53kpozdbJi8SVLwGeoyZ6wuSQq+aeouZHlJUkDW3LYOe5lEqUlO9bR+cKGtuVudcjDOmeQRqvS0dLjIv1vlfZk7ySOd7gcXxu5WP8mLZnohoSSHJWdw8R8UQpLcRR/cKA35Y2evxRi93FJwi801mv2sA7lDL6qqumqGcTZ0GwRDtubey5iCN+g5u39lPBAxN00x14nEr+YMZ1JgfwvFfu4upN6wx0cbijxBP6Wix8ykoHOpJK25JCj00ZUM40yy7TrHZ3BywZtEKupuDFmDqTeKrblfzbAfQw2mkny3xihpcFRyRudjZgSW3ChzQokEsyRn5E4wW3JGzOCQBpbcuDeER8zgiW5TyZ5cFJZf7FtJDHnl5XlDLIJG/reSc9R+H/t8QvIfTnlo1E7u0beSlYOZua9Memj1YGZ9kxMw9NtZa1gvcwKmDtDNWpqbTCb/AZbFgRwcT3F7AAAAAElFTkSuQmCC'
            let clone = exitButton.cloneNode(false)
            clone.id = null
            clone.innerHTML = `<img src="${syringe}" width="20" height="20"/>`
            clone.addEventListener('click', () => ipcRenderer.send('LCQT_OPEN_WINDOW'))
            exitButton.parentNode.insertBefore(clone, exitButton)
        })
    }

    const { ipcMain, BrowserWindow, webContents } = require('electron')

    ipcMain.on('LCQT_OPEN_WINDOW', event => {
        let window = new BrowserWindow({
            parent: BrowserWindow.fromWebContents(event.sender),
            modal: true,
            center: true,
            width: 800,
            height: 600,
            resizable: false,
            webPreferences: {
                nodeIntegration: true,
                contextIsolation: false,
                enableRemoteModule: true
            }
        })

        window.loadURL(`file://${lcqtDirectory}/gui.asar/index.html`)
    })

    for(const contents of webContents.getAllWebContents()) {
        contents.removeAllListeners('devtools-opened')
        contents.executeJavaScript(`(${rendererInject})(${JSON.stringify(lcqtDirectory)})`)
    }
})