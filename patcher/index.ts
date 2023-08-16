import * as asar from '@electron/asar'
import * as fs from 'fs'
import * as path from 'path'

const dir = (function () {
    if (__dirname === undefined || __dirname === 'C:\\snapshot\\patcher\\build') 
        return process.cwd()
    else 
        return __dirname
})()
    

//const guiasar = 'C:\\Coding\\Mixed\\lcqt2\\gui\\out\\gui.asar'
//get the path to the gui.asar file
//const guiasar = path.join(__dirname, '..', 'gui', 'out', 'gui.asar')
const guiasar = (function () {
    let dir_asar = path.join(dir, 'gui.asar')
    if (fs.existsSync(dir_asar)) return dir_asar

    let out_asar = path.join(dir, '..', 'gui', 'out', 'gui.asar')
    if (fs.existsSync(out_asar)) return out_asar

    console.log('gui.asar not found')
    process.exit(1)
})()


const lunarpath = (function () {
    const os = process.platform

    switch (os) {
        case 'win32':
            const localappdata = process.env.LOCALAPPDATA
            if (!localappdata) {
                console.log('LOCALAPPDATA not found')
                process.exit(1)
            }

            return `${localappdata}\\Programs\\lunarclient`
        case 'darwin':
            const home = process.env.HOME
            if (!home) {
                console.log('HOME not found')
                process.exit(1)
            }

            return `${home}/Applications/Lunar Client.app/Contents/MacOS`
        case 'linux':
            return `/usr/bin/lunarclient`
        default:
            console.log('Unsupported OS')
            process.exit(1)
    }
})()

const asar_path = `${lunarpath}/resources/app.asar`

if (!fs.existsSync(asar_path)) {
    console.log('asar not found, unable to patch')
    process.exit(1)
}
if (fs.existsSync(dir + '/unpacked/dist-electron'))
    fs.rmdirSync(dir + '/unpacked', { recursive: true })

if (!fs.existsSync(dir + '/unpacked')) 
    fs.mkdirSync(dir + '/unpacked')

console.log('Extracting asar...')
asar.extractAll(asar_path, dir + '/unpacked')

console.log('Patching...')

const main_js_path = dir + '/unpacked/dist-electron/electron/main.js'
const main_js = fs.readFileSync(main_js_path, 'utf-8')

const require_regex = /require\('.+?main-inject.js'\)\(\);/g
// if (require_regex.test(main_js)) {
//     console.log('Already patched!')
//     process.exit(0)
// }
main_js.replace(require_regex, '')
const patched_main_js = `require('${guiasar.replace(/\\/g, '\\\\')}\\\\main-inject.js')();\n` + main_js
// const patched_main_js = `import __lcqt__main__inject__ from '${guiasar.replace(/\\/g, '\\\\')}\\\\main-inject.js';__lcqt__main__inject__();\n` + main_js

fs.writeFileSync(main_js_path, patched_main_js)


console.log('Repacking asar...')
asar.createPackage(dir + '/unpacked', asar_path)
console.log('Asar repacked!')