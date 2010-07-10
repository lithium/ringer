
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;


public class ProfileEditorActivity extends PreferenceActivity
                implements View.OnClickListener
{
    public static final int RESULT_VOLUME_SCREEN=1;
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

        BragiDatabase mDbHelper = new BragiDatabase(this);
        Cursor mSlotCursor = mDbHelper.getAllSlots();

        PreferenceScreen screen = getPreferenceScreen();

        int l = mSlotCursor.getCount();
        final int idx = mSlotCursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);
        for(; mSlotCursor.moveToNext(); ) {
            final String name = mSlotCursor.getString(idx);
            RingtonePreference pref = new RingtonePreference(this);
            pref.setTitle(name);
            pref.setSummary("ringtone for the '"+name+"' slot");
            screen.addPreference(pref);
        }
    }

    public void onClick(View v) 
    {
        final long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button1) {
          finish();
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        final String pref_key = preference.getKey();
        if (pref_key.equals("volume_screen")) {
            Intent intent = new Intent(this, ProfileEditorVolumeActivity.class);
            startActivityForResult(intent, RESULT_VOLUME_SCREEN);
        }
        return true;
    }
}
