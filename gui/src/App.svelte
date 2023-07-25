<script lang="ts">
    import Module from "./components/Module.svelte";
    import Switch from "./components/Switch.svelte";
    import github from "./assets/github.svg"
    import discord from "./assets/discord.svg"
    const dev: boolean = import.meta.env.DEV

    type Config = {
        cosmeticsEnabled: boolean,
        freelookEnabled: boolean,
        jvmEnabled: boolean,
        customJvm: boolean,
        jvmArgs: string
    }

    let config: Config = {
        cosmeticsEnabled: false,
        freelookEnabled: false,
        jvmEnabled: true,
        customJvm: false,
        jvmArgs: ''
    }


    if(!dev) {
        const { ipcRenderer } = require('electron')

        ipcRenderer.invoke('LCQT_READ_CONFIG').then(savedConfig => {
            config = {...config, ...savedConfig}
        })

        window.addEventListener('beforeunload', () => {
            ipcRenderer.send('LCQT_SAVE_CONFIG', config)
        })
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

<main
        class="flex flex-col gap-4 p-5 bg-[#f8f9fa] items-stretch fixed w-full h-full overflow-y-scroll"
        class:max-w-[650px]={dev}
        class:max-h-[500px]={dev}
>

    <div class="flex flex-row text-2xl font-normal items-center gap-2 justify-center">
        Lunar Client Qt {__APP_VERSION__.slice(1)}
        <button on:click={() => openUrl('https://github.com/Nilsen84/lcqt2')}>
            <img src="{github}" width="24" alt="github">
        </button>
        <button on:click={() => openUrl('https://discord.gg/SHZUYWhwDP')}>
            <img src="{discord}" width="24" alt="discord">
        </button>
    </div>

    <Module name="Cosmetics Unlocker"></Module>

    <Module name="Cracked Account">
        <div class="flex flex-row w-full justify-between items-center">
            Username:
            <input type="text" class="p-1 outline-none" spellcheck="false" placeholder="Player999"/>
        </div>
    </Module>

    <Module name="Freelook Enable"></Module>

    <Module name="JVM" bind:enabled={config.jvmEnabled}>
        <div class="flex flex-row items-center gap-2">
            <Switch bind:enabled={config.customJvm}></Switch>
            <nobr>Custom JVM</nobr>
            <div class="w-10"></div>
            <input type="text" class="p-1 outline-none w-96 flex-grow" placeholder="C:\Path\To\Java Installation\bin\javaw.exe" spellcheck="false" disabled="{!config.customJvm}">
        </div>

        <div class="text-center mt-4">JVM Args</div>

        <textarea
                class="resize-none outline-none p-2 w-full break-all mt-1"
                spellcheck="false"
                rows="5"
                bind:value={config.jvmArgs}
        />
    </Module>
</main>

<style>
    main::-webkit-scrollbar {
        display: none;
    }
    textarea::-webkit-scrollbar {
        display: none;
    }
</style>
