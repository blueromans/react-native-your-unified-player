/**
 * android/src/main/jni/YourLibOnLoad.cpp (Refactored)
 * JNI entry point to register C++ components with Fabric on Android.
 */
 #include <fbjni/fbjni.h>
 #include <react/renderer/componentregistry/ComponentDescriptorProviderRegistry.h>
 // Include generated ComponentDescriptors header (Name based on library spec name from package.json)
 #include <react/renderer/components/YourUnifiedPlayerSpec/ComponentDescriptors.h>
 #include <react/jni/ReactMarker.h>
 #include <memory> // For std::make_shared
 
 // Include your component descriptor header if needed (though often handled by Codegen includes)
 #include "YourUnifiedPlayer.h" // Use refactored header name
 
 namespace facebook {
 namespace react {
 
 // Function to register component descriptors
 void registerYourUnifiedPlayerComponents( // Function name can be anything descriptive
     std::shared_ptr<ComponentDescriptorProviderRegistry const> registry) {
   // Register your component descriptor (use the refactored descriptor class name)
   registry->add(concreteComponentDescriptorProvider<YourUnifiedPlayerComponentDescriptor>()); // Use refactored descriptor name
 }
 
 // JNI OnLoad method
 jint JNI_OnLoad(JavaVM *vm, void *) {
   return facebook::jni::initialize(vm, [] {
     ReactMarker::logMarker(ReactMarker::NATIVE_MODULE_SETUP_START);
 
     // Register Fabric components
     auto registry = std::make_shared<ComponentDescriptorProviderRegistry>();
     registerYourUnifiedPlayerComponents(registry); // Call registration function
 
     ComponentDescriptorRegistry::singleton().setProviderRegistry(registry);
 
     ReactMarker::logMarker(ReactMarker::NATIVE_MODULE_SETUP_END);
   });
 }
 
 } // namespace react
 } // namespace facebook
 
 