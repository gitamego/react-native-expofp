import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-expofp' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type ExpofpProps = {
  style: ViewStyle;
  settings: {
    url: string;
    oneSignalUserId?: string,
    appKey?: string
    token?: string;
    secret?: string;
  }
};

const ComponentName = 'ExpofpView';

export const ExpofpView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<ExpofpProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
