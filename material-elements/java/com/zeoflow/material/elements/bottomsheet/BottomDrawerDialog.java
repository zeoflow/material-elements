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

package com.zeoflow.material.elements.bottomsheet;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.bottomsheet.utils.BottomDrawerDelegate;

public class BottomDrawerDialog extends AppCompatDialog implements BottomDialog
{

    BottomDrawerDelegate bottomDrawerDelegate;
    @StyleRes
    int theme;
    int backgroundColor;
    float sideMargins;
    float cornerRadius;
    DialogFragment dialogFragment;
    boolean autoStatusBarColor;
    float peekRatio;

    public BottomDrawerDialog(Context context)
    {
        this(context, R.style.BottomDialogTheme);
    }

    public BottomDrawerDialog(Context context, int theme)
    {
        super(context, theme);
        this.theme = theme;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDrawerDelegate = new BottomDrawerDelegate(this.getContext(), this);
        bottomDrawerDelegate.setListener(() -> {
            getDrawer().changeBackgroundColor(backgroundColor);
            getDrawer().changeCornerRadius(cornerRadius);
            getDrawer().changeSideMargins((int) sideMargins);
            getDrawer().setAutoStatusBar(autoStatusBarColor, dialogFragment);
        });
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    public BottomDrawer getDrawer()
    {
        return bottomDrawerDelegate.getDrawer();
    }

    public BottomSheetBehavior<BottomDrawer> getBehavior()
    {
        return bottomDrawerDelegate.getBehavior();
    }

    @Override
    public void setContentView(View view)
    {
        super.setContentView(bottomDrawerDelegate.wrapInBottomSheet(0, view, null));
    }

    @Override
    public void setContentView(int layoutResID)
    {
        super.setContentView(bottomDrawerDelegate.wrapInBottomSheet(layoutResID, null, null));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params)
    {
        super.setContentView(bottomDrawerDelegate.wrapInBottomSheet(0, view, params));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        bottomDrawerDelegate.open();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        bottomDrawerDelegate.onBackPressed();
    }

    @Override
    public void onDismiss()
    {
        dismiss();
    }

    @Override
    public void onCancel()
    {
        cancel();
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState()
    {
        Bundle superState = super.onSaveInstanceState();
        bottomDrawerDelegate.onSaveInstanceState(superState);
        return superState;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        bottomDrawerDelegate.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static class Builder
    {

        int theme = R.style.BottomDialogTheme;
        View handleView;
        boolean isCancelableOnTouchOutside = true;
        int backgroundColor = Color.parseColor("#ffffff");
        float sideMargins = -1;
        float cornerRadius = -1;
        DialogFragment dialogFragment;
        boolean autoStatusBarColor = false;
        float peekRatio;

        public Builder setTheme(int theme)
        {
            this.theme = theme;
            return this;
        }

        public Builder setHandleView(View handleView)
        {
            this.handleView = handleView;
            return this;
        }

        public Builder setBackgroundColor(String backgroundColor)
        {
            this.backgroundColor = Color.parseColor(backgroundColor);
            return this;
        }

        public Builder setCornerRadius(float cornerRadius)
        {
            this.cornerRadius = cornerRadius;
            return this;
        }
        public Builder setSideMargins(float sideMargins)
        {
            this.sideMargins = sideMargins;
            return this;
        }
        public Builder setPeekRatio(float peekRatio)
        {
            this.peekRatio = peekRatio;
            return this;
        }
        public Builder withAutoStatusBarColor(DialogFragment dialogFragment)
        {
            this.dialogFragment = dialogFragment;
            this.autoStatusBarColor = true;
            return this;
        }
        public Builder setBackgroundColor(@ColorInt int backgroundColor)
        {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setCancelableOnTouchOutside(boolean cancelableOnTouchOutside)
        {
            isCancelableOnTouchOutside = cancelableOnTouchOutside;
            return this;
        }

        public BottomDrawerDialog build(Context context)
        {
            if (sideMargins == -1)
            {
                sideMargins = context.getResources().getDimension(R.dimen.bottom_sheet_default_side_margins);
            }
            if (cornerRadius == -1)
            {
                cornerRadius = context.getResources().getDimension(R.dimen.minimized_sheet_top_radius);
            }
            BottomDrawerDialog drawerDialog = new BottomDrawerDialog(context, theme);
            drawerDialog.bottomDrawerDelegate.setCancelableOnTouchOutside(isCancelableOnTouchOutside);
            drawerDialog.bottomDrawerDelegate.setHandleView(handleView);
            drawerDialog.backgroundColor = backgroundColor;
            drawerDialog.sideMargins = sideMargins;
            drawerDialog.cornerRadius = cornerRadius;
            drawerDialog.autoStatusBarColor = autoStatusBarColor;
            drawerDialog.dialogFragment = dialogFragment;
            drawerDialog.peekRatio = peekRatio;
            return drawerDialog;
        }

    }

}
