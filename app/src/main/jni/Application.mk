#NDK_TOOLCHAIN_VERSION=4.9
APP_ABI :=  armeabi-v7a arm64-v8a x86
#APP_ABI := armeabi arm64-v8a
#APP_ABI := all

#APP_STL := gnustl_static
APP_STL := c++_static
APP_CPPFLAGS := -std=c++11 -frtti -fexceptions -pie -fPIE -fPIC
APP_PLATFORM := android-16
#APP_OPTIM := release
