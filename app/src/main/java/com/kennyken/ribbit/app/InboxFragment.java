package com.kennyken.ribbit.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class InboxFragment extends ListFragment {
    protected List<ParseObject> mMessages;

    public InboxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        return rootView;
    }

    public static InboxFragment newInstance() {
        InboxFragment fragment = new InboxFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);

        // Retrieve messages where current user is a recipient
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mMessages = messages;

                    if (getListView().getAdapter() == null) {
                        MessageAdapter adapter = new MessageAdapter(
                                getListView().getContext(),
                                mMessages
                        );
                        setListAdapter(adapter);

                    } else {
                        // Avoid reinstantiating adapter to maintain scroll position
                        MessageAdapter adapter = (MessageAdapter) getListView().getAdapter();
                        adapter.refill(mMessages);
                    }
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        if ( messageType.equals(ParseConstants.TYPE_TEXT_MESSAGE) ) {
            // view text message
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.text_message_title)
                    .setMessage(message.getString(ParseConstants.KEY_TEXT))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        } else {
            // view file-attached message
            ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
            Uri fileUri = Uri.parse(file.getUrl());

            if ( messageType.equals(ParseConstants.TYPE_IMAGE) ) {
                // view image
                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.setData(fileUri);
                startActivity(intent);
            } else if ( messageType.equals(ParseConstants.TYPE_VIDEO) ) {
                // view video
                Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
                intent.setDataAndType(fileUri, "video/*");
                startActivity(intent);
            }
        }

        // Delete message!
        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
        if ( ids.size() == 1 ) {
            // last recipient - delete whole thing
            message.deleteInBackground();
        } else {
            // more recipients received this message - just remove this recipient
            //ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();
        }
    }
}
