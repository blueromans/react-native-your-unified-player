require "json"

# Read package.json to get library version etc.
package = JSON.parse(File.read(File.join(__dir__, "package.json")))
# Define Folly flags (check compatibility with your RN version)
folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'
# Define Folly version (check compatibility with your RN version)
folly_version = '2021.07.22.00' # Example, adjust as needed

Pod::Spec.new do |s|
  # Library definition
  s.name         = "YourUnifiedPlayer" # Refactored name
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.platforms    = { :ios => "13.0" } # Minimum iOS version for Fabric/modern RN
  s.source       = { :git => "https://github.com/yourusername/react-native-your-unified-player.git", :tag => "#{s.version}" }

  # Source files for the pod
  s.source_files = "ios/**/*.{h,m,mm,swift}", "cpp/**/*.{h,cpp}" # Include cpp files for Fabric

  # React Native Dependencies (Fabric requires specific dependencies)
  s.dependency "React-Core"
  s.dependency "React-Codegen" # Needed for Codegen support
  s.dependency "RCT-Folly", folly_version # Folly library dependency
  s.dependency "RCTRequired"
  s.dependency "RCTTypeSafety"
  s.dependency "ReactCommon/turbomodule/core" # Core TurboModule/Fabric infra
  s.dependency "React-jsi" # JavaScript Interface dependency

  # === Add your specific iOS dependencies here ===
  # For WebRTC functionality, you MUST add a WebRTC library dependency.
  # Example using Google's official pod (check for latest version/suitability):
  s.dependency 'GoogleWebRTC'
  # Or point to a specific fork or local podspec if needed.
  # ==============================================

  # Required for Swift code compilation within the pod
  s.swift_version = '5.0' # Specify your Swift version

  # --- Build Settings for Fabric/C++ ---
  # Expose C++ headers correctly
  s.header_dir = "cpp" # Directory containing shared C++ headers
  # Define C++ standard and flags
  s.pod_target_xcconfig    = {
    "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\" \"$(PODS_ROOT)/Headers/Public/React-Codegen\"", # Add React-Codegen headers path
    "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
    "CLANG_CXX_LANGUAGE_STANDARD" => "c++17" # Use C++17 for modern RN/Fabric
  }
  # Apply Folly compiler flags
  s.compiler_flags = folly_compiler_flags
  # Additional pod target settings
  s.pod_target_xcconfig = {
    'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/boost" "$(PODS_ROOT)/RCT-Folly"',
    'USE_HEADERMAP' => 'YES',
    'DEFINES_MODULE' => 'YES' # Important for Swift bridging header generation
  }
  # Ensure the app target can find generated headers
  s.user_target_xcconfig = { "HEADER_SEARCH_PATHS" => "\"$(PODS_TARGET_SRCROOT)/../build/generated/ios/react/renderer/components\"" } # Adjust path based on actual codegen output

end
