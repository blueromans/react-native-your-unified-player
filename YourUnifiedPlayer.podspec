require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))
# Common Folly compiler flags
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'
# Folly Version matching RN 0.79.0 requirement (from previous error log)
folly_version = '2024.11.18.00'

Pod::Spec.new do |s|
  s.name         = "YourUnifiedPlayer" # Refactored name
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  # --- Verify Minimum iOS Target ---
  # Check React Native 0.79.0 documentation for the exact minimum required iOS version.
  s.platforms    = { :ios => "13.4" }
  s.source       = { :git => "https://github.com/blueromans/react-native-your-unified-player.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}", "cpp/**/*.{h,cpp}" # Include cpp files for Fabric

  # React Native Dependencies for Fabric (Check compatibility with RN 0.79.0)
  s.dependency "React-Core"
  s.dependency "React-Codegen"
  s.dependency "RCT-Folly", folly_version # Use folly_version variable matching RN 0.79.0
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"
  s.dependency "React-jsi"
  s.dependency "ReactCommon" # Explicit ReactCommon dependency

  # === Add your specific iOS dependencies here ===
  # For WebRTC functionality, uncomment and use the correct library/version
  # s.dependency 'GoogleWebRTC'
  # ==============================================

  # Required for Swift code
  s.swift_version = '5.0'

  # --- Build Settings for Fabric/C++ ---
  s.header_dir = "cpp"

  # Ensure all necessary header paths are included for the pod target
  # Added $(inherited) and more specific paths in previous attempts
  s.pod_target_xcconfig    = {
    "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\" \"$(PODS_ROOT)/React-Core/ReactCommon\" \"$(PODS_ROOT)/ReactCommon\" \"$(PODS_ROOT)/ReactCommon/react/renderer/components/view\" \"$(PODS_ROOT)/React-jsi/jsi/\" \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen/React-Codegen.framework/Headers\"", # Added more paths
    "OTHER_CPLUSPLUSFLAGS" => "$(inherited) -DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1", # Added $(inherited)
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    "DEFINES_MODULE" => "YES", # Moved DEFINES_MODULE here as well
    # Added FRAMEWORK_SEARCH_PATHS
    "FRAMEWORK_SEARCH_PATHS" => "$(inherited) \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen\""
  }
  s.compiler_flags = folly_compiler_flags

  # Ensure the app target can find generated headers (path might vary based on RN/Xcode version)
  s.user_target_xcconfig = { "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_TARGET_SRCROOT)/../build/generated/ios/react/renderer/components\" \"$(PODS_ROOT)/Headers/Public/React-Codegen/react/renderer/components\"" } # Added $(inherited)

end
