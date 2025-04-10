require "json"

package = JSON.parse(File.read(File.join(__dir__, "..", "package.json")))
# Common Folly compiler flags
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'
# --- CORRECTED Folly Version based on pod install error log for RN 0.77.0 ---
folly_version = '2024.11.18.00' # Required by React-cxxreact@0.77.0 in this build

# Define React Native path (adjust if needed)
react_native_path = "../node_modules/react-native"

Pod::Spec.new do |s|
  s.name         = "YourUnifiedPlayer"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  # --- Minimum iOS Target for RN 0.77.0 (Verify this version!) ---
  s.platforms    = { :ios => "12.4" } # RN 0.71+ often needed 12.4, verify for 0.77.0
  s.source       = { :git => "https://github.com/yourusername/react-native-your-unified-player.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift}", "cpp/**/*.{h,cpp}"
  s.exclude_files = "cpp/CMakeLists.txt" # Exclude CMakeLists from source_files

  # React Native Dependencies for Fabric (Check compatibility with RN 0.77.0)
  s.dependency "React-Core"
  s.dependency "React-Codegen"
  s.dependency "RCT-Folly", folly_version # Use corrected folly_version variable
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core"
  s.dependency "React-jsi"
  s.dependency "ReactCommon"
  s.dependency "React-RCTFabric" # Explicit Fabric Dependency

  # === Add your specific iOS dependencies here ===
  # --- UNCOMMENTED WebRTC Dependency ---
  s.dependency 'GoogleWebRTC'
  # ==============================================

  s.swift_version = '5.0'
  s.header_dir = "cpp"

  # --- Build Settings for Fabric/C++ ---
  # Check if these paths/flags are correct for RN 0.77.0
  s.pod_target_xcconfig = {
    "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\" \"$(PODS_TARGET_SRCROOT)/../cpp\" \"$(PODS_ROOT)/React-Core/ReactCommon\" \"$(PODS_ROOT)/ReactCommon\" \"$(PODS_ROOT)/React-jsi/jsi/\" \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen/React-Codegen.framework/Headers\"",
    "OTHER_CPLUSPLUSFLAGS" => "$(inherited) -DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
    "DEFINES_MODULE" => "YES",
    "FRAMEWORK_SEARCH_PATHS" => "$(inherited) \"$(PODS_CONFIGURATION_BUILD_DIR)/React-Codegen\""
  }
  s.compiler_flags = folly_compiler_flags

  s.user_target_xcconfig = {
      "HEADER_SEARCH_PATHS" => "$(inherited) \"$(PODS_TARGET_SRCROOT)/build/generated/ios/react/renderer/components\" \"$(PODS_ROOT)/Headers/Public/React-Codegen/react/renderer/components\""
  }

end
