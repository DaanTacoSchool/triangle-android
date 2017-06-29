package triangle.triangleapp.domain;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import static triangle.triangleapp.domain.ChatAction.ActionType.ACTION_JOIN;
import static triangle.triangleapp.domain.ChatAction.ActionType.ACTION_LEAVE;
import static triangle.triangleapp.domain.ChatAction.ActionType.ACTION_MESSAGE;
import static triangle.triangleapp.domain.ChatAction.ActionType.ACTION_NONE;

/**
 * Created by marco on 20-6-2017.
 */

public class ChatAction {
    @Expose
    @SerializedName("Message")
    private String message;

    @Expose
    @SerializedName("Timestamp")
    private Date timeStamp;

    @Expose
    @SerializedName("ActionType")
    @ActionType
    private int actionType;

    @Expose
    @SerializedName("StreamId")
    private String streamId;

    @Expose
    @SerializedName("Name")
    private String name;

    private boolean fromMe;

    public ChatAction() {
        this.timeStamp = new Date();
    }

    public ChatAction(String name, String message) {
        this();
        this.name = name;
        this.message = message;
        this.actionType = ACTION_MESSAGE;
        this.fromMe = true;
    }

    public ChatAction(String message, String name, int actionType, String streamId) {
        this();
        this.message = message;
        this.name = name;
        this.actionType = actionType;
        this.streamId = streamId;
        this.fromMe = true;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public int getActionType() {
        return actionType;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getName() {
        return name;
    }

    public boolean isFromMe() {
        return fromMe;
    }

    @IntDef({ACTION_NONE, ACTION_MESSAGE, ACTION_JOIN, ACTION_LEAVE})
    public @interface ActionType {
        int ACTION_NONE = 0;
        int ACTION_MESSAGE = 1;
        int ACTION_JOIN = 2;
        int ACTION_LEAVE = 3;
    }

    public static ChatAction join(String streamId) {
        return new ChatAction("", "", ActionType.ACTION_JOIN, streamId);
    }

    public static ChatAction message(String message, String name, String streamId) {
        return new ChatAction(message, name, ActionType.ACTION_MESSAGE, streamId);
    }
}
