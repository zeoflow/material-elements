package com.zeoflow.material.elements.sample;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zeoflow.material.elements.bottomsheet.BottomDrawerDialog;
import com.zeoflow.material.elements.bottomsheet.BottomDrawerFragment;
import com.zeoflow.material.elements.bottomsheet.BottomSheetBehavior;
import com.zeoflow.material.elements.bottomsheet.BottomSheetCallback;
import com.zeoflow.material.elements.bottomsheet.handle.PlainHandleView;
import com.zeoflow.utils.ContentCompat;

public class BottomDialog extends BottomDrawerFragment {

    private float alphaCancelButton = 0f;
    private ImageView cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_layout, container, false);
        cancelButton = view.findViewById(R.id.cancel);
        final float percent = 0.65f;
        setBottomSheetListener(new BottomSheetCallback.IOnBottomSheet() {
            @Override
            public void onSlide(View view, float slideOffset) {
                float alphaTemp = (slideOffset - percent) * (1f / (1f - percent));
                alphaCancelButton = (alphaTemp >= 0) ? alphaTemp : 0f;
                cancelButton.setAlpha(alphaCancelButton);
                cancelButton.setEnabled(true);
                setStatusBarLightText(slideOffset > 0.975f);
            }

            @Override
            public void onStateChanged(View view, int state) {

            }
        });
        cancelButton.setOnClickListener(v -> dismissWithBehavior());

        return view;
    }

    @Override
    public BottomDrawerDialog configureBottomDrawer() {
        PlainHandleView handleView = new PlainHandleView(getContext());
        int widthHandle = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_handle_width);
        int heightHandle = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_handle_height);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(widthHandle, heightHandle, Gravity.CENTER_HORIZONTAL);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_handle_top_margin);
        handleView.setLayoutParams(params);
        return new BottomDrawerDialog.Builder()
                .setCancelableOnTouchOutside(false)
                .setTheme(R.style.Plain)
                .setBackgroundColor(ContentCompat.getColor(R.color.colorBackground))
                .setHandleView(handleView)
                .setCornerRadius(30)
                .setSideMargins(30)
                .colouredStatusBar(
                        getActivity(),
                        false,
                        false
                )
                .build(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBottomSheetBehaviour().addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("alphaCancelButton", alphaCancelButton);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        alphaCancelButton = savedInstanceState != null ? savedInstanceState.getFloat("alphaCancelButton") : 0f;
        cancelButton.setAlpha(alphaCancelButton);
        cancelButton.setEnabled(alphaCancelButton > 0);
    }
}
