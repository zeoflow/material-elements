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

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.zeoflow.material.elements.shape.CornerFamily;

public abstract class BottomDrawerFragment extends DialogFragment implements ViewTreeObserver.OnGlobalLayoutListener {

    private BottomDrawerDialog bottomDrawerDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomDrawerDialog dialog = configureBottomDrawer();
        bottomDrawerDialog = dialog;
        return dialog;
    }

    public BottomDrawerDialog configureBottomDrawer() {
        return new BottomDrawerDialog(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bottomDrawerDialog != null) {
            BottomDrawer drawer = bottomDrawerDialog.getDrawer();
            if (drawer != null) {
                drawer.getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        }
        if (getDialog() != null) {
            getDialog().setOnDismissListener(dialog -> {
                if (isAdded()) {
                    dismissAllowingStateLoss();
                }
            });
        }
    }

    public void dismissWithBehavior() {
        bottomDrawerDialog.getBehavior().setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void expandWithBehavior() {
        bottomDrawerDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public int getCurrentState() {
        return bottomDrawerDialog.getBehavior().getState();
    }

    @Override
    public void onGlobalLayout() {
        bottomDrawerDialog.getDrawer().globalTranslationViews();
    }

    public void setBottomSheetListener(BottomSheetCallback.IOnBottomSheet listener) {
        bottomDrawerDialog.bottomDrawerDelegate.addBottomSheetCallback(listener);
    }

    public void changeCornerRadius(float radius) {
        bottomDrawerDialog.getDrawer().changeCornerRadius(radius);
    }

    public void changeBackgroundColor(int color) {
        bottomDrawerDialog.getDrawer().changeBackgroundColor(color);
    }

    public void changeExtraPadding(int extraPadding) {
        bottomDrawerDialog.getDrawer().changeExtraPadding(extraPadding);
    }

    public void changeTopCornerTreatment(@CornerFamily int cornerFamily) {
        bottomDrawerDialog.getDrawer().changeCornerTreatment(cornerFamily);
    }

    public void setStatusBarLightText(boolean isLight) {
        bottomDrawerDialog.setStatusBarLightText(isLight);
    }

    public BottomSheetBehavior<BottomDrawer> getBottomSheetBehaviour() {
        return bottomDrawerDialog.getBehavior();
    }

}
