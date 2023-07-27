import syringe from './syringe-solid.png'
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

    let lcqtOpts = ipcRenderer.sendSync('LCQT_GET_LAUNCH_OPTIONS')

    return originalSpawn(
        lcqtOpts.customJvm || cmd,
        [...lcqtOpts.jvmArgs, ...args, ...lcqtOpts.minecraftArgs],
        opts
    )
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
    let clone = exitButton.cloneNode(false)
    clone.id = null
    clone.innerHTML = `<img src="${syringe}" width="20" height="20"/>`
    clone.addEventListener('click', () => ipcRenderer.send('LCQT_OPEN_WINDOW'))
    exitButton.parentNode.insertBefore(clone, exitButton)
})