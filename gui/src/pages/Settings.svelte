<script lang="ts">
    import Switch from "../components/Switch.svelte";
    import { config } from "../config";
</script>

<div class="p-5 flex flex-col justify-start h-full">
    <div class="flex gap-4 items-center">
        <Switch bind:toggled={$config.customJvmEnabled}></Switch>
        Custom JVM
        <div class="flex flex-grow gap-2">
            <input
                    type="text"
                    spellcheck="false"
                    placeholder="C:\Path\To\Java\bin\javaw.exe"
                    bind:value={$config.customJvm}
                    class="ml-4 p-1 outline-none flex-grow rounded"
            >
            <label class="bg-white h-8 px-2 cursor-pointer flex items-center rounded">
                <input type="file" class="hidden" on:change={function() {
                    $config.customJvm = this.files[0].path ?? this.files[0].name
                }}>
                Choose...
            </label>
        </div>
    </div>
    <div class="flex flex-col mt-auto gap-2">
        <div class="flex gap-4 items-center">
            <Switch bind:toggled={$config.jvmArgsEnabled}></Switch>
            JVM Args
        </div>
        <textarea
                class="resize-none outline-none p-2 break-all mt-2 rounded-lg"
                spellcheck="false"
                bind:value={$config.jvmArgs}
                rows="8"
        />
    </div>
</div>