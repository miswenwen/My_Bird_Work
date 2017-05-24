#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := gson \
hyphenatechat \
orgapache \
BASE64Decoder

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := BirdSalesStatistics

include $(BUILD_PACKAGE)
##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson:lib/gson-2.2.2.jar \
hyphenatechat:lib/hyphenatechat_3.2.3.jar \
orgapache:lib/org.apache.http.legacy.liu.jar \
BASE64Decoder:lib/sun.misc.BASE64Decoder.jar 

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
