import React, { useState } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  TextInput,
  ScrollView,
} from 'react-native';
import { UnifiedPlayerView } from 'react-native-unified-player';

const STREAMING_URL_OPTIONS = [
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4',
];

function App(): React.JSX.Element {
  const [videoUrl, setVideoUrl] = useState<string>(STREAMING_URL_OPTIONS[0]!);
  const [inputUrl, setInputUrl] = useState('');
  const [urlIndex, setUrlIndex] = useState(0); // Set initial index to 0
  const [autoplay, setAutoplay] = useState(true);
  const [loop, setLoop] = useState(false);

  const handleLoadUrl = (url: string) => {
    setVideoUrl(url);
  };

  const handleNextUrl = () => {
    const nextIndex = (urlIndex + 1) % STREAMING_URL_OPTIONS.length;
    setUrlIndex(nextIndex);
    setVideoUrl(STREAMING_URL_OPTIONS[nextIndex]!);
  };

  const handlePreviousUrl = () => {
    const prevIndex =
      (urlIndex - 1 + STREAMING_URL_OPTIONS.length) %
      STREAMING_URL_OPTIONS.length;
    setUrlIndex(prevIndex);
    setVideoUrl(STREAMING_URL_OPTIONS[prevIndex]!);
  };

  const handleAutoplayToggle = () => {
    setAutoplay(!autoplay);
  };

  const handleLoopToggle = () => {
    setLoop(!loop);
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Unified Player Example</Text>

      <UnifiedPlayerView
        videoUrl={videoUrl}
        autoplay={autoplay}
        loop={loop}
        style={styles.player}
        onReadyToPlay={() => console.log('Ready to play')}
        onPlaybackComplete={() => console.log('Playback complete')}
        onError={(event: { nativeEvent: any }) =>
          console.error('Player Error:', event.nativeEvent)
        }
      />

      <View style={styles.controls}>
        <Text style={styles.currentUrlText}>Current URL Index: {urlIndex}</Text>
        <Text style={styles.currentUrlText}>Current URL: {videoUrl}</Text>

        <View style={styles.buttonRow}>
          <TouchableOpacity style={styles.button} onPress={handlePreviousUrl}>
            <Text style={styles.buttonText}>Previous URL</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleNextUrl}>
            <Text style={styles.buttonText}>Next URL</Text>
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

        <TextInput
          style={styles.input}
          placeholder="Enter custom video URL"
          value={inputUrl}
          onChangeText={setInputUrl}
          placeholderTextColor="#888"
        />
        <TouchableOpacity
          style={styles.button}
          onPress={() => handleLoadUrl(inputUrl)}
        >
          <Text style={styles.buttonText}>Load Custom URL</Text>
        </TouchableOpacity>
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
