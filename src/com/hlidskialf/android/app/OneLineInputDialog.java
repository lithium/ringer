package com.hlidskialf.android.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout;


public class OneLineInputDialog {
  private OnCompleteListener mCompleteListener;
  private OnCancelListener mCancelListener;
  private AlertDialog mDialog;
  private TextView mSplash;
  private EditText mEditText;

  public interface OnCompleteListener {
    public void onComplete(String value);
  };
  public interface OnCancelListener {
    public void onCancel();
  };

  public OneLineInputDialog(Context context, int title_res, String splash, String default_value)
  {
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    mSplash = new TextView(context);
    if (splash != null) {
      mSplash.setText(splash);
    }
    layout.addView(mSplash);

    mEditText = new EditText(context);
    if (default_value != null) {
      mEditText.setHint(default_value);
      mEditText.setText(default_value);
    }
    mEditText.setSingleLine();
    mEditText.setSelectAllOnFocus(true);
    layout.addView(mEditText);

    mDialog = new AlertDialog.Builder(context)
      .setTitle(title_res)
      .setView(layout)
      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) { 
          if (mCompleteListener != null) {
            mCompleteListener.onComplete(mEditText.getText().toString());
          }
          dialog.dismiss();
        }
      })
      .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) { 
          if (mCancelListener != null) {
            mCancelListener.onCancel();
          }
          dialog.dismiss(); 
        }
      })
      .show();
  }
  public void setOnCompleteListener(OnCompleteListener listener) { mCompleteListener = listener; }
  public void setOnCancelListener(OnCancelListener listener) { mCancelListener = listener; }
}
