package com.hlidskialf.android.bragi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;

import com.hlidskialf.android.tonepicker.TonePicker;

public class ProfileEditorRingtonesActivity extends PreferenceActivity
                implements View.OnClickListener,
                    Preference.OnPreferenceClickListener
{
    private static final int RESULT_TONEPICKER=1;

    private BragiDatabase.ProfileModel mProfile;
    private ContentValues mProfileValues;
    private BragiDatabase mDbHelper;
    private String mString_unset;
    private Preference mLastPreference;

    private static class SlotBucket
    {
      long slot_id;
      Ringtone ring;
    }

    private HashMap<Preference,SlotBucket> mSlotHash;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.profile_editor_ringtones);

        mDbHelper = new BragiDatabase(this);
        mString_unset = getString(R.string.unset);

        Intent intent = getIntent();
        mProfileValues = (ContentValues)intent.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);

        long profile_id = mProfileValues.getAsLong("_id");
        if (profile_id != -1)
          mProfile = mDbHelper.getProfile(profile_id, true);

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_actiontitle_ringer);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_action_cancel);
        v.setVisibility(View.VISIBLE);

        Preference pref;
        PreferenceScreen screen = getPreferenceScreen();

        pref = screen.findPreference("default_ring");
        pref.setOnPreferenceClickListener(this);
        _set_default_summary(pref, mProfile.default_ring);

        pref = screen.findPreference("default_notify");
        pref.setOnPreferenceClickListener(this);
        _set_default_summary(pref, mProfile.default_notify);

        pref = screen.findPreference("default_alarm");
        pref.setOnPreferenceClickListener(this);
        _set_default_summary(pref, mProfile.default_alarm);


        mSlotHash = new HashMap<Preference,SlotBucket>();
        Cursor slot_cursor = mDbHelper.getAllSlots();
        int l = slot_cursor.getCount();
        final int idx_id = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns._ID);
        final int idx_name = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);
        final int idx_slug = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns.SLUG);
        for(; slot_cursor.moveToNext(); ) {
            final long id = slot_cursor.getLong(idx_id);
            final String name = slot_cursor.getString(idx_name);
            final String slug = slot_cursor.getString(idx_slug);
            pref = new Preference(this);
            pref.setKey(slug);
            pref.setTitle(name);
            pref.setOnPreferenceClickListener(this);
           
            final Uri uri = mProfile.slots.get(id);
            _set_slot_ringtone(pref, uri, id);

            screen.addPreference(pref);
        }
        slot_cursor.close();



        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_profileeditorringtones)+ " - "+mProfile.name);

    }

    /*View.OnClickListener*/
    public void onClick(View v) 
    {
        final long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button1) {
          finish();
        }
    }

    /*Preference.OnPreferenceClickListener*/
    public boolean onPreferenceClick(Preference preference)
    {
      mLastPreference = preference;
      Intent intent = new Intent(this, TonePicker.class);
      intent.putExtra(Bragi.EXTRA_SHOW_BRAGI_SLOTS, false);
      startActivityForResult(intent, RESULT_TONEPICKER);
      return true;
    }

    @Override
    public void finish() 
    {
      mDbHelper.close();

      Intent intent = new Intent();
      intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfile.contentValues());
      setResult(RESULT_OK, intent);

      super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
      if (data != null) {
        if (requestCode == RESULT_TONEPICKER) {
          Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
          String key = mLastPreference.getKey();

          if (key != null && key.startsWith("default_")) {
            String uri_str = uri != null ? uri.toString() : "";
            if (key.equals("default_ring")) mProfile.default_ring = uri_str;
            else if (key.equals("default_notify")) mProfile.default_notify = uri_str;
            else if (key.equals("default_alarm")) mProfile.default_alarm = uri_str;
            _set_default_summary(mLastPreference, uri_str);
          } else {
            _set_slot_ringtone(mLastPreference, uri, -1);
          }
        }
      }
      super.onActivityResult(requestCode, resultCode, data);
    }


    private SlotBucket _set_slot_ringtone(Preference pref, Uri uri, long slot_id) 
    {
      SlotBucket bucket = mSlotHash.get(pref);
      if (bucket == null) {
        bucket = new SlotBucket();
        bucket.slot_id = slot_id;
      }
     
      if (uri != null) {
        bucket.ring = RingtoneManager.getRingtone(this, uri);
        pref.setSummary(bucket.ring.getTitle(this));
      }
      else
        pref.setSummary(mString_unset);
      
      mSlotHash.put(pref, bucket);

      if (slot_id == -1) {
        if (! mDbHelper.updateProfileSlot(mProfile.id, bucket.slot_id, uri) ) {
          Log.v("BragiProfileEditor", "failed to update profile");
        }
      }

      return bucket;
    }

    private void _set_default_summary(Preference pref, String uri_str)
    {
      if (uri_str != null) {
        Uri uri = Uri.parse( uri_str );
        Ringtone ring = RingtoneManager.getRingtone(this, uri);
        pref.setSummary(ring.getTitle(this));
      } else {
        pref.setSummary(mString_unset);
      }

    }
}
