
package com.hlidskialf.android.bragi;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class Bragi 
{
  public static final String PACKAGE="com.hlidskialf.android.bragi";

  public static final String PREFERENCES=PACKAGE+"_preferences";
  public static final String PREF_ACTIVE_PROFILE="active_profile";
  public static final long PREF_ACTIVE_PROFILE_DEFAULT=-1;
  public static final String PREF_SEEN_TUTORIAL="seen_tutorial";
  public static final boolean PREF_SEEN_TUTORIAL_DEFAULT=false;
  public static final String PREF_MAX_SLOT_SIZE="max_slot_size";
  public static final int PREF_MAX_SLOT_SIZE_DEFAULT=2;
  public static final String PREF_CLEAR_SLOTS="clear_slots";
  public static final boolean PREF_CLEAR_SLOTS_DEFAULT=true;
  public static final String PREF_CIRCLE_CROP="circle_crop";
  public static final boolean PREF_CIRCLE_CROP_DEFAULT=false;

  public static final String EXTRA_PROFILE_ID=PACKAGE+".extra.PROFILE_ID";
  public static final String EXTRA_PROFILE_VALUES=PACKAGE+".extra.PROFILE_VALUES";
  public static final String EXTRA_SHOW_BRAGI_SLOTS=PACKAGE+".extra.SHOW_BRAGI_SLOTS";


  public static Uri getUriForSlot(String slot_slug)
  {
    Uri ret = Uri.parse("file:///data/data/"+PACKAGE+"/files/slot_"+slot_slug);

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
    private int mMaxSlotSize;
    private boolean mClearSlots;

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

      SharedPreferences prefs = mContext.getSharedPreferences(Bragi.PREFERENCES, 0);
      int slot_size = Bragi.PREF_MAX_SLOT_SIZE_DEFAULT;
      try {
        slot_size = prefs.getInt(Bragi.PREF_MAX_SLOT_SIZE, Bragi.PREF_MAX_SLOT_SIZE_DEFAULT);
      } catch (java.lang.ClassCastException e) {
        // not sure why this happens...
      }
      mMaxSlotSize = 1048576 * slot_size;
      mClearSlots = prefs.getBoolean(Bragi.PREF_CLEAR_SLOTS, Bragi.PREF_CLEAR_SLOTS_DEFAULT);
      prefs = null;

    }

    protected void onPostExecute(Boolean result) { 
      mDialog.dismiss();
      mDb.close();
    }

    protected Boolean doInBackground(Void... nothing) 
    { 
      AudioManager audio = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

      /* set default ringtone, notification, alarm */
      /* don't change default tones unless something is set */
      if (mProfile.default_ring != null && !mProfile.default_ring.equals(""))
        Settings.System.putString(mResolver, Settings.System.RINGTONE, mProfile.default_ring);
      if (mProfile.default_notify != null && !mProfile.default_notify.equals(""))
        Settings.System.putString(mResolver, Settings.System.NOTIFICATION_SOUND, mProfile.default_notify);
      if (Build.VERSION.SDK_INT >= 5) {
        if (mProfile.default_alarm != null && !mProfile.default_alarm.equals(""))
          Settings.System.putString(mResolver, Settings.System.ALARM_ALERT, mProfile.default_alarm);
      }

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

      HashMap<Long,Uri> profile_slots = mDb.getProfileSlotHash(mProfileId);
      Cursor slots = mDb.getAllSlots();
      int idx_slot_id = slots.getColumnIndex(BragiDatabase.SlotColumns._ID);
      int idx_slot_slug = slots.getColumnIndex(BragiDatabase.SlotColumns.SLUG);
      while (slots.moveToNext()) {
        long slot_id = slots.getLong(idx_slot_id);
        String slot_slug = slots.getString(idx_slot_slug);
        Uri uri = null;

        if (profile_slots.containsKey(slot_id)) {
          uri = profile_slots.get(slot_id);
        }

        if (uri == null)
        {
          if (mClearSlots) {
            /* erase old slot value */
            Log.v("Bragi/activateProfile", "erasing " + String.valueOf(slot_id));
            try {
              FileOutputStream fos = mContext.openFileOutput("slot_"+slot_slug, Context.MODE_WORLD_READABLE); 
              fos.close();
            } catch(java.io.IOException e) { 
            }
          }

          continue;
        }

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
          FileOutputStream fos = mContext.openFileOutput("slot_"+slot_slug, Context.MODE_WORLD_READABLE); 

          int bufsize = Math.min(size, mMaxSlotSize);
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
      slots.close();

    
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


  public static Bitmap scaleBitmap(Bitmap src, int new_width, int new_height) {
    int width = src.getWidth();
    int height = src.getHeight();
    float scale_width = ((float) new_width) / width;
    float scale_height = ((float) new_height) / height;
    Matrix matrix = new Matrix();
    matrix.postScale(scale_width, scale_height);
    return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
  }
}
