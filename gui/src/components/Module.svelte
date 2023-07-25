<script lang="ts">
    import cogwheel from '../assets/cogwheel.svg'

    export let name: string
    export let enabled: boolean = false

    let drawer: HTMLDivElement
    let drawerContent: HTMLDivElement
    function toggleDrawer() {
        if(drawer.clientHeight) {
            drawer.style.height = '0'
        }else {
            drawer.style.height = drawerContent.clientHeight + 'px'
        }
    }
</script>

<div class="flex flex-col rounded-xl overflow-hidden flex-shrink-0" id="container">
    <div class="flex h-16 overflow-hidden relative">
        <label class="h-full aspect-square cursor-pointer">
            <input type="checkbox" bind:checked={enabled} class="hidden">
            <div class="h-full w-full bg-red-400" class:checked={enabled} class:unchecked={!enabled}></div>
        </label>

        <div
            class="w-full h-full bg-[#f8f9fa] flex items-center justify-center font-semibold text-[#343a40]"
             class:cursor-pointer={$$slots.default}
             on:click={toggleDrawer}
        >
            {name}
            {#if $$slots.default}
                <img src="{cogwheel}" width="30" class="absolute right-4"/>
            {/if}
        </div>
    </div>



    {#if $$slots.default}
        <div
                class="bg-[#f8f9fa] overflow-hidden transition-all ease-linear duration-100"
                bind:this={drawer}
                style="height: 0"
        >
            <div class="p-3" bind:this={drawerContent}>
                <slot></slot>
            </div>
        </div>
    {/if}
</div>

<style>
    .checked {
        background: linear-gradient(to top left, #11998e, #38ef7d);
    }

    .unchecked {
        background: linear-gradient(to right, #cb2d3e, #ef473a); /* W3C, IE 10+/ Edge, Firefox 16+, Chrome 26+, Opera 12+, Safari 7+ */
    }

    #container {
        box-shadow: 0 3px 7px rgba(0, 0, 0, 0.3);
    }
</style>