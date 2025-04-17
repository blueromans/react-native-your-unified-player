import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-unified-player' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type UnifiedPlayerProps = {
  color: string;
  style: ViewStyle;
};

const ComponentName = 'UnifiedPlayerView';

export const UnifiedPlayerView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<UnifiedPlayerProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
