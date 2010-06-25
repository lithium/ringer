package com.hlidskialf.android.tonepicker;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

class MusicCache {
  public String[]   names;
  public Object[][] objects;

  public MusicCache(int size) {
    setSize(size);
  }
  public void setSize(int size) {
    names = new String[size];
    objects = new Object[size][];
  }
  public int size() { return names.length; }

  public void put(int pos, String new_name, Object[] new_objects) {
    names[pos] = new_name;
    objects[pos] = new_objects;
  }
}

class AppCache {
  public Drawable icon; 
  public String name;
  public Intent intent;
}

public class TonePickerAdapter extends BaseExpandableListAdapter {
  private Context mContext;
  private RingtoneManager mRingManager, mNotifyManager, mAlarmManager;
  private Cursor mRingCursor, mNotifyCursor, mAlarmCursor;
  private Cursor[] mCursors;                               
  private Uri mExistingUri;                                
  private ComponentName mExcludeApp;
                                                           

  private static final int BUILTIN_CURSOR_RING=0;
  private static final int BUILTIN_CURSOR_NOTIFY=1;
  private static final int BUILTIN_CURSOR_ALARM=2;
  private static final int BUILTIN_CURSOR_COUNT=3;
  private static final String[] BUILTIN_NAMES = new String[] { "Ringtones","Notifications","Alarms", };
  private String[] mDefaults;

  private static final int mIndex_firstBuiltin=1;
  private static int mIndex_firstAlbum;
  private static int mIndex_firstContent;
  private static int mIndex_firstPicker;

  private MusicCache mMusicCache;

  private Object[] mContentIntents;
  private Object[] mPickerIntents;


  static final String[] ALBUM_CURSOR_COLS = new String[] {
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
  };
  

  public TonePickerAdapter(Context context, Uri existing_uri, ComponentName exclude_app)
  {
    mContext = context;
    mExistingUri = existing_uri;
    mExcludeApp = exclude_app;

    if (existing_uri == null)
      mDefaults = new String[] {"Silence"};
    else
      mDefaults = new String[] {mExistingUri.toString(), "Silence"};


    mCursors = new Cursor[BUILTIN_CURSOR_COUNT];
    mRingManager = new RingtoneManager(context);
    mRingManager.setType(RingtoneManager.TYPE_RINGTONE);
    mCursors[BUILTIN_CURSOR_RING] = mRingManager.getCursor();
    mNotifyManager = new RingtoneManager(context);
    mNotifyManager.setType(RingtoneManager.TYPE_NOTIFICATION);
    mCursors[BUILTIN_CURSOR_NOTIFY] = mNotifyManager.getCursor();
    mAlarmManager = new RingtoneManager(context);
    mAlarmManager.setType(RingtoneManager.TYPE_ALARM);
    mCursors[BUILTIN_CURSOR_ALARM] = mAlarmManager.getCursor();


    _cache_album_names();

    Intent intent;
    List<ResolveInfo> activities;

    intent = new Intent(Intent.ACTION_GET_CONTENT) .setType("audio/*") .addCategory(Intent.CATEGORY_OPENABLE);
    mContentIntents = _cache_intents(intent);

    intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    mPickerIntents = _cache_intents(intent);

    mIndex_firstAlbum = 1+BUILTIN_CURSOR_COUNT;
    mIndex_firstContent = mIndex_firstAlbum + mMusicCache.size();
    mIndex_firstPicker = mIndex_firstContent+1;
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
    int count = 1+BUILTIN_CURSOR_COUNT;
    if (mMusicCache != null)
      count += mMusicCache.size();

    if (mContentIntents.length > 0) count++;
    if (mPickerIntents.length > 0) count++;
    return count;
  }
  public Object getGroup(int groupPosition)
  {
    /* note: high -> low */
    if (groupPosition >= mIndex_firstPicker) 
      return mPickerIntents;

    if (groupPosition >= mIndex_firstContent) 
      return mContentIntents;

    if (groupPosition >= mIndex_firstAlbum) {
      return mMusicCache.objects[groupPosition - mIndex_firstAlbum];
    }

    if (groupPosition >= mIndex_firstBuiltin) {
      return mCursors[groupPosition - mIndex_firstBuiltin];
    }

    if (groupPosition == 0) {
      return mDefaults;
    }

    return null;
  }
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
  {
    if (convertView == null) {
      convertView = new TextView(mContext);
    }
    TextView convert = (TextView)convertView;
    convert.setHeight(32);
    convert.setPadding(32,0,0,0);
    String text = null;

    if (groupPosition >= mIndex_firstPicker) 
      text =  "Other Ringtone Apps";
    else
    if (groupPosition >= mIndex_firstContent) 
      text =  "Other Audio Apps";
    else
    if (groupPosition >= mIndex_firstAlbum) 
      text = mMusicCache.names[groupPosition - mIndex_firstAlbum];
    else 
    if (groupPosition >= mIndex_firstBuiltin)
      text = BUILTIN_NAMES[groupPosition - mIndex_firstBuiltin];
    else 
    if (groupPosition == 0)
      text = "Defaults";
      
    convert.setText(text);

    return convert;
  }



  public long getChildId(int groupPosition, int childPosition) 
  {
    return groupPosition<<32 & childPosition;
  }
  public int getChildrenCount(int groupPosition)
  {
    if (groupPosition >= mIndex_firstContent || groupPosition >= mIndex_firstPicker) {
      Object[] apps = (Object[])getGroup(groupPosition);
      return apps.length;
    }

    if (groupPosition >= mIndex_firstAlbum) {
      Object[] tracks = (Object[])getGroup(groupPosition);
      return tracks.length;
    }

    if (groupPosition >= mIndex_firstBuiltin) {
      Cursor c = (Cursor)getGroup(groupPosition);
      return c.getCount();
    }

    if (groupPosition == 0) {
      String[] defaults = (String[])getGroup(groupPosition);
      return defaults.length;
    }

    return 0;
  }
  public Object getChild(int groupPosition, int childPosition)
  {
    if (groupPosition >= mIndex_firstContent || groupPosition >= mIndex_firstPicker) {
      Object[] apps = (Object[])getGroup(groupPosition);
      AppCache app = (AppCache)apps[childPosition];
      return app.name;
    }

    if (groupPosition >= mIndex_firstAlbum) {
      final Object[] tracks = mMusicCache.objects[groupPosition - mIndex_firstAlbum];
      return (String)tracks[childPosition];
    }

    if (groupPosition >= mIndex_firstBuiltin) {
      Cursor c = (Cursor)getGroup(groupPosition);
      c.moveToPosition(childPosition);
      return c.getString(RingtoneManager.TITLE_COLUMN_INDEX);
    }

    if (groupPosition == 0) {
      return (String)mDefaults[childPosition];
    }


    return null;
  }
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
  {
    String title = (String)getChild(groupPosition, childPosition);
    if (convertView == null) {
      convertView = new TextView(mContext);
    }
    TextView convert = (TextView)convertView;
    convert.setHeight(32);
    convert.setPadding(32,0,0,0);
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

    mMusicCache = new MusicCache(size);
    Iterator it = album_map.entrySet().iterator();
    for (i=0; it.hasNext(); i++) {
      Entry<String,ArrayList> e = (Entry)it.next();
      ArrayList tracks = (ArrayList)e.getValue();
      mMusicCache.put(i, e.getKey(), (Object[]) tracks.toArray());
    }

  }



  private Object[] _cache_intents(Intent intent) {
      PackageManager package_manager = mContext.getPackageManager();
      List<ResolveInfo> list = package_manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
      if (list == null) return null;

      int N = list.size();
      ArrayList<AppCache> cache = new ArrayList<AppCache>(N);
      for (int i=0; i<N; i++) {
          ResolveInfo ri = list.get(i);
          if (mExcludeApp != null && (ri.activityInfo.packageName.equals(mExcludeApp.getPackageName())
                  || ri.activityInfo.name.equals(mExcludeApp.getClassName()))) {
              list.remove(i);
              N--;
              continue;
          }

          AppCache app = new AppCache();
          app.icon = ri.loadIcon(package_manager);
          app.name = ri.loadLabel(package_manager).toString();

          intent.setComponent( new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name) );
          app.intent = (Intent)intent.clone();
          cache.add(app);
      }

      return cache.toArray();
  }

}
