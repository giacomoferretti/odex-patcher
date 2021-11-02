# ODEX Patcher

[![GitHub all releases](https://img.shields.io/github/downloads/giacomoferretti/odex-patcher/total?color=success)](https://github.com/giacomoferretti/odex-patcher/releases/latest)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/giacomoferretti/odex-patcher)](https://github.com/giacomoferretti/odex-patcher/releases/latest)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/giacomoferretti/odex-patcher)
[![GitHub](https://img.shields.io/github/license/giacomoferretti/odex-patcher?color=blue)](LICENSE)

<!--
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/giacomoferretti/odex-patcher.svg)](http://isitmaintained.com/project/giacomoferretti/odex-patcher "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/giacomoferretti/odex-patcher.svg)](http://isitmaintained.com/project/giacomoferretti/odex-patcher "Percentage of issues still open")

The ART runtime was first introduced in Android 4.4 as an opt-in experimental option, you could enabled it through Developer options.
Since Android 5.0 it became the standard runtime.

The difference between Dalvik and the ART runtime is that Dalvik is JIT and ART is AOT.
-->

## Requisites to run the app

* ART runtime (default on devices running Android >5.0)
* Root access (preferably [Magisk](https://github.com/topjohnwu/Magisk))

## Requisites to build the app

* Android Studio Arctic Fox | 2020.3.1 Patch 3

## Compatibility

✅ = Working

❌ = Not working

❔ = Not tested, but should work

| Android Version <sup>(Oat Version)</sup> | Single Dex | Multi Dex |
|:-:|:-:|:-:|
| 4.4 - 4.4.2 <sup>(007)</sup>   | ✅ | ❌* |
| 4.4.3 - 4.4.4 <sup>(008)</sup> | ❔ | ❌* |
| 5.0 - 5.0.2 <sup>(039)</sup>   | ✅ | ✅ |
| 5.1 - 5.1.1 <sup>(045)</sup>   | ✅ | ✅ |
| 6.0 - 6.0.1 <sup>(064)</sup>   | ✅ | ✅ |
| 7.0 - 7.1 <sup>(079)</sup>     | ✅ | ✅ |
| 7.1.1 - 7.1.2 <sup>(088)</sup> | ✅ | ✅ |
| 8.0 <sup>(124)</sup>           | ✅ | ✅ |
| 8.1 <sup>(131)</sup>           | ✅ | ✅ |
| 9 <sup>(138)</sup>             | ✅ | ✅ |
| 10 <sup>(170)</sup>            | ✅ | ✅ |
| 11 <sup>(183)</sup>            | ✅ | ✅ |
| 12 <sup>(195)</sup>            | ❔ | ❔ |

\* Follow [#6](https://github.com/giacomoferretti/odex-patcher/issues/6)