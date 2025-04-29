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
  Platform,
  PermissionsAndroid,
} from 'react-native';
import {
  UnifiedPlayerView,
  UnifiedPlayer,
  UnifiedPlayerEventTypes, // Import event types
} from 'react-native-unified-player';

// Define some sample playlist URLs
const playlistUrls = [
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
];

const singleVideoUrl =
  'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4';
const singleThumbnailUrl =
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg';

function App(): React.JSX.Element {
  const playerRef = useRef(null);
  // currentVideoSource will now always hold a single URL string
  const [currentVideoSource, setCurrentVideoSource] =
    useState<string>(singleVideoUrl);
  const [isPlaylistMode, setIsPlaylistMode] = useState(false);
  const [thumbnailUrl, _setThumbnailUrl] = useState<string>(singleThumbnailUrl);
  const [autoplay, setAutoplay] = useState(true);
  const [loop, setLoop] = useState(false); // Loop prop controls native looping for single video OR playlist looping in JS
  const [isPaused, setIsPaused] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [currentPlayingIndex, setCurrentPlayingIndex] = useState(0); // Track playlist index

  // Track if the player is ready
  const [isPlayerReady, setIsPlayerReady] = useState(false);

  // Store the last progress update time
  const lastProgressTimeRef = useRef(0);
  const lastDurationRef = useRef(0);

  // Track if progress events are being received
  const [progressEventsReceived, setProgressEventsReceived] = useState(false);
  // State to store the captured image base64 string
  const [capturedImage, setCapturedImage] = useState<string | null>(null);
  // State to track recording status
  const [isRecording, setIsRecording] = useState(false);
  // State to store the recorded video path
  const [recordedVideoPath, setRecordedVideoPath] = useState<string | null>(
    null
  );

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

  // Effect to update the video source based on playlist mode and index
  useEffect(() => {
    if (isPlaylistMode) {
      if (
        currentPlayingIndex >= 0 &&
        currentPlayingIndex < playlistUrls.length
      ) {
        const nextUrl = playlistUrls[currentPlayingIndex]!;
        console.log(
          `useEffect setting source to playlist index ${currentPlayingIndex}: ${nextUrl}`
        );
        setCurrentVideoSource(nextUrl);
      } else {
        console.warn(`Invalid playlist index: ${currentPlayingIndex}`);
        // Optionally handle invalid index, e.g., reset to 0 or switch mode
        setCurrentPlayingIndex(0);
        setIsPlaylistMode(false); // Switch back to single mode if index is bad
      }
    } else {
      // If not in playlist mode, ensure the source is the single video URL
      if (currentVideoSource !== singleVideoUrl) {
        console.log(
          `useEffect setting source to single video: ${singleVideoUrl}`
        );
        setCurrentVideoSource(singleVideoUrl);
      }
    }
    // Dependency array: run when mode or index changes
  }, [isPlaylistMode, currentPlayingIndex, currentVideoSource]);

  const handleProgress = (data: any) => {
    // console.log('Progress event received:', data); // Reduce log verbosity
    const eventData = data.nativeEvent || data;
    if (
      eventData &&
      typeof eventData.duration === 'number' &&
      eventData.duration > 0
    ) {
      setCurrentTime(eventData.currentTime);
      setDuration(eventData.duration);
      lastProgressTimeRef.current = eventData.currentTime;
      lastDurationRef.current = eventData.duration;
      if (!progressEventsReceived) {
        setProgressEventsReceived(true);
        console.log('Progress events are being received with valid data');
      }
    }
  };

  // --- Event Handlers ---
  const handleLoadStart = () => {
    // Native code no longer sends index; JS layer manages playlist state.
    console.log(`Event: ${UnifiedPlayerEventTypes.LOAD_START}`);
    // Reset player readiness and progress state for the new video.
    setIsPlayerReady(false);
    setProgressEventsReceived(false);
    setCurrentTime(0);
    setDuration(0);
  };

  const handleReadyToPlay = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.READY}`);
    setIsPlayerReady(true);
    if (autoplay) {
      setTimeout(() => {
        const viewTag = getPlayerViewTag();
        if (viewTag !== null) {
          console.log('Auto-playing video');
          UnifiedPlayer.play(viewTag).catch((error) => {
            console.warn('Auto-play failed:', error);
          });
        }
      }, 500); // Delay might still be needed
    }
  };

  const handleError = (event: { nativeEvent: any }) => {
    console.error(`Event: ${UnifiedPlayerEventTypes.ERROR}`, event.nativeEvent);
    Alert.alert('Player Error', JSON.stringify(event.nativeEvent));
  };

  const handlePlaybackComplete = () => {
    console.log(`Event: ${UnifiedPlayerEventTypes.COMPLETE}`);
    // Playlist logic is now handled entirely in JS for both platforms
    if (isPlaylistMode) {
      const isLastVideo = currentPlayingIndex === playlistUrls.length - 1;
      if (isLastVideo && !loop) {
        console.log('Playlist completed.');
        // Reset index but stay in playlist mode, showing the first item again (paused)
        setCurrentPlayingIndex(0);
        // Don't automatically set source here, useEffect will handle it based on index change
      } else {
        // Advance to the next video index or loop back to the start
        const nextIndex = (currentPlayingIndex + 1) % playlistUrls.length;
        console.log(`Advancing playlist index to ${nextIndex}`);
        // Only update the index state. useEffect will update the source.
        setCurrentPlayingIndex(nextIndex);
      }
    } else if (!isPlaylistMode && loop) {
      // Handle single video looping in JS if native doesn't (belt and suspenders)
      console.log('Single video ended, looping (JS fallback)');
      const viewTag = getPlayerViewTag();
      if (viewTag) {
        UnifiedPlayer.seekTo(viewTag, 0).then(() =>
          UnifiedPlayer.play(viewTag)
        );
      }
    }
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

  const handleFullscreenChanged = (event: {
    nativeEvent: { isFullscreen: boolean };
  }) => {
    console.log('Fullscreen state changed:', event.nativeEvent.isFullscreen);
    setIsFullscreen(event.nativeEvent.isFullscreen);
  };

  const handleToggleFullscreen = () => {
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      const newFullscreenState = !isFullscreen;
      console.log('Toggling fullscreen to:', newFullscreenState);
      UnifiedPlayer.toggleFullscreen(viewTag, newFullscreenState)
        .then(() => {
          console.log('Fullscreen toggled successfully');
          setIsFullscreen(newFullscreenState);
        })
        .catch((error) => {
          console.error('Error toggling fullscreen:', error);
          Alert.alert('Fullscreen Error', String(error));
        });
    }
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

  // Request storage permissions for Android
  const requestStoragePermissions = async () => {
    if (Platform.OS !== 'android') {
      return true;
    }
    try {
      if (Platform.Version >= 33) {
        const result = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.READ_MEDIA_VIDEO
        );
        return result === PermissionsAndroid.RESULTS.GRANTED;
      } else if (Platform.Version >= 29) {
        return true;
      } else {
        const permissions = [
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
          PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
        ];
        const granted = await PermissionsAndroid.requestMultiple(permissions);
        return (
          granted[PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE] ===
            PermissionsAndroid.RESULTS.GRANTED &&
          granted[PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE] ===
            PermissionsAndroid.RESULTS.GRANTED
        );
      }
    } catch (err) {
      console.error('Error requesting storage permissions:', err);
      return false;
    }
  };

  // --- Recording Logic ---
  const handleStartRecording = async () => {
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      if (isRecording) {
        Alert.alert('Recording', 'Recording is already in progress');
        return;
      }
      if (!isPlayerReady || !progressEventsReceived) {
        Alert.alert(
          'Cannot Record',
          'The player is not ready yet. Please wait a moment and try again.'
        );
        return;
      }
      const hasPermissions = await requestStoragePermissions();
      if (!hasPermissions) {
        Alert.alert(
          'Permission Denied',
          'Storage permissions are required to record videos'
        );
        return;
      }
      try {
        console.log('Starting recording...');
        const result = await UnifiedPlayer.startRecording(viewTag);
        if (result) {
          setIsRecording(true);
          Alert.alert('Recording', 'Recording started successfully');
        } else {
          Alert.alert('Recording Error', 'Failed to start recording');
        }
      } catch (error) {
        console.error('Error starting recording:', error);
        Alert.alert('Recording Error', String(error));
      }
    } else {
      console.log('Could not get player view tag for recording');
      Alert.alert('Recording Error', 'Could not find player view.');
    }
  };

  const handleStopRecording = async () => {
    const viewTag = getPlayerViewTag();
    if (viewTag !== null) {
      if (!isRecording) {
        Alert.alert('Recording', 'No recording in progress');
        return;
      }
      try {
        console.log('Stopping recording...');
        const filePath = await UnifiedPlayer.stopRecording(viewTag);
        setIsRecording(false);
        if (filePath && filePath.length > 0) {
          setRecordedVideoPath(filePath);
          Alert.alert('Recording Completed', `Video saved to: ${filePath}`);
        } else {
          Alert.alert('Recording Error', 'Failed to save recording');
        }
      } catch (error) {
        console.error('Error stopping recording:', error);
        Alert.alert('Recording Error', String(error));
        setIsRecording(false);
      }
    } else {
      console.log('Could not get player view tag for stopping recording');
      Alert.alert('Recording Error', 'Could not find player view.');
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

  // Use the state variable directly for display
  const displayedIndex = currentPlayingIndex;

  return (
    <View style={styles.flexContainer}>
      {!isFullscreen ? (
        <ScrollView contentContainerStyle={styles.container}>
          <Text style={styles.title}>Unified Player Example</Text>

          <UnifiedPlayerView
            ref={playerRef}
            videoUrl={currentVideoSource} // Use the state variable for source
            thumbnailUrl={isPlaylistMode ? undefined : thumbnailUrl} // Thumbnail only for single video
            autoplay={autoplay}
            loop={loop} // Loop prop is now primarily for JS logic
            isPaused={isPaused}
            isFullscreen={isFullscreen}
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
            onFullscreenChanged={handleFullscreenChanged}
          />

          <View style={styles.controls}>
            <Text style={styles.currentUrlText}>
              Mode: {isPlaylistMode ? 'Playlist' : 'Single Video'}
              {isPlaylistMode &&
                ` (Item ${displayedIndex + 1}/${playlistUrls.length})`}
            </Text>
            <Text style={styles.currentUrlText}>
              Source:{' '}
              {typeof currentVideoSource === 'string'
                ? currentVideoSource
                : `Playlist (${playlistUrls.length} videos)`}
            </Text>
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
                style={[styles.button, isFullscreen && styles.activeButton]}
                onPress={handleToggleFullscreen}
              >
                <Text style={styles.buttonText}>
                  {isFullscreen ? 'Exit Fullscreen' : 'Enter Fullscreen'}
                </Text>
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
              <TouchableOpacity
                style={styles.button}
                onPress={handleLoopToggle}
              >
                <Text style={styles.buttonText}>
                  Loop: {loop ? 'ON' : 'OFF'}
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.button}
                onPress={handlePauseToggle}
              >
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
              <TouchableOpacity
                style={styles.button}
                onPress={handleGetDuration}
              >
                <Text style={styles.buttonText}>Get Duration</Text>
              </TouchableOpacity>
            </View>

            <View style={styles.buttonRow}>
              <TouchableOpacity
                style={styles.button}
                onPress={reinitializePlayer}
              >
                <Text style={styles.buttonText}>Reinitialize Player</Text>
              </TouchableOpacity>
            </View>

            {/* Toggle Playlist Mode */}
            <View style={styles.buttonRow}>
              <TouchableOpacity
                style={styles.button}
                onPress={() => {
                  const nextIsPlaylist = !isPlaylistMode;
                  setIsPlaylistMode(nextIsPlaylist);
                  // Reset index only when toggling mode
                  setCurrentPlayingIndex(0);
                  // Let useEffect handle setting the correct source
                }}
              >
                <Text style={styles.buttonText}>
                  {isPlaylistMode
                    ? 'Switch to Single Video'
                    : 'Switch to Playlist'}
                </Text>
              </TouchableOpacity>
            </View>

            {/* Add Capture Button */}
            <View style={styles.buttonRow}>
              <TouchableOpacity style={styles.button} onPress={handleCapture}>
                <Text style={styles.buttonText}>Capture Frame</Text>
              </TouchableOpacity>
            </View>

            {/* Add Recording Buttons */}
            <View style={styles.buttonRow}>
              <TouchableOpacity
                style={[
                  styles.button,
                  isRecording ? styles.recordingButton : null,
                ]}
                onPress={handleStartRecording}
                disabled={isRecording}
              >
                <Text style={styles.buttonText}>Start Recording</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={styles.button}
                onPress={handleStopRecording}
                disabled={!isRecording}
              >
                <Text style={styles.buttonText}>Stop Recording</Text>
              </TouchableOpacity>
            </View>

            {/* Display Recording Status */}
            {isRecording && (
              <View style={styles.recordingStatus}>
                <Text style={styles.recordingText}>
                  Recording in progress...
                </Text>
              </View>
            )}

            {/* Display Recorded Video Path and Play Button */}
            {recordedVideoPath && !isRecording && (
              <View style={styles.captureContainer}>
                <Text style={styles.captureTitle}>Recording Saved:</Text>
                <Text style={styles.recordingPath}>{recordedVideoPath}</Text>
                <View style={styles.buttonRow}>
                  <TouchableOpacity
                    style={styles.button}
                    onPress={() => {
                      try {
                        // Log the file path for debugging
                        console.log(
                          'Attempting to play recorded video from path:',
                          recordedVideoPath
                        );

                        // Check if the file exists (for debugging purposes)
                        if (Platform.OS === 'android') {
                          // Format the file path correctly for Android
                          let formattedPath = recordedVideoPath;
                          if (!formattedPath.startsWith('file://')) {
                            formattedPath = `file://${formattedPath}`;
                          }

                          console.log('Formatted path:', formattedPath);

                          // Reset player state before loading new URL
                          setIsPlayerReady(false);
                          setProgressEventsReceived(false);
                          setIsPlaylistMode(false); // Ensure we are in single video mode
                          setCurrentPlayingIndex(0);

                          // Reset the player to the original video first
                          setCurrentVideoSource(singleVideoUrl);

                          // Wait a moment before trying to play the recorded video
                          setTimeout(() => {
                            console.log('Now trying to play recorded video...');
                            // Update the video URL to the recorded video path
                            setCurrentVideoSource(formattedPath);

                            // Alert the user that we're trying to play the video
                            Alert.alert(
                              'Loading Video',
                              'Attempting to play the recorded video...'
                            );
                          }, 1000);
                        }
                      } catch (error) {
                        console.error('Error playing recorded video:', error);
                        Alert.alert(
                          'Playback Error',
                          `Failed to play recorded video: ${error}`
                        );
                      }
                    }}
                  >
                    <Text style={styles.buttonText}>Play Recorded Video</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.button}
                    onPress={() => {
                      // Reset to the original video
                      setIsPlaylistMode(false);
                      setCurrentPlayingIndex(0);
                      setCurrentVideoSource(singleVideoUrl);
                      Alert.alert('Reset', 'Player reset to original video');
                    }}
                  >
                    <Text style={styles.buttonText}>Reset Player</Text>
                  </TouchableOpacity>
                </View>
              </View>
            )}

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
      ) : (
        <>
          <UnifiedPlayerView
            ref={playerRef}
            videoUrl={currentVideoSource}
            thumbnailUrl={isPlaylistMode ? undefined : thumbnailUrl}
            autoplay={autoplay}
            loop={loop}
            isPaused={isPaused}
            isFullscreen={isFullscreen}
            style={styles.fullscreenPlayer}
            onLoadStart={handleLoadStart}
            onReadyToPlay={handleReadyToPlay}
            onError={handleError}
            onProgress={handleProgress}
            onPlaybackComplete={handlePlaybackComplete}
            onPlaybackStalled={handlePlaybackStalled}
            onPlaybackResumed={handlePlaybackResumed}
            onPlaying={handlePlaying}
            onPaused={handlePaused}
            onFullscreenChanged={handleFullscreenChanged}
          />
          <TouchableOpacity
            style={styles.exitFullscreenButton}
            onPress={handleToggleFullscreen}
          >
            <Text style={styles.exitFullscreenButtonText}>✕</Text>
          </TouchableOpacity>
        </>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  flexContainer: {
    flex: 1,
    backgroundColor: '#000',
  },
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
  fullscreenPlayer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    width: '100%',
    height: '100%',
    backgroundColor: '#000',
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
  recordingButton: {
    backgroundColor: '#dc3545', // Red color for recording
  },
  activeButton: {
    backgroundColor: '#28a745', // Green color for active state
  },
  exitFullscreenButton: {
    position: 'absolute',
    top: 40,
    right: 20,
    width: 40,
    height: 40,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1000,
  },
  exitFullscreenButtonText: {
    color: '#fff',
    fontSize: 24,
    fontWeight: 'bold',
  },
  recordingStatus: {
    marginTop: 10,
    padding: 10,
    backgroundColor: 'rgba(220, 53, 69, 0.2)',
    borderRadius: 5,
    width: '90%',
    alignItems: 'center',
  },
  recordingText: {
    color: '#dc3545',
    fontWeight: 'bold',
  },
  recordingPath: {
    fontSize: 12,
    color: '#555',
    textAlign: 'center',
    padding: 10,
    backgroundColor: '#f8f9fa',
    borderRadius: 5,
    borderWidth: 1,
    borderColor: '#ddd',
    width: '100%',
  },
});

export default App;
