import {writable} from "svelte/store";

const dev: boolean = import.meta.env.DEV

let defaultConfig = {
    cosmeticsEnabled: false,

    crackedEnabled: false,
    crackedUsername: '',

    freelookEnabled: false,

    noHitDelayEnabled: false,

    customJvmEnabled: false,
    customJvm: '',

    jvmArgsEnabled: true,
    jvmArgs: '',

    debugModsEnabled: false,

    fpsSpoofEnabled: false,
    fpsSpoofMultiplier: 1.0,

    rawInputEnabled: false,

    packFixEnabled: false,

    agents: [] as { enabled: boolean, path: string, option: string }[]
}

export type Config = typeof defaultConfig

function createConfig() {
    let config: Config = { ...defaultConfig }

    if (!dev) {
        const { ipcRenderer } = require('electron')
        config = {...config, ...ipcRenderer.sendSync('LCQT_READ_CONFIG')}

        if (typeof(config.customJvm) == 'boolean') {
            config.customJvm = ''
        }

        window.onchange = () => {
            ipcRenderer.send('LCQT_WRITE_CONFIG', config)
        }
    }

    return writable(config)
}

export const config = createConfig()