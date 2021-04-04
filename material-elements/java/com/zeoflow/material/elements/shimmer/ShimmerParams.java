/*
 * Copyright (C) 2021 ZeoFlow SRL
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

package com.zeoflow.material.elements.shimmer;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Px;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class ShimmerParams
{

    public Drawable drawable;
    @Px
    public float radius;
    @FloatRange(from = 0.0, to = 1.0)
    public float baseAlpha;
    @FloatRange(from = 0.0, to = 1.0)
    public float highlightAlpha;
    public float dropOff;
    public boolean shimmerEnable;
    public Shimmer shimmer;
    public boolean defaultChildVisible;
    @ColorInt
    public int baseColor;
    @ColorInt
    public int highlightColor;

    public ShimmerParams()
    {

    }

}
