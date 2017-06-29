package triangle.triangleapp.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.security.InvalidKeyException;

public class ConfigHelper {
    private static ConfigHelper store;
    private SharedPreferences SP;

    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_VERSION_NAME = "versionName";

    public static final String KEY_USERNAME = "username";
    public static final String KEY_WEBSOCKET_PROTOCOL = "websocket_protocol";

    public static final String KEY_KEY_ALGORITHM = "key_algorithm";
    public static final String KEY_SIGN_ALGORITHM = "sign_algorithm";
    public static final String KEY_KEY_SIZE = "key_size";
    public static final String KEY_KEY_ALIAS = "key_alias";
    public static final String KEY_KEY_STORE = "key_store";
    public static final String KEY_WEBAPI_DESTINATION_ADDRESS = "webapi_destination_address";
    public static final String KEY_FILE_HELPER_SAVE_LOCATION = "file_helper_save_location";
    public static final String KEY_MAX_RECORD_DURATION = "max_record_duration";
    public static final String KEY_DISPLAY_ORIENTATION = "display_orientation";
    public static final String KEY_CHAT_ADDRESS = "chat_address";

    /**
     * sets variables, internal constructor called from getinstance
     *
     * @param context activity context
     */
    private ConfigHelper(Context context) {
        SP = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    /**
     * gets an instance
     *
     * @param context activity context
     * @return static keystore object
     */
    public static ConfigHelper getInstance(Context context) {
        if (store == null) {
            store = new ConfigHelper(context);
        }
        return store;
    }

    /**
     * gets an existing istance
     *
     * @return existing instance
     */
    public static ConfigHelper getInstance() {
        return store;
    }

    /**
     * puts string key-value pair in config, overwrites existing key-value pair if existing
     *
     * @param key   string key value
     * @param value string value
     */
    public void put(String key, String value) {
        Editor editor;
        editor = SP.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * gets the value from given key
     *
     * @param key key to get value from
     * @return string value
     */
    public String get(String key) {
        return SP.getString(key, null);
    }

    /**
     * gets int value from key
     *
     * @param key key to get value from
     * @return int value from key
     */
    public int getInt(String key) {
        String tmp = "";
        int iTmp = 0;
        try {
            iTmp = SP.getInt(key, 0);
            if (iTmp == 0) {
                throw new InvalidKeyException();
            }
        } catch (Exception e) {
            tmp = SP.getString(key, null);
            try {
                iTmp = Integer.parseInt(tmp);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }

        return iTmp;
    }

    /**
     * puts int key-value pair in config, overwrites existing key-value pair if existing
     *
     * @param key string key
     * @param num int value
     */
    public void putInt(String key, int num) {
        Editor editor;
        editor = SP.edit();
        editor.putInt(key, num);
        editor.apply();
    }

    /**
     * saves pendng changes
     */
    public void save() {
        Editor editor;
        editor = SP.edit();
        editor.apply();
    }

    /**
     * clears config file
     */
    public void clear() {
        Editor editor;
        editor = SP.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * deletes config file
     */
    public void remove() {
        Editor editor;
        editor = SP.edit();
        editor.apply();
    }

    /*
    usage:

    private ConfigHelper store;//Holds our key pairs
     public void test(Context context){
        store = ConfigHelper.getInstance(context);//Creates or Gets our key pairs.  You MUST have access to current context!

        store.putInt("initial_int",5);
        store.put("initial_string","initstring");

        store.putInt(<key>,<int value>);
        store.put(<key>,<string value>);
        store.save();

        int getIntVal= store.getInt("initial_int");
        String getStringVal= store.get("initial_string");

        int getIntVal= store.getInt(<key>);
        String getStringVal= store.get(<key>);

        updating values is possible to simply overwrite them.

    }

     */
}