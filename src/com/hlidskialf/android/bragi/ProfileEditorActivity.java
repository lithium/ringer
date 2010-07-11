
package com.hlidskialf.android.bragi;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.content.Intent;
import android.net.Uri;
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
    private BragiDatabase.ProfileModel mProfile;
    private BragiDatabase mDbHelper;

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

        mDbHelper = new BragiDatabase(this);

        Intent caller = getIntent();
        long profile_id = caller.getLongExtra(Bragi.EXTRA_PROFILE_ID, -1);
        if (profile_id != -1)
          mProfile = mDbHelper.getProfile(profile_id);

        PreferenceScreen screen = getPreferenceScreen();
        Cursor slot_cursor = mDbHelper.getAllSlots();
        int l = slot_cursor.getCount();
        final int idx_name = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);
        final int idx_id = slot_cursor.getColumnIndex(BragiDatabase.SlotColumns._ID);
        for(; slot_cursor.moveToNext(); ) {
            final long id = slot_cursor.getLong(idx_id);
            final String name = slot_cursor.getString(idx_name);
            RingtonePreference pref = new RingtonePreference(this);
            pref.setTitle(name);
            
            //pref.setSummary("ringtone for the '"+name+"' slot");
            final Uri uri = mProfile.slots.get(id);
            if (uri != null)
              pref.setSummary(uri.toString());
            else
              pref.setSummary("***unset***");

            screen.addPreference(pref);
        }
        slot_cursor.close();
        mDbHelper.close();
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
        if (pref_key == null) return true;

        if (pref_key.equals("volume_screen")) {
            Intent intent = new Intent(this, ProfileEditorVolumeActivity.class);
            intent.putExtra(Bragi.EXTRA_PROFILE, mProfile);
            startActivityForResult(intent, RESULT_VOLUME_SCREEN);
        }
        return true;
    }
}
