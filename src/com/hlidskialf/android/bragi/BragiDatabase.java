
package com.hlidskialf.android.bragi;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.provider.BaseColumns;
import android.net.Uri;

public class BragiDatabase {
  private static final String DATABASE_NAME = "bragi.db";
  private static final int DATABASE_VERSION = 4;

  public static final String AUTHORITY = "com.hlidskialf.android.provider.bragi";

  public static final class SlotColumns implements BaseColumns {
    public static final String TABLE_NAME="slot";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "name ASC";


    public static final String SLUG = "slug";
    public static final String NAME = "name";
  }

  public static final class ProfileColumns implements BaseColumns {
    public static final String TABLE_NAME="profile";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "name ASC";


    public static final String NAME = "name";
    public static final String SILENT_MODE = "silent_mode";
    public static final String VIBRATE_RING = "vibrate_ring";
    public static final String VIBRATE_NOTIFY = "vibrate_notify";
    public static final String VOLUME_RINGER = "volume_ringer";
    public static final String VOLUME_MUSIC = "volume_music";
    public static final String VOLUME_CALL = "volume_call";
    public static final String VOLUME_SYSTEM = "volume_system";
    public static final String VOLUME_ALARM = "volume_alarm";
    public static final String VOLUME_NOTIFY = "volume_notify";
  }

  public static final class ProfileSlotColumns implements BaseColumns {
    public static final String TABLE_NAME="profile_slot";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "_ID ASC";

    public static final String PROFILE_ID = "profile_id";
    public static final String SLOT_ID = "slot_id";
    public static final String URI = "uri";
  }

  private static class OpenHelper extends SQLiteOpenHelper {
    OpenHelper(Context context) {
       super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
      String q;

      q = "CREATE TABLE " + SlotColumns.TABLE_NAME + "( "
        + SlotColumns._ID + " INTEGER PRIMARY KEY, "
        + SlotColumns.SLUG + " TEXT UNIQUE, "
        + SlotColumns.NAME + " TEXT);";
      db.execSQL(q);

      q = "CREATE TABLE " + ProfileColumns.TABLE_NAME + "( "
        + ProfileColumns._ID + " INTEGER PRIMARY KEY, "
        + ProfileColumns.NAME + " TEXT UNIQUE, "
        + ProfileColumns.SILENT_MODE + " INTEGER, "
        + ProfileColumns.VIBRATE_RING + " INTEGER, "
        + ProfileColumns.VIBRATE_NOTIFY + " INTEGER, "
        + ProfileColumns.VOLUME_RINGER + " INTEGER, "
        + ProfileColumns.VOLUME_MUSIC + " INTEGER, "
        + ProfileColumns.VOLUME_CALL + " INTEGER, "
        + ProfileColumns.VOLUME_SYSTEM + " INTEGER, "
        + ProfileColumns.VOLUME_ALARM + " INTEGER, "
        + ProfileColumns.VOLUME_NOTIFY + " INTEGER);";
      db.execSQL(q);
    
      q = "CREATE TABLE " + ProfileSlotColumns.TABLE_NAME + "( "
        + ProfileSlotColumns._ID + " INTEGER PRIMARY KEY, "
        + ProfileSlotColumns.PROFILE_ID + " INTEGER, "
        + ProfileSlotColumns.SLOT_ID + " INTEGER, "
        + ProfileSlotColumns.URI + " TEXT);";
      db.execSQL(q);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       Log.w("Example", "Upgrading database, this will drop tables and recreate.");
       db.execSQL("DROP TABLE IF EXISTS " + SlotColumns.TABLE_NAME);
       db.execSQL("DROP TABLE IF EXISTS " + ProfileColumns.TABLE_NAME);
       db.execSQL("DROP TABLE IF EXISTS " + ProfileSlotColumns.TABLE_NAME);
       onCreate(db);
    }
  }

  private Context mContext;
  private SQLiteOpenHelper mOpenHelper;

  public BragiDatabase(Context context) {
    mContext = context;
    mOpenHelper = new OpenHelper(context);
  }




  public Cursor getAllSlots() {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    return db.query(SlotColumns.TABLE_NAME, new String[] { SlotColumns._ID, SlotColumns.SLUG, SlotColumns.NAME }, null,null,null,null, SlotColumns.DEFAULT_SORT_ORDER);
  }

  public long addSlot(String name) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(SlotColumns.SLUG, _slugify(name));
    cv.put(SlotColumns.NAME, name);
    try {
      long id = db.insertOrThrow(SlotColumns.TABLE_NAME, "", cv);
      return id;
    } catch (SQLException e) {
      return -1;
    }
  }

  public boolean updateSlot(long slot_id, String new_name)
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(SlotColumns.NAME, new_name);

    int ret = db.update(SlotColumns.TABLE_NAME, cv, SlotColumns._ID+"=?", new String[] {String.valueOf(slot_id)});
    return (ret > 0);
  }

  public boolean deleteSlot(long slot_id) 
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    int ret = db.delete(SlotColumns.TABLE_NAME, SlotColumns._ID+"=?", new String[] {String.valueOf(slot_id)});
    return (ret > 0);
  }



  public Cursor getAllProfiles() {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    return db.query(ProfileColumns.TABLE_NAME, new String[] {
      ProfileColumns._ID,
      ProfileColumns.NAME,
      ProfileColumns.SILENT_MODE,
      ProfileColumns.VIBRATE_RING,
      ProfileColumns.VIBRATE_NOTIFY,
      ProfileColumns.VOLUME_RINGER,
      ProfileColumns.VOLUME_MUSIC,
      ProfileColumns.VOLUME_CALL,
      ProfileColumns.VOLUME_SYSTEM,
      ProfileColumns.VOLUME_ALARM,
      ProfileColumns.VOLUME_NOTIFY,
    }, null,null,null,null, ProfileColumns.DEFAULT_SORT_ORDER);
  }

  public long addProfile(String name) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(SlotColumns.NAME, name);
    try {
      long profile_id = db.insertOrThrow(ProfileColumns.TABLE_NAME, "", cv);
      synchronizeProfileSlots(profile_id);
      return profile_id;
    } catch (SQLException e) {
      Log.e("BragiDatabase", e.toString());
      return -1;
    }
  }

  public void synchronizeProfileSlots(long profile_id) 
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    Cursor slot_cursor = getAllSlots();
    final int slot_id_idx = slot_cursor.getColumnIndexOrThrow(SlotColumns._ID);
    final int l = slot_cursor.getCount()+1;
    String[] query_args = new String[l];
    int i;

    query_args[0] =  String.valueOf(profile_id); // prepare query args for delete later

    //add all the existing slots
    for (i=1; slot_cursor.moveToNext(); i++) {
      long slot_id = slot_cursor.getLong(slot_id_idx);
      updateProfileSlot(profile_id, slot_id, null);
      query_args[i] = String.valueOf(slot_id); 
    }
    slot_cursor.close();

    StringBuilder qmarks = new StringBuilder();
    for (i=0; i < l; i++) {
      qmarks.append("?");
      if (i < l) qmarks.append(",");
    }

    // delete nonexistent slots
    db.delete(ProfileSlotColumns.TABLE_NAME, 
      ProfileSlotColumns.PROFILE_ID+"=? AND "+ProfileSlotColumns.SLOT_ID+" IS NOT IN (" + qmarks.toString() + ")", query_args);
  }

  public boolean updateProfile(long profile_id, ContentValues cv) 
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int ret = db.update(ProfileColumns.TABLE_NAME, cv, ProfileColumns._ID+"=?", new String[] { String.valueOf(profile_id) });
    return (ret > 0);
  }
  public boolean deleteProfile(long profile_id) 
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int ret = db.delete(ProfileColumns.TABLE_NAME, ProfileColumns._ID+"=?", new String[] { String.valueOf(profile_id) });
    return (ret > 0);
  }


  public boolean updateProfileSlot(long profile_id, long slot_id, Uri uri)
  {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    SQLiteStatement stmt = db.compileStatement("SELECT COUNT(*) FROM "+ProfileSlotColumns.TABLE_NAME
      +" WHERE "+ProfileSlotColumns.PROFILE_ID+"=? AND "+ProfileSlotColumns.SLOT_ID+"=?");
    stmt.bindLong(0, profile_id);
    stmt.bindLong(1, slot_id);
    long row_exists = stmt.simpleQueryForLong();

    ContentValues cv = new ContentValues();
    cv.put(ProfileSlotColumns.URI, uri.toString());

    if (row_exists < 1) {
      int ret = db.update(ProfileSlotColumns.TABLE_NAME, cv, 
        ProfileSlotColumns.PROFILE_ID+"=? AND "+ProfileSlotColumns.SLOT_ID+"=?", 
        new String[] { String.valueOf(profile_id), String.valueOf(slot_id) }
      );
      return (ret > 0);
    }

    cv.put(ProfileSlotColumns.PROFILE_ID, profile_id);
    cv.put(ProfileSlotColumns.SLOT_ID, slot_id);
    try{ 
      long id = db.insertOrThrow(ProfileSlotColumns.TABLE_NAME, "", cv);
      return true;
    } catch (SQLException e) { 
    }

    return false;
  }








  private static String _slugify(String name) {
    String slug = name.toLowerCase();
    slug = slug.replaceAll("[^0-9a-zA-Z]+", "-");
    return slug;
  }

}
