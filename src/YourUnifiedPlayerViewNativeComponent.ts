/**
 * js/UnifiedVideoPlayerNativeComponent.ts
 *
 * Fabric Native Component Spec for the Unified Player.
 * Defines the interface between JS/TS and the native component using Codegen.
 * Uses Flow syntax comments for event payloads for better Codegen compatibility.
 * This file should ONLY contain the interface and the requireNativeComponent call.
 */
import React from 'react';
import { requireNativeComponent, View } from 'react-native';
import type { ViewProps } from 'react-native/Libraries/Components/View/ViewPropTypes';
import type { Float } from 'react-native/Libraries/Types/CodegenTypes';
import type { DirectEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

// --- Source Prop Interfaces (Used in Native Props definition) ---
interface UrlSourceProps {
  type: 'url';
  uri: string;
  signalingUrl?: string;
}
interface WebRTCSourceProps {
  type: 'webrtc';
  signalingUrl: string;
  streamConfig?: object;
  iceServers?: ReadonlyArray<{ urls: string | ReadonlyArray<string> }>;
}
// Use Readonly<> for complex prop types passed to native
type SourceProps = UrlSourceProps | WebRTCSourceProps;

// --- Native Component Interface for Codegen ---
// Using Flow syntax comments (/*: ... */) inside DirectEventHandler for event payloads.
export interface NativeYourUnifiedVideoPlayerProps extends ViewProps {
  // Source prop
  source?: SourceProps; // Use the Readonly type defined above

  // Common Playback controls
  paused?: boolean;
  muted?: boolean;
  volume?: Float; // Float is often preferred over Double for props unless precision needed

  // Resize mode
  resizeMode?: 'contain' | 'cover' | 'stretch';

  // --- Native Event Callbacks ---
  // Use Flow comments for payload types
  onUrlLoad?: DirectEventHandler<{
    duration: number;
    naturalSize?: { width: number; height: number };
  }>;
  onUrlProgress?: DirectEventHandler<{
    currentTime: number;
    duration: number;
  }>;
  onUrlEnd?: DirectEventHandler<{}>;
  onUrlReadyForDisplay?: DirectEventHandler<{}>;
  onWebRTCConnected?: DirectEventHandler<{
    connectionInfo?: string;
  }>;
  onWebRTCDisconnected?: DirectEventHandler<{
    code?: number;
    reason?: string;
  }>;
  onWebRTCStats?: DirectEventHandler<{
    stats: { [key: string]: any };
  }>;
  onError?: DirectEventHandler<{
    error: string;
    code?: string;
  }>;

  // Add ref support
  ref?: React.LegacyRef<View>;
}

// --- Codegen ---
// Export the raw HostComponent linked to the native implementation name.
export const NativeYourUnifiedPlayerView =
  requireNativeComponent<NativeYourUnifiedVideoPlayerProps>(
    'YourUnifiedPlayerView'
  ) as unknown as React.ComponentType<NativeYourUnifiedVideoPlayerProps>;

// --- Native Commands Interface (Defined here for Codegen) ---
// Note: This interface is used by codegenNativeCommands, not directly by this component export.
export interface YourUnifiedVideoPlayerNativeCommands {
  seekUrl: (viewRef: React.RefObject<any>, timeSeconds: Float) => void;
  sendWebRTCMessage: (viewRef: React.RefObject<any>, message: string) => void;
}

// Codegen for commands (If needed, usually imported separately where used)
// import { codegenNativeCommands } from 'react-native/Libraries/Utilities/codegenNativeCommands';
// export const Commands = codegenNativeCommands<UnifiedVideoPlayerNativeCommands>({
//   supportedCommands: ['seekUrl', 'sendWebRTCMessage'],
// });
