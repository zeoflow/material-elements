/*
 * Copyright 2021 ZeoFlow SRL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
