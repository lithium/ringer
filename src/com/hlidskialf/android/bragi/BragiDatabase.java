
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

import java.util.HashMap;


public class BragiDatabase {
  private static final String DATABASE_NAME = "bragi.db";
  private static final int DATABASE_VERSION = 000701;

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
    public static final String DEFAULT_RING = "default_ring";
    public static final String DEFAULT_NOTIFY = "default_notification";
    public static final String DEFAULT_ALARM = "default_alarm";
    public static final String SILENT_MODE = "silent_mode";
    public static final String VIBRATE_RING = "vibrate_ring";
    public static final String VIBRATE_NOTIFY = "vibrate_notify";
    public static final String VOLUME_RINGER = "volume_ringer";
    public static final String VOLUME_MUSIC = "volume_music";
    public static final String VOLUME_CALL = "volume_call";
    public static final String VOLUME_SYSTEM = "volume_system";
    public static final String VOLUME_ALARM = "volume_alarm";
    public static final String VOLUME_NOTIFY = "volume_notify";

    public static final String[] DEFAULT_PROJECTION = {
      ProfileColumns._ID,
      ProfileColumns.NAME,
      ProfileColumns.DEFAULT_RING,
      ProfileColumns.DEFAULT_NOTIFY,
      ProfileColumns.DEFAULT_ALARM,
      ProfileColumns.SILENT_MODE,
      ProfileColumns.VIBRATE_RING,
      ProfileColumns.VIBRATE_NOTIFY,
      ProfileColumns.VOLUME_RINGER,
      ProfileColumns.VOLUME_MUSIC,
      ProfileColumns.VOLUME_CALL,
      ProfileColumns.VOLUME_SYSTEM,
      ProfileColumns.VOLUME_ALARM,
      ProfileColumns.VOLUME_NOTIFY,
    };
  }

  public static final class ProfileSlotColumns implements BaseColumns {
    public static final String TABLE_NAME="profile_slot";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "_ID ASC";

    public static final String PROFILE_ID = "profile_id";
    public static final String SLOT_ID = "slot_id";
    public static final String URI = "uri";

    public static final String[] DEFAULT_PROJECTION = {
      ProfileSlotColumns.PROFILE_ID,
      ProfileSlotColumns.SLOT_ID,
      ProfileSlotColumns.URI,
    };
  }

  public static final class ProfileModel {
    public long id = -1;
    public String name;
    public String default_ring;
    public String default_notify;
    public String default_alarm;
    public int silent_mode;
    public int vibrate_ring;
    public int vibrate_notify;
    public int volume_ringer;
    public int volume_music;
    public int volume_call;
    public int volume_system;
    public int volume_alarm;
    public int volume_notify;
    public HashMap<Long,Uri> slots;

    public ProfileModel() {}
    public ProfileModel(Cursor c) { hydrate(c); }

    public void hydrate(Cursor c) {
      int idx;
      if ((idx = c.getColumnIndex(ProfileColumns._ID)) != -1) this.id = c.getLong(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.NAME)) != -1) this.name = c.getString(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.DEFAULT_RING)) != -1) this.default_ring = c.getString(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.DEFAULT_NOTIFY)) != -1) this.default_notify = c.getString(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.DEFAULT_ALARM)) != -1) this.default_alarm = c.getString(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.SILENT_MODE)) != -1) this.silent_mode = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VIBRATE_RING)) != -1) this.vibrate_ring = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VIBRATE_NOTIFY)) != -1) this.vibrate_notify = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_RINGER)) != -1) this.volume_ringer = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_MUSIC)) != -1) this.volume_music = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_CALL)) != -1) this.volume_call = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_SYSTEM)) != -1) this.volume_system = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_ALARM)) != -1) this.volume_alarm = c.getInt(idx);
      if ((idx = c.getColumnIndex(ProfileColumns.VOLUME_NOTIFY)) != -1) this.volume_notify = c.getInt(idx);
    }
    public void updateValues(ContentValues cv) {
      this.name = cv.getAsString(ProfileColumns.NAME);
      this.default_ring = cv.getAsString(ProfileColumns.DEFAULT_RING);
      this.default_notify = cv.getAsString(ProfileColumns.DEFAULT_NOTIFY);
      this.default_alarm = cv.getAsString(ProfileColumns.DEFAULT_ALARM);
      this.silent_mode = cv.getAsInteger(ProfileColumns.SILENT_MODE);
      this.vibrate_ring = cv.getAsInteger(ProfileColumns.VIBRATE_RING);
      this.vibrate_notify = cv.getAsInteger(ProfileColumns.VIBRATE_NOTIFY);
      this.volume_ringer = cv.getAsInteger(ProfileColumns.VOLUME_RINGER);
      this.volume_music = cv.getAsInteger(ProfileColumns.VOLUME_MUSIC);
      this.volume_call = cv.getAsInteger(ProfileColumns.VOLUME_CALL);
      this.volume_system = cv.getAsInteger(ProfileColumns.VOLUME_SYSTEM);
      this.volume_alarm = cv.getAsInteger(ProfileColumns.VOLUME_ALARM);
      this.volume_notify = cv.getAsInteger(ProfileColumns.VOLUME_NOTIFY);
    }
    public ContentValues contentValues() {
      ContentValues cv = new ContentValues();
      cv.put(ProfileColumns._ID, this.id);
      cv.put(ProfileColumns.NAME, this.name);
      cv.put(ProfileColumns.DEFAULT_RING, this.default_ring);
      cv.put(ProfileColumns.DEFAULT_NOTIFY, this.default_notify);
      cv.put(ProfileColumns.DEFAULT_ALARM, this.default_alarm);
      cv.put(ProfileColumns.SILENT_MODE, this.silent_mode);
      cv.put(ProfileColumns.VIBRATE_RING, this.vibrate_ring);
      cv.put(ProfileColumns.VIBRATE_NOTIFY, this.vibrate_notify);
      cv.put(ProfileColumns.VOLUME_RINGER, this.volume_ringer);
      cv.put(ProfileColumns.VOLUME_MUSIC, this.volume_music);
      cv.put(ProfileColumns.VOLUME_CALL, this.volume_call);
      cv.put(ProfileColumns.VOLUME_SYSTEM, this.volume_system);
      cv.put(ProfileColumns.VOLUME_ALARM, this.volume_alarm);
      cv.put(ProfileColumns.VOLUME_NOTIFY, this.volume_notify);
      return cv;
    }


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
        + ProfileColumns.DEFAULT_RING + " TEXT, "
        + ProfileColumns.DEFAULT_NOTIFY + " TEXT, "
        + ProfileColumns.DEFAULT_ALARM + " TEXT, "
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
  protected void finalize() throws Throwable
  {
    super.finalize();
    close();
  }

  public void close() 
  {
    mOpenHelper.close();
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
    long id = -1;
    try {
      id = db.insertOrThrow(SlotColumns.TABLE_NAME, "", cv);
    } catch (SQLException e) {
    }
    return id;
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



  public ProfileModel getProfile(long profile_id, boolean hash_slots) 
  {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    Cursor c = db.query(ProfileColumns.TABLE_NAME, ProfileColumns.DEFAULT_PROJECTION, 
      ProfileColumns._ID+"=?", new String[] {String.valueOf(profile_id)},
      null,null, ProfileColumns.DEFAULT_SORT_ORDER);
    if (!c.moveToFirst())
      return null;
    ProfileModel ret = new ProfileModel(c);
    c.close();

    if (hash_slots) {
      ret.slots = new HashMap<Long,Uri>();
      Cursor slots_c = getProfileSlots(profile_id);
      int uri_idx = slots_c.getColumnIndex(ProfileSlotColumns.URI);
      int slot_idx = slots_c.getColumnIndex(ProfileSlotColumns.SLOT_ID);
      while (slots_c.moveToNext()) {
        ret.slots.put( slots_c.getLong(slot_idx), Uri.parse(slots_c.getString(uri_idx)) );
      }
      slots_c.close();
    }
    db.close();

    return ret;
  }
  public Cursor getProfileSlots(long profile_id)
  {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    return db.query(ProfileSlotColumns.TABLE_NAME, ProfileSlotColumns.DEFAULT_PROJECTION, 
      ProfileSlotColumns.PROFILE_ID+"=?", new String[] {String.valueOf(profile_id)},
      null,null, ProfileSlotColumns.DEFAULT_SORT_ORDER);
  }

  public Cursor getAllProfiles() {
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    return db.query(ProfileColumns.TABLE_NAME, ProfileColumns.DEFAULT_PROJECTION, null,null,null,null, ProfileColumns.DEFAULT_SORT_ORDER);
  }

  public long addProfile(String name) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(SlotColumns.NAME, name);
    long profile_id = -1;
    try {
      profile_id = db.insertOrThrow(ProfileColumns.TABLE_NAME, "", cv);
      synchronizeProfileSlots(profile_id);
    } catch (SQLException e) {
      Log.e("BragiDatabase", e.toString());
    }
    return profile_id;
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
      if (i < l-1) qmarks.append(",");
    }

    // delete nonexistent slots
    db.delete(ProfileSlotColumns.TABLE_NAME, 
      ProfileSlotColumns.PROFILE_ID+"=? AND "+ProfileSlotColumns.SLOT_ID+" NOT IN (" + qmarks.toString() + ")", query_args);
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
    stmt.bindLong(1, profile_id);
    stmt.bindLong(2, slot_id);
    long row_count = stmt.simpleQueryForLong();

    ContentValues cv = new ContentValues();
    cv.put(ProfileSlotColumns.URI, uri != null ? uri.toString() : null);

    if (row_count > 0) {
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
      Log.e("BragiDatabase", "updateProfileSlot: "+e.toString());
    }

    return false;
  }







  private static String _slugify(String name) {
    String slug = name.toLowerCase();
    slug = slug.replaceAll("[^0-9a-zA-Z]+", "-");
    return slug;
  }

}
