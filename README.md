# BetterChunkLoader Sponge

This project is a continuation of [BetterChunkLoader](https://github.com/KaiKikuchi/BetterChunkLoader) for [SpongeForge](https://github.com/SpongePowered/SpongeForge)

* [Source]
* [Issues]
* [Website]
* [Downloads]
* [Wiki]
* [Discord]

Licence: [MIT](LICENSE.md) Permission to change this license to MIT was given by KaiKikuchi

This version of BetterChunkLoader for Sponge is intended to allow server owners to have better control over chunkloading. Often this control cannot be easily obtained through mods that provide ChunkLoaders. This version is NOT compatible with datastores used by original versions of BetterChunkLoader.

For instructions on how to use and or configure BetterChunkLoader check out our [Wiki]

## Contributions

Would you like to contribute some code? Do you have a bug you want to report? Or perhaps an idea for a feature? We'd be grateful for your contributions.

* Read our [guidelines].
* Open an issue if you have a bug to report, or a pull request with your changes.

## Getting and Building BetterChunkLoader

To get a copy of the BetterChunkLoader source, ensure you have Git installed, and run the following commands from a command prompt
or terminal:

1. `git clone git@bitbucket.org:shadownode/betterchunkloader.git`
2. `cd betterchunkloader`
3. `cp scripts/pre-commit .git/hooks`

To build BetterChunkLoader, navigate to the source directory and run either:

* `./gradlew build` on UNIX and UNIX like systems (including OS X and Linux)
* `gradlew build` on Windows systems

You will find the compiled JAR which will be named like `BetterChunkLoader-x.x.x-SNAPSHOT.jar` in `builds/libs/`.

[Source]: https://bitbucket.org/shadownode/betterchunkloader
[Issues]: https://bitbucket.org/shadownode/betterchunkloader/issues
[Downloads]: https://bitbucket.org/shadownode/downloads
[Website]: https://www.shadownode.ca
[Wiki]: https://bitbucket.org/shadownode/betterchunkloader/wiki
[guidelines]: CONTRIBUTING.md
[Discord]: https://discord.gg/ZqV39fE