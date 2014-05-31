package com.kennyken.ribbit.app;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ListActivity {
    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected MenuItem mSendMenuItem;
    protected Uri mMediaUri;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // Populate friends list
        setProgressBarIndeterminateVisibility(true);

        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser friend : friends) {
                        usernames[i] = friend.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> usernamesAdapter = new ArrayAdapter<String>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_checked,
                            usernames
                    );

                    setListAdapter(usernamesAdapter);

                } else {
                    Toast.makeText(RecipientsActivity.this,
                            R.string.users_query_error,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipients, menu);
        mSendMenuItem = menu.getItem(0);
        mSendMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_send:
                ParseObject message = createMessage();
                if ( message == null ) {
                    // error building message
                    Toast.makeText(this, R.string.file_build_error, Toast.LENGTH_LONG).show();
                } else {
                    send(message);
                    finish();  // exit this activity, return to MainActivity
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if ( l.getCheckedItemCount() > 0 ) {
            // only show Send button when recipients are selected
            mSendMenuItem.setVisible(true);
        } else
            mSendMenuItem.setVisible(false);
    }

    /*
     * Build message to be sent as ParseObject
     */
    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        // Process and attach media file
        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if ( fileBytes == null )
            return null;

        if ( mFileType.equals(ParseConstants.TYPE_IMAGE) )
            fileBytes = FileHelper.reduceImageForUpload(fileBytes);

        String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
        ParseFile file = new ParseFile(fileName, fileBytes);
        message.put(ParseConstants.KEY_FILE, file);

        return message;
    }

    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if ( e == null ) {
                    Toast.makeText(RecipientsActivity.this, R.string.message_sent,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RecipientsActivity.this, R.string.message_not_sent,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     * Get object ids for each checked recipient
     */
    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        for ( int i=0; i < getListView().getCount(); i++ ) {
            if ( getListView().isItemChecked(i) )
                recipientIds.add(mFriends.get(i).getObjectId());
        }

        return recipientIds;
    }
}
