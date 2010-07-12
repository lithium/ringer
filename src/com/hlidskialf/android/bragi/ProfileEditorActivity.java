
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;

import java.util.HashMap;


public class ProfileEditorActivity extends PreferenceActivity
                implements View.OnClickListener,
                Preference.OnPreferenceChangeListener
{
    private BragiDatabase.ProfileModel mProfile;
    private BragiDatabase mDbHelper;

    private static class SlotBucket
    {
      long slot_id;
      Ringtone ring;
    }

    private HashMap<Preference,SlotBucket> mSlotHash;

    public static final int REQUEST_VOLUME_SCREEN=1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.profile_editor);


        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.logo);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        v.setVisibility(View.VISIBLE);

        mDbHelper = new BragiDatabase(this);

        Intent caller = getIntent();
        long profile_id = caller.getLongExtra(Bragi.EXTRA_PROFILE_ID, -1);
        if (profile_id != -1)
          mProfile = mDbHelper.getProfile(profile_id, true);

        mSlotHash = new HashMap<Preference,SlotBucket>();
        PreferenceScreen screen = getPreferenceScreen();
        Cursor slot_cursor = mDbHelper.getAllSlots();
        int l = slot_cursor.getCount();
        final int idx_id = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns._ID);
        final int idx_name = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);
        final int idx_slug = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns.SLUG);
        for(; slot_cursor.moveToNext(); ) {
            final long id = slot_cursor.getLong(idx_id);
            final String name = slot_cursor.getString(idx_name);
            final String slug = slot_cursor.getString(idx_slug);
            RingtonePreference pref = new RingtonePreference(this);
            pref.setKey(slug);
            pref.setTitle(name);
            pref.setOnPreferenceChangeListener(this);
           
            final Uri uri = mProfile.slots.get(id);
            _set_slot_ringtone(pref, uri, id);

            screen.addPreference(pref);
        }
        slot_cursor.close();

        Preference pref;
        pref = screen.findPreference("default_ring");
        pref.setOnPreferenceChangeListener(this);
        _set_default_summary(pref, mProfile.default_ring);
        pref = screen.findPreference("default_notify");
        pref.setOnPreferenceChangeListener(this);
        _set_default_summary(pref, mProfile.default_notify);
        pref = screen.findPreference("default_alarm");
        pref.setOnPreferenceChangeListener(this);
        _set_default_summary(pref, mProfile.default_alarm);

        
    }

    public void finish() {
      mDbHelper.updateProfile(mProfile.id, mProfile.contentValues());
      mDbHelper.close();
      super.finish();
    }

    /*View.OnClickListener*/
    public void onClick(View v)  
    {
        final long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button1) {
          finish();
        }
    }

    /*Preference.OnPreferenceChangeListener*/
    public boolean onPreferenceChange(Preference preference, Object newValue) 
    {
      Log.v("BragiPreferenceChanged", preference.getKey() +" = " + newValue.toString());

      String key = preference.getKey();
      String uri_str = newValue.toString();
      Uri uri = Uri.parse(uri_str);

      if (key.equals("default_ring") || key.equals("default_notify") || key.equals("default_alarm")) {
        if (key.equals("default_ring")) mProfile.default_ring = uri_str;
        else if (key.equals("default_notify")) mProfile.default_notify = uri_str;
        else if (key.equals("default_alarm")) mProfile.default_alarm = uri_str;
        _set_default_summary(preference, uri_str);
      }
      else { /* its a slot ringtone */
        SlotBucket bucket = _set_slot_ringtone(preference, uri, -1);
        if (! mDbHelper.updateProfileSlot(mProfile.id, bucket.slot_id, uri) ) {
          Log.v("BragiProfileEditor", "failed to update profile");
        }
      }

      return false;
    }

    private void _set_default_summary(Preference pref, String uri_str)
    {
      if (uri_str != null) {
        Uri uri = Uri.parse( uri_str );
        Ringtone ring = RingtoneManager.getRingtone(this, uri);
        pref.setSummary(ring.getTitle(this));
      } else {
        pref.setSummary("***unset***");
      }

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
        pref.setSummary("***unset***");
      
      mSlotHash.put(pref, bucket);
      return bucket;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        final String pref_key = preference.getKey();
        if (pref_key == null) return true;

        if (pref_key.equals("volume_screen")) {
            Intent intent = new Intent(this, ProfileEditorVolumeActivity.class);
            intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfile.contentValues());
            startActivityForResult(intent, REQUEST_VOLUME_SCREEN);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
      if (requestCode == REQUEST_VOLUME_SCREEN) {
        ContentValues values = data.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);
        mProfile.updateValues(values);
        mDbHelper.updateProfile(mProfile.id, mProfile.contentValues());
      }

      super.onActivityResult(requestCode, resultCode, data);
    }

}
