package com.hlidskialf.android.bragi;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
}
