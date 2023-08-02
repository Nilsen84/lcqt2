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
                    name: 'Username',
                    type: 'text',
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
        }
    ]

    let selectedModule = null

    onMount(() => {
        const controller = new AbortController()

        document.addEventListener('keydown', k => {
            if (k.key === 'Escape') selectedModule = null
        }, { signal: controller.signal })

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
                <div class="flex justify-between items-center gap-5 text-lg">
                    {setting.name}:
                    <input
                            type="text"
                            class="outline-none p-1 rounded"
                            spellcheck="false"
                            placeholder={setting.placeholder}
                            bind:value={config[setting.key]}
                    >
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
                    <div class="bg-gray-300 peer-checked:bg-blue-500 h-8"></div>
                </label>
            </span>
        {/each}
    </div>

    <style>
        #container {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            grid-auto-rows: 200px;
        }

        .module {
            box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.2);
        }
    </style>
{/if}
