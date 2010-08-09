
package com.hlidskialf.android.bragi;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Bragi 
{
  public static final String PACKAGE="com.hlidskialf.android.bragi";

  public static final String PREFERENCES="bragipreferences.db";
  public static final String PREF_ACTIVE_PROFILE="active_profile";

  public static final String EXTRA_PROFILE_ID=PACKAGE+".extra.PROFILE_ID";
  public static final String EXTRA_PROFILE_VALUES=PACKAGE+".extra.PROFILE_VALUES";
  public static final String EXTRA_STARTED_FROM_BRAGI=PACKAGE+".extra.STARTED_FROM_BRAGI";


  public static Uri getUriForSlot(long slot_id)
  {
    Uri ret = Uri.parse("file:///data/data/"+PACKAGE+"/files/slot_"+String.valueOf(slot_id));

    return ret;
  }




  private static class ActiveProfileTask extends AsyncTask<Void, Void, Boolean>
  {
    private Context mContext;
    private ContentResolver mResolver;
    private long mProfileId;
    private BragiDatabase mDb;
    private BragiDatabase.ProfileModel mProfile;
    private ProgressDialog mDialog;

    public ActiveProfileTask(Context context, ContentResolver resolver, long profile_id)
    {
      mContext = context; 
      mProfileId = profile_id;
      mResolver = resolver;
    }

    protected void onPreExecute() {
      mDb = new BragiDatabase(mContext);
      mProfile = mDb.getProfile(mProfileId, false);

      mDialog = new ProgressDialog(mContext, R.style.Theme_Profile_ProgressDialog);
      mDialog.setMessage( mContext.getString(R.string.activate_dialog_message, mProfile.name) );
      mDialog.setIndeterminate(true);
      mDialog.setCancelable(false);
      mDialog.show();

    }

    protected void onPostExecute(Boolean result) { 
      mDialog.dismiss();
      mDb.close();
    }

    protected Boolean doInBackground(Void... nothing) 
    { 
      AudioManager audio = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

      /* set default ringtone, notification, alarm */
      Settings.System.putString(mResolver, Settings.System.RINGTONE, mProfile.default_ring);
      Settings.System.putString(mResolver, Settings.System.NOTIFICATION_SOUND, mProfile.default_notify);
      if (Build.VERSION.SDK_INT >= 5) Settings.System.putString(mResolver, Settings.System.ALARM_ALERT, mProfile.default_alarm);

      /* set silent mode */
      if (mProfile.silent_mode == 0) audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
      else if (mProfile.silent_mode == 1) audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
      else if (mProfile.silent_mode == 2) audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

      /* set vibration */
      if (mProfile.vibrate_ring == 0) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
      else if (mProfile.vibrate_ring == 1) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
      else if (mProfile.vibrate_ring == 2) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ONLY_SILENT);
      if (mProfile.vibrate_notify == 0) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
      else if (mProfile.vibrate_notify == 1) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ON);
      else if (mProfile.vibrate_notify == 2) audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_ONLY_SILENT);

      /* set volumes */
      audio.setStreamVolume(AudioManager.STREAM_RING, mProfile.volume_ringer, 0);
      audio.setStreamVolume(AudioManager.STREAM_MUSIC, mProfile.volume_music, 0);
      audio.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mProfile.volume_call, 0);
      audio.setStreamVolume(AudioManager.STREAM_SYSTEM, mProfile.volume_system, 0);
      audio.setStreamVolume(AudioManager.STREAM_ALARM, mProfile.volume_alarm, 0);
      audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mProfile.volume_notify, 0);

      /* copy slots */
      Cursor psc = mDb.getProfileSlots(mProfileId);
      int idx_uri = psc.getColumnIndex(BragiDatabase.ProfileSlotColumns.URI);
      int idx_slot_id = psc.getColumnIndex(BragiDatabase.ProfileSlotColumns.SLOT_ID);
      while (psc.moveToNext()) {
        
        long slot_id = psc.getLong(idx_slot_id);
        String uri_str = psc.getString(idx_uri);
        if (uri_str == null)
          continue;
        Uri uri = Uri.parse( uri_str );

        Cursor c = mResolver.query(uri, new String[] {
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
          FileOutputStream fos = mContext.openFileOutput("slot_"+String.valueOf(slot_id), Context.MODE_WORLD_READABLE); 

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

    
      return true;
    }
  }

  public static void activateProfile(Context context, ContentResolver resolver, long profile_id)
  {
    ActiveProfileTask task = new ActiveProfileTask(context,resolver,profile_id);
    task.execute();

    SharedPreferences prefs = context.getSharedPreferences(Bragi.PREFERENCES, 0);
    prefs.edit().putLong(Bragi.PREF_ACTIVE_PROFILE, profile_id).commit();

  }
}
