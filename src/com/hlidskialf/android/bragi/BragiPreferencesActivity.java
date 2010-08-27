package com.hlidskialf.android.bragi;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import com.hlidskialf.android.app.AboutDialog;

public class BragiPreferencesActivity extends PreferenceActivity
                implements View.OnClickListener
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    addPreferencesFromResource(R.xml.bragi_preferences);

    TextView tv;
    tv = (TextView)findViewById(R.id.actionbar_subtitle);
    tv.setText(getString(R.string.activity_preferences));

    ImageView v;
    v = (ImageView)findViewById(R.id.actionbar_logo);
    v.setImageResource(R.drawable.ic_actiontitle_ringer);

    v = (ImageView)findViewById(R.id.actionbar_button1);
    v.setOnClickListener(this);
    v.setVisibility(View.VISIBLE);
    v.setImageResource(R.drawable.ic_action_cancel);

  }

  /* View.OnClickListener */
  public void onClick(View v) {
    final long id = v.getId();
    if (id == R.id.actionbar_button1) {
      finish();
    }
  }

  private static abstract class ProgressToastTask extends AsyncTask<Void, Void, Boolean>
  {
    protected Context mContext;
    protected ProgressDialog mDialog;
    protected int mStr_progress, mStr_success, mStr_failure;

    public ProgressToastTask(Context context, int progress_msg, int success_msg, int failure_msg)
    {
      mContext = context;
      mStr_progress = progress_msg;
      mStr_success = success_msg;
      mStr_failure = failure_msg;
    }

    protected void onPreExecute() 
    {
      mDialog = new ProgressDialog(mContext, R.style.Theme_Profile_ProgressDialog);
      mDialog.setMessage( mContext.getString(mStr_progress) );
      mDialog.setIndeterminate(true);
      mDialog.setCancelable(false);
      mDialog.show();
    }

    protected void onPostExecute(Boolean result) 
    { 
      mDialog.dismiss();
      Toast slice = Toast.makeText(mContext, result ? mStr_success : mStr_failure, Toast.LENGTH_LONG);
      slice.show();
    }
  }


  private static class BackupTask extends ProgressToastTask
  {
    public BackupTask(Context context)
    {
      super(context, R.string.backup_dialog_message, R.string.backup_success, R.string.backup_failure);
    }

    protected Boolean doInBackground(Void... nothing) 
    {
      try{
        File external = Environment.getExternalStorageDirectory();
        File out_dir = new File(external.getAbsolutePath()+ "/hlidskialf/ringer");
        boolean ret = out_dir.mkdirs();
        Log.e("BragiBackup", "tried to make "+out_dir.getAbsolutePath()+" = "+ret);
        FileOutputStream fos = new FileOutputStream(new File(out_dir, "backup.json"));

        String backup = Bragi.serializeToJSON(mContext);
        fos.write(backup.getBytes());
        fos.close();
        return true;
      } catch (java.io.IOException e) {
        Log.e("BragiBackup", e.toString());
      }
      return false;
    }
  }
  private static class RestoreTask extends ProgressToastTask
  {
    public RestoreTask(Context context)
    {
      super(context, R.string.restore_dialog_message, R.string.restore_success, R.string.restore_failure);
    }

    protected Boolean doInBackground(Void... nothing) 
    {
      try {
        File external = Environment.getExternalStorageDirectory();
        File in_file = new File(external.getAbsolutePath()+ "/hlidskialf/ringer/backup.json");
        if (!in_file.exists()) {
          return false;
        }
        FileInputStream fis = new FileInputStream(in_file);
        int len = fis.available();
        byte[] buf = new byte[len];
        fis.read(buf);
        fis.close();

        return Bragi.unserializeFromJSON(mContext, new String(buf));
      } catch (java.io.IOException e) {
        Log.e("BragiRestore", e.toString());
      }

      return false;
    }
  }



  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) 
  {
    String key = preference.getKey();
    if (key != null && key.equals("screen_about")) {
      AboutDialog dialog = new AboutDialog(this, new ComponentName(this, BragiActivity.class), R.string.about_dialog_title, R.string.about_dialog_message);
      return true;
    }
    if (key != null && key.equals("screen_show_tutorial")) {
      Intent intent = new Intent(this, BragiTutorialActivity.class);
      startActivityForResult(intent, -1);
      return true;
    }
    if (key != null && key.equals("screen_backup")) {
      new AlertDialog.Builder(this)
        .setMessage(R.string.confirm_backup_dialog_message)
        .setTitle(R.string.confirm_backup_dialog_title)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dia, int which) {
            do_backup();
          }
        })
        .show();
      return true;
    }
    if (key != null && key.equals("screen_restore")) {
      new AlertDialog.Builder(this)
        .setMessage(R.string.confirm_restore_dialog_message)
        .setTitle(R.string.confirm_restore_dialog_title)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dia, int which) {
            do_restore();
          }
        })
        .show();
      return true;
    }

    return false;
  }

  private void do_backup()
  {
    BackupTask task = new BackupTask(this);
    task.execute();
  }

  private void do_restore()
  {
    RestoreTask task = new RestoreTask(this);
    task.execute();
  }
}
