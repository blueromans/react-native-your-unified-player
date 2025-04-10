import { forwardRef } from 'react';
import { NativeYourUnifiedPlayerView } from './YourUnifiedPlayerViewNativeComponent';
import type { NativeYourUnifiedVideoPlayerProps } from './YourUnifiedPlayerViewNativeComponent';

const YourUnifiedPlayerView = forwardRef<
  any,
  NativeYourUnifiedVideoPlayerProps
>((props, ref) => {
  return <NativeYourUnifiedPlayerView {...props} ref={ref} />;
});

YourUnifiedPlayerView.displayName = 'YourUnifiedPlayerView';

export default YourUnifiedPlayerView;
