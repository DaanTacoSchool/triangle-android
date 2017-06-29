package triangle.triangleapp.presentation.stream;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Koen on 21-06-17.
 */

@IntDef({ConnectionState.UNKNOWN, ConnectionState.CONNECTED, ConnectionState.DISCONNECTED})
@Retention(RetentionPolicy.SOURCE)
public @interface ConnectionState {
    int UNKNOWN = 0;
    int CONNECTED = 1;
    int DISCONNECTED = 2;
}