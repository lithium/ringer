package com.hlidskialf.android.tonepicker;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.widget.SimpleCursorTreeAdapter;


public class TonePickerActivity extends ExpandableListActivity {
    private SimpleCursorTreeAdapter mAdapter;
    //private TonepickerGroupCursor mGroupCursor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*
        mGroupCursor = new TonepickerGroupCursor();
        mAdapter = SimpleCursorTreeAdapter((Context)this, mGroupCursor, 
            R.layout.collapsed_group, R.layout.expanded_group, 
            new String[] {"title"}, new int[] {android.R.id.text1}
            R.layout.child_layout, R.layout.child_layout,
            new String[] {"title"}, new int[] {android.R.id.text1}
        );

        setListAdapater(mAdapter)
        */
    }
}
