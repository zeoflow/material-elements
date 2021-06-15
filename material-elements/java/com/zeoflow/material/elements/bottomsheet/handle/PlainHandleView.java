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
import android.graphics.RectF;
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

public class PlainHandleView extends View implements TranslationUpdater {

    @FloatRange(from = 0.0, to = 1.0)
    private float currentOffset = 0f;
    private final RectF rect = new RectF();
    private final RectF tempRect = new RectF();

    private final Paint paint = new Paint();
    private final float thickness = (float) getResources().getDimensionPixelSize(R.dimen.bottom_sheet_handle_thickness);

    public PlainHandleView(Context context) {
        this(context, null);
    }

    public PlainHandleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlainHandleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(ContextCompat.getColor(context, R.color.bottom_drawer_handle_view_color));
        paint.setStrokeWidth(thickness);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setSaveEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(tempRect, thickness, thickness, paint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        rect.set(left, top, right, bottom);
    }

    @Override
    public void updateTranslation(float value) {
        currentOffset = value;
        float offset = (getWidth() * currentOffset) / 2;
        tempRect.set(0 + offset, 0f, getWidth() - offset, getHeight());
        invalidate();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        PlainHandleViewSavedState customViewSavedState = new PlainHandleViewSavedState(superState);
        customViewSavedState.offset = currentOffset;
        superState = customViewSavedState;
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d("show_handle", "restore");
        PlainHandleViewSavedState customViewSavedState = (PlainHandleViewSavedState) state;
        currentOffset = customViewSavedState.offset;
        float offset = (getWidth() * currentOffset) / 2;
        tempRect.set(0 + offset, 0f, getWidth() - offset, getHeight());
        super.onRestoreInstanceState(state);
    }

    private static class PlainHandleViewSavedState extends BaseSavedState {

        private float offset = 0f;

        public PlainHandleViewSavedState(Parcel source) {
            super(source);
            offset = source.readFloat();
        }

        public PlainHandleViewSavedState(Parcelable superState) {
            super(superState);
        }

        public float getOffset() {
            return offset;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(offset);
        }

        @Override
        public int describeContents() {
            return 0;
        }

    }

}
