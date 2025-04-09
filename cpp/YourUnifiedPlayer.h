#pragma once

#include <react/renderer/components/view/ConcreteViewShadowNode.h>
#include <react/renderer/core/LayoutContext.h>
#include <react/renderer/imagemanager/ImageManager.h>
#include <react/renderer/componentregistry/ComponentDescriptor.h>

// Include generated spec header (Name based on JS component name 'UnifiedNativeVideoPlayer')
// Replace YourUnifiedPlayerSpec with the actual library name from package.json codegenConfig
#include <react/renderer/components/YourUnifiedPlayerSpec/FBUnifiedNativeVideoPlayerSpec.h>

namespace facebook {
namespace react {

// Forward declarations using generated names based on JS component name
class UnifiedNativeVideoPlayerEventEmitter; // Generated EventEmitter
class YourUnifiedPlayerShadowNode; // Forward declare refactored ShadowNode name

// Component name must match the one registered in JS/TS with requireNativeComponent
extern const char UnifiedNativeVideoPlayerComponentName[];

/*
 * Descriptor for <YourUnifiedPlayer> component. (Refactored)
 */
class YourUnifiedPlayerComponentDescriptor final // Refactored class name
    : public ConcreteComponentDescriptor<YourUnifiedPlayerShadowNode> { // Use refactored ShadowNode name
 public:
  YourUnifiedPlayerComponentDescriptor(ComponentDescriptorParameters const &parameters) // Refactored constructor
      : ConcreteComponentDescriptor<YourUnifiedPlayerShadowNode>(parameters) {} // Use refactored ShadowNode name

};

/*
 * Props for <YourUnifiedPlayer> component. (Refactored)
 */
class YourUnifiedPlayerProps : public ViewProps, public FBUnifiedNativeVideoPlayerSpec_Props { // Refactored class name, Spec_Props is generated
public:
    YourUnifiedPlayerProps() = default;
    YourUnifiedPlayerProps(const PropsParserContext& context, const YourUnifiedPlayerProps &sourceProps, const RawProps &rawProps); // Refactored constructor and sourceProps type

    // Add custom prop handling if needed
};


/*
 * Fabric ShadowNode for <YourUnifiedPlayer> component. (Refactored)
 * Handles layout, props, and potentially state.
 */
class YourUnifiedPlayerShadowNode : public ConcreteViewShadowNode< // Refactored class name
                                          UnifiedNativeVideoPlayerComponentName, // Keep JS registration name
                                          YourUnifiedPlayerProps, // Use refactored Props name
                                          UnifiedNativeVideoPlayerEventEmitter // Use generated EventEmitter name
                                      > {
    // Use refactored class names in Base definition
    using Base = ConcreteViewShadowNode<
        UnifiedNativeVideoPlayerComponentName,
        YourUnifiedPlayerProps,
        UnifiedNativeVideoPlayerEventEmitter>;

   public:
    static ShadowNodeTraits BaseTraits() {
        auto traits = Base::BaseTraits();
        // traits.set(ShadowNodeTraits::Trait::Leaf); // If it cannot have children
        return traits;
    }

    // Refactored constructor
    YourUnifiedPlayerShadowNode(
        const ShadowNodeFragment &fragment,
        const ShadowNodeFamily::Shared &family,
        ShadowNodeTraits traits);

    // Add methods related to layout or state if needed

   private:
    // Add any internal state if managed here
};

} // namespace react
} // namespace facebook
