
package com.hlidskialf.android.bragi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class BragiWidgetProvider extends AppWidgetProvider 
{
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
  {
    Bragi.updateWidget(context,appWidgetManager,appWidgetIds);
  }
}
