package com.zeoflow.material.elements.square;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class SquareView extends View
{

  public SquareView(Context context)
  {
    super(context);
  }

  public SquareView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public SquareView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    setMeasuredDimension(width, width);
  }

}
