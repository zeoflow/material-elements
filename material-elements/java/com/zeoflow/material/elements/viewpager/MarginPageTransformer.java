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

package com.zeoflow.material.elements.viewpager;

import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.core.util.Preconditions;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adds space between pages via the {@link MaterialViewPager.PageTransformer} API.
 * <p>
 * Internally relies on {@link View#setTranslationX} and {@link View#setTranslationY}.
 * <p>
 * Note: translations on pages are not reset when this adapter is changed for another one, so you
 * might want to set them manually to 0 when dynamically switching to another transformer, or
 * when switching MaterialViewPager orientation.
 *
 * @see MaterialViewPager#setPageTransformer
 * @see CompositePageTransformer
 */
public final class MarginPageTransformer implements MaterialViewPager.PageTransformer {
    private final int mMarginPx;

    /**
     * Creates a {@link MarginPageTransformer}.
     *
     * @param marginPx non-negative margin
     */
    public MarginPageTransformer(@Px int marginPx) {
        Preconditions.checkArgumentNonnegative(marginPx, "Margin must be non-negative");
        mMarginPx = marginPx;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        MaterialViewPager viewPager = requireViewPager(page);

        float offset = mMarginPx * position;

        if (viewPager.getOrientation() == MaterialViewPager.ORIENTATION_HORIZONTAL) {
            page.setTranslationX(viewPager.isRtl() ? -offset : offset);
        } else {
            page.setTranslationY(offset);
        }
    }

    private MaterialViewPager requireViewPager(@NonNull View page) {
        ViewParent parent = page.getParent();
        ViewParent parentParent = parent.getParent();

        if (parent instanceof RecyclerView && parentParent instanceof MaterialViewPager) {
            return (MaterialViewPager) parentParent;
        }

        throw new IllegalStateException(
                "Expected the page view to be managed by a MaterialViewPager instance.");
    }
}
