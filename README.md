# ODEX Patcher

[![GitHub all releases](https://img.shields.io/github/downloads/giacomoferretti/odex-patcher/total?color=success)](https://github.com/giacomoferretti/odex-patcher/releases/latest)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/giacomoferretti/odex-patcher)](https://github.com/giacomoferretti/odex-patcher/releases/latest)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/giacomoferretti/odex-patcher)
[![GitHub](https://img.shields.io/github/license/giacomoferretti/odex-patcher?color=blue)](LICENSE)

## Introduction

ODEX Patcher allows you to execute arbitrary code while maintaining the original signature of the target app.
It does this by patching the OAT (Ahead-of-Time) files that the ART runtime creates when an app is installed.

This tool can be useful when an app uses SafetyNet for example, as you cannot simply edit the app and sign it with your key.

## Examples

☺️ Coming soon...

## Requirements

Android 4.4+ devices with root access ([Magisk](https://github.com/topjohnwu/Magisk) is suggested)

## Building and Development

- Clone sources: `git clone https://github.com/giacomoferretti/odex-patcher`
- Open the project with Android Studio.
  - Currently using: Android Studio Arctic Fox | 2020.3.1 Patch 3

## Compatibility matrix

<!--
Tested on:
Android Emulator 6.0 (23) x86
Lineage OS 17.1 (Android 10) arm
-->

✅ = Working

❌ = Not working

❔ = Not tested, but should work

<!--
| Android Version | Oat Version | Single Dex 32bit | Single Dex 64bit | Multi Dex 32bit | Multi Dex 64bit |
|:---------------:|:-----------:|:----------------:|:----------------:|:---------------:|:---------------:|
| 4.4 - 4.4.2     | 007         | ❔ | ❔ | ❌<sup>(1)</sup> | ❌<sup>(1)</sup> |
| 4.4.3 - 4.4.4   | 008         | ❔ | ❔ | ❌<sup>(1)</sup> | ❌<sup>(1)</sup> |
| 5.0 - 5.0.2     | 039         | ❔ | ❔ | ❔ | ❔ |
| 5.1 - 5.1.1     | 045         | ❔ | ❔ | ❔ | ❔ |
| 6.0 - 6.0.1     | 064         | ❔ | ❔ | ❔ | ❔ |
| 7.0 - 7.1       | 079         | ❔ | ❔ | ❔ | ❔ |
| 7.1.1 - 7.1.2   | 088         | ❔ | ❔ | ❔ | ❔ |
| 8.0             | 124         | ❔ | ❔ | ❔ | ❔ |
| 8.1             | 131         | ❔ | ❔ | ❔ | ❔ |
| 9               | 138         | ❔ | ❔ | ❔ | ❔ |
| 10              | 170         | ❔ | ❔ | ❔ | ❔ |
| 11              | 183         | ❔ | ❔ | ❔ | ❔ |
| 12              | 195         | ❔ | ❔ | ❔ | ❔ |
| Sv2             | 199         | ❔ | ❔ | ❔ | ❔ |
-->

<table>
    <thead>
		<tr>
			<th colspan="2"></th>
			<th colspan="2">Single Dex</th>
			<th colspan="2">Multi Dex</th>
		</tr>
        <tr>
            <th>Android Version</th>
            <th>Oat Version</th>
            <th>32bit</th>
            <th>64bit</th>
            <th>32bit</th>
            <th>64bit</th>
        </tr>
    </thead>
	<tbody align="center">
		<tr>
			<td>4.4 - 4.4.2</td>
			<td>007</td>
			<td>❔</td>
			<td>❔</td>
			<td>❌<sup>(1)</sup></td>
			<td>❌<sup>(1)</sup></td>
		</tr>
		<tr>
			<td>4.4.3 - 4.4.4</td>
			<td>008</td>
			<td>❔</td>
			<td>❔</td>
			<td>❌<sup>(1)</sup></td>
			<td>❌<sup>(1)</sup></td>
		</tr>
		<tr>
			<td>5.0 - 5.0.2</td>
			<td>039</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>5.1 - 5.1.1</td>
			<td>045</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>6.0 - 6.0.1</td>
			<td>064</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>7.0 - 7.1</td>
			<td>079</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>7.1.1 - 7.1.2</td>
			<td>088</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>8.0</td>
			<td>124</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>8.1</td>
			<td>131</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>9</td>
			<td>138</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>10</td>
			<td>170</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>11</td>
			<td>183</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>12</td>
			<td>195</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
		<tr>
			<td>Sv2</td>
			<td>199</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
			<td>❔</td>
		</tr>
	</tbody>
</table>

<sup>(1)</sup> Follow [#6](https://github.com/giacomoferretti/odex-patcher/issues/6)