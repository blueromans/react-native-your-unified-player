# react-native-unified-player

Unified Player

## Installation

```bash
yarn add react-native-unified-player
# or
npm install react-native-unified-player
```

## Usage

```typescript
import { UnifiedPlayerView, UnifiedPlayer, UnifiedPlayerEventTypes, UnifiedPlayerEvents } from 'react-native-unified-player';
import React, { useRef, useEffect } from 'react';
import { View } from 'react-native';

const MyPlayerComponent = () => {
  const playerRef = useRef(null);

  useEffect(() => {
    const readyListener = UnifiedPlayerEvents.addListener(UnifiedPlayerEventTypes.READY, () => {
      console.log('Player is ready to play');
      // You can call UnifiedPlayer methods here, e.g., UnifiedPlayer.play(playerRef.current.getNativeTag());
    });

    const errorListener = UnifiedPlayerEvents.addListener(UnifiedPlayerEventTypes.ERROR, (error) => {
      console.error('Player error:', error);
    });

    const progressListener = UnifiedPlayerEvents.addListener(UnifiedPlayerEventTypes.PROGRESS, (data) => {
      console.log(`Progress: ${data.currentTime}/${data.duration}`);
    });

    const completeListener = UnifiedPlayerEvents.addListener(UnifiedPlayerEventTypes.COMPLETE, () => {
      console.log('Playback complete');
    });

    return () => {
      readyListener.remove();
      errorListener.remove();
      progressListener.remove();
      completeListener.remove();
    };
  }, []);

  return (
    <View style={{ flex: 1 }}>
      <UnifiedPlayerView
        ref={playerRef}
        style={{ flex: 1 }}
        videoUrl="YOUR_VIDEO_URL_HERE"
        autoplay={false}
        loop={false}
        onReadyToPlay={() => console.log('View prop: Ready to play')}
        onError={(e) => console.log('View prop: Error', e)}
        onPlaybackComplete={() => console.log('View prop: Playback complete')}
        onProgress={(data) => console.log('View prop: Progress', data)}
      />
    </View>
  );
};

export default MyPlayerComponent;
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
