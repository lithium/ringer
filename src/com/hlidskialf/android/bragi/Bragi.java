
package com.hlidskialf.android.bragi;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Log;

import java.util.Iterator;
import java.util.Map.Entry;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Bragi 
{
  public static final String EXTRA_PROFILE_ID="com.hlidskialf.android.bragi.extra.PROFILE_ID";
  public static final String EXTRA_PROFILE_VALUES="com.hlidskialf.android.bragi.extra.PROFILE_VALUES";


  public static Uri getUriForSlot(long profile_id)
  {
    Uri ret = Uri.parse("file:///data/data/com.hlidskialf.android.bragi/files/slot_"+String.valueOf(profile_id));

    return ret;
  }

  public static void activateProfile(Context context, ContentResolver resolver, long profile_id)
  {
    BragiDatabase db = new BragiDatabase(context);
    Cursor psc = db.getProfileSlots(profile_id);
    int idx_uri = psc.getColumnIndex(BragiDatabase.ProfileSlotColumns.URI);
    int idx_slot_id = psc.getColumnIndex(BragiDatabase.ProfileSlotColumns.SLOT_ID);
    while (psc.moveToNext()) {
      
      long slot_id = psc.getLong(idx_slot_id);
      Uri uri = Uri.parse( psc.getString(idx_uri) );

      Cursor c = resolver.query(uri, new String[] {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.MIME_TYPE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
      }, null, null, null);
      if (! c.moveToFirst()) {
        Log.v("Bragi/activateProfile", "moveToFirst failed");
        continue;
      }
      final int idx_data = c.getColumnIndex(MediaStore.Audio.Media.DATA);
      final int idx_size = c.getColumnIndex(MediaStore.Audio.Media.SIZE);
      final int idx_title = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
      String data = c.getString(idx_data);
      int size = c.getInt(idx_size);
      String title = c.getString(idx_title);

      Log.v("Bragi/activateProfile", "Found data:" + data +" = "+title);

      try {
        FileInputStream fis = new FileInputStream(data);
        FileOutputStream fos = context.openFileOutput("slot_"+String.valueOf(slot_id), Context.MODE_WORLD_READABLE); 

        int bufsize = Math.min(size, 512384);
        byte[] buffer = new byte[bufsize];
        int red = fis.read(buffer, 0, bufsize);
        fis.close();
        fos.write(buffer, 0, red);
        fos.close();
      } catch(java.io.FileNotFoundException e) {
        Log.e("Bragi/activateProfile", "file not found: "+e.toString());
      } catch(java.io.IOException e) {
        Log.e("Bragi/activateProfile", "io exeption: "+e.toString());
      }

    }
    psc.close();

    db.close();

    
  }
}
