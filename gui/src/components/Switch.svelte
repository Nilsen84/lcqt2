<script lang="ts">
    export let toggled: boolean = false
</script>

<label class="">
    <input type="checkbox" class="hidden" bind:checked={toggled}>
    <div class="w-[3.75rem] h-8 rounded-full cursor-pointer relative bg-gray-400" id="bg"/>
</label>

<style>
    input:checked + #bg {
        @apply bg-blue-500;
    }

    #bg::before {
        content: '';
        @apply absolute bg-white w-6 h-6 rounded-full m-1 duration-100;
    }

    input:checked + #bg::before {
        @apply translate-x-7;
    }
</style>