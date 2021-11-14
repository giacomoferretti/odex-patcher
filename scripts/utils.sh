#!/usr/bin/env bash

#
# Copyright 2020-2021 Giacomo Ferretti
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

function logError {
    printf "[E] %s\n" "${1}"
}

function logInfo {
    printf "[I] %s\n" "${1}"
}

function logDebug {
    printf "[D] %s\n" "${1}"
}

function adbInstall {
    adb -s "${1}" install "${2}"
}

function adbPush {
    adb -s "${1}" push "${2}" "${3}"
}

function runCommand {
    adb -s "${1}" shell "${2}"
}

function runAsRoot {
    adb -s "${1}" shell "su 0 'sh' -c \"${2}\""
}

function runAsRootVerbose {
    printf "==> %s\n" "${2}"
    runAsRoot "${1}" "${2}"
}

function selectAdbDevice {
    ADB_DEVICES=()
    while IFS='' read -r line; do ADB_DEVICES+=("${line}"); done < <(adb devices | grep -v "List" | grep -v "permissions" | grep "device\|recovery" | awk 'NF {print $1}')

    # Check if at least one device is connected
    if [[ "${#ADB_DEVICES[@]}" -eq 0 ]]; then
        logError "No ADB device connected."
        exit 1
    fi

    ADB_TARGET="${ADB_DEVICES[0]}"

    # Choose device if multiple devices
    if [[ "${#ADB_DEVICES[@]}" -gt 1 ]]; then
        printf "Available devices:\n"

        PS3="Select a device (1-${#ADB_DEVICES[@]}): "
        select option in "${ADB_DEVICES[@]}"; do
            if [[ 1 -le "${REPLY}" ]] && [[ "${REPLY}" -le "${#ADB_DEVICES[@]}" ]]; then
                break;
            else
                printf "%s is not a valid selection.\n" "${REPLY}"
            fi
        done

        ADB_TARGET="${option}"
    fi
}

function getDeviceProperties {
    # Extract Android SDK
    ADB_TARGET_SDK=$(runCommand "${1}" "getprop ro.build.version.sdk")
    ADB_TARGET_SDK=${ADB_TARGET_SDK//[$'\001'-$'\037']}

    # Extract Android release
    ADB_TARGET_RELEASE=$(runCommand "${1}" "getprop ro.build.version.release")
    ADB_TARGET_RELEASE=${ADB_TARGET_RELEASE//[$'\001'-$'\037']}

    # Extract device CPU ABI
    ADB_TARGET_CPU=$(runCommand "${1}" "getprop ro.product.cpu.abi")
    ADB_TARGET_CPU=${ADB_TARGET_CPU//[$'\001'-$'\037']}

    # Convert CPU ABI to correct ISA
    case $ADB_TARGET_CPU in
        "armeabi-v7a" | "armeabi")
            ADB_TARGET_ISA="arm"
            ;;

        "arm64-v8a")
            ADB_TARGET_ISA="arm64"
            ;;

        "x86")
            ADB_TARGET_ISA="x86"
            ;;

        "x86_64")
            ADB_TARGET_ISA="x86_64"
            ;;

        *)
            ADB_TARGET_ISA="none"
            ;;
    esac
}

function installPackageIfNotInstalled {
    # Extract target package path
    local PACKAGE_APK_PATH
    PACKAGE_APK_PATH=$(runCommand "${ADB_TARGET}" "pm path ${2} 2>/dev/null")

    if [[ ! "${PACKAGE_APK_PATH}" == "package:"* ]]; then
        adbInstall "${ADB_TARGET}" "${1}"
    fi
}

function cleanInstallPackage {
    # Extract target package path
    local PACKAGE_APK_PATH
    PACKAGE_APK_PATH=$(runCommand "${ADB_TARGET}" "pm path ${2} 2>/dev/null")

    if [[ "${PACKAGE_APK_PATH}" == "package:"* ]]; then
        adbUninstall "${ADB_TARGET}" "${2}"
    fi

    adbInstall "${ADB_TARGET}" "${1}"
}
