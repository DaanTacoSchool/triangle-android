package triangle.triangleapp.logic.impl;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import triangle.triangleapp.R;
import triangle.triangleapp.domain.ChatAction;


/**
 * Created by D2110175 on 21-6-2017.
 */

public class ChatArrayAdapter extends ArrayAdapter<ChatAction> {

    private static final String TAG = "ChatArrayAdapter";
    private TextView chatText,chatName;
    private List<ChatAction> ChatActionList = new ArrayList<ChatAction>();
    private Context context;

    @Override
    public void add(ChatAction object) {
        ChatActionList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.ChatActionList.size();
    }

    public ChatAction getItem(int index) {
        return this.ChatActionList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatAction ChatActionObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (ChatActionObj.isFromMe()) {
            row = inflater.inflate(R.layout.right, parent, false);
            chatName = (TextView) row.findViewById(R.id.msgName);
            chatName.setText(ChatActionObj.getName());
            Log.e(TAG,ChatActionObj.getName());
        }else{
            row = inflater.inflate(R.layout.left, parent, false);
            chatName = (TextView) row.findViewById(R.id.msgName);
            chatName.setText(ChatActionObj.getName());
            Log.e(TAG,ChatActionObj.getName());
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(ChatActionObj.getMessage());

        return row;
    }
}