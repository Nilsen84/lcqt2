import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'
import asar from '@electron/asar'
import cp from "child_process";
import * as esbuild from 'esbuild'

const gitTag = (() => {
    try {
        return cp.execFileSync(
            'git',
            ['describe', '--tags', '--abbrev=0', '--match=v*'],
            {encoding: 'ascii'}
        ).trim()
    }catch {
        return "v0.0.0"
    }
})()

const esbuildRendererPlugin = {
    name: 'renderer',
    setup(build) {
        build.onLoad({ filter: /renderer-inject\.js$/ }, async args => {
            let res = await esbuild.build({
                entryPoints: [args.path],
                bundle: true,
                minify: true,
                platform: 'browser',
                format: 'iife',
                write: false,
                external: [
                    'electron',
                    'path',
                    'child_process'
                ],
                loader: {
                    '.png': 'dataurl',
                    '.svg': 'dataurl'
                }
            })
            return {
                contents: res.outputFiles[0].contents,
                loader: "text"
            }
        })
    }
}

// https://vitejs.dev/config/
export default defineConfig({
    build: {
        outDir: 'out/dist'
    },
    plugins: [
        svelte(),
        {
            name: 'asar',
            apply: 'build',
            writeBundle: async () => {
                await esbuild.build({
                    entryPoints: ['inject/main-inject.cjs'],
                    bundle: true,
                    outdir: 'out/dist',
                    minify: true,
                    platform: 'node',
                    external: ['electron'],
                    plugins: [esbuildRendererPlugin],
                    define: {
                        __APP_VERSION__: JSON.stringify(gitTag)
                    }
                })
            },
            closeBundle: async () => {
                await asar.createPackage('out/dist', 'out/gui.asar')
            }
        }
    ],
    base: './',
    define: {
        __APP_VERSION__: JSON.stringify(gitTag)
    },
})