For building the project:

1. make sure there is a file named 'Android.mk' in the jni folder

if not, the content is:


		LOCAL_PATH := $(call my-dir)

		include $(CLEAR_VARS)

		LOCAL_MODULE    := imageprocessing
		LOCAL_SRC_FILES := imageprocessing.c
		LOCAL_LDLIBS    := -lm -llog -ljnigraphics

		include $(BUILD_SHARED_LIBRARY)


2. If there is then run the ndk-build script from the NDK in the directory of the project

3. Run project with ecplipse
