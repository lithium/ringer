package com.hlidskialf.android.bragi;


import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BragiChooserActivity extends ListActivity
                implements View.OnClickListener,
                           Bragi.ActivateProfileTask.CompleteListener 
{

    private class ChooserAdapter extends CursorAdapter
    {
        private class ViewHolder {
          ImageView icon;
          TextView text;
        }
        private LayoutInflater mInflater;
        private int mColIdx_id;
        private int mColIdx_icon;
        private int mColIdx_name;

        public ChooserAdapter(Context context, Cursor c)
        {
          super(context,c);
          mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          mColIdx_id = c.getColumnIndex(BragiDatabase.ProfileColumns._ID);
          mColIdx_icon = c.getColumnIndex(BragiDatabase.ProfileColumns.ICON);
          mColIdx_name = c.getColumnIndex(BragiDatabase.ProfileColumns.NAME);
        }

        public void bindView (View view, Context context, Cursor cursor)
        {
          ViewHolder holder = (ViewHolder)view.getTag();

          final long profile_id = cursor.getLong(mColIdx_id);

          final byte[] blob = cursor.getBlob(mColIdx_icon);
          if (blob != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            holder.icon.setImageBitmap(bmp);
          }
          else {
            holder.icon.setImageResource(R.drawable.ic_launcher_ringer);
          }

          final String name = cursor.getString(mColIdx_name);
          holder.text.setText(name);
        }
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            View view = mInflater.inflate(R.layout.chooser_list_item, null);
            ViewHolder holder = new ViewHolder();
            holder.text = (TextView)view.findViewById(android.R.id.text1);
            holder.icon = (ImageView)view.findViewById(android.R.id.icon1);
            view.setTag(holder);
            return view;
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageView v;
        v = (ImageView)findViewById(R.id.actionbar_logo);
        v.setImageResource(R.drawable.ic_actiontitle_ringer);
        v.setOnClickListener(this);

        BragiDatabase mDbHelper = new BragiDatabase(this);
        Cursor mCursor = mDbHelper.getAllProfiles();

        setListAdapter(new ChooserAdapter(this, mCursor));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
      Bragi.activateProfile(this, getContentResolver(), id, this);
    }

    /*View.OnClickListener*/
    public void onClick(View v) 
    {
      if (v.getId() == R.id.actionbar_logo) {
        Intent intent = new Intent(this, BragiActivity.class);
        startActivity(intent);
        finish();
      }
    }
    /*Bragi.ActivateProfileTask.CompleteListener*/
    public void onComplete(boolean result)
    {
      finish();
    }
}
