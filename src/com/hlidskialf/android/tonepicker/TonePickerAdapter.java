package com.hlidskialf.android.tonepicker;


import android.content.ComponentName;
import android.content.ContentUris;
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


abstract class BaseCache {
  public String name;

  public View getView(Context context, View convertView, ViewGroup parent)
  {
    if (convertView == null) {
      convertView = new TextView(context);
    }
    TextView convert = (TextView)convertView;
    convert.setHeight(32);
    convert.setPadding(32,0,0,0);
    convert.setText(name);
    return convert;
  }
}

class AppCache extends BaseCache {
  public Drawable icon; 
  public Intent intent;
}
class RingCache extends BaseCache {
  public Uri uri;
}
class MediaCache extends BaseCache {
  public Uri uri;
}
class DefaultCache extends BaseCache {
  public DefaultCache(String new_name) { name = new_name; }
}

public class TonePickerAdapter extends BaseExpandableListAdapter {
  private Context mContext;
  private Uri mExistingUri;                                
  private ComponentName mExcludeApp;
                                                           
  private static final String[] BUILTIN_NAMES = new String[] { "Ringtones","Notifications","Alarms", };
  private static final int INDEX_DEFAULTS=0;
  private static final int INDEX_FIRST_BUILTIN=1;
  private static final int INDEX_RINGTONES=1;
  private static final int INDEX_NOTIFICATIONS=2;
  private static final int INDEX_ALARMS=3;
  private static final int INDEX_FIRST_ALBUM=4;
  private static int INDEX_CONTENTS;
  private static int INDEX_PICKERS;

  private Object[] mDefaults;
  private Object[] mRingtoneCache;
  private Object[] mNotifyCache;
  private Object[] mAlarmCache;
  private MusicCache mMusicCache;
  private Object[] mContentIntents;
  private Object[] mPickerIntents;


  

  public TonePickerAdapter(Context context, Uri existing_uri, ComponentName exclude_app)
  {
    mContext = context;
    mExistingUri = existing_uri;
    mExcludeApp = exclude_app;

    if (existing_uri == null) {
      mDefaults = new DefaultCache[] { new DefaultCache("Silence") };
    }
    else {
      mDefaults = new DefaultCache[] { new DefaultCache(mExistingUri.toString()), new DefaultCache("Silence") };
    }


    mRingtoneCache = _cache_builtins(RingtoneManager.TYPE_RINGTONE);
    mNotifyCache = _cache_builtins(RingtoneManager.TYPE_NOTIFICATION);
    mAlarmCache = _cache_builtins(RingtoneManager.TYPE_NOTIFICATION);

    _cache_album_names();

    Intent intent;
    List<ResolveInfo> activities;

    intent = new Intent(Intent.ACTION_GET_CONTENT) .setType("audio/*") .addCategory(Intent.CATEGORY_OPENABLE);
    mContentIntents = _cache_intents(intent);

    intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    mPickerIntents = _cache_intents(intent);

    INDEX_CONTENTS = INDEX_FIRST_ALBUM + mMusicCache.size();
    INDEX_PICKERS = INDEX_CONTENTS+1;

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
    int count = INDEX_FIRST_ALBUM; //defaults + builtins
    if (mMusicCache != null)
      count += mMusicCache.size();

    if (mContentIntents.length > 0) count++;
    if (mPickerIntents.length > 0) count++;
    return count;
  }
  public Object getGroup(int groupPosition)
  {
    /* note: high -> low */
    if (groupPosition == INDEX_PICKERS) 
      return mPickerIntents;

    if (groupPosition == INDEX_CONTENTS) 
      return mContentIntents;

    if (groupPosition >= INDEX_FIRST_ALBUM) {
      return mMusicCache.objects[groupPosition - INDEX_FIRST_ALBUM];
    }

    if (groupPosition == INDEX_RINGTONES) {
      return mRingtoneCache;
    }
    if (groupPosition == INDEX_NOTIFICATIONS) {
      return mNotifyCache;
    }
    if (groupPosition == INDEX_ALARMS) {
      return mAlarmCache;
    }

    if (groupPosition == INDEX_DEFAULTS) {
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

    if (groupPosition == INDEX_PICKERS) 
      text =  "Other Ringtone Apps";
    else
    if (groupPosition == INDEX_CONTENTS) 
      text =  "Other Audio Apps";
    else
    if (groupPosition >= INDEX_FIRST_ALBUM) 
      text = mMusicCache.names[groupPosition - INDEX_FIRST_ALBUM];
    else 
    if (groupPosition >= INDEX_FIRST_BUILTIN)
      text = BUILTIN_NAMES[groupPosition - INDEX_FIRST_BUILTIN];
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
    Object[] objs = (Object[])getGroup(groupPosition);
    return objs.length;

  }
  public Object getChild(int groupPosition, int childPosition)
  {
    Object[] objs = (Object[])getGroup(groupPosition);
    return (BaseCache)objs[childPosition];

    /*
    if (groupPosition == INDEX_CONTENTS || groupPosition == INDEX_PICKERS) {
      AppCache app = (AppCache)objs[childPosition];
      return (String)app.name;
    }

    if (groupPosition >= INDEX_FIRST_ALBUM) {
      MediaCache track = (MediaCache)objs[childPosition];
      return (String)track.name;
    }


    if (groupPosition == INDEX_RINGTONES || groupPosition == INDEX_NOTIFICATIONS || groupPosition == INDEX_ALARMS) {
      RingCache ring = (RingCache)objs[childPosition];
      return (String)ring.name;
    }

    if (groupPosition == 0) {
      return (String)mDefaults[childPosition];
    }



    return null;
    */
  }
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
  {
    BaseCache obj = (BaseCache)getChild(groupPosition, childPosition);
    return obj.getView(mContext, convertView, parent);

/*
    if (convertView == null) {
      convertView = new TextView(mContext);
    }
    TextView convert = (TextView)convertView;
    convert.setHeight(32);
    convert.setPadding(32,0,0,0);
    convert.setText(title);
    return convert;
    */
  }



  private Object[] _cache_builtins(int ringtone_type) 
  {
    RingtoneManager mgr = new RingtoneManager(mContext);
    ArrayList<RingCache> rings = new ArrayList<RingCache>();
    Cursor cursor;
    mgr.setType(ringtone_type);

    cursor = mgr.getCursor();

    for (cursor.moveToFirst(); cursor.moveToNext(); ) {
      RingCache ring = new RingCache();
      ring.name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
      ring.uri = Uri.parse( cursor.getString(RingtoneManager.URI_COLUMN_INDEX) );
      rings.add(ring);
    }

    cursor.close();

    return rings.toArray();
  }

  static final String[] ALBUM_CURSOR_COLS = new String[] {
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM,
  };
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

      ArrayList<MediaCache> tracks = null;
      if (!album_map.containsKey(title)) {
        tracks = new ArrayList<MediaCache>();
        album_map.put(title, tracks);
      }
      else {
        tracks = (ArrayList<MediaCache>)album_map.get(title);
      }

      MediaCache track = new MediaCache();
      track.name = cursor.getString(colidx_title);
      track.uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(colidx_id));
      tracks.add(track);
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
