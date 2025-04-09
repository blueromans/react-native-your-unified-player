import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
import type { ViewProps } from 'react-native/Libraries/Components/View/ViewPropTypes';
import type { HostComponent } from 'react-native';
import type {
  DirectEventHandler,
  Int32,
} from 'react-native/Libraries/Types/CodegenTypes';
// Using codegenNativeComponent requires Flow syntax for event types,
// or careful TypeScript configuration. Using basic types here for clarity,
// but precise event payload typing might require Flow syntax within the interface.
// Alternatively, use generic types and handle specifics in JS/TS layer.
// Example using generic types for events:
// type EventPayload = Readonly<{ [key: string]: any }>; // Adjust as needed

// --- Event Payload Interfaces (For reference, use in JS/TS layer) ---
// These would be used when handling the events in your wrapper component,
// not directly in the Codegen interface below in this basic example.
interface UrlLoadEvent {
  duration: Int32;
  naturalSize: { width: Int32; height: Int32 };
}
interface UrlProgressEvent {
  currentTime: Int32;
  duration: Int32;
}
interface WebRTCConnectedEvent {
  connectionInfo: string;
}
interface WebRTCDisconnectedEvent {
  code: Int32;
  reason: string;
}
interface WebRTCStatsEvent {
  stats: string; // Pass stats as JSON string
}
interface ErrorEvent {
  error: string;
  code: string;
}

// --- Source Prop Types ---
interface SourceProps {
  type: string;
  uri: string;
  signalingUrl: string;
}

// --- Native Component Interface for Codegen ---
// Note: Use Readonly<> for objects/arrays passed as props for Codegen.
// Event definitions use DirectEventHandler. Precise payload typing within
// this interface often requires Flow syntax comments (/*: ... */) or careful TS setup.
// Using simple event types here for demonstration.
export interface NativeUnifiedVideoPlayerProps extends ViewProps {
  // Source prop (passed as a Readonly object)
  source: Readonly<SourceProps>;

  // Common Playback controls
  paused: boolean;
  muted: boolean;
  volume: Int32;

  // Resize mode
  resizeMode: string;

  // --- Native Event Callbacks ---
  // URL specific
  onUrlLoad?: DirectEventHandler<Readonly<UrlLoadEvent>>;
  onUrlProgress?: DirectEventHandler<Readonly<UrlProgressEvent>>;
  onUrlEnd?: DirectEventHandler<Readonly<{}>>;
  onUrlReadyForDisplay?: DirectEventHandler<Readonly<{}>>;

  // WebRTC specific
  onWebRTCConnected?: DirectEventHandler<Readonly<WebRTCConnectedEvent>>;
  onWebRTCDisconnected?: DirectEventHandler<Readonly<WebRTCDisconnectedEvent>>;
  onWebRTCStats?: DirectEventHandler<Readonly<WebRTCStatsEvent>>;

  // Common
  onError?: DirectEventHandler<Readonly<ErrorEvent>>;
}

// --- Codegen ---
// This links the interface to the native component named "UnifiedNativeVideoPlayer"
// The actual native implementation must register itself with this name.
export default codegenNativeComponent<NativeUnifiedVideoPlayerProps>(
  'YourUnifiedPlayerView',
  {
    interfaceOnly: true,
    paperComponentNameDeprecated: 'YourUnifiedPlayerView',
  }
) as HostComponent<NativeUnifiedVideoPlayerProps>; // Cast for type safety

// --- Native Commands Interface (If needed) ---
// Define methods callable from JS/TS on the native component instance
export interface UnifiedVideoPlayerNativeCommands {
  // Note: Command arguments need Codegen compatible types
  seekUrl: (
    viewRef: React.ElementRef<HostComponent<NativeUnifiedVideoPlayerProps>>,
    timeSeconds: Int32
  ) => void;
  sendWebRTCMessage: (
    viewRef: React.ElementRef<HostComponent<NativeUnifiedVideoPlayerProps>>,
    message: string
  ) => void;
  // Add other commands
}

// Codegen for commands (if you have commands)
export const Commands: UnifiedVideoPlayerNativeCommands =
  codegenNativeCommands<UnifiedVideoPlayerNativeCommands>({
    supportedCommands: ['seekUrl', 'sendWebRTCMessage'],
  });
