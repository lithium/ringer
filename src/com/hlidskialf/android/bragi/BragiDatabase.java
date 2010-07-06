
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
  private static final int DATABASE_VERSION = 2;

  public static final String AUTHORITY = "com.hlidskialf.android.provider.bragi";

  public static final class SlotColumns implements BaseColumns {
    public static final String TABLE_NAME="slots";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "name ASC";


    public static final String SLUG = "slug";
    public static final String NAME = "name";
  }

  private static class OpenHelper extends SQLiteOpenHelper {
    OpenHelper(Context context) {
       super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
       String q = "CREATE TABLE " + SlotColumns.TABLE_NAME + "( "
        + SlotColumns._ID + " INTEGER PRIMARY KEY, "
        + SlotColumns.SLUG + " TEXT UNIQUE, "
        + SlotColumns.NAME + " TEXT"
        + "); ";
       db.execSQL(q);
       Log.v("BragiDatabase", q);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       Log.w("Example", "Upgrading database, this will drop tables and recreate.");
       db.execSQL("DROP TABLE IF EXISTS " + SlotColumns.TABLE_NAME);
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

  private static String _slugify(String name) {
    String slug = name.toLowerCase();
    slug = slug.replaceAll("\\W+", "-");
    return slug;
  }


}
