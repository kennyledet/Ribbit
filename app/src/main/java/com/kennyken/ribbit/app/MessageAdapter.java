package com.kennyken.ribbit.app;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/*
 * Custom ArrayAdapter for Inbox message list items (as ParseObjects)
 */
public class MessageAdapter extends ArrayAdapter<ParseObject>{
    protected Context mContext;
    protected List<ParseObject> mMessages;

    public MessageAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.message_item, messages);

        mContext = context;
        mMessages = messages;
    }

    /*
     * Convert vanilla view to message-specific layout
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);

            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseObject message = mMessages.get(position);

        String fileType = message.getString(ParseConstants.KEY_FILE_TYPE);
        int imageResource = getIconImageResource(fileType);
        if (imageResource == -1) {  // default to chat icon if icon can't be resolved
            holder.iconImageView.setImageResource(R.drawable.ic_action_chat);
        } else {
            holder.iconImageView.setImageResource(imageResource);
        }
        holder.nameLabel.setText(message.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }

    /*
     * Includes data to be displayed in custom layout
     */
    private static class ViewHolder {
        ImageView iconImageView;
        TextView nameLabel;
    }

    private int getIconImageResource(String key) {
        int imageResource = -1;
        if ( key.equals(ParseConstants.TYPE_IMAGE) ) {
            imageResource = R.drawable.ic_action_picture;
        } else if ( key.equals(ParseConstants.TYPE_VIDEO) ) {
            imageResource = R.drawable.ic_action_play_over_video;
        } else if ( key.equals(ParseConstants.TYPE_TEXT_MESSAGE) ) {
            imageResource = R.drawable.ic_action_chat;
        }
        return imageResource;
    }
}
