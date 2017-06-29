package triangle.triangleapp.persistence.chat;

import triangle.triangleapp.domain.ChatAction;

/**
 * Created by marco on 20-6-2017.
 */

public interface ChatCallback {
    void onMessageReceived(ChatAction message);
}
