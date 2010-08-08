package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.hlidskialf.android.app.OneLineInputDialog;

public class SlotEditorActivity extends ListActivity
                implements View.OnClickListener
{
    private BragiDatabase mDbHelper;
    private Cursor mSlotCursor;
    private int mColIdx_slot_name;
    private Intent mIntent;

    private static final int MENU_EDIT_ID=1;
    private static final int MENU_DELETE_ID=2;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mIntent = getIntent();
        String action = mIntent.getAction();

        TextView tv;
        tv = (TextView)findViewById(R.id.actionbar_subtitle);
        tv.setText(getString(R.string.activity_sloteditor));

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setOnClickListener(this);
        v.setImageResource(R.drawable.logo);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setImageResource(android.R.drawable.ic_menu_add);
        v.setVisibility(View.VISIBLE);

        v = (ImageView)findViewById(R.id.actionbar_button2);
        v.setOnClickListener(this);
        v.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        v.setVisibility(View.VISIBLE);

        mDbHelper = new BragiDatabase(this);

        mSlotCursor = mDbHelper.getAllSlots();
        setListAdapter( new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, mSlotCursor, 
          new String[] { BragiDatabase.SlotColumns.NAME }, 
          new int[] {android.R.id.text1})
        );
        mColIdx_slot_name = mSlotCursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);


        ListView mListView = getListView();
        registerForContextMenu(mListView);
    }

    public void finish() {
      mSlotCursor.close();
      mDbHelper.close();
      super.finish();
    }

    public void onClick(View v) {
        long id = v.getId();
        if (id == R.id.actionbar_logo || id == R.id.actionbar_button2) {
          finish();
        }
        else
        if (id == R.id.actionbar_button1) {
          OneLineInputDialog dia = new OneLineInputDialog(this,R.string.bragi_label,"New slot name",null);
          dia.setOnCompleteListener(new OneLineInputDialog.OnCompleteListener() {
            public void onComplete(String value) {
              mDbHelper.addSlot(value);
              mSlotCursor.requery();
            }
          });
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
      rename_slot(position, id);
    }

    protected void rename_slot(int position, long id)
    {
      final String cur_name = get_name(position);
      final long slot_id = id;

      OneLineInputDialog dia = new OneLineInputDialog(this, R.string.bragi_label, "New name for '"+cur_name+"'",cur_name);
      dia.setOnCompleteListener(new OneLineInputDialog.OnCompleteListener() {
        public void onComplete(String value) {
          mDbHelper.updateSlot(slot_id, value); 
          mSlotCursor.requery();
        }
      });
    }


    protected String get_name(int position)
    {
      mSlotCursor.moveToPosition(position);
      return mSlotCursor.getString(mColIdx_slot_name);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      super.onCreateContextMenu(menu,v,menuInfo);

      menu.add(0, MENU_EDIT_ID, 0, R.string.edit);
      menu.add(0, MENU_DELETE_ID, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    { 
      super.onContextItemSelected(item);
      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      final int id = item.getItemId();

      if (id == MENU_EDIT_ID) {
        rename_slot(info.position, info.id);
        return true;
      }

      if (id == MENU_DELETE_ID) {
        mDbHelper.deleteSlot(info.id);
        mSlotCursor.requery();
        return true;
      }

      return false;
    }




}
