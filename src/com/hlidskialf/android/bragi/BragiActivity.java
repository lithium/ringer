package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckedTextView;
import android.widget.SimpleCursorAdapter;

import com.hlidskialf.android.app.OneLineInputDialog;

public class BragiActivity extends ListActivity
                implements View.OnClickListener,
                           DialogInterface.OnClickListener
{
    private BragiDatabase mDbHelper;
    private Cursor mProfileCursor;
    private long mActiveProfileId=-1;
    private long mActivePosition=-1;
    private long mConfirmDeleteId=-1;

    private static final int MENU_ACTIVATE_ID=1;
    private static final int MENU_EDIT_ID=2;
    private static final int MENU_DELETE_ID=3;
    private static final int MENU_PREFERENCES_ID=4;
    private static final int MENU_ABOUT_ID=5;
    private static final int MENU_HELP_ID=6;

    private static final int RESULT_EDIT_PROFILE=1;
    private static final int RESULT_PREFERENCES=2;

    private class ProfileAdapter extends SimpleCursorAdapter
    {
        public ProfileAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
        {
          super(context,layout,c,from,to);
        }

        public void bindView (View view, Context context, Cursor cursor)
        {
          super.bindView(view,context,cursor);

          ImageView iv = (ImageView)view.findViewById(android.R.id.icon1);
          final int col_idx = cursor.getColumnIndex(BragiDatabase.SlotColumns._ID);
          final long profile_id = cursor.getLong(col_idx);
          iv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              edit_profile(0,profile_id);
            }
          });
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
          View ret = super.getView(position,convertView,parent);

          CheckedTextView ctv = (CheckedTextView)ret.findViewById(android.R.id.text1);
          ctv.setChecked(position == mActivePosition);

          return ret;
        }



    }

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
        v.setImageResource(R.drawable.ic_actiontitle_ringer);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setVisibility(View.VISIBLE);
        v.setImageResource(R.drawable.ic_action_add);

        v = (ImageView)findViewById(R.id.actionbar_button2);
        v.setOnClickListener(this);
        v.setVisibility(View.VISIBLE);
        v.setImageResource(R.drawable.ic_action_star);

        ListView mListView = getListView();
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        registerForContextMenu(mListView);


        mDbHelper = new BragiDatabase(this);
        mProfileCursor = mDbHelper.getAllProfiles();
        setListAdapter( new ProfileAdapter(this, R.layout.list_item, mProfileCursor, 
          new String[] { BragiDatabase.ProfileColumns.NAME }, 
          new int[] {android.R.id.text1})
        );
        int mColIdx_slot_id = mProfileCursor.getColumnIndex(BragiDatabase.SlotColumns._ID);

        SharedPreferences prefs = getSharedPreferences(Bragi.PREFERENCES, 0);
        mActiveProfileId = prefs.getLong(Bragi.PREF_ACTIVE_PROFILE, Bragi.PREF_ACTIVE_PROFILE_DEFAULT);
        if (mActiveProfileId != -1) {
          // find position for id
          mProfileCursor.moveToFirst();
          int position = 0;
          while (true) {
            long id = mProfileCursor.getLong(mColIdx_slot_id);
            if (mActiveProfileId == id) {
              mActivePosition = position;
              mListView.setItemChecked(position, true);
              break;
            }
            position++;
            if (!mProfileCursor.moveToNext()) 
              break;
          }
        }

        boolean seen_tutorial = prefs.getBoolean(Bragi.PREF_SEEN_TUTORIAL, Bragi.PREF_SEEN_TUTORIAL_DEFAULT);
        if (! seen_tutorial) {
          prefs.edit().putBoolean(Bragi.PREF_SEEN_TUTORIAL, true).commit();
          // XXX: show tutorial dialog
        }

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
      super.onCreateOptionsMenu(menu);

      MenuItem mi;
      mi = menu.add(Menu.NONE, MENU_HELP_ID, Menu.NONE, R.string.menu_option_help);
      mi.setIcon(android.R.drawable.ic_menu_help);
      mi = menu.add(Menu.NONE, MENU_ABOUT_ID, Menu.NONE, R.string.menu_option_about);
      mi.setIcon(android.R.drawable.ic_menu_info_details);
      mi = menu.add(Menu.NONE, MENU_PREFERENCES_ID, Menu.NONE, R.string.menu_option_preferences);
      mi.setIcon(android.R.drawable.ic_menu_preferences);

      return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
      int id = item.getItemId();
      if (id == MENU_PREFERENCES_ID) {
        Intent intent = new Intent(this, BragiPreferencesActivity.class);
        startActivityForResult(intent, RESULT_PREFERENCES);
        return true;
      }
      return false;
    }


    @Override
    public void finish() {
      mProfileCursor.close();
      mDbHelper.close();
      super.finish();
    }

    /* DialogInterface.OnClickListener */
    public void onClick(DialogInterface dialog, int which) {
      switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        if (mConfirmDeleteId != -1) {
          mDbHelper.deleteProfile(mConfirmDeleteId);
          mProfileCursor.requery();
          mConfirmDeleteId = -1;
        }
      }
    }

    /* View.OnClickListener */
    public void onClick(View v) {
      final long id = v.getId();
      Intent intent = new Intent();
      if (id == R.id.actionbar_button2) {
        intent.setClass(this, SlotEditorActivity.class);
        startActivity(intent);
      }
      if (id == R.id.actionbar_button1) {
          OneLineInputDialog dia = new OneLineInputDialog(this,R.string.bragi_label,getString(R.string.profile_dialog_new_name),null);
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
        mActivePosition = position;
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

        mConfirmDeleteId=info.id;
        int col_idx = mProfileCursor.getColumnIndex(BragiDatabase.ProfileColumns.NAME);
        mProfileCursor.moveToPosition(info.position);
        String name = mProfileCursor.getString(col_idx);
        String message = getString(R.string.confirm_delete_profile_dialog_message, name);

        AlertDialog dialog = new AlertDialog.Builder(this)
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setCancelable(true)
          .setTitle(R.string.confirm_delete_profile_dialog_title)
          .setMessage(message)
          .setPositiveButton(android.R.string.ok, this)
          .setNegativeButton(android.R.string.cancel, null)
          .show()
          ;

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
        getListView().setItemChecked(position, true);

        //mProfileCursor.moveToPosition(position);
        //BragiDatabase.ProfileModel profile = new BragiDatabase.ProfileModel(mProfileCursor);
        Bragi.activateProfile(this, getContentResolver(), id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        mProfileCursor.requery();
    }
}
