
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.database.Cursor;
import android.content.res.Resources;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import java.util.HashMap;

import com.hlidskialf.android.preference.SeekBarPreference;


public class ProfileEditorVolumeActivity extends PreferenceActivity
                implements View.OnClickListener,
                    Preference.OnPreferenceChangeListener
{
    private ContentValues mProfileValues;
    private String[] mNames_silentmode;
    private String[] mNames_onoffsilent;
    private HashMap<String,String> mMaxHash;
    private AudioManager mAudioManager;
    private PreferenceScreen mScreen;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        addPreferencesFromResource(R.xml.profile_editor_volume);

        Intent intent = getIntent();
        mProfileValues = (ContentValues)intent.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);
        mScreen = getPreferenceScreen();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        mMaxHash = new HashMap<String,String>(6);


        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_actiontitle_ringer);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.ic_action_cancel);
        v.setVisibility(View.VISIBLE);


        Resources res = getResources();
        mNames_silentmode = res.getStringArray(R.array.silent_mode_names);
        mNames_onoffsilent = res.getStringArray(R.array.onoffsilent_names);

        _set_max_vol("volume_ringer", AudioManager.STREAM_RING);
        _set_max_vol("volume_notify", AudioManager.STREAM_NOTIFICATION);
        _set_max_vol("volume_music", AudioManager.STREAM_MUSIC);
        _set_max_vol("volume_call", AudioManager.STREAM_VOICE_CALL);
        _set_max_vol("volume_system", AudioManager.STREAM_SYSTEM);
        _set_max_vol("volume_alarm", AudioManager.STREAM_ALARM);

        int i; 
        int l = mScreen.getPreferenceCount();
        for (i=0; i < l; i++) {
          Preference pref = mScreen.getPreference(i);
          pref.setOnPreferenceChangeListener(this);
          _set_pref_summary(pref);
        }

        String profile_name = (String)mProfileValues.get("name");
        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_profileeditorvolume)+(profile_name == null ? "" : " - "+profile_name));

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



    private void _set_max_vol(String pref_key, int audio_stream)
    {
      int max = mAudioManager.getStreamMaxVolume(audio_stream);
      String suffix = " / "+String.valueOf(max);

      SeekBarPreference pref = (SeekBarPreference) mScreen.findPreference(pref_key);
      pref.setProgress(mProfileValues.getAsInteger(pref_key));
      pref.setMax(max);
      pref.setSuffix(suffix);

      mMaxHash.put(pref_key, suffix);
    }
    private String _get_max_vol(String pref_key) 
    {
      if (mMaxHash == null) return "";
      String ret = mMaxHash.get(pref_key);
      if (ret == null) return "";
      return ret;
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
          value = String.valueOf(idx) + _get_max_vol(key);
        }

        pref.setSummary(value);
    }
}
