# ODEX Patcher

[![GitHub all releases](https://img.shields.io/github/downloads/giacomoferretti/odex-patcher/total?color=success)](https://github.com/giacomoferretti/odex-patcher/releases/latest)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/giacomoferretti/odex-patcher?include_prereleases)](https://github.com/giacomoferretti/odex-patcher/releases)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/giacomoferretti/odex-patcher)
[![GitHub](https://img.shields.io/github/license/giacomoferretti/odex-patcher?color=blue)](LICENSE)

## Introduction

ODEX Patcher allows you to execute arbitrary code while maintaining the original signature of the target app.
It does this by patching the OAT (Ahead-of-Time) files that the ART runtime creates when an app is installed.

This tool can be useful when an app uses SafetyNet for example, as you cannot simply edit the app and sign it with your key.

## Examples

☺️ Coming soon...

## Supported Versions

Android 4.4 ~ 13

### Requirements

- Root access ([Magisk](https://github.com/topjohnwu/Magisk) is suggested)

## Downloads

Latest release: [2.0.0-rc1](https://github.com/giacomoferretti/odex-patcher/releases/tag/v2.0.0-rc1)

## Building and Development

- Clone sources: `git clone https://github.com/giacomoferretti/odex-patcher`
- Open the project with Android Studio.
  - Currently using: Android Studio Chipmunk | 2021.2.1 Patch 2

## Compatibility matrix

✅ = Working

❌ = Not working

<table>
    <thead>
		<tr>
			<th colspan="3"></th>
			<th colspan="2">Single Dex</th>
			<th colspan="2">Multi Dex</th>
		</tr>
        <tr>
            <th>Android Version</th>
            <th>Oat Version</th>
            <th>Vdex Version</th>
            <th>32bit</th>
            <th>64bit</th>
            <th>32bit</th>
            <th>64bit</th>
        </tr>
    </thead>
	<tbody align="center">
		<tr>
			<td>4.4 - 4.4.2</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-4.4_r1/runtime/oat.cc#25">007</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>❌<sup>(1)</sup></td>
			<td>❌<sup>(1)</sup></td>
		</tr>
		<tr>
			<td>4.4.3 - 4.4.4</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-4.4.3_r1/runtime/oat.cc#25">008</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>❌<sup>(1)</sup></td>
			<td>❌<sup>(1)</sup></td>
		</tr>
		<tr>
			<td>5.0 - 5.0.2</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-5.0.0_r1/runtime/oat.cc#26">039</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>5.1 - 5.1.1</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-5.1.0_r1/runtime/oat.cc#26">045</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>6.0 - 6.0.1</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-6.0.0_r1/runtime/oat.h#35">064</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>7.0 - 7.1</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-7.0.0_r1/runtime/oat.h#35">079</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>7.1.1 - 7.1.2</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-7.1.1_r1/runtime/oat.h#35">088</a></td>
			<td>-</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>8.0</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/oat.h#35">124</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r1/runtime/vdex_file.h#69">006</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>8.1</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/oat.h#36">131</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r1/runtime/vdex_file.h#76">010</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>9</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/oat.h#36">138</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r1/runtime/vdex_file.h#96">019</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>10</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/oat.h#36">170</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r1/runtime/vdex_file.h#118">021</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>11</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/oat.h#36">183</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r1/runtime/vdex_file.h#118">021</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>12</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/oat.h#36">195</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-12.0.0_r1/runtime/vdex_file.h#127">027</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>12L</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-12.1.0_r1/runtime/oat.h#36">199</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-12.1.0_r1/runtime/vdex_file.h#127">027</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
		<tr>
			<td>13</td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/runtime/oat.h#36">225</a></td>
			<td><a href="https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r1/runtime/vdex_file.h#127">027</a></td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
			<td>✅</td>
		</tr>
	</tbody>
</table>

<sup>(1)</sup> Follow [#6](https://github.com/giacomoferretti/odex-patcher/issues/6)

## License

```
Copyright 2020-2022 Giacomo Ferretti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
