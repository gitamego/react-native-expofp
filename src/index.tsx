import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  NativeModules,
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

// Add native module interface
interface ExpofpModule {
  preload(url: string): Promise<void>;
}

const ExpofpModule = NativeModules.ExpofpModule as ExpofpModule;

export const ExpofpView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<ExpofpProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

// Add preload function
export const preload = async (url: string): Promise<void> => {
  if (!UIManager.getViewManagerConfig(ComponentName)) {
    throw new Error(LINKING_ERROR);
  }
  return UIManager.dispatchViewManagerCommand(
    ComponentName,
    'preload',
    [url]
  );
};
