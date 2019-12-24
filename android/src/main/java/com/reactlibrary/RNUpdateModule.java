
package com.reactlibrary.update;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.os.Build;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.FileProvider;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.JSBundleLoader;

import java.io.File;
import java.lang.reflect.Field;

public class RNUpdateModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private static boolean ActionViewVisible = false;
  static ReactApplicationContext RCTContext;

  public RNUpdateModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    RCTContext = reactContext;
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
    public void actionViewIntent(String path, String mime, final Promise promise) {
        try {
            Uri uriForFile = FileProvider.getUriForFile(getCurrentActivity(),
                    this.getReactApplicationContext().getPackageName() + ".provider", new File(path));

            if (Build.VERSION.SDK_INT >= 24) {
                // Create the intent with data and type
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uriForFile, mime);
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Set flag to give temporary permission to external app to use FileProvider
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Validate that the device can open the file
                PackageManager pm = getCurrentActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    this.getReactApplicationContext().startActivity(intent);
                }

            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse("file://" + path), mime).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                this.getReactApplicationContext().startActivity(intent);
            }
            ActionViewVisible = true;

            final LifecycleEventListener listener = new LifecycleEventListener() {
                @Override
                public void onHostResume() {
                    if(ActionViewVisible)
                        promise.resolve(null);
                    RCTContext.removeLifecycleEventListener(this);
                }

                @Override
                public void onHostPause() {

                }

                @Override
                public void onHostDestroy() {

                }
            };
            RCTContext.addLifecycleEventListener(listener);
        } catch(Exception ex) {
            promise.reject("EUNSPECIFIED", ex.getLocalizedMessage());
        }
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