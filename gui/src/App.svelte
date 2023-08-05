<script lang="ts">
    import Modules from "./pages/Modules.svelte";
    import Settings from "./pages/Settings.svelte";
    import github from "./assets/github.svg"
    import discord from "./assets/discord.svg"
    import type { Config } from "./config";

    const dev: boolean = import.meta.env.DEV

    let pages = [
        {
            name: 'Modules',
            component: Modules
        },
        {
            name: 'Settings',
            component: Settings
        }
    ]

    let selected = pages[0]

    let config: Config = {
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

        packFixEnabled: false
    }

    if(!dev) {
        const configPath: string = window.process.argv.pop()
        const fs = require('fs')

        try {
            config = {...config, ...JSON.parse(fs.readFileSync(configPath, 'utf8'))}
        }catch(e) {
            console.error(e)
        }

        if(typeof(config.customJvm) == 'boolean') {
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

    function openUrl(url: string) {
        if(dev) {
            window.open(url, '_blank')
        }else {
            require('electron').shell.openExternal(url)
        }
    }
</script>

<svelte:head>
    <title>Lunar Client Qt {__APP_VERSION__}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@200;300;400;500;600&display=swap" rel="stylesheet">
</svelte:head>

<main class="{dev ? 'w-[900px] h-[600px]' : 'w-screen h-screen'} bg-black flex">
    <div class="h-full w-36 bg-white flex flex-col items-stretch text-center">
        <div class="flex p-3 gap-1 justify-center text-lg">
            LCQT
            <button on:click={() => openUrl('https://github.com/Nilsen84/lcqt2')}>
                <img src="{github}" width="20" alt="github">
            </button>
            <button on:click={() => openUrl('https://discord.gg/mjvm8PzB2u')}>
                <img src="{discord}" width="20" alt="discord">
            </button>
        </div>
        <hr>
        {#each pages as page}
            <label class="cursor-pointer">
                <input type="radio" class="peer hidden" value={page} bind:group={selected}>
                <div class="py-3 peer-checked:bg-blue-500 peer-checked:text-white">
                    {page.name}
                </div>
            </label>
        {/each}
    </div>
    <div class="bg-gray-200 flex-grow">
        <svelte:component this={selected.component} config={config}></svelte:component>
    </div>
</main>