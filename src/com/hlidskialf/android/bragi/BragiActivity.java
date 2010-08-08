package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.database.Cursor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;

import com.hlidskialf.android.app.OneLineInputDialog;

public class BragiActivity extends ListActivity
                implements View.OnClickListener
{
    private BragiDatabase mDbHelper;
    private Cursor mProfileCursor;
    private long mActiveProfileId=-1;

    private static final int MENU_ACTIVATE_ID=1;
    private static final int MENU_EDIT_ID=2;
    private static final int MENU_DELETE_ID=3;

    private static final int RESULT_EDIT_PROFILE=1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_main));

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setImageResource(R.drawable.logo);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setVisibility(View.VISIBLE);
        v.setImageResource(android.R.drawable.ic_menu_add);

        v = (ImageView)findViewById(R.id.actionbar_button2);
        v.setOnClickListener(this);
        v.setVisibility(View.VISIBLE);
        v.setImageResource(android.R.drawable.ic_menu_preferences);

        ListView mListView = getListView();
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        registerForContextMenu(mListView);


        mDbHelper = new BragiDatabase(this);
        mProfileCursor = mDbHelper.getAllProfiles();
        setListAdapter( new SimpleCursorAdapter(this, android.R.layout.simple_list_item_single_choice, mProfileCursor, 
          new String[] { BragiDatabase.ProfileColumns.NAME }, 
          new int[] {android.R.id.text1})
        );
        int mColIdx_slot_id = mProfileCursor.getColumnIndex(BragiDatabase.SlotColumns._ID);

        SharedPreferences prefs = getSharedPreferences(Bragi.PREFERENCES, 0);
        mActiveProfileId = prefs.getLong(Bragi.PREF_ACTIVE_PROFILE, -1);
        Log.v("Bragi", "active profile: "+String.valueOf(mActiveProfileId));
        if (mActiveProfileId != -1) {
          // find position for id
          mProfileCursor.moveToFirst();
          int position = 0;
          while (true) {
            long id = mProfileCursor.getLong(mColIdx_slot_id);
            if (mActiveProfileId == id) {
              Log.v("Bragi", " setting: "+String.valueOf(position));
              mListView.setItemChecked(position, true);
              break;
            }
            position++;
            if (!mProfileCursor.moveToNext()) 
              break;
          }
        }




    }

    public void finish() {
      mProfileCursor.close();
      mDbHelper.close();
      super.finish();
    }

    public void onClick(View v) {
      final long id = v.getId();
      Intent intent = new Intent();
      if (id == R.id.actionbar_button2) {
        intent.setClass(this, SlotEditorActivity.class);
        startActivity(intent);
      }
      if (id == R.id.actionbar_button1) {
          OneLineInputDialog dia = new OneLineInputDialog(this,R.string.bragi_label,"New profile name",null);
          dia.setOnCompleteListener(new OneLineInputDialog.OnCompleteListener() {
            public void onComplete(String value) {
              mDbHelper.addProfile(value);
              mProfileCursor.requery();
            }
          });
      }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
      if (id != mActiveProfileId) {
        activate_profile(position,id);
        mActiveProfileId = id;
      }
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      super.onCreateContextMenu(menu,v,menuInfo);

      menu.add(0, MENU_EDIT_ID, 0, R.string.edit);
      menu.add(0, MENU_ACTIVATE_ID, 0, R.string.activate);
      menu.add(0, MENU_DELETE_ID, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    { 
      super.onContextItemSelected(item);
      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      final int id = item.getItemId();

      if (id == MENU_ACTIVATE_ID) {
        getListView().performItemClick(info.targetView, info.position, info.id);
        return true;
      }

      if (id == MENU_EDIT_ID) {
        edit_profile(info.position, info.id);
        return true;
      }

      if (id == MENU_DELETE_ID) {
        mDbHelper.deleteProfile(info.id);
        mProfileCursor.requery();
        return true;
      }

      return false;
    }

    protected void edit_profile(int position, long id) 
    { 
      Intent intent = new Intent(this, ProfileEditorActivity.class);
      intent.putExtra(Bragi.EXTRA_PROFILE_ID, id);
      startActivityForResult(intent, RESULT_EDIT_PROFILE);
    }

    protected void activate_profile(int position, long id) 
    {
        Log.v("Bragi", "activate!");
        setSelection(position);

        //mProfileCursor.moveToPosition(position);
        //BragiDatabase.ProfileModel profile = new BragiDatabase.ProfileModel(mProfileCursor);
        Bragi.activateProfile(this, getContentResolver(), id);
    }
}
