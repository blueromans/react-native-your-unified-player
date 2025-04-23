import React, { useState, useRef } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  findNodeHandle,
} from 'react-native';
import { UnifiedPlayerView, UnifiedPlayer } from 'react-native-unified-player';

function App(): React.JSX.Element {
  const playerRef = useRef(null);
  const [videoUrl] = useState<string>(
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
  );
  const [autoplay, setAutoplay] = useState(true);
  const [loop, setLoop] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  const handleProgress = (data: { currentTime: number; duration: number }) => {
    setCurrentTime(data.currentTime);
    setDuration(data.duration);
  };

  const handleAutoplayToggle = () => {
    setAutoplay(!autoplay);
  };

  const handleLoopToggle = () => {
    setLoop(!loop);
  };

  const getPlayerViewTag = () => {
    return findNodeHandle(playerRef.current);
  };

  const handlePlay = () => {
    const viewTag = getPlayerViewTag();
    if (viewTag) {
      UnifiedPlayer.testMethod('Hello from React Native!');
      UnifiedPlayer.play(viewTag);
    }
  };

  const handlePause = () => {
    const viewTag = getPlayerViewTag();
    if (viewTag) {
      UnifiedPlayer.pause(viewTag);
    }
  };

  const handleSeekTo = (time: number) => {
    const viewTag = getPlayerViewTag();
    if (viewTag) {
      UnifiedPlayer.seekTo(viewTag, time);
    }
  };

  const handleGetCurrentTime = async () => {
    const viewTag = getPlayerViewTag();
    if (viewTag) {
      const time = await UnifiedPlayer.getCurrentTime(viewTag);
      console.log('Current Time:', time);
    }
  };

  const handleGetDuration = async () => {
    const viewTag = getPlayerViewTag();
    if (viewTag) {
      const duration = await UnifiedPlayer.getDuration(viewTag);
      console.log('Duration:', duration);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Unified Player Example</Text>

      <UnifiedPlayerView
        ref={playerRef}
        videoUrl={videoUrl}
        autoplay={autoplay}
        loop={loop}
        authToken="YOUR_AUTH_TOKEN_HERE" // Replace with your actual auth token
        style={styles.player}
        onReadyToPlay={() => console.log('Ready to play')}
        onPlaybackComplete={() => console.log('Playback complete')}
        onError={(event: { nativeEvent: any }) =>
          console.error('Player Error:', event.nativeEvent)
        }
        onProgress={handleProgress}
      />

      <View style={styles.controls}>
        <Text style={styles.currentUrlText}>Current URL: {videoUrl}</Text>
        <Text style={styles.currentUrlText}>
          Progress: {currentTime.toFixed(2)}s / {duration.toFixed(2)}s
        </Text>

        <View style={styles.buttonRow}>
          <TouchableOpacity style={styles.button} onPress={handlePlay}>
            <Text style={styles.buttonText}>Play</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handlePause}>
            <Text style={styles.buttonText}>Pause</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.button}
            onPress={() => handleSeekTo(10)}
          >
            <Text style={styles.buttonText}>Seek to 10s</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={styles.button}
            onPress={handleAutoplayToggle}
          >
            <Text style={styles.buttonText}>
              Autoplay: {autoplay ? 'ON' : 'OFF'}
            </Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleLoopToggle}>
            <Text style={styles.buttonText}>Loop: {loop ? 'ON' : 'OFF'}</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={styles.button}
            onPress={handleGetCurrentTime}
          >
            <Text style={styles.buttonText}>Get Current Time</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleGetDuration}>
            <Text style={styles.buttonText}>Get Duration</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    paddingVertical: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333',
  },
  player: {
    width: '90%',
    aspectRatio: 16 / 9, // Standard video aspect ratio
    backgroundColor: 'black',
    marginBottom: 20,
  },
  controls: {
    width: '90%',
    alignItems: 'center',
  },
  currentUrlText: {
    fontSize: 14,
    marginBottom: 5,
    color: '#555',
    textAlign: 'center',
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    marginBottom: 10,
  },
  button: {
    backgroundColor: '#007bff',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 5,
    marginHorizontal: 5,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: '#ccc',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 10,
    color: '#333',
    backgroundColor: '#fff',
  },
});

export default App;
