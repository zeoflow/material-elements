package com.zeoflow.material.elements.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zeoflow.R;

import java.util.WeakHashMap;

public class AutofitLayout extends FrameLayout
{

  private boolean mEnabled;
  private boolean mIsAutofitWidthEnabled;
  private boolean mIsAutofitHeightEnabled;
  private float mMinTextSize;
  private float mPrecision;
  private WeakHashMap<View, AutofitHelper> mHelpers = new WeakHashMap<>();

  public AutofitLayout(Context context)
  {
    super(context);
    init(context, null, 0);
  }

  public AutofitLayout(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public AutofitLayout(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(Context context, AttributeSet attrs, int defStyle)
  {
    boolean sizeToFit = true;
    boolean autofitWidthEnabled = true;
    boolean autofitHeightEnabled = false;
    int minTextSize = -1;
    float precision = -1;

    if (attrs != null)
    {
      @SuppressLint("CustomViewStyleable") TypedArray ta = context.obtainStyledAttributes(
          attrs,
          R.styleable.AutofitTextView,
          defStyle,
          0);
      sizeToFit = ta.getBoolean(R.styleable.AutofitTextView_sizeToFit, sizeToFit);
      minTextSize = ta.getDimensionPixelSize(R.styleable.AutofitTextView_minTextSize,
          minTextSize);
      autofitWidthEnabled = ta.getBoolean(R.styleable.AutofitTextView_autofitWidthEnabled,
          autofitWidthEnabled);
      autofitHeightEnabled = ta.getBoolean(R.styleable.AutofitTextView_autofitHeightEnabled,
          autofitHeightEnabled);
      precision = ta.getFloat(R.styleable.AutofitTextView_precision, precision);
      ta.recycle();
    }

    mIsAutofitWidthEnabled = autofitWidthEnabled;
    mIsAutofitHeightEnabled = autofitHeightEnabled;
    mEnabled = sizeToFit;
    mMinTextSize = minTextSize;
    mPrecision = precision;
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params)
  {
    super.addView(child, index, params);
    TextView textView = (TextView) child;
    AutofitHelper helper = AutofitHelper.create(textView);
    helper.setAutofitWidthEnabled(mIsAutofitWidthEnabled);
    helper.setAutofitHeightEnabled(mIsAutofitHeightEnabled);
    helper.setEnabled(mEnabled);
    if (mPrecision > 0)
    {
      helper.setPrecision(mPrecision);
    }
    if (mMinTextSize > 0)
    {
      helper.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, mMinTextSize);
    }
    mHelpers.put(textView, helper);
  }

  /**
   * Returns the {@link AutofitHelper} for this child View.
   */
  public AutofitHelper getAutofitHelper(TextView textView)
  {
    return mHelpers.get(textView);
  }

  /**
   * Returns the {@link AutofitHelper} for this child View.
   */
  public AutofitHelper getAutofitHelper(int index)
  {
    return mHelpers.get(getChildAt(index));
  }
}
