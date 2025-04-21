/* eslint-disable react-native/no-inline-styles */
import React, { useState, useRef, useCallback } from 'react';
import {
  View,
  StyleSheet,
  Text,
  SafeAreaView,
  TextInput,
  ScrollView,
  TouchableOpacity,
  findNodeHandle,
  Alert,
  ActivityIndicator,
} from 'react-native';
import {
  UnifiedPlayerView,
  UnifiedPlayer,
  UnifiedPlayerEvents,
  UnifiedPlayerEventTypes,
} from 'react-native-unified-player';

// Different URL formats to try
const STREAMING_URL_OPTIONS = [
  {
    label: 'Future date URL with live=true',
    url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
  },
];

const App = () => {
  // Player reference and view tag for native module commands
  const playerRef = useRef<any>(null);
  const [viewTag, setViewTag] = useState<number | null>(null);

  // Player state
  const [playing, setPlaying] = useState(false);
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [logMessages, setLogMessages] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Input state
  const [urlIndex, setUrlIndex] = useState(8);
  const [videoUrl, setVideoUrl] = useState(STREAMING_URL_OPTIONS[0]?.url || '');
  const [authToken, setAuthToken] = useState('');

  // Store the log message with timestamp
  const addLog = useCallback((message: string) => {
    const now = new Date().toISOString();
    const parts = now.split('T');
    const timestamp = parts.length > 1 ? parts[1]?.split('.')[0] || '' : '';
    const logEntry = `[${timestamp}] ${message}`;
    setLogMessages((prevLogs) => [logEntry, ...prevLogs.slice(0, 19)]);
  }, []);

  // Handle play button
  const handlePlay = useCallback(() => {
    if (viewTag !== null) {
      UnifiedPlayer.play(viewTag);
      setPlaying(true);
      addLog('Play command sent');
    }
  }, [viewTag, addLog]);

  // Handle pause button
  const handlePause = useCallback(() => {
    if (viewTag !== null) {
      UnifiedPlayer.pause(viewTag);
      setPlaying(false);
      addLog('Pause command sent');
    }
  }, [viewTag, addLog]);

  // Handle seek
  const handleSeek = useCallback(
    (seconds: number) => {
      if (viewTag !== null) {
        UnifiedPlayer.seekTo(viewTag, seconds);
        addLog(`Seek to ${seconds} seconds`);
      }
    },
    [viewTag, addLog]
  );

  // Try next URL option
  const handleTryNextUrl = useCallback(() => {
    setIsLoading(true);
    setError(null);

    const nextIndex = (urlIndex + 1) % STREAMING_URL_OPTIONS.length;
    setUrlIndex(nextIndex);
    const nextUrl = STREAMING_URL_OPTIONS[nextIndex]?.url || '';
    setVideoUrl(nextUrl);

    addLog(`Trying URL: ${nextUrl}`);

    // Give time for the player to reset
    setTimeout(() => {
      setIsLoading(false);
    }, 500);
  }, [urlIndex, addLog]);

  // Setup event listeners
  React.useEffect(() => {
    // Get view tag when the ref is available
    if (playerRef.current) {
      const tag = findNodeHandle(playerRef.current);
      if (tag) {
        setViewTag(tag);
      }
    }

    // Setup event listeners
    const readyListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.READY,
      () => {
        addLog('Player ready to play');
        // Auto-play when ready
        handlePlay();
      }
    );

    const errorListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.ERROR,
      (event) => {
        const errorMessage = `Error: ${event.code} - ${event.message}`;
        setError(errorMessage);
        addLog(errorMessage);

        // Show alert with option to try next URL
        Alert.alert(
          'Playback Error',
          `Failed to play this stream.\n\nError: ${event.message}`,
          [
            {
              text: 'Try Next URL',
              onPress: handleTryNextUrl,
            },
            {
              text: 'OK',
              style: 'cancel',
            },
          ]
        );
      }
    );

    const progressListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.PROGRESS,
      (event) => {
        setCurrentTime(event.currentTime);
        setDuration(event.duration);
      }
    );

    const completeListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.COMPLETE,
      () => {
        setPlaying(false);
        addLog('Playback completed');
      }
    );

    const stalledListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.STALLED,
      () => {
        addLog('Playback stalled');
      }
    );

    const resumedListener = UnifiedPlayerEvents.addListener(
      UnifiedPlayerEventTypes.RESUMED,
      () => {
        addLog('Playback resumed');
      }
    );

    // Clean up listeners
    return () => {
      readyListener.remove();
      errorListener.remove();
      progressListener.remove();
      completeListener.remove();
      stalledListener.remove();
      resumedListener.remove();
    };
  }, [handlePlay, addLog, handleTryNextUrl]);

  // Format time for display
  const formatTime = useCallback((seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }, []);

  // Load a new URL
  const handleLoadUrl = useCallback(() => {
    setError(null);
    // Check for future date in URL
    if (videoUrl.includes('start=') && videoUrl.includes('2025')) {
      addLog(
        'Warning: URL contains a future date which may cause playback issues'
      );
    }
    addLog(`Loading URL: ${videoUrl}`);
    setVideoUrl(videoUrl);
  }, [videoUrl, addLog]);

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.title}>Custom Video Player</Text>

      <View style={styles.urlInfoContainer}>
        <Text style={styles.urlInfoText}>
          URL {urlIndex + 1}/{STREAMING_URL_OPTIONS.length}:{' '}
          {STREAMING_URL_OPTIONS[urlIndex]?.label || 'Unknown'}
        </Text>
      </View>

      <View style={styles.urlContainer}>
        <TextInput
          style={styles.urlInput}
          value={videoUrl}
          onChangeText={setVideoUrl}
          placeholder="Enter video URL"
        />
        <TouchableOpacity style={styles.loadButton} onPress={handleLoadUrl}>
          <Text style={styles.buttonText}>Load</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.authContainer}>
        <TextInput
          style={styles.authInput}
          value={authToken}
          onChangeText={setAuthToken}
          placeholder="Auth token (optional)"
        />
      </View>

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      <View style={styles.playerContainer}>
        {isLoading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#fff" />
            <Text style={styles.loadingText}>Loading Stream...</Text>
          </View>
        ) : (
          <UnifiedPlayerView
            videoUrl={videoUrl}
            authToken={authToken}
            style={styles.player}
            autoplay={true}
            loop={false}
            // @ts-ignore - React Native requires ref as a special prop
            ref={playerRef}
          />
        )}
      </View>

      <View style={styles.controlsContainer}>
        <View style={styles.progressRow}>
          <Text style={styles.timeText}>{formatTime(currentTime)}</Text>
          <View style={styles.progressBar}>
            <View
              style={[
                styles.progressFill,
                {
                  width:
                    duration > 0 ? `${(currentTime / duration) * 100}%` : 0,
                },
              ]}
            />
          </View>
          <Text style={styles.timeText}>{formatTime(duration)}</Text>
        </View>

        <View style={styles.buttonRow}>
          <TouchableOpacity
            style={[styles.controlButton, styles.seekButton]}
            onPress={() => handleSeek(Math.max(0, currentTime - 10))}
          >
            <Text style={styles.buttonText}>-10s</Text>
          </TouchableOpacity>

          {playing ? (
            <TouchableOpacity
              style={[styles.controlButton, styles.pauseButton]}
              onPress={handlePause}
            >
              <Text style={styles.buttonText}>Pause</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={[styles.controlButton, styles.playButton]}
              onPress={handlePlay}
            >
              <Text style={styles.buttonText}>Play</Text>
            </TouchableOpacity>
          )}

          <TouchableOpacity
            style={[styles.controlButton, styles.seekButton]}
            onPress={() => handleSeek(currentTime + 10)}
          >
            <Text style={styles.buttonText}>+10s</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity
          style={styles.nextUrlButton}
          onPress={handleTryNextUrl}
        >
          <Text style={styles.buttonText}>Try Next URL Format</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.logContainer}>
        <Text style={styles.logTitle}>Player Log:</Text>
        <ScrollView style={styles.logScroll}>
          {logMessages.map((message, index) => (
            <Text key={index} style={styles.logMessage}>
              {message}
            </Text>
          ))}
        </ScrollView>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginTop: 10,
    marginBottom: 10,
  },
  urlInfoContainer: {
    width: '90%',
    padding: 5,
    backgroundColor: '#e0e0e0',
    borderRadius: 4,
    marginBottom: 5,
  },
  urlInfoText: {
    fontSize: 12,
    color: '#333',
    textAlign: 'center',
  },
  urlContainer: {
    width: '90%',
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  urlInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 4,
    padding: 8,
    backgroundColor: '#fff',
    marginRight: 10,
    fontSize: 12,
  },
  loadButton: {
    backgroundColor: '#4a86e8',
    padding: 10,
    borderRadius: 4,
  },
  authContainer: {
    width: '90%',
    marginBottom: 10,
  },
  authInput: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 4,
    padding: 8,
    backgroundColor: '#fff',
    fontSize: 12,
  },
  errorContainer: {
    width: '90%',
    backgroundColor: '#ffeeee',
    borderWidth: 1,
    borderColor: '#ff6666',
    borderRadius: 4,
    padding: 8,
    marginBottom: 10,
  },
  errorText: {
    color: '#cc0000',
    fontSize: 12,
  },
  playerContainer: {
    width: '90%',
    height: 220,
    backgroundColor: '#000',
    borderRadius: 8,
    overflow: 'hidden',
    marginBottom: 15,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.8)',
  },
  loadingText: {
    color: '#fff',
    marginTop: 10,
  },
  player: {
    width: '100%',
    height: '100%',
  },
  controlsContainer: {
    width: '90%',
    marginBottom: 10,
  },
  progressRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  timeText: {
    fontSize: 12,
    color: '#666',
    width: 40,
    textAlign: 'center',
  },
  progressBar: {
    flex: 1,
    height: 6,
    backgroundColor: '#ddd',
    borderRadius: 3,
    marginHorizontal: 8,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#4a86e8',
    borderRadius: 3,
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 15,
    marginBottom: 10,
  },
  controlButton: {
    paddingHorizontal: 15,
    paddingVertical: 10,
    borderRadius: 4,
    minWidth: 80,
    alignItems: 'center',
  },
  playButton: {
    backgroundColor: '#4caf50',
  },
  pauseButton: {
    backgroundColor: '#ff9800',
  },
  seekButton: {
    backgroundColor: '#607d8b',
  },
  nextUrlButton: {
    backgroundColor: '#673ab7',
    paddingVertical: 10,
    borderRadius: 4,
    alignItems: 'center',
  },
  buttonText: {
    color: 'white',
    fontWeight: '600',
  },
  logContainer: {
    width: '90%',
    flex: 1,
    backgroundColor: '#333',
    borderRadius: 8,
    padding: 10,
    marginBottom: 20,
  },
  logTitle: {
    color: '#fff',
    fontWeight: 'bold',
    marginBottom: 5,
  },
  logScroll: {
    flex: 1,
  },
  logMessage: {
    color: '#ddd',
    fontSize: 11,
    fontFamily: 'Courier',
    marginBottom: 3,
  },
});

export default App;
