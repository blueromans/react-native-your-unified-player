require "json"

package = JSON.parse(File.read(File.join(__dir__, "..", "package.json")))
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
  s.dependency "RCT-Folly", folly_version # Use folly_version variable matching RN 0.79.0
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"
  s.dependency "React-jsi"

  # === Add your specific iOS dependencies here ===
  # For WebRTC functionality, uncomment and use the correct library/version
  s.dependency 'GoogleWebRTC'
  # ==============================================

  # Required for Swift code
  s.swift_version = '5.0' # Specify your Swift version

  # --- Build Settings for Fabric/C++ ---
  s.header_dir = "cpp"
  s.pod_target_xcconfig    = {
    "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\"",
    "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17"
  }
  s.compiler_flags = folly_compiler_flags
  s.pod_target_xcconfig = {
    'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/boost" "$(PODS_ROOT)/RCT-Folly"',
    'USE_HEADERMAP' => 'YES',
    'DEFINES_MODULE' => 'YES' # Important for Swift bridging header generation
  }
  # Adjust path based on actual codegen output location for your RN version
  s.user_target_xcconfig = { "HEADER_SEARCH_PATHS" => "\"$(PODS_TARGET_SRCROOT)/../build/generated/ios/react/renderer/components\"" }

end
