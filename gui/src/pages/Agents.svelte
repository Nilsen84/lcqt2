<script lang="ts">
    import trash from '../assets/trash.svg'
    import { config } from '../config'

    function addAgent(file) {
        if($config.agents.some(a => a.path == file.path)) {
            return
        }

        $config.agents = [...$config.agents, {
            enabled: true,
            path: file.path,
            option: ''
        }]
    }

    function removeAgent(i) {
        $config.agents.splice(i, 1)
        $config.agents = $config.agents
        window.dispatchEvent(new Event('change'))
    }
</script>

<div class="flex flex-col items-stretch">
    <div class="h-[52px] bg-white flex items-center text-lg relative px-2">
        <div class="flex-1">
            <label
                    class="border-2 border-black rounded-md w-9 h-9 text-4xl flex justify-center border-opacity-70 cursor-pointer"
                    on:change={e => {
                        addAgent(e.target.files[0])
                        e.target.value = ''
                    }}
            >
                <input type="file" class="hidden" accept=".jar">
                <span class="absolute top-1 font-light opacity-80">+</span>
            </label>
        </div>
        Java Premain Agents
        <div class="flex-1"></div>
    </div>

    <div class="flex flex-col p-4 gap-2">
        {#each $config.agents as agent, i (agent.path)}
            <div class="flex items-center bg-white px-2 w-full h-10 rounded-lg">
                <input tabindex="-1" type="checkbox" class="scale-[115%]" bind:checked={agent.enabled}>
                <span
                        title={agent.path}
                        class="ml-2 h-10 flex items-center"
                        on:click={() => agent.enabled ^= 1}
                >
                    {agent.path.replace(/^.*[\\\/]/, '')}
                </span>
                <div class="flex-1"></div>
                <input
                        type="text"
                        class="outline-none p-1 text-sm border-2 rounded mr-1"
                        placeholder="Option"
                        spellcheck="false"
                >
                <button tabindex="-1" class="flex justify-center items-center" on:click={() => removeAgent(i)}>
                    <img draggable="false" src={trash} alt="remove" width="26" class="opacity-75">
                </button>
            </div>
        {/each}
    </div>
</div>


<style>
</style>