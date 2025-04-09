import { View, StyleSheet } from 'react-native';
import { YourUnifiedPlayerView } from 'react-native-your-unified-player';

export default function App() {
  return (
    <View style={styles.container}>
      <YourUnifiedPlayerView
        source={{
          type: 'url',
          uri: 'https://example.com/sample.mp4',
          signalingUrl: '',
        }}
        style={styles.player}
        paused={false}
        muted={false}
        volume={1.0}
        resizeMode="contain"
        onUrlLoad={(event) => {
          const { duration, naturalSize } = event.nativeEvent;
          console.log('Video loaded:', duration, naturalSize);
        }}
        onError={(event) => {
          const { error, code } = event.nativeEvent;
          console.error('Player error:', error, code);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F5FCFF',
  },
  player: {
    width: '100%',
    height: 300,
    backgroundColor: '#000',
  },
});
