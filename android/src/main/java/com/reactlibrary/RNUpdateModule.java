
package com.reactlibrary.update;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSBundleLoader;

import java.io.File;
import java.lang.reflect.Field;

public class RNUpdateModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNUpdateModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNUpdate";
  }

  private ReactInstanceManager resolveInstanceManager() throws NoSuchFieldException, IllegalAccessException {
    ReactInstanceManager instanceManager;
    final Activity currentActivity = getCurrentActivity();
    if (currentActivity == null) {
      return null;
    }
    ReactApplication reactApplication = (ReactApplication) currentActivity.getApplication();
    instanceManager = reactApplication.getReactNativeHost().getReactInstanceManager();
    return instanceManager;
  }

  private void loadBundleLegacy() {
    final Activity currentActivity = getCurrentActivity();
    if (currentActivity == null) {
      return;
    }
    currentActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        currentActivity.recreate();
      }
    });
  }

  public String getVersionNameString() {
    PackageManager packageManager = this.getReactApplicationContext().getPackageManager();
    String packageName = this.getReactApplicationContext().getPackageName();
    String versionName = "";
    try {
      PackageInfo info = packageManager.getPackageInfo(packageName, 0);
      versionName = String.valueOf(info.versionName);
    } catch(PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return versionName;
  }

  public String getJSBundleFile() {
    String bundleFile = this.getReactApplicationContext().getFilesDir().getAbsolutePath() + "/JSBundle/android_" + getVersionNameString() + "/index.android.bundle";
    File file = new File(bundleFile);
    if (file != null && file.exists() && file.length() > 0) {
      return file.getAbsolutePath();
    }
    return "";
  }

  @ReactMethod
  public void reloadJSBundle() {
    String filePath = getJSBundleFile();
    if (filePath == "") {
      return;
    }
    try {
      final ReactInstanceManager instanceManager = resolveInstanceManager();
      if (instanceManager == null) {
        return;
      }
      JSBundleLoader jsBundleLoader = JSBundleLoader.createFileLoader(filePath);
      Field bundleLoaderField = instanceManager.getClass().getDeclaredField("mBundleLoader");
      bundleLoaderField.setAccessible(true);
      bundleLoaderField.set(instanceManager, jsBundleLoader);
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override
        public void run() {
          try {
            instanceManager.recreateReactContextInBackground();
          } catch(Exception e) {
            loadBundleLegacy();
          }
        }
      });
    } catch(Exception e) {
      loadBundleLegacy();
    }
  }

}