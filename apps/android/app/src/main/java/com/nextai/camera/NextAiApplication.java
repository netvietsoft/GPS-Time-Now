package com.nextai.camera;

import android.app.Application;
import android.util.Log;

public class NextAiApplication extends Application {
    public static final String TAG = "TimeNowApp";
    private static int sLoadedNativeCount = 0;
    private static String sNativeReport = "";

    @Override
    public void onCreate() {
        super.onCreate();
        String[] libs = new String[] {
            "jpeg",
            "mp3lame",
            "ffmpeg",
            "xheymediav8",
            "xhey_videoedit",
            "native-lib",
            "nnpackage-f99779bd"
        };
        StringBuilder sb = new StringBuilder();
        int loaded = 0;
        for (int i = 0; i < libs.length; i++) {
            String lib = libs[i];
            try {
                System.loadLibrary(lib);
                loaded++;
                sb.append(lib).append(":OK ");
            } catch (Throwable t) {
                sb.append(lib).append(":ERR ");
            }
        }
        sLoadedNativeCount = loaded;
        sNativeReport = sb.toString().trim();
        Log.i(TAG, "Time Now Runtime initialized | NativeLibs=" + loaded + "/7 (" + sNativeReport + ")");
    }

    public static int getLoadedNativeCount() {
        return sLoadedNativeCount;
    }

    public static String getNativeReport() {
        return sNativeReport;
    }
}
