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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.CheckedTextView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import android.util.Log;

import com.hlidskialf.android.bragi.R;
import com.hlidskialf.android.bragi.BragiDatabase;
import com.hlidskialf.android.bragi.Bragi;


public class TonePickerAdapter extends BaseExpandableListAdapter {
  private Context mContext;
  private Uri mExistingUri;                                
  private ComponentName mExcludeApp;
  private LayoutInflater mInflater;
  private boolean mShowSlots;

  public long mSelectedId = -1;
                                                           
  private static final String[] BUILTIN_NAMES = new String[] { "Ringtones","Notifications","Alarms", };

  private static int INDEX_SLOTS;
  private static int INDEX_FIRST_BUILTIN;
  private static int INDEX_RINGTONES;
  private static int INDEX_NOTIFICATIONS;
  private static int INDEX_ALARMS;
  private static int INDEX_FIRST_ALBUM;
  private static int INDEX_CONTENTS;
  private static int INDEX_PICKERS;

  private Object[] mSlotCache;
  private Object[] mRingtoneCache;
  private Object[] mNotifyCache;
  private Object[] mAlarmCache;
  private MusicCache mMusicCache;
  private Object[] mContentIntents;
  private Object[] mPickerIntents;

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


  public abstract class BaseCache {
    public String name;
    public long id;

    public class ViewHolder {
      CheckedTextView label;
      ImageView icon;
      //ImageView selected;
    };

    public View getView(View convertView, ViewGroup parent)
    {
      ViewHolder holder;
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.tonepicker_child, null);
        holder = new ViewHolder();
        holder.icon = (ImageView)convertView.findViewById(android.R.id.icon1);
        holder.label = (CheckedTextView)convertView.findViewById(android.R.id.text1);
        //holder.selected = (ImageView)convertView.findViewById(android.R.id.icon2);
        convertView.setTag(holder);

      } else {
        holder = (ViewHolder)convertView.getTag();
      }

      holder.label.setText(name);
      holder.icon.setVisibility(View.GONE);


      return convertView;
    }
  }

  class AppCache extends BaseCache {
    public Drawable icon; 
    public Intent intent;
    public View getView(View convertView, ViewGroup parent)
    { 
      convertView = super.getView(convertView,parent);
      ViewHolder holder = (ViewHolder)convertView.getTag();

      holder.icon.setImageDrawable(icon);
      holder.icon.setVisibility(View.VISIBLE);

      //holder.selected.setVisibility(View.GONE);

      return convertView;

    }
  }
  class RingCache extends BaseCache {
    public Uri uri;
    public View getView(View convertView, ViewGroup parent)
    { 
      convertView = super.getView(convertView,parent);
      ViewHolder holder = (ViewHolder)convertView.getTag();

      holder.label.setChecked(mSelectedId == id);
        /*
      holder.selected.setVisibility(View.VISIBLE);
      if (mSelectedId == id) {
        holder.selected.setImageResource(android.R.drawable.button_onoff_indicator_on);
      }
      else {
        holder.selected.setImageResource(android.R.drawable.button_onoff_indicator_off);
      }
      */
      return convertView;
    }
  }
  class BuiltinRingCache extends RingCache {
    Ringtone ringtone;
  }
  class SlotCache extends RingCache {
    public SlotCache(String new_name, Uri new_uri) { name = new_name; uri = new_uri; }
  }
  

  public TonePickerAdapter(Context context, ComponentName exclude_app, boolean show_bragi_slots)
  {
    mContext = context;
    mExcludeApp = exclude_app;
    mShowSlots = show_bragi_slots;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    if (mShowSlots) {
        INDEX_SLOTS=0;
        mSlotCache = _cache_slots();
    }
    else {
        INDEX_SLOTS=-1;
    }
    INDEX_FIRST_BUILTIN=INDEX_SLOTS+1;
    INDEX_RINGTONES=INDEX_FIRST_BUILTIN+0;
    INDEX_NOTIFICATIONS=INDEX_FIRST_BUILTIN+1;
    INDEX_ALARMS=INDEX_FIRST_BUILTIN+2;
    INDEX_FIRST_ALBUM=INDEX_FIRST_BUILTIN+3;


    mRingtoneCache = _cache_builtins(RingtoneManager.TYPE_RINGTONE);
    mNotifyCache = _cache_builtins(RingtoneManager.TYPE_NOTIFICATION);
    mAlarmCache = _cache_builtins(RingtoneManager.TYPE_NOTIFICATION);

    _cache_album_names();

    Intent intent;
    intent = new Intent(Intent.ACTION_GET_CONTENT) .setType("audio/*") .addCategory(Intent.CATEGORY_OPENABLE);
    mContentIntents = _cache_intents(intent);
    intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    mPickerIntents = _cache_intents(intent);

    INDEX_CONTENTS = INDEX_FIRST_ALBUM + (mMusicCache != null ? mMusicCache.size() : 0);
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
    int count = INDEX_FIRST_ALBUM; //slots + builtins
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
    if (groupPosition == INDEX_ALARMS) {
      return mAlarmCache;
    }
    if (groupPosition == INDEX_NOTIFICATIONS) {
      return mNotifyCache;
    }
    if (groupPosition == INDEX_RINGTONES) {
      return mRingtoneCache;
    }
    if (mShowSlots && groupPosition == INDEX_SLOTS) {
      return mSlotCache;
    }

    return null;
  }
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
  {
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.tonepicker_group, null);
      TextView tv = (TextView)convertView.findViewById(android.R.id.text1);
      convertView.setTag(tv);
    }
    TextView tv = (TextView)convertView.getTag();
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
    if (mShowSlots && groupPosition == INDEX_SLOTS)
      text = "Bragi Slots";
      
    tv.setText(text);

    return convertView;
  }



  public long getChildId(int groupPosition, int childPosition) 
  {
    return ((long)groupPosition<<32) | childPosition;
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
  }
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) 
  {
    BaseCache obj = (BaseCache)getChild(groupPosition, childPosition);
    obj.id = getChildId(groupPosition, childPosition);
    return obj.getView(convertView, parent);

  }


  private Object[] _cache_slots() 
  {
    ArrayList<SlotCache> slots = new ArrayList<SlotCache>();
    BragiDatabase db = new BragiDatabase(mContext);
    Cursor cursor = db.getAllSlots();
    final int slot_id_idx = cursor.getColumnIndexOrThrow(BragiDatabase.SlotColumns._ID);
    final int slot_name_idx = cursor.getColumnIndexOrThrow(BragiDatabase.SlotColumns.NAME);
    int l = cursor.getCount();
    while (cursor.moveToNext()) {
        long slot_id = cursor.getLong(slot_id_idx);
        Uri uri = Bragi.getUriForSlot(slot_id);
        String name = cursor.getString(slot_name_idx);
        SlotCache slot = new SlotCache(name, uri);
        slots.add(slot);
    }
    cursor.close();
    db.close();
    return slots.toArray();

  }

  private Object[] _cache_builtins(int ringtone_type) 
  {
    RingtoneManager mgr = new RingtoneManager(mContext);
    ArrayList<RingCache> rings = new ArrayList<RingCache>();
    Cursor cursor;
    mgr.setType(ringtone_type);
    mgr.setIncludeDrm(true);

    cursor = mgr.getCursor();

    for (cursor.moveToFirst(); cursor.moveToNext(); ) {
      RingCache ring = new RingCache();
      ring.name = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
      ring.uri = mgr.getRingtoneUri(cursor.getPosition());
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
    int imax = cursor.getCount();
    for (cursor.moveToFirst(); cursor.moveToNext(); ) {
      String title = cursor.getString(colidx_artist) + " / " + cursor.getString(colidx_album);

      ArrayList<RingCache> tracks = null;
      if (!album_map.containsKey(title)) {
        tracks = new ArrayList<RingCache>();
        album_map.put(title, tracks);
      }
      else {
        tracks = (ArrayList<RingCache>)album_map.get(title);
      }

      RingCache track = new RingCache();
      track.name = cursor.getString(colidx_title);
      track.uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(colidx_id));
      tracks.add(track);
    }

    int size = album_map.size();

    mMusicCache = new MusicCache(size);
    Iterator it = album_map.entrySet().iterator();
    for (int i=0; it.hasNext(); i++) {
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
