package triangle.triangleapp.helpers;

import android.content.Context;
import android.util.Log;

import triangle.triangleapp.BuildConfig;


/**
 * Created by D2110175 on 16-6-2017.
 */

public class InitializationHelper {
    private static int versionCode;
    private static String versionName;
    // Cannot be final because they cannot be initialized yet.

    private ConfigHelper store;
    private static final String TAG = "InitializationHelper";

    /**
     * Constructor
     *
     * @param c a context
     */
    public InitializationHelper(Context c) {
        store = ConfigHelper.getInstance(c);
        checkVersion();
    }

    /**
     * Checks the app version, if different from config version, calls setConfigValues
     */
    private void checkVersion() {
        versionCode = BuildConfig.VERSION_CODE;
        versionName = BuildConfig.VERSION_NAME;

        int configCode = store.getInt(ConfigHelper.KEY_VERSION_CODE);

        if (versionCode != configCode || configCode == 0) {
            Log.i(TAG, "diff versions: " + versionCode + " & " + store.getInt(ConfigHelper.KEY_VERSION_CODE));
            setConfigValues();
        } else {
            String socketIp = store.get(ConfigHelper.KEY_WEBSOCKET_PROTOCOL);
            if (socketIp == null || socketIp.equals("")) {
                setConfigValues();
            }
        }

    }

    /**
     * sets default config values, runs after cache is cleared, clean install, version changes and possibly after device reboots
     */
    private void setConfigValues() {
        store.putInt(ConfigHelper.KEY_VERSION_CODE, versionCode);
        store.put(ConfigHelper.KEY_VERSION_NAME, versionName);
        store.put(ConfigHelper.KEY_WEBSOCKET_PROTOCOL, "ws");

        store.put(ConfigHelper.KEY_USERNAME, "anoniem");
        store.put(ConfigHelper.KEY_WEBAPI_DESTINATION_ADDRESS, "ws://188.226.164.87/server/send");

        store.put(ConfigHelper.KEY_KEY_ALGORITHM, "RSA");
        store.put(ConfigHelper.KEY_SIGN_ALGORITHM, "SHA1withRSA");
        store.put(ConfigHelper.KEY_KEY_SIZE, "1024");
        store.put(ConfigHelper.KEY_KEY_ALIAS, "TriangleKey");
        store.put(ConfigHelper.KEY_KEY_STORE, "AndroidKeyStore");
        store.put(ConfigHelper.KEY_FILE_HELPER_SAVE_LOCATION, "TriangleApp");
        store.put(ConfigHelper.KEY_MAX_RECORD_DURATION, "4000");
        store.put(ConfigHelper.KEY_DISPLAY_ORIENTATION, "90");
        store.put(ConfigHelper.KEY_CHAT_ADDRESS, "ws://188.226.164.87/server/chat/");
        store.save();


    }
}
