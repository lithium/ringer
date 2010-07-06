package com.hlidskialf.android.bragi;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class SlotEditorActivity extends ListActivity
                implements View.OnClickListener
{
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

        BragiDatabase mDbHelper = new BragiDatabase(this);
        mDbHelper.addSlot("Default Ringtone");
        mDbHelper.addSlot("Default Notification");
        mDbHelper.addSlot("Default Alarm");

        Cursor c = mDbHelper.getAllSlots();
        setListAdapter( new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, c, 
          new String[] { BragiDatabase.SlotColumns.NAME }, 
          new int[] {android.R.id.text1})
        );

    }

    @Override 
    public void onClick(View v) {
        long id = v.getId();
        if (id == R.id.actionbar_logo) {
          finish();
        }
    }
}
