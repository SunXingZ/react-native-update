import { NativeModules, Platform } from 'react-native';
import DeviceInfo from "react-native-device-info";
import { DocumentDirectoryPath, downloadFile, unlink, exists, mkdir } from 'react-native-fs';
import { unzip } from "react-native-zip-archive";

const Hotupdate = {};

const iosBundleName = "main.jsbundle";
const androidBundleName = "index.android.bundle";
const bundleName = Platform.OS == "ios" ? iosBundleName : androidBundleName;
const bundleDir = `${DocumentDirectoryPath}/JSBundle`;
const platformDir = `${bundleDir}/${Platform.OS}_${DeviceInfo.getVersion()}`;

Hotupdate.bundleFileExists = async() => {
  const bundleFile = `${platformDir}/${bundleName}`;
  const bundleFileExists = await exists(bundleFile).then((exists) => exists).catch(() => false);
  return bundleFileExists;
}

Hotupdate.downloadJSBundleFromServer = async(downloadUrl, progress, callback) => {
  const bundleDirExists = await exists(bundleDir).then((exists) => exists).catch(() => false);
  if (!bundleDirExists) {
    await mkdir(bundleDir);
  }
  const zipFile = `${platformDir}.zip`;
  const options = {
    fromUrl: downloadUrl,
    toFile: zipFile,
    background: false,
    progress: (res) => {
      progress(parseInt((res.bytesWritten / res.contentLength) * 100));
    }
  };
  downloadFile(options).promise.then((response) => {
    // 解压zip文件
    if (response.statusCode == 200) {
      unzip(zipFile, bundleDir)
        .then((path) => {
          callback({
            path,
            status: 1
          });
        })
        .catch((error) => {
          callback({
            error,
            status: 0
          });
        })
        .finally(() => {
          unlink(zipFile);
        });
    } else {
      callback({
        error: "下载失败",
        status: 0
      });
    }
  }).catch((error) => {
    callback({
      error,
      status: 0
    });
  });
}

Hotupdate.reloadJSBundle = () => {
  NativeModules.RNUpdate.reloadJSBundle();
}

export default Hotupdate;

