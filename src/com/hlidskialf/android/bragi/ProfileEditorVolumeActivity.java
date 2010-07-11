
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.database.Cursor;
import android.content.res.Resources;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;


public class ProfileEditorVolumeActivity extends PreferenceActivity
                implements View.OnClickListener,
                    Preference.OnPreferenceChangeListener
{
    ContentValues mProfileValues;
    String[] mNames_silentmode;
    String[] mNames_onoffsilent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.profile_editor_volume);

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.logo);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        v.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        mProfileValues = (ContentValues)intent.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);

        Resources res = getResources();
        mNames_silentmode = res.getStringArray(R.array.silent_mode_names);
        mNames_onoffsilent = res.getStringArray(R.array.onoffsilent_names);

        PreferenceScreen screen = getPreferenceScreen();
        int i; 
        int l = screen.getPreferenceCount();
        for (i=0; i < l; i++) {
          Preference pref = screen.getPreference(i);
          pref.setOnPreferenceChangeListener(this);
          _set_pref_summary(pref);
        }
    }

    private void _set_pref_summary(Preference pref)
    {
        String key = pref.getKey();
        int idx = mProfileValues.getAsInteger(key);
        String value;
        if (key.equals("silent_mode")) {
          value = mNames_silentmode[idx];
        }
        else
        if (key.equals("vibrate_ring") || key.equals("vibrate_notify")) {
          value = mNames_onoffsilent[idx];
        }
        else {
          value = String.valueOf(idx);
        }
        //mProfileValues.getAsString(pref.getKey());
        pref.setSummary(value);
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
      int value = Integer.valueOf( newValue.toString() );
      if (key != null) {
        mProfileValues.put(key, value);
      }

      _set_pref_summary(preference);

      return false;
    }

    @Override
    public void finish() 
    {
      Intent intent = new Intent();
      intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfileValues);
      setResult(RESULT_OK, intent);
      Log.v("BragiVOlumeActivity", "finish");
      super.finish();
    }
}
