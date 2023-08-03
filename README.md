<h1>
    Lunar Client Qt 2
    <a href="https://discord.gg/mjvm8PzB2u">
        <img src=".github/assets/discord.svg" alt="discord" height="32" style="vertical-align: -5px;"/>
    </a>
</h1>

Continuation of the original [lunar-client-qt](https://github.com/nilsen84/lunar-client-qt), moved to a new repo because of the complete rewrite and redesign.

<img src=".github/assets/screenshot.png" width="600" alt="screenshot of lcqt">

## Installation
#### Windows
Simply download and run the setup exe from the [latest release](https://github.com/nilsen84/lcqt2/releases/latest).

#### Arch Linux
Use the AUR package [lunar-client-qt2](https://aur.archlinux.org/packages/lunar-client-qt2)

#### MacOS/Linux
> If you are using Linux, be sure to have the `Lunar Client-X.AppImage` renamed to `lunarclient` in `/usr/bin/`.
1. Download the macos/linux.tar.gz file from the [newest release](https://github.com/nilsen84/lcqt2/releases/latest).
2. Extract it anywhere
3. Run the `Lunar Client Qt` executable

> **IMPORTANT:** All 3 files which where inside the tar need to stay together.  
> You are allowed to move all 3 together, you're also allowed to create symlinks.

## Building
#### Prerequisites
- Rust Nightly
- NPM

#### Building
LCQT2 is made up of 3 major components:
- The injector - responsible for locating the launcher executable and injecting a javascript patch into it
- The gui - contains the gui opened by pressing the syringe button, also contains the javascript patch used by the injector
- The agent - java agent which implements all game patches

In order for lcqt to work properly all 3 components need to be built into the same directory.

```bash
$ ./gradlew installDist # builds all 3 components and generates a bundle in build/install/lcqt2
```
```bash
$ ./gradlew run # equivalent to ./gradlew installDist && './build/install/lcqt2/Lunar Client Qt'
```
> `./gradlew installDebugDist` and `./gradlew runDebug` do the same thing except they build the rust injector in debug mode.
