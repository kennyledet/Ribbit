package com.kennyken.ribbit.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    //public String appName = getString(R.string.app_name);
    public String TAG = MainActivity.class.getSimpleName();
    public ParseUser currentUser;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; // 10MB

    protected Uri mMediaUri;


    protected DialogInterface.OnClickListener mCameraDialogListener =
            new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch ( which ) {
                case 0:  // take picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // get path to save images
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if (mMediaUri == null) {
                        Toast.makeText(MainActivity.this,
                                R.string.external_storage_error,
                                Toast.LENGTH_LONG).show();
                    } else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1:  // take video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if (mMediaUri == null) {
                        Toast.makeText(MainActivity.this,
                                R.string.external_storage_error,
                                Toast.LENGTH_LONG).show();
                    } else {
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        // limit video captures to 10 seconds / LQ
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2:  // choose picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");

                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    break;
                case 3:  // choose video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");

                    Toast.makeText(MainActivity.this, R.string.video_limit, Toast.LENGTH_LONG).show();

                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                    break;
            }
        }
    };

    private Uri getOutputMediaFileUri(int mediaType) {
        if ( isExternalStorageAvailable() ) {
            // get external storage directory
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES),
                    getString(R.string.app_name)
            );

            // create subdirectory
            if ( ! mediaStorageDir.exists() ) {
                if ( ! mediaStorageDir.mkdirs() ) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            // create file name with timestamp
            File mediaFile;

            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getPath() + File.separator;
            switch (mediaType) {
                case MEDIA_TYPE_IMAGE:
                    mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                    break;
                case MEDIA_TYPE_VIDEO:
                    mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                    break;
                default:
                    return null;
            }

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpened(getIntent());

        // Check if a user is cached
        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {  // Immediately switch to login activity
            sendToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }

    /* Handle result of camera actions here */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "Request code: "+requestCode);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if ( data == null ) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                    return;
                } else {
                    mMediaUri = data.getData();
                    Log.i(TAG, "Media URI: "+mMediaUri);
                }

                if ( requestCode == PICK_VIDEO_REQUEST ) {
                    // assert file less than 10MB
                    int fileSize = 0;
                    InputStream input = null;
                    try {
                        input = getContentResolver().openInputStream(mMediaUri);
                        fileSize = input.available();
                    } catch (java.io.FileNotFoundException e) {
                        Toast.makeText(this, R.string.file_error, Toast.LENGTH_LONG).show();
                        return;
                    } catch (IOException e) {
                        Toast.makeText(this, R.string.file_error, Toast.LENGTH_LONG).show();
                        return;
                    } finally {
                        try { input.close(); } catch (IOException e) { }
                    }

                    if ( fileSize > FILE_SIZE_LIMIT ) {
                        Toast.makeText(this, R.string.file_too_large, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            } else {  // add newly created media to Gallery by broadcasting Intent
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            // Send user to recipients selection to send set media uri
            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String fileType;
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
                fileType = ParseConstants.TYPE_IMAGE;
            } else {
                fileType = ParseConstants.TYPE_VIDEO;
            }
            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);

            startActivity(recipientsIntent);

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        // don't allow going back to main activity from login in back stack
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Manually set overflow menu icons
        menu.findItem(R.id.action_camera).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_new_message).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.

        int id = item.getItemId();
        switch( id ) {
            case R.id.action_logout:
                ParseUser.logOut();
                sendToLogin();
                return true;
            case R.id.action_edit_friends:
                Intent editFriendsIntent = new Intent(this, EditFriendsActivity.class);
                startActivity(editFriendsIntent);
                return true;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mCameraDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
            case R.id.action_new_message:
                final EditText messageField = new EditText(this);

                new AlertDialog.Builder(this)
                    .setTitle("New Message")
                    .setView(messageField)
                    .setPositiveButton("Choose Recipients", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String message = messageField.getText().toString();
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
}