import {
  requireNativeComponent,
  UIManager,
  Platform,
  type ViewStyle,
  NativeModules,
  TurboModuleRegistry,
  type TurboModule,
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
    oneSignalUserId?: string;
    appKey?: string;
    token?: string;
    secret?: string;
  };
};

const ComponentName = 'ExpofpView';

export const ExpofpView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<ExpofpProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

interface ExpofpModule extends TurboModule {
  preload(url: string): Promise<void>;
}

export const preload = async (url: string): Promise<void> => {
  if (Platform.OS === 'ios') {
    const Module = TurboModuleRegistry.get<ExpofpModule>('ExpofpModule');
    if (!Module) {
      throw new Error(LINKING_ERROR);
    }
    return Module.preload(url);
  }
  if (Platform.OS === 'android') {
    const Module = NativeModules.ExpofpModule;
    if (!Module) {
      throw new Error(LINKING_ERROR);
    }
    return Module.preload(url);
  }
  throw new Error('Unsupported platform');
};
