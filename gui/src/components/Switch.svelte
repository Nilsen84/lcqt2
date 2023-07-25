<script lang="ts">
    export let enabled: boolean = false
</script>

<!--<label>
    <input type="checkbox" class="hidden" bind:checked={enabled}>
    <div class="h-10 w-16 bg-red-400"></div>
</label>-->

<label class="">
    <input type="checkbox" class="hidden" bind:checked={enabled}>
    <div class="w-16 h-8 rounded-full cursor-pointer relative" id="bg"/>
</label>

<style>
    #bg {
        background: linear-gradient(to right, #cb2d3e, #ef473a); /* W3C, IE 10+/ Edge, Firefox 16+, Chrome 26+, Opera 12+, Safari 7+ */
    }

    input:checked + #bg {
        background: linear-gradient(to top left, #11998e, #38ef7d);
    }

    #bg::before {
        content: '';
        @apply absolute bg-white w-6 h-6 rounded-full m-1 duration-100;
    }

    #bg::before {
        @apply absolute bg-white w-6 h-6 rounded-full m-1 duration-100;
    }

    input:checked + #bg::before {
        @apply translate-x-8;
    }
</style>