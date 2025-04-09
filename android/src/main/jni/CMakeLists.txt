# android/src/main/jni/CMakeLists.txt (Refactored)
# Defines C++ build targets for Android NDK

cmake_minimum_required(VERSION 3.13)
project(yourunifiedplayer) # Lowercase project name convention

# Add React Native dependencies (paths might vary based on RN version/setup)
# Ensure REACT_NATIVE_DIR is correctly set in your environment or build scripts
find_package(ReactAndroid REQUIRED CONFIG) # Use find_package for RN >= 0.71

# Include generated Codegen headers from build directory
# The exact path depends on build setup, this is an example
include_directories(${CMAKE_CURRENT_BINARY_DIR}/codegen/jni/react/renderer/components/YourUnifiedPlayerSpec) # Use Spec name from package.json

# Define your library target (use lowercase convention)
add_library(
        yourunifiedplayer # Lowercase library name
        SHARED
        # Your C++ source files
        UnifiedVideoPlayer.cpp # Keeping C++ names generic for now
        UnifiedVideoPlayerShadowNode.cpp
        YourLibOnLoad.cpp
        # Add generated spec implementation file (path depends on Codegen output)
        ${CMAKE_CURRENT_BINARY_DIR}/codegen/jni/react/renderer/components/YourUnifiedPlayerSpec/YourUnifiedPlayerSpec-generated.cpp # Use Spec name
        )

# Link against React Native libraries (names provided by find_package)
target_link_libraries(
        yourunifiedplayer
        ReactAndroid::react_codegen_react_renderer_components_YourUnifiedPlayerSpec # Link generated spec code
        ReactAndroid::react_nativemodule_core
        ReactAndroid::react_render_componentregistry
        ReactAndroid::react_render_core
        ReactAndroid::react_render_debug
        ReactAndroid::react_render_graphics
        ReactAndroid::react_render_imagemanager
        ReactAndroid::react_render_mapbuffer
        ReactAndroid::react_render_mounting
        ReactAndroid::react_render_mounting_core
        ReactAndroid::react_render_uimanager
        ReactAndroid::react_render_view CachingDrawFilter # Example, check required RN libs
        ReactAndroid::turbomodulejsijni
        ReactAndroid::jsi
        ReactAndroid::fbjni
        )
