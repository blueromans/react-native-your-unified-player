require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
# Common Folly compiler flags
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'
# Folly Version matching RN 0.77.0 requirement (based on previous error log)
folly_version = '2024.11.18.00' # Required by React-cxxreact@0.77.0 in build log

# Define React Native path (adjust if needed)
react_native_path = "../node_modules/react-native"

Pod::Spec.new do |s|
  s.name         = "YourUnifiedPlayer"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  # Minimum iOS Target for RN 0.77.0 (Verify this version!)
  s.platforms    = { :ios => "12.4" } # Verify for 0.77.0
  s.source       = { :git => "https://github.com/yourusername/react-native-your-unified-player.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}", "cpp/**/*.{h,cpp}"
  s.exclude_files = "cpp/CMakeLists.txt"

  # React Native Dependencies for Fabric (Check compatibility with RN 0.77.0)
  s.dependency "React-Core"
  s.dependency "React-Codegen"
  s.dependency "RCT-Folly", folly_version
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"
  s.dependency "React-jsi"
  s.dependency "ReactCommon"
  s.dependency "React-RCTFabric"

  # === Add your specific iOS dependencies here ===
  s.dependency 'GoogleWebRTC' # Keep uncommented as per previous step
  # ==============================================

  s.swift_version = '5.0'
  s.header_dir = "cpp"

  # --- Consolidated Build Settings for Fabric/C++ ---
  # Ensure C++17 standard and libc++ library are used, include necessary paths
  s.pod_target_xcconfig = {
    # Inherit existing paths and add required ones
    "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\" \"$(PODS_TARGET_SRCROOT)/../cpp\" \"$(PODS_ROOT)/React-Core/ReactCommon\" \"$(PODS_ROOT)/ReactCommon\" \"$(PODS_ROOT)/React-jsi/jsi/\" \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen/React-Codegen.framework/Headers\"",
    # Ensure C++ flags inherit and apply Folly flags
    "OTHER_CPLUSPLUSFLAGS" => "$(inherited) -DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32",
    # Explicitly set C++ standard and library
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    "CLANG_CXX_LIBRARY" => "libc++",
    "DEFINES_MODULE" => "YES",
    "FRAMEWORK_SEARCH_PATHS" => "$(inherited) \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen\""
  }
  # s.compiler_flags = folly_compiler_flags # Included via OTHER_CPLUSPLUSFLAGS now

  # User target settings (ensure app can find generated headers)
  s.user_target_xcconfig = {
      "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_TARGET_SRCROOT)/build/generated/ios/react/renderer/components\" \"$(PODS_ROOT)/Headers/Public/React-Codegen/react/renderer/components\""
  }

end
