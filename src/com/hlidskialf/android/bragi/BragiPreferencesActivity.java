package com.hlidskialf.android.bragi;

import android.os.Bundle;
import android.content.ComponentName;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlidskialf.android.app.AboutDialog;

public class BragiPreferencesActivity extends PreferenceActivity
                implements View.OnClickListener
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    addPreferencesFromResource(R.xml.bragi_preferences);

    TextView tv;
    tv = (TextView)findViewById(R.id.actionbar_subtitle);
    tv.setText(getString(R.string.activity_preferences));

    ImageView v;
    v = (ImageView)findViewById(R.id.actionbar_logo);
    v.setImageResource(R.drawable.ic_actiontitle_ringer);

    v = (ImageView)findViewById(R.id.actionbar_button1);
    v.setOnClickListener(this);
    v.setVisibility(View.VISIBLE);
    v.setImageResource(R.drawable.ic_action_cancel);

  }

  /* View.OnClickListener */
  public void onClick(View v) {
    final long id = v.getId();
    if (id == R.id.actionbar_button1) {
      finish();
    }
  }

  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
  {
    String key = preference.getKey();
    if (key != null && key.equals("screen_about")) {
      AboutDialog dialog = new AboutDialog(this, new ComponentName(this, BragiActivity.class), R.string.about_dialog_title, R.string.about_dialog_message);
      return true;
    }
    if (key != null && key.equals("screen_show_tutorial")) {
      Intent intent = new Intent(this, BragiTutorialActivity.class);
      startActivityForResult(intent, -1);
      return true;
    }

    return false;
  }
}
