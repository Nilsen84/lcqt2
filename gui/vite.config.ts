import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'
import asar from '@electron/asar'

// https://vitejs.dev/config/
export default defineConfig({
    build: {
      outDir: 'out/dist',
    },
    plugins: [
        svelte(),
        {
            name: 'asar',
            apply: 'build',
            closeBundle: () => {
                asar.createPackage('out/dist', 'out/gui.asar')
            }
        }
    ],
    base: './'
})