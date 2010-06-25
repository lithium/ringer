package com.hlidskialf.android.tonepicker;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.Ringtone;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;


/*

  - Existing
  - Silence
  + Ringtones
  + Notifications
   - foo
   - bar
  + Alarms
  + artist / album
  + artist / album
  ....
  + artist / album
  + artist / album
  + Other Audio Apps
   - ACTION_GET_CONTENT audio/* CATEGORY_OPENABLE
  + Other Ringtone Apps
   - ACTION_RINGTONE_PICKER

                0: EXISTING_URI
                1: RINGTONE_CURSOR
                2: NOTIFY_CURSOR
                3: ALARM_CURSOR
                4: AUDIO_INTENT_CURSOR
5 .. 5+NUM_ALBUMS: ALBUM_CURSOR
   5+NUM_ALBUMS+1: PICKER_CURSOR
               -1: SILENCE
*/





public class TonePickerActivity extends ExpandableListActivity {





    private TonePickerAdapter mAdapter; 

    private Intent mInitialIntent;
    private Uri mExistingUri;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri existing_uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        //int ringtone_type = mInitialIntent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);

        mAdapter = new TonePickerAdapter((Context)this, existing_uri, getComponentName());
        setListAdapter((ExpandableListAdapter)mAdapter);


        getExpandableListView().expandGroup(0);
        
    }
}
