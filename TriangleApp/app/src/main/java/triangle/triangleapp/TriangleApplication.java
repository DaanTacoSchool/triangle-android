package triangle.triangleapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import triangle.triangleapp.helpers.InitializationHelper;

/**
 * Created by marco on 19-6-2017.
 */

public class TriangleApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();

        TriangleApplication.context = getApplicationContext();
        InitializationHelper initHelper = new InitializationHelper(this.getApplicationContext());
        //Auto-executes required code, no need to do this manually. so it is unused as it should!
    }

    /**
     * Gets the context of the application
     *
     * @return The ApplicationContext
     */
    public static Context getAppContext() {
        return TriangleApplication.context;
    }
}
