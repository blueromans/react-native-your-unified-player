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

  // Callback when video begins loading
  onLoadStart?: () => void;

  // Callback when video is ready to play
  onReadyToPlay?: () => void;

  // Callback when an error occurs
  onError?: (error: any) => void;

  // Callback when video playback finishes
  onPlaybackComplete?: () => void;

  // Callback for playback progress
  onProgress?: (data: { currentTime: number; duration: number }) => void;

  // Callback when playback is stalled (buffering)
  onPlaybackStalled?: () => void;

  // Callback when playback resumes after stalling
  onPlaybackResumed?: () => void;

  // Callback when playback is paused
  onPaused?: () => void;

  // Callback when playback is playing
  onPlaying?: () => void;
};

// Native component registration
const NativeUnifiedPlayerView =
  requireNativeComponent<UnifiedPlayerProps>('UnifiedPlayerView');

// Newline added here

// Native module for player control methods
const UnifiedPlayerModule = NativeModules.UnifiedPlayer;

// Export event types for reference
export const UnifiedPlayerEventTypes = {
  LOAD_START: 'onLoadStart',
  READY: 'onReadyToPlay',
  ERROR: 'onError',
  PROGRESS: 'onProgress',
  COMPLETE: 'onPlaybackComplete',
  STALLED: 'onPlaybackStalled',
  RESUMED: 'onPlaybackResumed',
  PLAYING: 'onPlaying',
  PAUSED: 'onPaused',
};

// Export events emitter for event listeners
export const UnifiedPlayerEvents = NativeModules.UnifiedPlayer;

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
   * @returns Promise resolving to true if successful
   */
  play: (viewTag: number): Promise<boolean> => {
    try {
      console.log('UnifiedPlayer.play called with viewTag:', viewTag);
      return UnifiedPlayerModule.play(viewTag)
        .then((result: boolean) => {
          console.log('Native play method called successfully');
          return result;
        })
        .catch((error: any) => {
          console.log(
            'Error calling play:',
            error instanceof Error ? error.message : String(error)
          );
          throw error;
        });
    } catch (error) {
      console.log(
        'Error calling play:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
    }
  },

  /**
   * Pause playback
   * @param viewTag - The tag of the player view
   * @returns Promise resolving to true if successful
   */
  pause: (viewTag: number): Promise<boolean> => {
    try {
      console.log('UnifiedPlayer.pause called with viewTag:', viewTag);
      return UnifiedPlayerModule.pause(viewTag)
        .then((result: boolean) => {
          console.log('Native pause method called successfully');
          return result;
        })
        .catch((error: any) => {
          console.log(
            'Error calling pause:',
            error instanceof Error ? error.message : String(error)
          );
          throw error;
        });
    } catch (error) {
      console.log(
        'Error calling pause:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
    }
  },

  /**
   * Seek to a specific time
   * @param viewTag - The tag of the player view
   * @param time - Time in seconds to seek to
   * @returns Promise resolving to true if successful
   */
  seekTo: (viewTag: number, time: number): Promise<boolean> => {
    try {
      console.log(
        'UnifiedPlayer.seekTo called with viewTag:',
        viewTag,
        'time:',
        time
      );
      return UnifiedPlayerModule.seekTo(viewTag, time)
        .then((result: boolean) => {
          console.log('Native seekTo method called successfully');
          return result;
        })
        .catch((error: any) => {
          console.log(
            'Error calling seekTo:',
            error instanceof Error ? error.message : String(error)
          );
          throw error;
        });
    } catch (error) {
      console.log(
        'Error calling seekTo:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
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

  /**
   * Capture the current video frame as a base64 encoded image
   * @param viewTag - The tag of the player view
   * @returns Promise resolving to the base64 encoded image string
   */
  capture: (viewTag: number): Promise<string> => {
    try {
      console.log('UnifiedPlayer.capture called with viewTag:', viewTag);
      return UnifiedPlayerModule.capture(viewTag)
        .then((base64String: string) => {
          console.log('Native capture method called successfully');
          return base64String;
        })
        .catch((error: any) => {
          console.log(
            'Error calling capture:',
            error instanceof Error ? error.message : String(error)
          );
          throw error;
        });
    } catch (error) {
      console.log(
        'Error calling capture:',
        error instanceof Error ? error.message : String(error)
      );
      return Promise.reject(error);
    }
  },
};
