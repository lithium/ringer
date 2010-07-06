package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.hlidskialf.android.app.OneLineInputDialog;

public class SlotEditorActivity extends ListActivity
                implements View.OnClickListener
{
    private BragiDatabase mDbHelper;
    private Cursor mSlotCursor;

    private static final int CONTEXT_RENAME_ID=1;
    private static final int CONTEXT_DELETE_ID=2;

    private int mColIdx_slot_name;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setImageResource(R.drawable.logo);
        v.setOnClickListener(this);

        v = (ImageView)findViewById(R.id.actionbar_button1);
        v.setOnClickListener(this);
        v.setVisibility(View.VISIBLE);
        v.setImageResource(android.R.drawable.ic_menu_add);

        mDbHelper = new BragiDatabase(this);
        mDbHelper.addSlot("Default Ringtone");
        mDbHelper.addSlot("Default Notification");
        mDbHelper.addSlot("Default Alarm");

        mSlotCursor = mDbHelper.getAllSlots();
        setListAdapter( new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, mSlotCursor, 
          new String[] { BragiDatabase.SlotColumns.NAME }, 
          new int[] {android.R.id.text1})
        );

        mColIdx_slot_name = mSlotCursor.getColumnIndex(BragiDatabase.SlotColumns.NAME);

        registerForContextMenu(getListView());
    }

    @Override 
    public void onClick(View v) {
        long id = v.getId();
        if (id == R.id.actionbar_logo) {
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
      rename_slot(position);
    }

    protected void rename_slot(int position)
    {
      final String cur_name = getName(position);

      OneLineInputDialog dia = new OneLineInputDialog(this, R.string.bragi_label, "New name for '"+cur_name+"'",cur_name);
      dia.setOnCompleteListener(new OneLineInputDialog.OnCompleteListener() {
        public void onComplete(String value) {
          mDbHelper.renameSlot(cur_name, value); 
          mSlotCursor.requery();
        }
      });
    }

    protected String getName(int position)
    {
      mSlotCursor.moveToPosition(position);
      return mSlotCursor.getString(mColIdx_slot_name);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      super.onCreateContextMenu(menu,v,menuInfo);

      menu.add(0, CONTEXT_RENAME_ID, 0, R.string.rename);
      menu.add(0, CONTEXT_DELETE_ID, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item)
    { 
      super.onContextItemSelected(item);
      final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      final int id = item.getItemId();

      if (id == CONTEXT_RENAME_ID) {
        rename_slot(info.position);
        return true;
      }

      if (id == CONTEXT_DELETE_ID) {
        mDbHelper.removeSlot(getName(info.position));
        mSlotCursor.requery();
        return true;
      }

      return false;
    }




}
