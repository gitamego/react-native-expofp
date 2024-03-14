import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { ExpofpView } from 'react-native-expofp';

export default function App() {
  return (
    <View style={styles.container}>
      <ExpofpView style={styles.box} url="https://demo.expofp.com" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '100%',
    height: '100%',
    marginVertical: 20,
  },
});
