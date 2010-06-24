package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.app.Activity;
import android.os.Bundle;

import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import android.media.RingtoneManager;
import android.text.TextUtils;

import android.content.Context;
import android.util.Log;


public class BragiActivity extends ListActivity
{
    RingtoneManager mRingtoneManager;
    Cursor mRingtoneCursor;
    SimpleCursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

  /*
        mRingtoneManager = new RingtoneManager((Activity)this);
        mRingtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION);
        mRingtoneCursor = mRingtoneManager.getCursor();

        Log.v("BRAGI", "num ringtones: "+mRingtoneCursor.getCount());
        Log.v("BRAGI", "ringtone columns: "+TextUtils.join(",", mRingtoneCursor.getColumnNames()));

        mAdapter = new SimpleCursorAdapter((Context)this, R.layout.list_item, mRingtoneCursor, new String[] {"title"}, new int[] {android.R.id.text1});
        setListAdapter(mAdapter);
*/
    }
}
