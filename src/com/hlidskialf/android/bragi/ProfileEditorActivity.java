
package com.hlidskialf.android.bragi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
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



public class ProfileEditorActivity extends PreferenceActivity
                implements View.OnClickListener,
                Preference.OnPreferenceChangeListener
{
    private BragiDatabase.ProfileModel mProfile;
    private String[] mNames_onoffsilent;
    private BragiDatabase mDbHelper;
    private String[] mNames_silentmode;


    public static final int RESULT_VOLUME_SCREEN=1;
    public static final int RESULT_RINGTONES_SCREEN=2;
    public static final int RESULT_ICON_SCREEN=3;


    public boolean mUseCircleCrop = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.profile_editor);

        SharedPreferences prefs = getSharedPreferences(Bragi.PREFERENCES, 0);
        mUseCircleCrop = prefs.getBoolean(Bragi.PREF_CIRCLE_CROP, Bragi.PREF_CIRCLE_CROP_DEFAULT);


        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_actiontitle_ringer);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_action_cancel);
        v.setVisibility(View.VISIBLE);

        Resources res = getResources();
        mNames_onoffsilent = res.getStringArray(R.array.onoffsilent_names);
        mNames_silentmode = res.getStringArray(R.array.silent_mode_names);


        mDbHelper = new BragiDatabase(this);

        Intent caller = getIntent();
        long profile_id = caller.getLongExtra(Bragi.EXTRA_PROFILE_ID, -1);
        if (profile_id != -1)
          mProfile = mDbHelper.getProfile(profile_id, true);


        Preference pref;
        PreferenceScreen screen = getPreferenceScreen();

        pref = screen.findPreference("name");
        pref.setSummary(mProfile.name);
        pref.setDefaultValue(mProfile.name);
        pref.setOnPreferenceChangeListener(this);

        pref = screen.findPreference("silent_mode");
        pref.setOnPreferenceChangeListener(this);
        pref.setDefaultValue(mProfile.silent_mode);
        _set_pref_summary(pref);

        pref = screen.findPreference("vibrate_ring");
        pref.setOnPreferenceChangeListener(this);
        _set_pref_summary(pref);

        pref = screen.findPreference("vibrate_notify");
        pref.setOnPreferenceChangeListener(this);
        _set_pref_summary(pref);

        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_profileeditor)+" - "+mProfile.name);

        
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
      String key = preference.getKey();
      if (key == null) return false;

      if (key.equals("name")) {
          mProfile.name = newValue.toString();
          mDbHelper.updateProfile(mProfile.id, mProfile.contentValues());
          preference.setSummary(mProfile.name);
      }
      else
      if (key.equals("silent_mode")) {
        mProfile.silent_mode = Integer.valueOf( newValue.toString() );
        _set_pref_summary(preference);
      }
      else
      if (key.equals("vibrate_ring")) {
        mProfile.vibrate_ring = Integer.valueOf( newValue.toString() );
        _set_pref_summary(preference);
      }
      else
      if (key.equals("vibrate_notify")) {
        mProfile.vibrate_notify = Integer.valueOf( newValue.toString() );
        _set_pref_summary(preference);
      }
      return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        final String pref_key = preference.getKey();
        if (pref_key == null) return true;

        if (pref_key.equals("volume_screen")) {
            Intent intent = new Intent(this, ProfileEditorVolumeActivity.class);
            intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfile.contentValues());
            startActivityForResult(intent, RESULT_VOLUME_SCREEN);
        }
        else
        if (pref_key.equals("ringtones_screen")) {
            Intent intent = new Intent(this, ProfileEditorRingtonesActivity.class);
            intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfile.contentValues());
            startActivityForResult(intent, RESULT_RINGTONES_SCREEN);
        }
        else
        if (pref_key.equals("icon_screen")) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", mUseCircleCrop ? "circle" : "true");
            intent.putExtra("return-data", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            startActivityForResult(intent, RESULT_ICON_SCREEN);

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
      if (data != null) {

        if (requestCode == RESULT_VOLUME_SCREEN || requestCode == RESULT_RINGTONES_SCREEN) {
          ContentValues values = data.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);
          mProfile.updateValues(values);
          mDbHelper.updateProfile(mProfile.id, mProfile.contentValues());
        }
        else
        if (requestCode == RESULT_ICON_SCREEN) {
          Bitmap crop_data = (Bitmap)data.getParcelableExtra("data");
          mProfile.icon = Bragi.scaleBitmap(crop_data, 72, 72);
        }

      }

      super.onActivityResult(requestCode, resultCode, data);
    }

    private void _set_pref_summary(Preference pref)
    {
        String key = pref.getKey();
        String value = "";
        if (key.equals("silent_mode")) {
          value = mNames_silentmode[mProfile.silent_mode];
        }
        if (key.equals("vibrate_ring")) {
          value = mNames_onoffsilent[mProfile.vibrate_ring];
        }
        if (key.equals("vibrate_notify")) {
          value = mNames_onoffsilent[mProfile.vibrate_notify];
        }
        pref.setSummary(value);
    }

}
