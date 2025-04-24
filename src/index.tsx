import { type ElementRef, forwardRef } from 'react'; // Import from 'react'
import {
  requireNativeComponent,
  UIManager,
  NativeModules,
  Platform,
  type ViewStyle,
} from 'react-native';

// Check if the native module is available
const LINKING_ERROR =
  `The package 'react-native-unified-player' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

// Verify the native module exists
if (
  !UIManager.getViewManagerConfig('UnifiedPlayerView') &&
  !NativeModules.UnifiedPlayer
) {
  throw new Error(LINKING_ERROR);
}

// Define the props for the UnifiedPlayerView component
export type UnifiedPlayerProps = {
  // Video source URL
  videoUrl: string;

  // Apply custom styling
  style: ViewStyle;

  // Autoplay video when loaded
  autoplay?: boolean;

  // Should video loop when finished
  loop?: boolean;

  // Is the player currently paused
  isPaused?: boolean;

  // Callback when video is ready to play
  onReadyToPlay?: () => void;

  // Callback when an error occurs
  onError?: (error: any) => void;

  // Callback when video playback finishes
  onPlaybackComplete?: () => void;

  // Callback for playback progress
  onProgress?: (data: { currentTime: number; duration: number }) => void;
};

// Native component registration
const NativeUnifiedPlayerView =
  requireNativeComponent<UnifiedPlayerProps>('UnifiedPlayerView');

// Newline added here

// Native module for player control methods
const UnifiedPlayerModule = NativeModules.UnifiedPlayer;

// Export event types for reference
export const UnifiedPlayerEventTypes = {
  READY: 'onReadyToPlay',
  ERROR: 'onError',
  PROGRESS: 'onProgress',
  COMPLETE: 'onPlaybackComplete',
  STALLED: 'onPlaybackStalled',
  RESUMED: 'onPlaybackResumed',
};

/**
 * UnifiedPlayerView component for video playback
 */
export const UnifiedPlayerView = forwardRef<
  ElementRef<typeof NativeUnifiedPlayerView>,
  UnifiedPlayerProps
>((props, ref) => {
  return <NativeUnifiedPlayerView {...props} ref={ref} />;
});

/**
 * API methods for controlling playback
 */
export const UnifiedPlayer = {
  /**
   * Start playback
   * @param viewTag - The tag of the player view
   */
  play: (viewTag: number): void => {
    try {
      console.log('UnifiedPlayer.play called with viewTag:', viewTag);
      UnifiedPlayerModule.play(viewTag);
      console.log('Native play method called successfully');
    } catch (error) {
      console.log(
        'Error calling play:',
        error instanceof Error ? error.message : String(error)
      );
    }
  },

  /**
   * Pause playback
   * @param viewTag - The tag of the player view
   */
  pause: (viewTag: number): void => {
    try {
      console.log('UnifiedPlayer.pause called with viewTag:', viewTag);
      UnifiedPlayerModule.pause(viewTag);
      console.log('Native pause method called successfully');
    } catch (error) {
      console.log(
        'Error calling pause:',
        error instanceof Error ? error.message : String(error)
      );
    }
  },

  /**
   * Seek to a specific time
   * @param viewTag - The tag of the player view
   * @param time - Time in seconds to seek to
   */
  seekTo: (viewTag: number, time: number): void => {
    try {
      console.log(
        'UnifiedPlayer.seekTo called with viewTag:',
        viewTag,
        'time:',
        time
      );
      UnifiedPlayerModule.seekTo(viewTag, time);
      console.log('Native seekTo method called successfully');
    } catch (error) {
      console.log(
        'Error calling seekTo:',
        error instanceof Error ? error.message : String(error)
      );
    }
  },

  /**
   * Get current playback time
   * @param viewTag - The tag of the player view
   * @returns Promise resolving to current time in seconds
   */
  getCurrentTime: (viewTag: number): Promise<number> => {
    try {
      console.log('UnifiedPlayer.getCurrentTime called with viewTag:', viewTag);
      return UnifiedPlayerModule.getCurrentTime(viewTag);
    } catch (error) {
      console.log(
        'Error calling getCurrentTime:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
    }
  },

  /**
   * Get video duration
   * @param viewTag - The tag of the player view
   * @returns Promise resolving to duration in seconds
   */
  getDuration: (viewTag: number): Promise<number> => {
    try {
      console.log('UnifiedPlayer.getDuration called with viewTag:', viewTag);
      return UnifiedPlayerModule.getDuration(viewTag);
    } catch (error) {
      console.log(
        'Error calling getDuration:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
    }
  },
};
