# React Native Your Unified Player

A powerful React Native component that provides a unified interface for playing both MP4 videos and WebRTC streams. Built with Fabric and the new React Native architecture.

## Features

- 🎥 Support for MP4 video playback
- 🌐 WebRTC streaming capabilities
- 🔄 Unified interface for both video types
- 📱 Native performance with Fabric architecture
- 🎛️ Comprehensive playback controls
- 📊 Event handling for player states
- 🔍 Support for different resize modes
- 🎚️ Volume and mute controls
- ⏯️ Play/pause functionality

## Installation

```bash
yarn add react-native-your-unified-player
```

or

```bash
npm install react-native-your-unified-player
```

## Usage

### Basic Usage

```jsx
import { YourUnifiedPlayerView } from 'react-native-your-unified-player';

// For MP4 video
<YourUnifiedPlayerView
  source={{
    type: 'url',
    uri: 'https://example.com/video.mp4'
  }}
  style={{ width: '100%', height: 300 }}
  paused={false}
  muted={false}
  volume={1.0}
  resizeMode="contain"
  onUrlLoad={({ duration, naturalSize }) => {
    console.log('Video loaded:', duration, naturalSize);
  }}
  onError={({ error, code }) => {
    console.error('Player error:', error, code);
  }}
/>

// For WebRTC stream
<YourUnifiedPlayerView
  source={{
    type: 'webrtc',
    signalingUrl: 'wss://example.com/signaling',
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' }
    ]
  }}
  style={{ width: '100%', height: 300 }}
  onWebRTCConnected={({ connectionInfo }) => {
    console.log('WebRTC connected:', connectionInfo);
  }}
  onWebRTCDisconnected={({ code, reason }) => {
    console.log('WebRTC disconnected:', code, reason);
  }}
/>
```

### Props

#### Common Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `paused` | `boolean` | `false` | Controls playback state |
| `muted` | `boolean` | `false` | Controls audio mute state |
| `volume` | `number` | `1.0` | Controls audio volume (0.0 to 1.0) |
| `resizeMode` | `'contain' \| 'cover' \| 'stretch'` | `'contain'` | Controls how the video fits in the view |

#### Source Props

For MP4 videos:
```typescript
{
  type: 'url';
  uri: string;
}
```

For WebRTC streams:
```typescript
{
  type: 'webrtc';
  signalingUrl: string;
  streamConfig?: object;
  iceServers?: ReadonlyArray<{ urls: string | ReadonlyArray<string> }>;
}
}
```

### Events

#### URL Events
- `onUrlLoad`: Fired when the video is loaded
- `onUrlProgress`: Fired during video playback
- `onUrlEnd`: Fired when video playback ends
- `onUrlReadyForDisplay`: Fired when the video is ready to display

#### WebRTC Events
- `onWebRTCConnected`: Fired when WebRTC connection is established
- `onWebRTCDisconnected`: Fired when WebRTC connection is lost
- `onWebRTCStats`: Fired with WebRTC connection statistics

#### Common Events
- `onError`: Fired when an error occurs

### Commands

The component supports the following commands:

```typescript
// Seek to a specific time in the video
seekUrl(viewRef: React.RefObject<YourUnifiedPlayerView>, timeSeconds: number): void;

// Send a message through WebRTC data channel
sendWebRTCMessage(viewRef: React.RefObject<YourUnifiedPlayerView>, message: string): void;
```

## Development

### Prerequisites

- Node.js >= 16
- Yarn >= 1.22
- React Native >= 0.79.0
- iOS: Xcode >= 14.0
- Android: Android Studio >= 2022.3

### Setup

1. Clone the repository
2. Install dependencies:
   ```bash
   yarn install
   ```
3. Build the project:
   ```bash
   yarn prepare
   ```

### Running the Example App

1. Navigate to the example directory:
   ```bash
   cd example
   ```
2. Install dependencies:
   ```bash
   yarn install
   ```
3. Run the app:
   ```bash
   yarn ios  # for iOS
   yarn android  # for Android
   ```

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Yaşar Özyurt ([@blueromans](https://github.com/blueromans))

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
