# react-native-unified-player

Unified Player with VLC integration for enhanced codec and streaming protocol support.

## Installation

```sh
npm install react-native-unified-player
```

### iOS Setup for VLC

The iOS version of this player uses MobileVLCKit for advanced video playback capabilities. It supports a wide range of formats including RTSP, RTMP, HLS, and other protocols supported by VLC.

The VLC dependencies are automatically installed via CocoaPods through the podspec file. After installing the package:

1. Navigate to the iOS folder of your project:
   ```sh
   cd ios
   ```

2. Install pods:
   ```sh
   pod install
   ```

3. VLC requires "Bitcode" to be disabled in your Xcode project. This is handled automatically in the podspec file, but if you experience issues, check that your project's Build Settings have "Enable Bitcode" set to "No".

4. Starting from iOS 14, you need to provide a message for the `NSLocalNetworkUsageDescription` key in your `Info.plist` if you're using external streaming sources. Add something like:
   ```xml
   <key>NSLocalNetworkUsageDescription</key>
   <string>This app requires access to the local network to stream media content</string>
   ```

### Troubleshooting VLC Integration

If you encounter build issues with VLC:

1. **Compilation Errors**: If you see errors about missing properties or methods, it might be because the VLC API has changed. This package uses MobileVLCKit 3.3.17, which is compatible with the current implementation.

2. **Playback Issues on iOS 14+**: VLC has known issues with certain RTSP streams on iOS 14. If you experience a black screen or playback failures specifically on newer iOS devices, try the following:
   - Add additional media options in your code:
     ```js
     // Example: Adding network caching for better buffering
     <UnifiedPlayerView 
       videoUrl="rtsp://example.com/stream"
       mediaOptions={["--network-caching=1500", "--live-caching=1500"]}
     />
     ```

3. **Link Required Frameworks**: Make sure you have all required frameworks linked in your Xcode project:
   - AudioToolbox.framework
   - AVFoundation.framework
   - CoreMedia.framework
   - CoreVideo.framework
   - libc++.tbd
   - libiconv.tbd
   - libz.tbd

## Usage

```js
import { UnifiedPlayerView } from "react-native-unified-player";

// ...

<UnifiedPlayerView 
  videoUrl="https://example.com/video.mp4" 
  autoplay={true}
  loop={false}
  authToken="optional-auth-token"
/>
```

### Supported Formats

With VLC integration, the player supports a wide range of formats:

- Network streams: RTSP, RTP, RTMP, HLS, MMS
- Container formats: MP4, MKV, AVI, FLV, MOV, TS, and many more
- Codec support: H.264, H.265, VP8, VP9, and many others
- Audio: Multiple audio tracks support including 5.1
- Subtitles: Support for various subtitle formats including SSA

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
