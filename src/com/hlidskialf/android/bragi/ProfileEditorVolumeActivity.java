
package com.hlidskialf.android.bragi;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.content.res.Resources;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import java.util.HashMap;

import com.hlidskialf.android.preference.SeekBarPreference;


public class ProfileEditorVolumeActivity extends Activity
                implements View.OnClickListener,
                    SeekBar.OnSeekBarChangeListener
{
    private ContentValues mProfileValues;
    private AudioManager mAudioManager;

    private class ViewHolder {
      String cv_key;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        View v = findViewById(R.id.button_bar);
        v.setVisibility(View.GONE);

        Intent intent = getIntent();
        mProfileValues = (ContentValues)intent.getParcelableExtra(Bragi.EXTRA_PROFILE_VALUES);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);


        ImageView iv;
        iv = (ImageView)findViewById(R.id.actionbar_logo);
        iv.setOnClickListener(this);
        iv.setImageResource(R.drawable.ic_actiontitle_ringer);

        iv = (ImageView)findViewById(R.id.actionbar_button1);
        iv.setOnClickListener(this);
        iv.setImageResource(R.drawable.ic_action_cancel);
        iv.setVisibility(View.VISIBLE);

        _set_max_vol("volume_ringer", R.id.volume_ringer, AudioManager.STREAM_RING);
        _set_max_vol("volume_music", R.id.volume_music, AudioManager.STREAM_MUSIC);
        _set_max_vol("volume_call", R.id.volume_call, AudioManager.STREAM_VOICE_CALL);
        _set_max_vol("volume_system", R.id.volume_system, AudioManager.STREAM_SYSTEM);
        _set_max_vol("volume_alarm", R.id.volume_alarm, AudioManager.STREAM_ALARM);

        if (Integer.valueOf(Build.VERSION.SDK) >= 3) {
          v = findViewById(R.id.row_notify);
          v.setVisibility(View.VISIBLE);
          _set_max_vol("volume_notify", R.id.volume_notify, AudioManager.STREAM_NOTIFICATION);
        }

        String profile_name = (String)mProfileValues.get("name");
        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_profileeditorvolume)+(profile_name == null ? "" : " - "+profile_name));
    }
    private void _set_max_vol(String cv_key, int view_id, int audio_stream)
    {
      int max = mAudioManager.getStreamMaxVolume(audio_stream);

      SeekBar seek = (SeekBar)findViewById(view_id);
      seek.setMax(max);
      seek.setProgress(mProfileValues.getAsInteger(cv_key));
      seek.setOnSeekBarChangeListener(this);

      ViewHolder holder = new ViewHolder();
      holder.cv_key = cv_key;
      seek.setTag(holder);
    }

    /*View.OnClickListener*/
    public void onClick(View v) 
    {
        final long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button1) {
          finish();
        }
    }

    /*SeekBar.OnSeekBarChangeListener*/
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
      ViewHolder holder = (ViewHolder)seekBar.getTag();

      mProfileValues.put(holder.cv_key, progress);
    }
    public void onStartTrackingTouch(SeekBar seekBar) {}
    public void onStopTrackingTouch(SeekBar seekBar) {}



    @Override
    public void finish() 
    {
      Intent intent = new Intent();
      intent.putExtra(Bragi.EXTRA_PROFILE_VALUES, mProfileValues);
      setResult(RESULT_OK, intent);
      super.finish();
    }

}
