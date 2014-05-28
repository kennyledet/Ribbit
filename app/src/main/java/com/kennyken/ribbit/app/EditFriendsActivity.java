package com.kennyken.ribbit.app;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends ListActivity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_friends);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(
                ParseConstants.KEY_FRIENDS_RELATION);

        setProgressBarIndeterminateVisibility(true);

        // Pull down list of all usernames from Parse backend
        ParseQuery<ParseUser> usersQuery = ParseUser.getQuery();
        usersQuery.orderByAscending(ParseConstants.KEY_USERNAME);

        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if ( e == null ) {

                    mUsers = users;

                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        usernames[i] = user.getUsername(); i++;
                    }

                    ArrayAdapter<String> usernamesAdapter = new ArrayAdapter<String>(
                            EditFriendsActivity.this, android.R.layout.simple_list_item_checked,
                            usernames);

                    setListAdapter(usernamesAdapter);

                    addFriendCheckmarks();

                } else {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(EditFriendsActivity.this, R.string.users_query_error, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        SaveCallback saveCallback = new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        };

        if ( getListView().isItemChecked(position) ) {
            // Add clicked user to current user's friends relation
            mFriendsRelation.add(mUsers.get(position));
            mCurrentUser.saveInBackground(saveCallback);
        } else {
            // Remove friend
            mFriendsRelation.remove(mUsers.get(position));
            mCurrentUser.saveInBackground(saveCallback);
        }
    }

    private void addFriendCheckmarks() {
        // Pre-check each username that is already a friend in the list
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if ( e == null ) {
                    for ( int i=0; i < mUsers.size(); i++ ) {
                        ParseUser user = mUsers.get(i);
                        for (ParseUser friend : friends ) {
                            if ( user.getObjectId().equals(friend.getObjectId()) ) {
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }
}
