package com.hlidskialf.android.tonepicker;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.Ringtone;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;



class TonePickerAdapter extends BaseExpandableListAdapter {
  private Context mContext;
  private RingtoneManager mRingManager, mNotifyManager, mAlarmManager;
  private Cursor mRingCursor, mNotifyCursor, mAlarmCursor;
  private Cursor[] mCursors;

  private static final int RING_CURSOR=0;
  private static final int NOTIFY_CURSOR=1;
  private static final int ALARM_CURSOR=2;
  private static final int NUM_CURSOR=3;




  public TonePickerAdapter(Context context) 
  {
    mContext = context;

    mCursors = new Cursor[NUM_CURSOR];

    mRingManager = new RingtoneManager(context);
    mRingManager.setType(RingtoneManager.TYPE_RINGTONE);
    mCursors[RING_CURSOR] = mRingManager.getCursor();

    mNotifyManager = new RingtoneManager(context);
    mNotifyManager.setType(RingtoneManager.TYPE_NOTIFICATION);
    mCursors[NOTIFY_CURSOR] = mNotifyManager.getCursor();

    mAlarmManager = new RingtoneManager(context);
    mAlarmManager.setType(RingtoneManager.TYPE_ALARM);
    mCursors[ALARM_CURSOR] = mAlarmManager.getCursor();

  }

  public boolean isChildSelectable(int groupPosition, int childPosition) 
  {
    return true;
  }
  public boolean hasStableIds()
  {
    return true;
  }


  public long getGroupId(int groupPosition)
  {
    return groupPosition;
  }
  public int getGroupCount()
  {
    return NUM_CURSOR;
  }
  public Object getGroup(int groupPosition)
  {
    return mCursors[groupPosition];
  }
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
  {
    if (convertView == null) {
      convertView = new TextView(mContext);
    }
    TextView convert = (TextView)convertView;

    if (groupPosition == RING_CURSOR) convert.setText("Ringtones");
    else 
    if (groupPosition == NOTIFY_CURSOR) convert.setText("Notifications");
    else 
    if (groupPosition == ALARM_CURSOR) convert.setText("Alarms");

    return convert;
  }



  public long getChildId(int groupPosition, int childPosition) 
  {
    return groupPosition<<32 & childPosition;
  }
  public int getChildrenCount(int groupPosition)
  {
    return mCursors[groupPosition].getCount();
  }
  public Object getChild(int groupPosition, int childPosition)
  {

    Cursor c = (Cursor)getGroup(groupPosition);
    c.moveToPosition(childPosition);
    String title = c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
    return title;
  }
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
  {
    String title = (String)getChild(groupPosition, childPosition);
    if (convertView == null) {
      convertView = new TextView(mContext);
    }
    TextView convert = (TextView)convertView;
    convert.setText(title);
    return convert;
  }


}


public class TonePickerActivity extends ExpandableListActivity {

    private TonePickerAdapter mAdapter; 

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAdapter = new TonePickerAdapter((Context)this);
        setListAdapter((ExpandableListAdapter)mAdapter);

    }
}
