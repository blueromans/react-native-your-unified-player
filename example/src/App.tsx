import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  findNodeHandle,
  Alert,
  NativeModules,
} from 'react-native';
import { UnifiedPlayerView, UnifiedPlayer } from 'react-native-unified-player';

function App(): React.JSX.Element {
  const playerRef = useRef(null);
  const [videoUrl] = useState<string>(
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
  );
  const [autoplay, setAutoplay] = useState(true);
  const [loop, setLoop] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);

  // Track if the player is ready
  const [isPlayerReady, setIsPlayerReady] = useState(false);

  // Store the last progress update time
  const lastProgressTimeRef = useRef(0);
  const lastDurationRef = useRef(0);

  // Track if progress events are being received
  const [progressEventsReceived, setProgressEventsReceived] = useState(false);

  const getPlayerViewTag = () => {
    return findNodeHandle(playerRef.current);
  };

  // Function to reinitialize the player if progress events aren't coming through
  const reinitializePlayer = useCallback(() => {
    console.log('Attempting to reinitialize player');
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      // Pause and play to try to kick-start the player
      UnifiedPlayer.pause(viewTag);
      setTimeout(() => {
        UnifiedPlayer.play(viewTag);
      }, 500);
    }
  }, []);

  // Add a debug log for component mounting
  useEffect(() => {
    console.log('App component mounted');

    // Debug: Log all available native modules
    console.log('Available Native Modules:', Object.keys(NativeModules));
    console.log('UnifiedPlayer module:', NativeModules.UnifiedPlayer);

    // Set up a timer to check if progress events are being received
    const progressCheckTimer = setTimeout(() => {
      if (!progressEventsReceived) {
        console.warn('No progress events received after 5 seconds');
        // Try to reinitialize the player
        reinitializePlayer();
      }
    }, 5000);

    return () => {
      clearTimeout(progressCheckTimer);
      console.log('App component unmounted');
    };
  }, [progressEventsReceived, reinitializePlayer]);

  const handleProgress = (data: any) => {
    console.log('Progress event received:', data);

    // Log only the essential properties to avoid cyclical structure issues
    console.log('Progress event properties:', {
      currentTime: data.nativeEvent?.currentTime,
      duration: data.nativeEvent?.duration,
      hasNativeEvent: !!data.nativeEvent,
      nativeEventKeys: data.nativeEvent ? Object.keys(data.nativeEvent) : [],
    });

    // Check if nativeEvent exists and contains the data
    const eventData = data.nativeEvent || data;

    // Only update if we have valid data
    if (
      eventData &&
      typeof eventData.duration === 'number' &&
      eventData.duration > 0
    ) {
      setCurrentTime(eventData.currentTime);
      setDuration(eventData.duration);

      // Store the last known values
      lastProgressTimeRef.current = eventData.currentTime;
      lastDurationRef.current = eventData.duration;

      // Mark that we've received progress events
      if (!progressEventsReceived) {
        setProgressEventsReceived(true);
        console.log('Progress events are being received with valid data');
      }
    } else {
      console.log(
        'Progress event has invalid duration:',
        eventData ? eventData.duration : 'eventData is null/undefined'
      );
    }
  };

  const handleAutoplayToggle = () => {
    setAutoplay(!autoplay);
  };

  const handleLoopToggle = () => {
    setLoop(!loop);
  };

  const handlePauseToggle = () => {
    setIsPaused(!isPaused);
  };

  const handlePlay = () => {
    const viewTag = getPlayerViewTag();
    console.log('Play button pressed, viewTag:', viewTag);

    // Debug: Log the native module again
    console.log(
      'UnifiedPlayer module when play is called:',
      NativeModules.UnifiedPlayer
    );

    if (viewTag !== null) {
      try {
        UnifiedPlayer.play(viewTag);
        setIsPaused(false);
      } catch (error) {
        console.log('Error calling play method:', error);
      }
    } else {
      console.log('Could not get player view tag for play');
    }
  };

  const handlePause = () => {
    const viewTag = getPlayerViewTag();
    console.log('Pause button pressed, viewTag:', viewTag);

    // Debug: Log the native module again
    console.log(
      'UnifiedPlayer module when pause is called:',
      NativeModules.UnifiedPlayer
    );

    if (viewTag !== null) {
      try {
        UnifiedPlayer.pause(viewTag);
        setIsPaused(true);
      } catch (error) {
        console.log('Error calling pause method:', error);
      }
    } else {
      console.log('Could not get player view tag for pause');
    }
  };

  // JavaScript-based workaround for seek
  const handleSeekTo = (time: number) => {
    console.log('Seek button pressed, attempting to seek to:', time);

    // Check if we've received progress events
    if (!progressEventsReceived) {
      console.warn('Cannot seek: No progress events received yet');
      Alert.alert(
        'Cannot Seek',
        'The player is not ready yet. Please wait a moment and try again.'
      );
      return;
    }

    // Use the isPaused prop to control the player
    // This is a workaround since the direct seekTo method isn't working

    // Store current state
    const wasPlaying = !isPaused;

    // 1. Pause the player
    setIsPaused(true);

    // 2. Wait a moment for the pause to take effect
    setTimeout(() => {
      // 3. Try to use the native seekTo method first
      const viewTag = getPlayerViewTag();
      if (viewTag !== null) {
        try {
          console.log('Attempting native seekTo as a fallback');
          UnifiedPlayer.seekTo(viewTag, time);
        } catch (e) {
          console.warn('Native seekTo failed:', e);
        }
      }

      // 4. Resume playback if it was playing before
      setTimeout(() => {
        if (wasPlaying) {
          setIsPaused(false);
        }
      }, 500);
    }, 300);
  };

  // JavaScript-based workaround for getCurrentTime
  const handleGetCurrentTime = async () => {
    console.log('Get current time button pressed');

    if (!progressEventsReceived) {
      console.warn('Cannot get current time: No progress events received yet');
      Alert.alert(
        'Cannot Get Time',
        'The player is not ready yet. Please wait a moment and try again.'
      );
      return;
    }

    try {
      // Try the native method first
      const viewTag = getPlayerViewTag();
      if (viewTag !== null) {
        const nativeTime = await UnifiedPlayer.getCurrentTime(viewTag);
        console.log('Native getCurrentTime result:', nativeTime);

        // If native method returns a valid time, use it
        if (nativeTime > 0) {
          Alert.alert(
            'Current Time',
            `Native: ${nativeTime.toFixed(2)} seconds`
          );
          return;
        }
      }

      // Fallback to the cached value from progress events
      const jsTime = lastProgressTimeRef.current;
      console.log('JS-based getCurrentTime result:', jsTime);
      Alert.alert('Current Time', `JS Fallback: ${jsTime.toFixed(2)} seconds`);
    } catch (error) {
      console.log('Error in getCurrentTime:', error);
      Alert.alert('Error', `Failed to get current time: ${error}`);
    }
  };

  // JavaScript-based workaround for getDuration
  const handleGetDuration = async () => {
    console.log('Get duration button pressed');

    if (!progressEventsReceived) {
      console.warn('Cannot get duration: No progress events received yet');
      Alert.alert(
        'Cannot Get Duration',
        'The player is not ready yet. Please wait a moment and try again.'
      );
      return;
    }

    try {
      // Try the native method first
      const viewTag = getPlayerViewTag();
      if (viewTag !== null) {
        const nativeDuration = await UnifiedPlayer.getDuration(viewTag);
        console.log('Native getDuration result:', nativeDuration);

        // If native method returns a valid duration, use it
        if (nativeDuration > 0) {
          Alert.alert(
            'Duration',
            `Native: ${nativeDuration.toFixed(2)} seconds`
          );
          return;
        }
      }

      // Fallback to the cached value from progress events
      const jsDuration = lastDurationRef.current;
      console.log('JS-based getDuration result:', jsDuration);
      Alert.alert('Duration', `JS Fallback: ${jsDuration.toFixed(2)} seconds`);
    } catch (error) {
      console.error('Error in getDuration:', error);
      Alert.alert('Error', `Failed to get duration: ${error}`);
    }
  };

  // Handle player ready event
  const handleReadyToPlay = () => {
    console.log('Player is ready to play');
    setIsPlayerReady(true);

    // Start playing to initialize progress events
    if (autoplay) {
      // Add a small delay to ensure the player is fully initialized
      setTimeout(() => {
        const viewTag = getPlayerViewTag();
        if (viewTag !== null) {
          console.log('Auto-playing to initialize progress events');
          UnifiedPlayer.play(viewTag);

          // If we still don't get progress events after playing, try seeking to 0
          // This can sometimes kick-start the progress events
          setTimeout(() => {
            if (!progressEventsReceived) {
              console.log(
                'Trying to kick-start progress events with a seek to 0'
              );
              try {
                UnifiedPlayer.seekTo(viewTag, 0);
              } catch (e) {
                console.warn('Kick-start seek failed:', e);
              }
            }
          }, 1000);
        }
      }, 500);
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
        isPaused={isPaused}
        style={styles.player}
        onReadyToPlay={handleReadyToPlay}
        onPlaybackComplete={() => console.log('Playback complete')}
        onError={(event: { nativeEvent: any }) => {
          console.error('Player Error:', event.nativeEvent);
          Alert.alert('Player Error', JSON.stringify(event.nativeEvent));
        }}
        onProgress={handleProgress}
      />

      <View style={styles.controls}>
        <Text style={styles.currentUrlText}>Current URL: {videoUrl}</Text>
        <Text style={styles.currentUrlText}>
          Progress: {currentTime.toFixed(2)}s / {duration.toFixed(2)}s
        </Text>
        <Text style={styles.statusText}>
          Player Status: {isPlayerReady ? 'Ready' : 'Not Ready'} | Progress
          Events: {progressEventsReceived ? 'Receiving' : 'Not Receiving'}
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
          <TouchableOpacity style={styles.button} onPress={handlePauseToggle}>
            <Text style={styles.buttonText}>
              Paused: {isPaused ? 'ON' : 'OFF'}
            </Text>
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

        <View style={styles.buttonRow}>
          <TouchableOpacity style={styles.button} onPress={reinitializePlayer}>
            <Text style={styles.buttonText}>Reinitialize Player</Text>
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
  statusText: {
    fontSize: 14,
    marginBottom: 10,
    color: '#555',
    textAlign: 'center',
    fontWeight: 'bold',
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
