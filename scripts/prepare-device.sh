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

SINGLEDEX_PACKAGE="me.hexile.sara.singletextview"
SINGLEDEX_ORIGINAL="${SCRIPT_DIR}/assets/me.hexile.sara.singletextview/singletextview-release-1000000-original.apk"
SINGLEDEX_MODDED="${SCRIPT_DIR}/assets/me.hexile.sara.singletextview/singletextview-release-1000000-modded.apk"

MULTIDEX_PACKAGE="me.hexile.sara.multidex"
MULTIDEX_ORIGINAL="${SCRIPT_DIR}/assets/me.hexile.sara.multidex/multidex-release-1000001-original.apk"
MULTIDEX_MODDED="${SCRIPT_DIR}/assets/me.hexile.sara.multidex/multidex-release-1000001-modded.apk"

selectAdbDevice
getDeviceProperties "${ADB_TARGET}"

# Debug info
logDebug "ADB target: ${ADB_TARGET}"
logDebug "Android SDK: ${ADB_TARGET_SDK}"
logDebug "Android release: ${ADB_TARGET_RELEASE}"
logDebug "Device cpu: ${ADB_TARGET_CPU}"
logDebug "Device isa: ${ADB_TARGET_ISA}"

cleanInstallPackage "${SINGLEDEX_ORIGINAL}" "${SINGLEDEX_PACKAGE}"
cleanInstallPackage "${MULTIDEX_ORIGINAL}" "${MULTIDEX_PACKAGE}"

adbPush "${ADB_TARGET}" "${SINGLEDEX_MODDED}" /sdcard
adbPush "${ADB_TARGET}" "${MULTIDEX_MODDED}" /sdcard