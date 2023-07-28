<h1>
    Lunar Client Qt 2
    <a href="https://discord.gg/mjvm8PzB2u">
        <img src=".github/assets/discord.svg" height="32" style="vertical-align: -5px;"/>
    </a>
</h1>

Continuation of the original [lunar-client-qt](https://github.com/nilsen84/lunar-client-qt), moved to a new repo because of the complete rewrite and redesign.


## Installation
#### Windows
Simply download and run the setup exe from the [latest release](https://github.com/nilsen84/lcqt2/releases/latest).
#### MacOS/Linux
1. Download the macos/linux.tar.gz file from the [newest release](https://github.com/nilsen84/lcqt2/releases/latest).
2. Extract it anywhere
3. Run the `Lunar Client Qt` executable

> **IMPORTANT:** All 3 files which where inside the tar need to stay together.  
> You are allowed to move all 3 together, you're also allowed to create symlinks.

## Building
LCQT2 is made up of 3 major components:
- The injector - responsible for locating the launcher executable and injecting a javascript patch into it
- The gui - contains the gui opened by pressing the syringe button, also contains the javascript patch used by the injector
- The patcher - the java agent which implements all game patches
  
#### Prerequisites
- Rust Nightly
- NPM

#### Building
In order for lcqt to work properly all 3 components need to be built into the same directory.

`./gradlew installDist` will build all 3 components and generate a bundle in `build/install/lcqt2`

`./gradlew run` is equivalent to `./gradlew installDist && './build/install/lcqt2/Lunar Client Qt'`

There are also debug variations of both: `./gradlew runDebug` `./gradlew installDebugDist`  
the difference being the rust injector is now built in debug mode.
