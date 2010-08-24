package com.hlidskialf.android.preference;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


public class ImagePreference extends Preference
{
  private Bitmap mBitmap;

  public ImagePreference(Context context) { this(context,null,0); }
  public ImagePreference(Context context, AttributeSet attrs) { this(context,attrs,0); }
  public ImagePreference(Context context, AttributeSet attrs, int style) {
    super(context,attrs,style); 
  }

  @Override
  protected void onBindView(View view)
  {
    super.onBindView(view);
    Log.v("ImagePreference", "!!!bindView!!!");
    ImageView iv = (ImageView)view.findViewById(android.R.id.icon1);
    if (mBitmap != null) {
      iv.setImageBitmap(mBitmap);
    }
  }

  public void setBitmap(Bitmap image)
  {
    mBitmap = image;
    notifyChanged();
  }

}
