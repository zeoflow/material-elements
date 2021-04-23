/*
 * Copyright (C) 2021 ZeoFlow
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

package com.zeoflow.material.elements.bottomsheet.handle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.bottomsheet.TranslationUpdater;

public class PullHandleView extends View implements TranslationUpdater
{

    @FloatRange(from = 0.0, to = 1.0)
    private float currentOffset = 0f;
    private final Paint paint = new Paint();
    private final float thickness = (float) getResources().getDimensionPixelSize(R.dimen.bottom_sheet_handle_thickness);

    public PullHandleView(Context context)
    {
        this(context, null);
    }

    public PullHandleView(Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PullHandleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        paint.setColor(ContextCompat.getColor(context, R.color.bottom_drawer_handle_view_color));
        paint.setStrokeWidth(thickness);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float radius = thickness / 2;

        int halfWidth = getWidth() / 2;
        int halfHeight = getHeight() / 2;

        float leftX = radius;
        float rightX = getWidth() - radius;
        float leftRightY = halfHeight - ((halfHeight - radius) * currentOffset);

        float centerX = halfWidth;
        float centerY = halfHeight + ((halfHeight - radius) * currentOffset);

        canvas.drawCircle(leftX, leftRightY, radius, paint);
        canvas.drawLine(leftX, leftRightY, centerX, centerY, paint);

        canvas.drawCircle(centerX, centerY, radius, paint);

        canvas.drawLine(centerX, centerY, rightX, leftRightY, paint);
        canvas.drawCircle(rightX, leftRightY, radius, paint);
    }

    @Override
    public void updateTranslation(float value)
    {
        if (value != currentOffset)
        {
            currentOffset = value;
            invalidate();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        HandleViewSavedState customViewSavedState = new HandleViewSavedState(superState);
        customViewSavedState.offset = currentOffset;
        superState = customViewSavedState;
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        Log.d("show_handle", "restore");
        HandleViewSavedState customViewSavedState = (HandleViewSavedState) state;
        currentOffset = customViewSavedState.offset;
        super.onRestoreInstanceState(state);
    }

    private class HandleViewSavedState extends BaseSavedState
    {

        private float offset = 0f;

        public HandleViewSavedState(Parcel source)
        {
            super(source);
            offset = source.readFloat();
        }

        public HandleViewSavedState(Parcelable superState)
        {
            super(superState);
        }
        public float getOffset()
        {
            return offset;
        }
        @Override
        public void writeToParcel(Parcel out, int flags)
        {
            super.writeToParcel(out, flags);
            out.writeFloat(offset);
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

    }

}
