#!/usr/bin/env bash

# Config
TARGET_PACKAGES=("me.hexile.sara.multidex" "com.small.apk")
OUTPUT_FOLDER="extracted_oat"

### !!! DO NOT EDIT AFTER THIS IF YOU DON'T KNOW WHAT YOU'RE DOING !!! ###

function logError {
    printf "[E] %s\n" "${1}"
}

function logInfo {
    printf "[I] %s\n" "${1}"
}

function logDebug {
    printf "[D] %s\n" "${1}"
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

ADB_DEVICES=()
while IFS='' read -r line; do ADB_DEVICES+=("${line}"); done < <(adb devices | grep -v "List" | grep -v "permissions" | grep "device" | awk 'NF {print $1}')

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

# Extract Android SDK
ADB_TARGET_SDK=$(runCommand "${ADB_TARGET}" "getprop ro.build.version.sdk")
ADB_TARGET_SDK=${ADB_TARGET_SDK//[$'\001'-$'\037']}

# Extract Android release
ADB_TARGET_RELEASE=$(runCommand "${ADB_TARGET}" "getprop ro.build.version.release")
ADB_TARGET_RELEASE=${ADB_TARGET_RELEASE//[$'\001'-$'\037']}

# Extract device CPU ABI
ADB_TARGET_CPU=$(runCommand "${ADB_TARGET}" "getprop ro.product.cpu.abi")
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

    # Copy .odex file
    if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_OAT_FILE} /data/local/tmp/uselessfile" >/dev/null; then
        runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
        adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.odex" >/dev/null
    fi

    # Copy .vdex and .art files (Only on Android 8.0+)
    if [[ "${ADB_TARGET_SDK}" -gt 25 ]]; then
        if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_DIR_PATH}/oat/${ADB_TARGET_ISA}/base.vdex /data/local/tmp/uselessfile" >/dev/null; then
            runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
            adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.vdex" >/dev/null
        fi

        if runAsRoot "${ADB_TARGET}" "cp ${PACKAGE_DIR_PATH}/oat/${ADB_TARGET_ISA}/base.art /data/local/tmp/uselessfile" >/dev/null; then
            runAsRoot "${ADB_TARGET}" "chmod 777 /data/local/tmp/uselessfile" >/dev/null
            adb -s "${ADB_TARGET}" pull /data/local/tmp/uselessfile "${PACKAGE_OUTPUT_FOLDER}/base.art" >/dev/null
        fi
    fi
done