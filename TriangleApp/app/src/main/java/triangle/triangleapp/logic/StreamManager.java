package triangle.triangleapp.logic;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import triangle.triangleapp.helpers.ConfigHelper;
import triangle.triangleapp.domain.ChatAction;
import triangle.triangleapp.helpers.IntegrityHelper;
import triangle.triangleapp.logic.impl.CameraLiveStream;
import triangle.triangleapp.persistence.chat.ChatAdapter;
import triangle.triangleapp.persistence.ConnectionCallback;
import triangle.triangleapp.persistence.ViewersCallback;
import triangle.triangleapp.persistence.chat.ChatCallback;
import triangle.triangleapp.persistence.stream.impl.HttpStream;
import triangle.triangleapp.persistence.stream.StreamAdapter;
import triangle.triangleapp.persistence.chat.impl.WebSocketChat;
import triangle.triangleapp.persistence.stream.impl.WebSocketStream;

import static io.reactivex.internal.subscriptions.SubscriptionHelper.cancel;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public class StreamManager {
    private static final String TAG = "StreamManager";
    private LiveStream mLiveStream;
    private boolean mIsStreaming;
    private Surface mPreviewView;
    private StreamAdapter mStreamAdapter;
    private ChatAdapter mChatAdapter;
    private KeyPair mKeyPair;
    private StreamManagerCallback mManagerCallback;

    public StreamManager(StreamManagerCallback managerCallback) {
        mManagerCallback = managerCallback;
        mLiveStream = new CameraLiveStream();
        mChatAdapter = new WebSocketChat();

        StreamCallback httpStreamCallback = new StreamCallback() {
            @Override
            public void onConnectError(Exception e, boolean fatal) {
                mManagerCallback.streamError(e, fatal);
            }

            @Override
            public void onSendError(Exception e, boolean fatal) {
                mManagerCallback.streamError(e, fatal);
            }
        };

        mStreamAdapter = new WebSocketStream(new ViewersCallback() {
            @Override
            public void getViewersCount(int viewersAmount) {
                mManagerCallback.setViewersCount(viewersAmount);
            }
        });

        // Try to get the keypair from the store else we generate
        try {
            mKeyPair = IntegrityHelper.getKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mStreamAdapter.connect(new ConnectionCallback() {
            @Override
            public void onConnected() {
                PublicKey pub = mKeyPair.getPublic();
                mStreamAdapter.sendPublicKey(pub);

                mManagerCallback.streamConnected();

                getViewerCount();

                //Send username to server
                try {
                    String username = ConfigHelper.getInstance().get(ConfigHelper.KEY_USERNAME);
                    JSONObject streamInfo = new JSONObject();
                    streamInfo.put("StreamerName", username);
                    mStreamAdapter.sendText(streamInfo.toString());
                } catch (JSONException ex) {
                    Log.e(TAG, "An error occurred while attempting to send username to server", ex);
                }
                mManagerCallback.streamConnected();
                initChat();
            }

            @Override
            public void onError(@NonNull Exception ex) {
                mManagerCallback.streamError(ex, true);
                Log.e(TAG, "Error occurred during connecting", ex);
            }
        });
    }

    private void initChat() {
        mChatAdapter.connect(new ConnectionCallback() {
            @Override
            public void onConnected() {
                Log.i(TAG, "Chat Adapter has connected!");
                mManagerCallback.chatConnected();

                ChatAction action = ChatAction.join(mStreamAdapter.getId());
                mChatAdapter.sendMessage(action);
                mChatAdapter.setCallback(new ChatCallback() {
                    @Override
                    public void onMessageReceived(ChatAction message) {
                        switch (message.getActionType()) {
                            case ChatAction.ActionType.ACTION_MESSAGE: {
                                mManagerCallback.chatMessageReceived(message);
                                break;
                            }
                            case ChatAction.ActionType.ACTION_JOIN:
                            case ChatAction.ActionType.ACTION_LEAVE:
                            case ChatAction.ActionType.ACTION_NONE:
                            default: {
                                // not needed to handle
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(@NonNull Exception ex) {
                Log.e(TAG, "Error occurred during ChatAdapter connection.", ex);
                mManagerCallback.chatError(ex);
            }
        });
    }

    /**
     * Sets the preview view to use for previews
     *
     * @param surface The surface to use for preview
     */
    public void setPreviewView(@NonNull Surface surface) {
        this.mPreviewView = surface;
    }

    /**
     * Starts or stops the streaming
     *
     * @return True if the streaming started, else returns false
     */
    public boolean stream() {
        if (mIsStreaming) {
            mLiveStream.stop();
        } else {
            mLiveStream.setPreviewView(mPreviewView);
            mLiveStream.start(new FileRecordedCallback() {
                @Override
                public void recordCompleted(@NonNull byte[] fileInBytes) {
                    Log.i(TAG, "File completed");
                    mStreamAdapter.sendFile(fileInBytes, mKeyPair.getPrivate());
                }
            });
        }
        mIsStreaming = !mIsStreaming;

        return mIsStreaming;
    }

    private void getViewerCount() {
        Observable.interval(10, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Long aLong) throws Exception {
                        mStreamAdapter.getViewers(null);
                    }
                });
    }

    /**
     * Checks if streaming
     *
     * @return True if the streaming started, else returns false
     */
    public boolean getIsStreaming() {
        return mIsStreaming;
    }

    /**
     * Sends a chat message using the adapter
     *
     * @param message The message to be sent
     */
    public void sendChatMessage(@NonNull String message) {
        ChatAction chatAction = ChatAction.message(message, ConfigHelper.getInstance().get(ConfigHelper.KEY_USERNAME), mStreamAdapter.getId());
        mChatAdapter.sendMessage(chatAction);
    }
}
