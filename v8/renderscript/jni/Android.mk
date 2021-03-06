LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CLANG := true
LOCAL_SDK_VERSION := 8

LOCAL_SRC_FILES:= \
    android_renderscript_RenderScript.cpp

LOCAL_SHARED_LIBRARIES := \
        libjnigraphics

LOCAL_STATIC_LIBRARIES := \
        libcutils \
        libRSDispatch

rs_generated_include_dir := $(call generated-sources-dir-for,SHARED_LIBRARIES,libRSSupport,,)

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	frameworks/rs \
	frameworks/rs/cpp \
	$(rs_generated_include_dir)

LOCAL_CFLAGS += -Wno-unused-parameter -U_FORTIFY_SOURCE

LOCAL_ADDITIONAL_DEPENDENCIES := $(addprefix $(rs_generated_include_dir)/,rsgApiFuncDecl.h)
LOCAL_MODULE:= librsjni
LOCAL_ADDITIONAL_DEPENDENCIES += $(rs_generated_source)
LOCAL_MODULE_TAGS := optional
LOCAL_REQUIRED_MODULES := libRSSupport
LOCAL_32_BIT_ONLY := true

LOCAL_LDFLAGS += -ldl

include $(BUILD_SHARED_LIBRARY)
