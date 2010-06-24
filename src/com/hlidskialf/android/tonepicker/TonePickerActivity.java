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
import android.provider.MediaStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Iterator;


class TonePickerAdapter extends BaseExpandableListAdapter {
  private Context mContext;
  private RingtoneManager mRingManager, mNotifyManager, mAlarmManager;
  private Cursor mRingCursor, mNotifyCursor, mAlarmCursor;
  private Cursor[] mCursors;


  private static final int RING_CURSOR=0;
  private static final int NOTIFY_CURSOR=1;
  private static final int ALARM_CURSOR=2;
  private static final int LAST_BUILTIN_CURSOR=3;

  static final String[] ALBUM_CURSOR_COLS = new String[] {
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
  };

  private class MusicCache {
    int size;
    public String[] album_names;
    public Object[][] album_tracks;
  } 
  private MusicCache mMusicCache;

  

  public TonePickerAdapter(Context context) 
  {
    mContext = context;

    mCursors = new Cursor[LAST_BUILTIN_CURSOR];

    mRingManager = new RingtoneManager(context);
    mRingManager.setType(RingtoneManager.TYPE_RINGTONE);
    mCursors[RING_CURSOR] = mRingManager.getCursor();

    mNotifyManager = new RingtoneManager(context);
    mNotifyManager.setType(RingtoneManager.TYPE_NOTIFICATION);
    mCursors[NOTIFY_CURSOR] = mNotifyManager.getCursor();

    mAlarmManager = new RingtoneManager(context);
    mAlarmManager.setType(RingtoneManager.TYPE_ALARM);
    mCursors[ALARM_CURSOR] = mAlarmManager.getCursor();

    _cache_album_names();

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
    return LAST_BUILTIN_CURSOR + mMusicCache.size;
  }
  public Object getGroup(int groupPosition)
  {
    if (groupPosition < LAST_BUILTIN_CURSOR) {
      return mCursors[groupPosition];
    }

    return mMusicCache.album_tracks[groupPosition - LAST_BUILTIN_CURSOR];
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
    else 
      convert.setText(mMusicCache.album_names[groupPosition - LAST_BUILTIN_CURSOR]);

    return convert;
  }



  public long getChildId(int groupPosition, int childPosition) 
  {
    return groupPosition<<32 & childPosition;
  }
  public int getChildrenCount(int groupPosition)
  {
    if (groupPosition < LAST_BUILTIN_CURSOR) {
      return mCursors[groupPosition].getCount();
    }
    Object[] tracks = (Object[])getGroup(groupPosition);
    return tracks.length;
  }
  public Object getChild(int groupPosition, int childPosition)
  {
    if (groupPosition < LAST_BUILTIN_CURSOR) {
      Cursor c = (Cursor)getGroup(groupPosition);
      c.moveToPosition(childPosition);
      return c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
    }
    final Object[] tracks = mMusicCache.album_tracks[groupPosition - LAST_BUILTIN_CURSOR];
    return (String)tracks[childPosition];
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



  private void _cache_album_names()
  {

    StringBuilder where = new StringBuilder();
    where.append(MediaStore.Audio.Media.TITLE + " != ''");
    where.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1"); // hide recordings
    String sort = MediaStore.Audio.Media.ARTIST_KEY + " ASC, " + MediaStore.Audio.Media.ALBUM_KEY + " ASC ";

    Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ALBUM_CURSOR_COLS, where.toString(), null, sort);
    if (cursor == null)
      return;
    final int colidx_id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
    final int colidx_title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
    final int colidx_artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
    final int colidx_album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);


    LinkedHashMap<String,ArrayList> album_map = new LinkedHashMap<String,ArrayList>();
    int i;
    int imax = cursor.getCount();
    for (i=0, cursor.moveToFirst(); i < imax; i++, cursor.moveToNext()) {
      String title = cursor.getString(colidx_artist) + " / " + cursor.getString(colidx_album);
      ArrayList tracks = null;
      if (!album_map.containsKey(title)) {
        tracks = new ArrayList();
        album_map.put(title, tracks);
      }
      else {
        tracks = (ArrayList)album_map.get(title);
      }
      tracks.add(cursor.getString(colidx_title));
    }

    int size = album_map.size();

    mMusicCache = new MusicCache();
    mMusicCache.album_names = new String[size];
    mMusicCache.album_tracks = new Object[size][];

    Iterator it = album_map.entrySet().iterator();
    for (i=0; it.hasNext(); i++) {
      Entry<String,ArrayList> e = (Entry)it.next();
      mMusicCache.album_names[i] = e.getKey();
      ArrayList tracks = (ArrayList)e.getValue();
      //mMusicCache.album_tracks[i] = (String[])((ArrayList)e.getValue()).toArray();
      mMusicCache.album_tracks[i] = tracks.toArray();
    }
    mMusicCache.size = i;

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
