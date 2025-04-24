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
  Image, // Import Image component
} from 'react-native';
import {
  UnifiedPlayerView,
  UnifiedPlayer,
  UnifiedPlayerEventTypes, // Import event types
} from 'react-native-unified-player';

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
  // State to store the captured image base64 string
  const [capturedImage, setCapturedImage] = useState<string | null>(null);

  const getPlayerViewTag = () => {
    return findNodeHandle(playerRef.current);
  };

  // Function to reinitialize the player if progress events aren't coming through
  const reinitializePlayer = useCallback(() => {
    console.log('Attempting to reinitialize player');
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      // Pause and play to try to kick-start the player
      UnifiedPlayer.pause(viewTag)
        .then(() => {
          // Wait a moment before playing
          return new Promise<void>((resolve) => {
            setTimeout(() => {
              resolve();
            }, 500);
          });
        })
        .then(() => {
          return UnifiedPlayer.play(viewTag);
        })
        .then(() => {
          console.log('Player reinitialized successfully');
        })
        .catch((error) => {
          console.warn('Error reinitializing player:', error);
        });
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
  }, [progressEventsReceived, reinitializePlayer]); // Added reinitializePlayer back

  const handleProgress = (data: any) => {
    // console.log('Progress event received:', data); // Reduce log verbosity

    // Log only the essential properties to avoid cyclical structure issues
    // console.log('Progress event properties:', {
    //   currentTime: data.nativeEvent?.currentTime,
    //   duration: data.nativeEvent?.duration,
    //   hasNativeEvent: !!data.nativeEvent,
    //   nativeEventKeys: data.nativeEvent ? Object.keys(data.nativeEvent) : [],
    // });

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
      // console.log( // Reduce log verbosity
      //   'Progress event has invalid duration:',
      //   eventData ? eventData.duration : 'eventData is null/undefined'
      // );
    }
  };

  // --- Event Handlers ---
  const handleLoadStart = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.LOAD_START}`);
  };

  const handleReadyToPlay = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.READY}`);
    setIsPlayerReady(true);

    // Start playing to initialize progress events
    if (autoplay) {
      // Add a small delay to ensure the player is fully initialized
      setTimeout(() => {
        const viewTag = getPlayerViewTag();
        if (viewTag !== null) {
          console.log('Auto-playing to initialize progress events');

          UnifiedPlayer.play(viewTag)
            .then(() => {
              // If we still don't get progress events after playing, try seeking to 0
              // This can sometimes kick-start the progress events
              return new Promise<void>((resolve) => {
                setTimeout(() => {
                  if (!progressEventsReceived) {
                    console.log(
                      'Trying to kick-start progress events with a seek to 0'
                    );
                    UnifiedPlayer.seekTo(viewTag, 0)
                      .then(() => resolve())
                      .catch((error) => {
                        console.warn('Kick-start seek failed:', error);
                        resolve();
                      });
                  } else {
                    resolve();
                  }
                }, 1000);
              });
            })
            .catch((error) => {
              console.warn('Auto-play failed:', error);
            });
        }
      }, 500);
    }
  };

  const handleError = (event: { nativeEvent: any }) => {
    console.error(`Event: ${UnifiedPlayerEventTypes.ERROR}`, event.nativeEvent);
    Alert.alert('Player Error', JSON.stringify(event.nativeEvent));
  };

  const handlePlaybackComplete = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.COMPLETE}`);
  };

  const handlePlaybackStalled = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.STALLED}`);
  };

  const handlePlaybackResumed = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.RESUMED}`);
  };

  const handlePlaying = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.PLAYING}`);
  };

  const handlePaused = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.PAUSED}`);
  };

  // --- Capture Logic ---
  const handleCapture = async () => {
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      console.log('Capture button pressed, viewTag:', viewTag);
      try {
        const base64String = await UnifiedPlayer.capture(viewTag);
        console.log('Capture successful, base64 length:', base64String.length);
        setCapturedImage(`data:image/png;base64,${base64String}`); // Prepend data URI scheme
      } catch (error) {
        console.error('Capture failed:', error);
        Alert.alert('Capture Error', String(error));
        setCapturedImage(null); // Clear previous image on error
      }
    } else {
      console.log('Could not get player view tag for capture');
      Alert.alert('Capture Error', 'Could not find player view.');
    }
  };

  // --- Control Handlers ---
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

    if (viewTag !== null) {
      UnifiedPlayer.play(viewTag)
        .then(() => {
          console.log('Play successful');
          setIsPaused(false);
        })
        .catch((error) => {
          console.log('Error calling play method:', error);
          Alert.alert('Play Error', String(error));
        });
    } else {
      console.log('Could not get player view tag for play');
    }
  };

  const handlePause = () => {
    const viewTag = getPlayerViewTag();
    console.log('Pause button pressed, viewTag:', viewTag);

    if (viewTag !== null) {
      UnifiedPlayer.pause(viewTag)
        .then(() => {
          console.log('Pause successful');
          setIsPaused(true);
        })
        .catch((error) => {
          console.log('Error calling pause method:', error);
          Alert.alert('Pause Error', String(error));
        });
    } else {
      console.log('Could not get player view tag for pause');
    }
  };

  const handleSeekTo = (time: number) => {
    console.log('Seek button pressed, attempting to seek to:', time);

    if (!progressEventsReceived) {
      console.warn('Cannot seek: No progress events received yet');
      Alert.alert(
        'Cannot Seek',
        'The player is not ready yet. Please wait a moment and try again.'
      );
      return;
    }

    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      const wasPlaying = !isPaused;
      if (wasPlaying) {
        UnifiedPlayer.pause(viewTag)
          .then(() => UnifiedPlayer.seekTo(viewTag, time))
          .then(() =>
            wasPlaying ? UnifiedPlayer.play(viewTag) : Promise.resolve(true)
          )
          .then(() => console.log('Seek operation completed successfully'))
          .catch((error) => {
            console.warn('Seek operation failed:', error);
            Alert.alert('Seek Error', String(error));
          });
      } else {
        UnifiedPlayer.seekTo(viewTag, time)
          .then(() => console.log('Seek operation completed successfully'))
          .catch((error) => {
            console.warn('Seek operation failed:', error);
            Alert.alert('Seek Error', String(error));
          });
      }
    } else {
      console.log('Could not get player view tag for seek');
    }
  };

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
      const viewTag = getPlayerViewTag();
      if (viewTag !== null) {
        const nativeTime = await UnifiedPlayer.getCurrentTime(viewTag);
        console.log('Native getCurrentTime result:', nativeTime);
        if (nativeTime > 0) {
          Alert.alert(
            'Current Time',
            `Native: ${nativeTime.toFixed(2)} seconds`
          );
          return;
        }
      }
      const jsTime = lastProgressTimeRef.current;
      console.log('JS-based getCurrentTime result:', jsTime);
      Alert.alert('Current Time', `JS Fallback: ${jsTime.toFixed(2)} seconds`);
    } catch (error) {
      console.log('Error in getCurrentTime:', error);
      Alert.alert('Error', `Failed to get current time: ${error}`);
    }
  };

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
      const viewTag = getPlayerViewTag();
      if (viewTag !== null) {
        const nativeDuration = await UnifiedPlayer.getDuration(viewTag);
        console.log('Native getDuration result:', nativeDuration);
        if (nativeDuration > 0) {
          Alert.alert(
            'Duration',
            `Native: ${nativeDuration.toFixed(2)} seconds`
          );
          return;
        }
      }
      const jsDuration = lastDurationRef.current;
      console.log('JS-based getDuration result:', jsDuration);
      Alert.alert('Duration', `JS Fallback: ${jsDuration.toFixed(2)} seconds`);
    } catch (error) {
      console.error('Error in getDuration:', error);
      Alert.alert('Error', `Failed to get duration: ${error}`);
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
        // Add all event handlers
        onLoadStart={handleLoadStart}
        onReadyToPlay={handleReadyToPlay}
        onError={handleError}
        onProgress={handleProgress}
        onPlaybackComplete={handlePlaybackComplete}
        onPlaybackStalled={handlePlaybackStalled}
        onPlaybackResumed={handlePlaybackResumed}
        onPlaying={handlePlaying}
        onPaused={handlePaused}
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

        {/* Add Capture Button */}
        <View style={styles.buttonRow}>
          <TouchableOpacity style={styles.button} onPress={handleCapture}>
            <Text style={styles.buttonText}>Capture Frame</Text>
          </TouchableOpacity>
        </View>

        {/* Display Captured Image */}
        {capturedImage && (
          <View style={styles.captureContainer}>
            <Text style={styles.captureTitle}>Captured Frame:</Text>
            <Image
              source={{ uri: capturedImage }}
              style={styles.capturedImage}
              resizeMode="contain"
            />
          </View>
        )}
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
  captureContainer: {
    marginTop: 20,
    alignItems: 'center',
    width: '90%',
  },
  captureTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  capturedImage: {
    width: '100%',
    aspectRatio: 16 / 9,
    borderWidth: 1,
    borderColor: '#ccc',
    backgroundColor: '#e0e0e0',
  },
});

export default App;
