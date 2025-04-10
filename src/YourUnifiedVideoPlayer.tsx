/**
 * js/UnifiedVideoPlayer.tsx
 *
 * User-facing wrapper component for the Fabric UnifiedVideoPlayer.
 * Handles refs, event callbacks, default props, and imperative commands.
 * THIS is the component you should import and use in your app.
 */
import React, {
  useRef,
  useImperativeHandle,
  forwardRef,
  useCallback,
} from 'react';
import { UIManager, findNodeHandle, View } from 'react-native';
import type { NativeSyntheticEvent } from 'react-native';

// Import the interface and the raw native component from the spec file
import { NativeYourUnifiedPlayerView } from './YourUnifiedPlayerViewNativeComponent';
import type { NativeYourUnifiedVideoPlayerProps } from './YourUnifiedPlayerViewNativeComponent';

// If using commands via UIManager (fallback) or needing command definitions
// import { Commands } from './UnifiedVideoPlayerNativeComponent'; // Import Commands if generated

// --- Command Handle Interface (Exported for users of the ref) ---
export interface YourUnifiedVideoPlayerCommands {
  seekUrl: (timeSeconds: number) => void;
  sendWebRTCMessage: (message: string) => void;
}

// --- Event Payload Interfaces (Copied/Imported for use in callbacks) ---
interface UrlLoadEvent {
  duration: number;
  naturalSize?: { width: number; height: number };
}
interface UrlProgressEvent {
  currentTime: number;
  duration: number;
}
interface WebRTCConnectedEvent {
  connectionInfo?: string;
}
interface WebRTCDisconnectedEvent {
  code?: number;
  reason?: string;
}
interface WebRTCStatsEvent {
  stats: { [key: string]: any };
}
interface ErrorEvent {
  error: string;
  code?: string;
}

// --- Component Definition using forwardRef ---
const YourUnifiedVideoPlayer: React.ForwardRefRenderFunction<
  YourUnifiedVideoPlayerCommands,
  NativeYourUnifiedVideoPlayerProps
> = (props, ref) => {
  const {
    onUrlLoad,
    onUrlProgress,
    onUrlEnd,
    onUrlReadyForDisplay,
    onWebRTCConnected,
    onWebRTCDisconnected,
    onWebRTCStats,
    onError,
    ...restProps
  } = props;

  // Ref to the raw native component instance
  const playerRef = useRef<View>(null);

  // --- Event Handlers ---
  const _onUrlLoad = useCallback(
    (event: NativeSyntheticEvent<UrlLoadEvent>) => {
      onUrlLoad && onUrlLoad(event);
    },
    [onUrlLoad]
  );

  const _onUrlProgress = useCallback(
    (event: NativeSyntheticEvent<UrlProgressEvent>) => {
      onUrlProgress && onUrlProgress(event);
    },
    [onUrlProgress]
  );

  const _onUrlEnd = useCallback(
    (event: NativeSyntheticEvent<object>) => {
      onUrlEnd && onUrlEnd(event);
    },
    [onUrlEnd]
  );

  const _onUrlReadyForDisplay = useCallback(
    (event: NativeSyntheticEvent<object>) => {
      onUrlReadyForDisplay && onUrlReadyForDisplay(event);
    },
    [onUrlReadyForDisplay]
  );

  const _onWebRTCConnected = useCallback(
    (event: NativeSyntheticEvent<WebRTCConnectedEvent>) => {
      onWebRTCConnected && onWebRTCConnected(event);
    },
    [onWebRTCConnected]
  );

  const _onWebRTCDisconnected = useCallback(
    (event: NativeSyntheticEvent<WebRTCDisconnectedEvent>) => {
      onWebRTCDisconnected && onWebRTCDisconnected(event);
    },
    [onWebRTCDisconnected]
  );

  const _onWebRTCStats = useCallback(
    (event: NativeSyntheticEvent<WebRTCStatsEvent>) => {
      onWebRTCStats && onWebRTCStats(event);
    },
    [onWebRTCStats]
  );

  const _onError = useCallback(
    (event: NativeSyntheticEvent<ErrorEvent>) => {
      console.error('Native Unified Player Error:', event.nativeEvent);
      onError && onError(event);
    },
    [onError]
  );

  // --- Imperative Commands ---
  useImperativeHandle(ref, () => ({
    seekUrl: (timeSeconds: number) => {
      if (playerRef.current /* && props.source?.type === 'url' */) {
        // Check source type if needed
        const nodeHandle = findNodeHandle(playerRef.current);
        if (nodeHandle) {
          // Option 1: Use generated Commands (Preferred for Fabric)
          // Commands.seekUrl(playerRef.current, timeSeconds);

          // Option 2: Fallback using UIManager (Less ideal for Fabric)
          UIManager.dispatchViewManagerCommand(
            nodeHandle,
            'seekUrl', // Command name must match native side
            [timeSeconds]
          );
        }
      } else {
        console.warn('seekUrl command requires player ref.');
      }
    },
    sendWebRTCMessage: (message: string) => {
      if (playerRef.current /* && props.source?.type === 'webrtc' */) {
        const nodeHandle = findNodeHandle(playerRef.current);
        if (nodeHandle) {
          // Option 1: Use generated Commands
          // Commands.sendWebRTCMessage(playerRef.current, message);

          // Option 2: Fallback using UIManager
          UIManager.dispatchViewManagerCommand(
            nodeHandle,
            'sendWebRTCMessage', // Command name must match native side
            [message]
          );
        }
      } else {
        console.warn('sendWebRTCMessage command requires player ref.');
      }
    },
  }));

  // Render the raw native component, passing props and handlers
  return (
    <NativeYourUnifiedPlayerView
      ref={playerRef}
      {...restProps}
      onUrlLoad={_onUrlLoad}
      onUrlProgress={_onUrlProgress}
      onUrlEnd={_onUrlEnd}
      onUrlReadyForDisplay={_onUrlReadyForDisplay}
      onWebRTCConnected={_onWebRTCConnected}
      onWebRTCDisconnected={_onWebRTCDisconnected}
      onWebRTCStats={_onWebRTCStats}
      onError={_onError}
    />
  );
};

// --- Default Props ---
const defaultProps: Partial<NativeYourUnifiedVideoPlayerProps> = {
  paused: false,
  muted: false,
  volume: 1.0,
  resizeMode: 'contain',
};

// Assign default props to the wrapper component
(
  YourUnifiedVideoPlayer as unknown as { defaultProps: typeof defaultProps }
).defaultProps = defaultProps;

// --- Export with forwardRef ---
export default forwardRef(YourUnifiedVideoPlayer);
