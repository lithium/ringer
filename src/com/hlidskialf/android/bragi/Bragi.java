
package com.hlidskialf.android.bragi;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.provider.Settings;
import android.os.Build;

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

    AudioManager audio = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    BragiDatabase.ProfileModel profile = db.getProfile(profile_id, false);


    Settings.System.putString(resolver, Settings.System.RINGTONE, profile.default_ring);
    Settings.System.putString(resolver, Settings.System.NOTIFICATION_SOUND, profile.default_notify);
    if (Build.VERSION.SDK_INT >= 5) Settings.System.putString(resolver, Settings.System.ALARM_ALERT, profile.default_alarm);

    if (profile.silent_mode == 0) audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    else if (profile.silent_mode == 1) audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    else if (profile.silent_mode == 2) audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

    if (profile.vibrate_ring == 0) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
    else if (profile.vibrate_ring == 1) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
    else if (profile.vibrate_ring == 2) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ONLY_SILENT);

    if (profile.vibrate_notify == 0) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
    else if (profile.vibrate_notify == 1) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
    else if (profile.vibrate_notify == 2) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ONLY_SILENT);

    audio.setStreamVolume(AudioManager.STREAM_RING, profile.volume_ringer, 0);
    audio.setStreamVolume(AudioManager.STREAM_MUSIC, profile.volume_music, 0);
    audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, profile.volume_call, 0);
    audio.setStreamVolume(AudioManager.STREAM_SYSTEM, profile.volume_system, 0);
    audio.setStreamVolume(AudioManager.STREAM_ALARM, profile.volume_alarm, 0);
    audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, profile.volume_notify, 0);



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
