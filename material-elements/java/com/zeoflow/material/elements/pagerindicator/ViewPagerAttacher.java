package com.zeoflow.material.elements.pagerindicator;

import android.database.DataSetObserver;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * @author Nikita Olifer
 */
public class ViewPagerAttacher extends AbstractViewPagerAttacher<ViewPager> {

    private DataSetObserver dataSetObserver;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    private ViewPager pager;
    private PagerAdapter attachedAdapter;

    @Override
    public void attachToPager(@NonNull final ScrollingPagerIndicator indicator, @NonNull final ViewPager pager) {
        attachedAdapter = pager.getAdapter();
        if (attachedAdapter == null) {
            throw new IllegalStateException("Set adapter before call attachToPager() method");
        }

        this.pager = pager;

        updateIndicatorDotsAndPosition(indicator);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                indicator.reattach();
            }

            @Override
            public void onInvalidated() {
                onChanged();
            }
        };
        attachedAdapter.registerDataSetObserver(dataSetObserver);

        onPageChangeListener = new ViewPager.OnPageChangeListener() {

            boolean idleState = true;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixel) {
                updateIndicatorOnPagerScrolled(indicator, position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                if (idleState) {
                    updateIndicatorDotsAndPosition(indicator);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                idleState = state == ViewPager.SCROLL_STATE_IDLE;
            }
        };
        pager.addOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public void detachFromPager() {
        attachedAdapter.unregisterDataSetObserver(dataSetObserver);
        pager.removeOnPageChangeListener(onPageChangeListener);
    }

    private void updateIndicatorDotsAndPosition(ScrollingPagerIndicator indicator) {
        indicator.setDotCount(attachedAdapter.getCount());
        indicator.setCurrentPosition(pager.getCurrentItem());
    }
}
