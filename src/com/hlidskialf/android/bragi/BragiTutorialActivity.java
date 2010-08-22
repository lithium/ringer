package com.hlidskialf.android.bragi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BragiTutorialActivity extends Activity
        implements View.OnClickListener
{
  private ViewHolder mViewHolder;
  private Page[] mPages;
  private int mCurPage=-1;

  private class ViewHolder
  {
    Button button_next;
    Button button_back;
    Button button_skip;
    TextView title;
    TextView content;
  }

  private class Page
  {
    String title;
    String content;
    public Page(int title, int content) { 
      this.title = getString(title); 
      this.content = getString(content); 
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.tutorial);
    mViewHolder = new ViewHolder();
    mViewHolder.button_next = (Button)findViewById(android.R.id.button1);
    mViewHolder.button_next.setOnClickListener(this);
    mViewHolder.button_back = (Button)findViewById(android.R.id.button2);
    mViewHolder.button_back.setOnClickListener(this);
    mViewHolder.button_skip = (Button)findViewById(android.R.id.button3);
    mViewHolder.button_skip.setOnClickListener(this);
    mViewHolder.title = (TextView)findViewById(android.R.id.title);
    mViewHolder.content = (TextView)findViewById(android.R.id.text1);

    mPages = new Page[3];
    mPages[0] = new Page(R.string.tutorial_page1_title, R.string.tutorial_page1_content);
    mPages[1] = new Page(R.string.tutorial_page2_title, R.string.tutorial_page2_content);
    mPages[2] = new Page(R.string.tutorial_page3_title, R.string.tutorial_page3_content);

    show_page(0);
  }

  private void show_page(int page)
  {
    mCurPage = page;
    mViewHolder.title.setText( mPages[mCurPage].title );
    mViewHolder.content.setText( mPages[mCurPage].content );

    if (mCurPage == 0) {
      mViewHolder.button_back.setVisibility(View.GONE);
    }
    else
    if (mCurPage > 0) {
      mViewHolder.button_back.setVisibility(View.VISIBLE);
    }

    if (mCurPage == mPages.length-1) {
      mViewHolder.button_next.setText( R.string.done );
    }
    else {
      mViewHolder.button_next.setText( R.string.next );
    }
  }
  private void next_page()
  {
    if (mCurPage+1 >= mPages.length) {
      finish();
    }
    else {
      show_page(mCurPage+1);
    }
  }
  private void back_page()
  {
    if (mCurPage > 0) {
      show_page(mCurPage-1);
    }
  }

  /* View.OnClickListener */
  public void onClick(View v) 
  {
    if (v.equals(mViewHolder.button_skip)) 
    {
      finish();
    }
    else
    if (v.equals(mViewHolder.button_next)) 
    {
      next_page();
    }
    else
    if (v.equals(mViewHolder.button_back)) 
    {
      back_page();
    }
  }
}
