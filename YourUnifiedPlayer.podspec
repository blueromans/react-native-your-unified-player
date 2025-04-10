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
  s.source       = { :git => "https://github.com/yourusername/react-native-your-unified-player.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}", "cpp/**/*.{h,cpp}" # Include cpp files for Fabric

  # React Native Dependencies for Fabric (Check compatibility with RN 0.79.0)
  s.dependency "React-Core"
  s.dependency "React-Codegen"
  s.dependency "RCT-Folly", folly_version
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"
  s.dependency "React-jsi"
  # Explicitly add ReactCommon as a dependency as well? Sometimes helps.
  s.dependency "ReactCommon"


  # === Add your specific iOS dependencies here ===
  # For WebRTC functionality, uncomment and use the correct library/version
  # s.dependency 'GoogleWebRTC'
  # ==============================================

  # Required for Swift code
  s.swift_version = '5.0'

  # --- Build Settings for Fabric/C++ ---
  s.header_dir = "cpp"
  # Ensure all necessary header paths are included for the pod target
  s.pod_target_xcconfig    = {
    # Added React-jsi path, Codegen output framework headers path
    "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\" \"$(PODS_ROOT)/React-Core/ReactCommon\" \"$(PODS_ROOT)/React-jsi/jsi/\" \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen/React-Codegen.framework/Headers\"",
    "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    "DEFINES_MODULE" => "YES" # Moved DEFINES_MODULE here as well
  }
  s.compiler_flags = folly_compiler_flags
  # Ensure the pod target config includes necessary paths (redundancy can sometimes help)
  # Note: The structure of HEADER_SEARCH_PATHS here might be slightly different syntax
  # s.pod_target_xcconfig['HEADER_SEARCH_PATHS'] ||= '$(inherited) ' # Start with inherited paths
  # s.pod_target_xcconfig['HEADER_SEARCH_PATHS'] += '"$(PODS_ROOT)/boost" '
  # s.pod_target_xcconfig['HEADER_SEARCH_PATHS'] += '"$(PODS_ROOT)/RCT-Folly" '
  # s.pod_target_xcconfig['HEADER_SEARCH_PATHS'] += '"$(PODS_ROOT)/React-Core/ReactCommon" ' # Already added above, ensure consistent syntax
  # s.pod_target_xcconfig['HEADER_SEARCH_PATHS'] += '"$(PODS_ROOT)/React-jsi/jsi/"'

  # Ensure the app target can find generated headers (path might vary based on RN/Xcode version)
  s.user_target_xcconfig = { "HEADER_SEARCH_PATHS" => "\"$(PODS_TARGET_SRCROOT)/../build/generated/ios/react/renderer/components\" \"$(PODS_ROOT)/Headers/Public/React-Codegen/react/renderer/components\"" } # Added alternative path

end
