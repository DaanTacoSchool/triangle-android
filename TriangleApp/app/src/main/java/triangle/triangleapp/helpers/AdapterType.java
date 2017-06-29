package triangle.triangleapp.helpers;

import android.support.annotation.IntDef;

/**
 * Created by marco on 21-6-2017.
 */

@IntDef({AdapterType.TYPE_STREAM, AdapterType.TYPE_CHAT})
public @interface AdapterType {
    int TYPE_STREAM = 1;
    int TYPE_CHAT = 2;
}
