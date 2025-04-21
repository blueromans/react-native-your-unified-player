import React from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-unified-player' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

/**
 * Props for the UnifiedPlayerView component
 */
export type UnifiedPlayerProps = {
  // Video source URL
  videoUrl: string;

  // Apply custom styling
  style: ViewStyle;

  // Autoplay video when loaded
  autoplay?: boolean;

  // Should video loop when finished
  loop?: boolean;

  // Optional auth token for protected streams
  authToken?: string;

  // Callback when video is ready to play
  onReadyToPlay?: () => void;

  // Callback when an error occurs
  onError?: (error: any) => void;

  // Callback when video playback finishes
  onPlaybackComplete?: () => void;

  // Callback for playback progress
  onProgress?: (data: { currentTime: number; duration: number }) => void;
};

// Name of the native component
const ComponentName = 'UnifiedPlayerView';

// Native component import
const NativeUnifiedPlayerView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<UnifiedPlayerProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

// Native module for additional control methods
const UnifiedPlayerModule = NativeModules.UnifiedPlayerModule;

/**
 * UnifiedPlayerView component for video playback
 */
export const UnifiedPlayerView = (props: UnifiedPlayerProps) => {
  return <NativeUnifiedPlayerView {...props} />;
};

/**
 * API methods for controlling playback
 */
export const UnifiedPlayer = {
  // Play the video
  play: (viewTag: number) => {
    if (UnifiedPlayerModule?.play) {
      UnifiedPlayerModule.play(viewTag);
    }
  },

  // Pause the video
  pause: (viewTag: number) => {
    if (UnifiedPlayerModule?.pause) {
      UnifiedPlayerModule.pause(viewTag);
    }
  },

  // Seek to a specific time
  seekTo: (viewTag: number, time: number) => {
    if (UnifiedPlayerModule?.seekTo) {
      UnifiedPlayerModule.seekTo(viewTag, time);
    }
  },

  // Get the current playback time
  getCurrentTime: (viewTag: number): Promise<number> => {
    if (UnifiedPlayerModule?.getCurrentTime) {
      return UnifiedPlayerModule.getCurrentTime(viewTag);
    }
    return Promise.resolve(0);
  },

  // Get the duration of the video
  getDuration: (viewTag: number): Promise<number> => {
    if (UnifiedPlayerModule?.getDuration) {
      return UnifiedPlayerModule.getDuration(viewTag);
    }
    return Promise.resolve(0);
  },
};

// Events emitter for native events
let eventEmitter: NativeEventEmitter | null = null;

if (UnifiedPlayerModule) {
  eventEmitter = new NativeEventEmitter(UnifiedPlayerModule);
}

export const UnifiedPlayerEvents = {
  addListener: (eventType: string, listener: (...args: any[]) => any) => {
    if (eventEmitter) {
      return eventEmitter.addListener(eventType, listener);
    }
    return { remove: () => {} };
  },
};

// Event names
export const UnifiedPlayerEventTypes = {
  READY: 'onReadyToPlay',
  ERROR: 'onError',
  PROGRESS: 'onProgress',
  COMPLETE: 'onPlaybackComplete',
  STALLED: 'onPlaybackStalled',
  RESUMED: 'onPlaybackResumed',
};
