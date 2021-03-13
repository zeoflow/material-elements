package com.zeoflow.material.elements.pagerindicator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.zeoflow.material.elements.viewpager.MaterialViewPager;

@SuppressWarnings("UnusedDeclaration")
public class MaterialViewPagerAttacher extends AbstractViewPagerAttacher<MaterialViewPager>
{

  private RecyclerView.AdapterDataObserver dataSetObserver;
  private RecyclerView.Adapter attachedAdapter;
  private MaterialViewPager.OnPageChangeCallback onPageChangeListener;
  private MaterialViewPager pager;

  @Override
  public void attachToPager(@NonNull final ScrollingPagerIndicator indicator, @NonNull final MaterialViewPager pager)
  {
    attachedAdapter = pager.getAdapter();
    if (attachedAdapter == null)
    {
      throw new IllegalStateException("Set adapter before call attachToPager() method");
    }

    this.pager = pager;

    updateIndicatorDotsAndPosition(indicator);

    dataSetObserver = new RecyclerView.AdapterDataObserver()
    {
      @Override
      public void onChanged()
      {
        indicator.reattach();
      }
    };
    attachedAdapter.registerAdapterDataObserver(dataSetObserver);

    onPageChangeListener = new MaterialViewPager.OnPageChangeCallback()
    {

      boolean idleState = true;

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixel)
      {
        updateIndicatorOnPagerScrolled(indicator, position, positionOffset);
      }

      @Override
      public void onPageSelected(int position)
      {
        if (idleState)
        {
          updateIndicatorDotsAndPosition(indicator);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state)
      {
        idleState = state == MaterialViewPager.SCROLL_STATE_IDLE;
      }
    };

    pager.registerOnPageChangeCallback(onPageChangeListener);
  }

  @Override
  public void detachFromPager()
  {
    attachedAdapter.unregisterAdapterDataObserver(dataSetObserver);
    pager.unregisterOnPageChangeCallback(onPageChangeListener);
  }

  private void updateIndicatorDotsAndPosition(ScrollingPagerIndicator indicator)
  {
    indicator.setDotCount(attachedAdapter.getItemCount());
    indicator.setCurrentPosition(pager.getCurrentItem());
  }
}
