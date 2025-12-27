[![GNU General Public License](https://img.shields.io/badge/license-GPL%20v2-blue.svg?style=flat-square)](http://www.gnu.org/licenses/gpl-2.0.html)
[![Build and Test](https://github.com/NSIS-Dev/nsl-assembler/workflows/Build%20and%20Test/badge.svg)](https://github.com/NSIS-Dev/nsl-assembler/actions)

# nsL Assembler

nsL is a new C-like programming language for writing [NSIS][nsis] installation wizards. The nsL assembler takes nsL code and translates it into original NSIS script which can then be compiled.

**Features:**

* Complete support for complex arithmetic and Boolean expression will all operators
* Automatically declared un-typed variables with assemble-time scope checking
* Native high-level constructs such as if, switch, while, do, for
* New function, section and page declaration syntax
* Functions, instructions and macros callable using C-style syntax
* Built-in wrapper instructions for all NSIS instructions using new syntax
* Recursive macros; providing assemble-time loops
* Fast assemble speed

## Build

The only pre-requistes for building are [Java][java] and [Ant][ant].

1. Clone repository `git clone https://github.com/NSIS-Dev/nsl-assembler nsL`
2. Change directory `cd nsL`
3. Build JAR `ant jar`
 
## License

This work is licensed under the [The GNU General Public License, Version 2](LICENSE.md).

[nsis]: https://nsis.sourceforge.net
[java]: https://www.java.com
[ant]: https://ant.apache.org/
