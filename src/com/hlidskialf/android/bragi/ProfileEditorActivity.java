
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ProfileEditorActivity extends PreferenceActivity
                implements View.OnClickListener
{
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
        
    }

    public void onClick(View v) {
        final long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button1) {
          finish();
        }
    }
}
