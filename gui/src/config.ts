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
        const configPath: string = window.process.argv.pop()
        const fs = require('fs')

        try {
            config = {...config, ...JSON.parse(fs.readFileSync(configPath, 'utf8'))}
        }catch(e) {
            console.error(e)
        }

        if (typeof(config.customJvm) == 'boolean') {
            config.customJvm = ''
        }

        window.onchange = () => {
            fs.writeFileSync(
                configPath,
                JSON.stringify(config, null, 4),
                'utf8'
            )
        }
    }

    return writable(config)
}

export const config = createConfig()