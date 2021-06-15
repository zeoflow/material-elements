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

package com.zeoflow.material.elements.bottomsheet.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.zeoflow.material.elements.R;
import com.zeoflow.material.elements.bottomsheet.BottomDialog;
import com.zeoflow.material.elements.bottomsheet.BottomDrawer;
import com.zeoflow.material.elements.bottomsheet.BottomSheetBehavior;
import com.zeoflow.material.elements.bottomsheet.BottomSheetCallback;

import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class BottomDrawerDelegate extends BottomSheetCallback.IOnBottomSheet {

    private final Context context;
    private final BottomDialog dialog;
    private final CopyOnWriteArrayList<BottomSheetBehavior.BottomSheetCallback> callbacks = new CopyOnWriteArrayList<>();
    BottomSheetBehavior<BottomDrawer> behavior;
    BottomDrawer drawer;
    CoordinatorLayout coordinator;
    private float offset = 0f;
    private boolean isCancelableOnTouchOutside = true;
    private View handleView;
    private IOnDrawer listener;

    public BottomDrawerDelegate(Context context, BottomDialog dialog) {
        this.context = context;
        this.dialog = dialog;
    }

    public void setListener(IOnDrawer listener) {
        this.listener = listener;
    }

    public BottomDrawer getDrawer() {
        return drawer;
    }

    public BottomSheetBehavior<BottomDrawer> getBehavior() {
        return behavior;
    }

    public boolean isCancelableOnTouchOutside() {
        return isCancelableOnTouchOutside;
    }

    public void setCancelableOnTouchOutside(boolean cancelableOnTouchOutside) {
        isCancelableOnTouchOutside = cancelableOnTouchOutside;
    }

    public View getHandleView() {
        return handleView;
    }

    public void setHandleView(View handleView) {
        this.handleView = handleView;
    }

    @SuppressLint("ClickableViewAccessibility")
    public View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {
        View wrappedView = view;
        FrameLayout container = (FrameLayout) View.inflate(context, R.layout.bottom_drawer_layout, null);
        coordinator = container.findViewById(R.id.bottom_sheet_coordinator);
        if (layoutResId != 0 && wrappedView == null) {
            wrappedView = LayoutInflater.from(context).inflate(layoutResId, coordinator, false);
        }
        drawer = coordinator.findViewById(R.id.bottom_sheet_root);
        behavior = BottomSheetBehavior.from(drawer);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.setHideable(true);
        behavior.setHalfExpandedRatio(0.7f);

        if (params == null) {
            drawer.addView(wrappedView);
        } else {
            drawer.addView(wrappedView, params);
        }
        drawer.addHandleView(handleView);

        coordinator.getBackground().setAlpha((int) offset);

        if (behavior != null) {
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int state) {
                    for (BottomSheetBehavior.BottomSheetCallback callback : callbacks) {
                        callback.onStateChanged(bottomSheet, state);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    for (BottomSheetBehavior.BottomSheetCallback callback : callbacks) {
                        callback.onSlide(bottomSheet, slideOffset);
                    }
                }
            });
        }

        addBottomSheetCallback();

        coordinator.findViewById(R.id.touch_outside).setOnClickListener(v ->
        {
            if (isCancelableOnTouchOutside) {
                if (behavior != null) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        ViewCompat.setAccessibilityDelegate(drawer, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setDismissable(true);
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS) {
                    dialog.onCancel();
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
        drawer.setOnTouchListener((v, event) -> true);
        if (listener != null) {
            listener.onReady();
        }
        // TODO initial time colored status bar
        return container;
    }

    private void addBottomSheetCallback() {
        callbacks.add(new BottomSheetCallback(this));
    }

    public void addBottomSheetCallback(BottomSheetCallback.IOnBottomSheet listener) {
        callbacks.add(new BottomSheetCallback(listener));
    }

    @Override
    public void onSlide(View view, float slideOffset) {
        offset = (slideOffset != slideOffset) ? 0f : slideOffset;
        offset++;
        updateBackgroundOffset();
        drawer.onSlide(offset / 2f);
    }

    @Override
    public void onStateChanged(View view, int state) {
        if (state == BottomSheetBehavior.STATE_HIDDEN) {
            dialog.onDismiss();
        }
    }

    public void removeBottomSheetCallback(BottomSheetBehavior.BottomSheetCallback callback) {
        callbacks.remove(callback);
    }

    private void updateBackgroundOffset() {
        if (offset <= 1) {
            coordinator.getBackground().setAlpha((int) (255 * offset));
        } else {
            coordinator.getBackground().setAlpha(255);
        }
    }

    public void open() {
        if (behavior == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() ->
        {
            if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }, 50);
    }

    public void onBackPressed() {
        if (behavior == null) {
            return;
        }
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onSaveInstanceState(Bundle superState) {
        superState.putFloat("offset", offset);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        offset = savedInstanceState.getFloat("offset");
        updateBackgroundOffset();
    }

    public interface IOnDrawer {

        void onReady();

    }

}
