[![GNU General Public License](https://img.shields.io/badge/license-GPL%20v2-blue.svg?style=flat-square)](http://www.gnu.org/licenses/gpl-2.0.html)
[![Build and Test](https://github.com/NSIS-Dev/nsl-assembler/workflows/Build%20and%20Test/badge.svg)](https://github.com/NSIS-Dev/nsl-assembler/actions)

# nsL Assembler

nsL is a new C-like programming language for writing [NSIS][nsis] installation wizards. The nsL assembler takes nsL code and translates it into an NSIS script, which can then be compiled.

> [!IMPORTANT]  
> This repository started as a pure mirror of the SourceForge-hosted [SVN repository][sourceforge], which has not been updated since April 2011. However, starting in December 2025, several additions have
been made that are unique to this repository and that will not be merged back.

**Features:**

* Complete support for complex arithmetic and Boolean expressions with all operators
* Automatically declared un-typed variables with assembly-time scope checking
* Native high-level constructs such as if, switch, while, do, for
* New function, section and page declaration syntax
* Functions, instructions and macros callable using C-style syntax
* Built-in wrapper instructions for all NSIS instructions using new syntax
* Recursive macros; providing assemble-time loops
* Fast assembly speed

## Prerequisites

This project requires [Java 8 or later][java].

## Build

The project uses [Gradle][gradle] for dependency management. The Gradle wrapper is included, so no installation is required.

1. Clone repository `git clone https://github.com/NSIS-Dev/nsl-assembler nsL`
2. Change directory `cd nsL`
3. Build JAR `./gradlew build`

The compiled JAR will be in `build/libs/nsL.jar`.

**Common Gradle commands:**
- `./gradlew build` - Build project and run tests
- `./gradlew test` - Run tests only
- `./gradlew jar` - Build JAR only
- `./gradlew clean` - Clean build artifacts
- `./gradlew run --args="script.nsl"` - Run the assembler
 
## License

This work is licensed under the [The GNU General Public License, Version 2](LICENSE.md).

[java]: https://www.java.com
[nsis]: https://nsis.sourceforge.net
[gradle]: https://gradle.org/
[sourceforge]: https://sourceforge.net/projects/nslassembler/
