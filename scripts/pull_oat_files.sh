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

ORIGINAL_PWD="$(pwd)"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
. "${SCRIPT_DIR}/utils.sh"

TARGET_PACKAGES=("me.hexile.sara.singletextview" "me.hexile.sara.multidex")
OUTPUT_FOLDER="extracted_oat"

mkdir -p "${OUTPUT_FOLDER}"

selectAdbDevice
getDeviceProperties "${ADB_TARGET}"

# Debug info
logDebug "ADB target: ${ADB_TARGET}"
logDebug "Android SDK: ${ADB_TARGET_SDK}"
logDebug "Android release: ${ADB_TARGET_RELEASE}"
logDebug "Device cpu: ${ADB_TARGET_CPU}"
logDebug "Device isa: ${ADB_TARGET_ISA}"

for package in "${TARGET_PACKAGES[@]}"; do
    # Extract target package path
    PACKAGE_APK_PATH=$(runCommand "${ADB_TARGET}" "pm path ${package} 2>/dev/null")

    # Check if target app is installed
    printf "[I] Checking if %s is installed... " "${package}"
    if [[ ! "${PACKAGE_APK_PATH}" == "package:"* ]]; then
        printf "\n"
        printf "[E] %s is not installed on the device.\n" "${package}" >&2
        exit 1
    fi
    printf "OK\n"

    # Cleanup path
    PACKAGE_APK_PATH=${PACKAGE_APK_PATH//[$'\001'-$'\037']}
    PACKAGE_APK_PATH=${PACKAGE_APK_PATH//package:}

    # Extract base folder
    PACKAGE_DIR_PATH=$(dirname "${PACKAGE_APK_PATH}")

    # Convert apk path to oat path (Android <6.0)
    PACKAGE_OAT_PATH=${PACKAGE_APK_PATH//\//@}
    PACKAGE_OAT_PATH=${PACKAGE_OAT_PATH:1}

    if [[ "${ADB_TARGET_SDK}" -eq 19 ]]; then
        # Android 4.4
        PACKAGE_OAT_FILE="/data/dalvik-cache/${PACKAGE_OAT_PATH}@classes.dex"
    elif [[ "${ADB_TARGET_SDK}" -lt 23 ]]; then
        # Android 5.0 - 5.1
        PACKAGE_OAT_FILE="/data/dalvik-cache/${ADB_TARGET_ISA}/${PACKAGE_OAT_PATH}@classes.dex"
    else
        # Android 6.0+
        PACKAGE_OAT_FILE="${PACKAGE_DIR_PATH}/oat/${ADB_TARGET_ISA}/base.odex"
    fi

    # Extract base folder
    PACKAGE_OAT_DIR=$(dirname "${PACKAGE_OAT_FILE}")

    if ! runAsRoot "${ADB_TARGET}" "exit"; then
        logError "Cannot copy files without root access."
        exit 1
    fi

    # Check if output folder exists
    PACKAGE_OUTPUT_FOLDER="${OUTPUT_FOLDER}/${package}/${ADB_TARGET_SDK}_${ADB_TARGET_RELEASE}_${ADB_TARGET_ISA}"
    [[ ! -d "${PACKAGE_OUTPUT_FOLDER}" ]] && mkdir -p "${PACKAGE_OUTPUT_FOLDER}"

    # Get files info
    {
        printf "SELinux: "
        runCommand "${ADB_TARGET}" "getenforce"
        printf "\n"

        runAsRootVerbose "${ADB_TARGET}" "ls -l ${PACKAGE_OAT_DIR}"
        runAsRootVerbose "${ADB_TARGET}" "ls -Z ${PACKAGE_OAT_DIR}"
        runAsRootVerbose "${ADB_TARGET}" "ls -ln ${PACKAGE_OAT_DIR}"

        if [[ "${ADB_TARGET_SDK}" -gt 22 ]]; then
            runAsRootVerbose "${ADB_TARGET}" "ls -l ${PACKAGE_DIR_PATH}"
            runAsRootVerbose "${ADB_TARGET}" "ls -Z ${PACKAGE_DIR_PATH}"
            runAsRootVerbose "${ADB_TARGET}" "ls -ln ${PACKAGE_DIR_PATH}"
        fi
    } > "${PACKAGE_OUTPUT_FOLDER}/ls.txt"

    # Copy .apk file
    if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_APK_PATH} /data/local/tmp/uselessfile" >/dev/null; then
        runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
        adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.apk" >/dev/null
    fi

    # Copy .odex file
    if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_OAT_FILE} /data/local/tmp/uselessfile" >/dev/null; then
        runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
        adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.odex" >/dev/null
    fi

    # Copy .vdex file (Only on Android 8.0+)
    if [[ "${ADB_TARGET_SDK}" -gt 25 ]]; then
        if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_DIR_PATH}/oat/${ADB_TARGET_ISA}/base.vdex /data/local/tmp/uselessfile" >/dev/null; then
            runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
            adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.vdex" >/dev/null
        fi
    fi
done