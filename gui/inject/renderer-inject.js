import syringe from './syringe.svg'

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

waitForElement(".fa-gears").then(faGears => {
    let settingsButton = faGears.parentNode

    let clone = settingsButton.cloneNode(false)
    clone.id = null
    clone.innerHTML = `<img src='${syringe}' width="30" alt="lcqt"/>`
    clone.addEventListener('click', () => window.electron.ipcRenderer.sendMessage('LCQT_OPEN_WINDOW'))

    settingsButton.parentNode.insertBefore(clone, settingsButton)
})
