/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.hlidskialf.android.app;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutDialog 
{
  private AlertDialog mDialog;
  private PackageInfo mPackageInfo;

  private class ViewHolder 
  {
    TextView title_text;
    TextView version_text;
    ScrollView scroll; 
    TextView about_text;
  }

  public AboutDialog(Context context, ComponentName component, int title_res, int summary_res)
  {
    ViewHolder holder = new ViewHolder();
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(6,0,6,6);
    layout.setGravity(Gravity.CENTER);

    holder.title_text = new TextView(context);
    holder.title_text.setTextSize(14);
    layout.addView(holder.title_text);

    holder.version_text = new TextView(context);
    holder.version_text.setGravity(Gravity.CENTER);
    layout.addView(holder.version_text);

    holder.about_text = new TextView(context);
    holder.about_text.setText(summary_res);
    holder.about_text.setGravity(Gravity.CENTER);
    holder.about_text.setTextSize(14);
    holder.scroll = new ScrollView(context);
    holder.scroll.addView(holder.about_text);
    layout.addView(holder.scroll);

    try{
      PackageInfo package_info = context.getPackageManager().getPackageInfo(component.getPackageName(), 0);
      if (package_info != null && package_info.applicationInfo != null) {
        if (package_info.applicationInfo.name != null) 
          holder.title_text.setText(package_info.applicationInfo.name);
        if (package_info.versionName != null)
          holder.version_text.setText("v"+package_info.versionName);
      }
    } catch (android.content.pm.PackageManager.NameNotFoundException e) {
    }


    mDialog = new AlertDialog.Builder(context)
      .setTitle(title_res)
      .setView(layout)
      .setPositiveButton(android.R.string.ok, null)
      .setNegativeButton(android.R.string.cancel, null)
      .show();
  }

}
