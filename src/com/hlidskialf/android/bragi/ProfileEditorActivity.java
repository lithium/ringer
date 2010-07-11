
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
          mProfile = mDbHelper.getProfile(profile_id);

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
    }

    public void finish() {
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
      Log.v("BragiPreferenceChanged", preference.getKey() +" = " + newValue.getClass().toString());

      Uri uri = Uri.parse(newValue.toString());
      SlotBucket bucket = _set_slot_ringtone(preference, uri, -1);

      if (! mDbHelper.updateProfileSlot(mProfile.id, bucket.slot_id, uri) ) {
        Log.v("BragiProfileEditor", "failed to update profile");
      }
      return false;
    }


    private SlotBucket _set_slot_ringtone(Preference pref, Uri uri, long slot_id) 
    {
      SlotBucket bucket = mSlotHash.get(pref);
      if (bucket == null) {
        bucket = new SlotBucket();
        bucket.slot_id = slot_id;
      }
      
      bucket.ring = null;
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
      if (resultCode != RESULT_OK)
        return;

      if (requestCode == REQUEST_VOLUME_SCREEN) {
        ContentValues values = data.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);
        mProfile.updateValues(values);
        mDbHelper.updateProfile(mProfile.id, mProfile.contentValues());
      }
    }

}
