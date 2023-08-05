<script lang="ts">
    import circleLeft from "../assets/circle-left.png"
    import {onMount} from "svelte";
    import type {Config} from "../config";

    export let config: Config

    const modules = [
        {
            name: 'Cosmetics Unlocker',
            key: 'cosmeticsEnabled',
        },
        {
            name: 'Cracked Account',
            key: 'crackedEnabled',
            settings: [
                {
                    type: 'text',
                    name: 'Username',
                    placeholder: 'Player999',
                    key: 'crackedUsername',
                }
            ],
        },
        {
            name: 'Freelook Enable',
            key: 'freelookEnabled',
        },
        {
            name: 'NoHitDelay',
            key: 'noHitDelayEnabled',
        },
        {
            name: 'Debug + Staff Mods',
            key: 'debugModsEnabled'
        },
        /*{ debug object just for testing everything works fine
            name: 'FPS Spoof',
            key: 'fpsSpoofEnabled',
            settings: [{
                type: 'slider',
                name: 'Multiplier',
                min: 0.5,
                max: 10.0,
                step: 0.1,
                key: 'fpsSpoofMultiplier'
            },
                {
                    type: 'text',
                    name: 'Username',
                    placeholder: 'Player999',
                    key: 'crackedUsername',
                },{
                    type: 'slider',
                    name: 'Multiplier',
                    min: 0.5,
                    max: 10.0,
                    step: 0.1,
                    key: 'fpsSpoofMultiplier'
                },
                {
                    type: "combo",
                    name: "Test",
                    values: ["Hii", "asdasdas"],
                    key: "asd"
                }]
        },*/
        {
            name: 'FPS Spoof',
            key: 'fpsSpoofEnabled',
            settings: [{
                type: 'slider',
                name: 'Multiplier',
                min: 0.5,
                max: 10.0,
                step: 0.1,
                key: 'fpsSpoofMultiplier'
            }]
        },
    ]

    let selectedModule = null

    onMount(() => {
        const controller = new AbortController()

        document.addEventListener('keydown', k => {
            if (k.key === 'Escape') selectedModule = null
        }, {signal: controller.signal})

        return () => controller.abort()
    })
</script>

{#if selectedModule}
    <div class="bg-white h-[53px] flex items-center text-lg">
        <div class="flex-1">
            <button class="p-2" on:click={() => selectedModule = null}>
                <img class="opacity-70" src={circleLeft} width="30"/>
            </button>
        </div>
        {selectedModule.name}
        <div class="flex-1"></div>
    </div>
    <div class="flex flex-col p-5">
        {#each selectedModule.settings as setting}
            {#if setting.type === 'text'}
                <div class="flex justify-between items-center mb-5 gap-5 text-lg">
                    {setting.name}:
                    <input
                            type="text"
                            class="outline-none p-1 rounded"
                            spellcheck="false"
                            placeholder={setting.placeholder}
                            bind:value={config[setting.key]}
                    >
                </div>
            {:else if setting.type === 'slider'}
                <div class="flex justify-between items-center mb-5 gap-5 text-lg">
                    {setting.name}:
                    <div class="flex text-sm gap-2 relative">
                        {config[setting.key].toFixed(1)}
                        <input
                                type="range"
                                min={setting.min}
                                max={setting.max}
                                step={setting.step}
                                bind:value={config[setting.key]}
                                class="w-96"
                        >
                    </div>
                </div>
            {:else if setting.type === 'combo'}
                <div class="flex justify-between items-center mb-5 gap-5 text-lg">
                    {setting.name}:
                    <select class="outline-none p-2 rounded">
                        {#each setting.values as val}
                            <option>{val}</option>
                        {/each}
                    </select>
                </div>
            {/if}
        {/each}
    </div>
{:else}
    <div id="container" class="h-full overflow-y-scroll gap-5 p-5">
        {#each modules as module}
            <span class="bg-white rounded-xl flex flex-col justify-end overflow-hidden module relative">
                <div class="flex-grow flex items-center justify-center font-normal tracking-wide">
                    <span>{module.name}</span>
                </div>
                {#if module.settings}
                   <button
                           class="absolute bottom-8 h-8 w-full bg-gray-200 text-gray-950 tracking-wide text-[15px]"
                           on:click={() => selectedModule = module}>
                        SETTINGS
                    </button>
                {/if}
                <label class="cursor-pointer">
                    <input type="checkbox" class="hidden peer" bind:checked={config[module.key]}>
                    <div class="flex items-center justify-center bg-gray-300 peer-checked:bg-blue-500 h-8">
                        {#if config[module.key]}
                            <p class="text-white text-xl">Enabled</p>
                        {:else}
                            <p class="text-xl">Disabled</p>
                        {/if}
                    </div>
                </label>
            </span>
        {/each}
    </div>
{/if}

<style>
    #container {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        grid-auto-rows: 200px;
        overflow: auto;
    }

    input[type="range"] {
        appearance: none;
        outline: none;
        background: transparent;
    }

    input[type="range"]::-webkit-slider-runnable-track {
        @apply bg-gray-400 rounded;
        height: 8px;
    }

    input[type="range"]::-webkit-slider-thumb {
        margin-top: -4px;
        @apply rounded-full bg-blue-400;
    }


    .module {
        box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.2);
    }
</style>
