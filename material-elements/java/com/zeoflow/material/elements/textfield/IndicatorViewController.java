

package com.zeoflow.material.elements.textfield;

import com.google.android.material.R;

import static android.view.View.TRANSLATION_Y;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zeoflow.material.elements.animation.AnimationUtils;
import com.zeoflow.material.elements.animation.AnimatorSetCompat;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


final class IndicatorViewController {

  
  private static final int CAPTION_TRANSLATE_Y_ANIMATION_DURATION = 217;

  
  private static final int CAPTION_OPACITY_FADE_ANIMATION_DURATION = 167;

  
  @IntDef({COUNTER_INDEX, ERROR_INDEX, HELPER_INDEX})
  @Retention(RetentionPolicy.SOURCE)
  private @interface IndicatorIndex {}

  static final int ERROR_INDEX = 0;
  static final int HELPER_INDEX = 1;
  static final int COUNTER_INDEX = 2;

  
  @IntDef({CAPTION_STATE_NONE, CAPTION_STATE_ERROR, CAPTION_STATE_HELPER_TEXT})
  @Retention(RetentionPolicy.SOURCE)
  private @interface CaptionDisplayState {}

  private static final int CAPTION_STATE_NONE = 0;
  private static final int CAPTION_STATE_ERROR = 1;
  private static final int CAPTION_STATE_HELPER_TEXT = 2;

  private final Context context;
  @NonNull private final TextInputLayout textInputView;

  private LinearLayout indicatorArea;
  private int indicatorsAdded;

  private FrameLayout captionArea;
  private int captionViewsAdded;
  @Nullable private Animator captionAnimator;
  private final float captionTranslationYPx;
  private int captionDisplayed;
  private int captionToShow;

  @Nullable private CharSequence errorText;
  private boolean errorEnabled;
  @Nullable private TextView errorView;
  @Nullable private CharSequence errorViewContentDescription;
  private int errorTextAppearance;
  @Nullable private ColorStateList errorViewTextColor;

  private CharSequence helperText;
  private boolean helperTextEnabled;
  @Nullable private TextView helperTextView;
  private int helperTextTextAppearance;
  @Nullable private ColorStateList helperTextViewTextColor;

  private Typeface typeface;

  public IndicatorViewController(@NonNull TextInputLayout textInputView) {
    this.context = textInputView.getContext();
    this.textInputView = textInputView;
    this.captionTranslationYPx =
        context.getResources().getDimensionPixelSize(R.dimen.design_textinput_caption_translate_y);
  }

  void showHelper(final CharSequence helperText) {
    cancelCaptionAnimator();
    this.helperText = helperText;
    helperTextView.setText(helperText);

    
    if (captionDisplayed != CAPTION_STATE_HELPER_TEXT) {
      captionToShow = CAPTION_STATE_HELPER_TEXT;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(helperTextView, helperText));
  }

  void hideHelperText() {
    cancelCaptionAnimator();

    
    if (captionDisplayed == CAPTION_STATE_HELPER_TEXT) {
      captionToShow = CAPTION_STATE_NONE;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(helperTextView, null));
  }

  void showError(final CharSequence errorText) {
    cancelCaptionAnimator();
    this.errorText = errorText;
    errorView.setText(errorText);

    
    if (captionDisplayed != CAPTION_STATE_ERROR) {
      captionToShow = CAPTION_STATE_ERROR;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(errorView, errorText));
  }

  void hideError() {
    errorText = null;
    cancelCaptionAnimator();
    
    if (captionDisplayed == CAPTION_STATE_ERROR) {
      
      if (helperTextEnabled && !TextUtils.isEmpty(helperText)) {
        captionToShow = CAPTION_STATE_HELPER_TEXT;
      } else {
        
        captionToShow = CAPTION_STATE_NONE;
      }
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(errorView, null));
  }

  
  private boolean shouldAnimateCaptionView(
      @Nullable TextView captionView, @Nullable final CharSequence captionText) {
    return ViewCompat.isLaidOut(textInputView)
        && textInputView.isEnabled()
        && (captionToShow != captionDisplayed
            || captionView == null
            || !TextUtils.equals(captionView.getText(), captionText));
  }

  private void updateCaptionViewsVisibility(
      final @CaptionDisplayState int captionToHide,
      final @CaptionDisplayState int captionToShow,
      boolean animate) {

    if (captionToHide == captionToShow) {
      return;
    }

    if (animate) {
      final AnimatorSet captionAnimator = new AnimatorSet();
      this.captionAnimator = captionAnimator;
      List<Animator> captionAnimatorList = new ArrayList<>();

      createCaptionAnimators(
          captionAnimatorList,
          helperTextEnabled,
          helperTextView,
          CAPTION_STATE_HELPER_TEXT,
          captionToHide,
          captionToShow);

      createCaptionAnimators(
          captionAnimatorList,
          errorEnabled,
          errorView,
          CAPTION_STATE_ERROR,
          captionToHide,
          captionToShow);

      AnimatorSetCompat.playTogether(captionAnimator, captionAnimatorList);
      final TextView captionViewToHide = getCaptionViewFromDisplayState(captionToHide);
      final TextView captionViewToShow = getCaptionViewFromDisplayState(captionToShow);

      captionAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
              captionDisplayed = captionToShow;
              IndicatorViewController.this.captionAnimator = null;
              if (captionViewToHide != null) {
                captionViewToHide.setVisibility(View.INVISIBLE);
                if (captionToHide == CAPTION_STATE_ERROR && errorView != null) {
                  errorView.setText(null);
                }
              }

              if (captionViewToShow != null) {
                captionViewToShow.setTranslationY(0f);
                captionViewToShow.setAlpha(1f);
              }
            }

            @Override
            public void onAnimationStart(Animator animator) {
              if (captionViewToShow != null) {
                captionViewToShow.setVisibility(VISIBLE);
              }
            }
          });
      captionAnimator.start();
    } else {
      setCaptionViewVisibilities(captionToHide, captionToShow);
    }
    textInputView.updateEditTextBackground();
    textInputView.updateLabelState(animate);
    textInputView.updateTextInputBoxState();
  }

  private void setCaptionViewVisibilities(
      @CaptionDisplayState int captionToHide, @CaptionDisplayState int captionToShow) {
    if (captionToHide == captionToShow) {
      return;
    }

    if (captionToShow != CAPTION_STATE_NONE) {
      TextView captionViewToShow = getCaptionViewFromDisplayState(captionToShow);
      if (captionViewToShow != null) {
        captionViewToShow.setVisibility(VISIBLE);
        captionViewToShow.setAlpha(1f);
      }
    }

    if (captionToHide != CAPTION_STATE_NONE) {
      TextView captionViewDisplayed = getCaptionViewFromDisplayState(captionToHide);
      if (captionViewDisplayed != null) {
        captionViewDisplayed.setVisibility(View.INVISIBLE);
        
        if (captionToHide == CAPTION_STATE_ERROR) {
          captionViewDisplayed.setText(null);
        }
      }
    }
    captionDisplayed = captionToShow;
  }

  private void createCaptionAnimators(
      @NonNull List<Animator> captionAnimatorList,
      boolean captionEnabled,
      @Nullable TextView captionView,
      @CaptionDisplayState int captionState,
      @CaptionDisplayState int captionToHide,
      @CaptionDisplayState int captionToShow) {
    
    if (captionView == null || !captionEnabled) {
      return;
    }
    
    if ((captionState == captionToShow) || (captionState == captionToHide)) {
      captionAnimatorList.add(
          createCaptionOpacityAnimator(captionView, captionToShow == captionState));
      if (captionToShow == captionState) {
        captionAnimatorList.add(createCaptionTranslationYAnimator(captionView));
      }
    }
  }

  private ObjectAnimator createCaptionOpacityAnimator(TextView captionView, boolean display) {
    float endValue = display ? 1f : 0f;
    ObjectAnimator opacityAnimator = ObjectAnimator.ofFloat(captionView, View.ALPHA, endValue);
    opacityAnimator.setDuration(CAPTION_OPACITY_FADE_ANIMATION_DURATION);
    opacityAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    return opacityAnimator;
  }

  private ObjectAnimator createCaptionTranslationYAnimator(TextView captionView) {
    ObjectAnimator translationYAnimator =
        ObjectAnimator.ofFloat(captionView, TRANSLATION_Y, -captionTranslationYPx, 0f);
    translationYAnimator.setDuration(CAPTION_TRANSLATE_Y_ANIMATION_DURATION);
    translationYAnimator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    return translationYAnimator;
  }

  void cancelCaptionAnimator() {
    if (captionAnimator != null) {
      captionAnimator.cancel();
    }
  }

  boolean isCaptionView(@IndicatorIndex int index) {
    return index == ERROR_INDEX || index == HELPER_INDEX;
  }

  @Nullable
  private TextView getCaptionViewFromDisplayState(@CaptionDisplayState int captionDisplayState) {
    switch (captionDisplayState) {
      case CAPTION_STATE_ERROR:
        return errorView;
      case CAPTION_STATE_HELPER_TEXT:
        return helperTextView;
      case CAPTION_STATE_NONE:
      default: 
    }
    return null;
  }

  void adjustIndicatorPadding() {
    if (canAdjustIndicatorPadding()) {
      
      ViewCompat.setPaddingRelative(
          indicatorArea,
          ViewCompat.getPaddingStart(textInputView.getEditText()),
          0,
          ViewCompat.getPaddingEnd(textInputView.getEditText()),
          0);
    }
  }

  private boolean canAdjustIndicatorPadding() {
    return indicatorArea != null && textInputView.getEditText() != null;
  }

  void addIndicator(TextView indicator, @IndicatorIndex int index) {
    if (indicatorArea == null && captionArea == null) {
      indicatorArea = new LinearLayout(context);
      indicatorArea.setOrientation(LinearLayout.HORIZONTAL);
      textInputView.addView(indicatorArea, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

      captionArea = new FrameLayout(context);
      LinearLayout.LayoutParams captionAreaLp =
          new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
      indicatorArea.addView(captionArea, captionAreaLp);

      if (textInputView.getEditText() != null) {
        adjustIndicatorPadding();
      }
    }

    if (isCaptionView(index)) {
      captionArea.setVisibility(VISIBLE);
      captionArea.addView(indicator);
      captionViewsAdded++;
    } else {
      LinearLayout.LayoutParams indicatorAreaLp =
          new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      indicatorArea.addView(indicator, indicatorAreaLp);
    }
    indicatorArea.setVisibility(VISIBLE);
    indicatorsAdded++;
  }

  void removeIndicator(TextView indicator, @IndicatorIndex int index) {
    if (indicatorArea == null) {
      return;
    }

    if (isCaptionView(index) && captionArea != null) {
      captionViewsAdded--;
      setViewGroupGoneIfEmpty(captionArea, captionViewsAdded);
      captionArea.removeView(indicator);
    } else {
      indicatorArea.removeView(indicator);
    }
    indicatorsAdded--;
    setViewGroupGoneIfEmpty(indicatorArea, indicatorsAdded);
  }

  private void setViewGroupGoneIfEmpty(@NonNull ViewGroup viewGroup, int indicatorsAdded) {
    if (indicatorsAdded == 0) {
      viewGroup.setVisibility(View.GONE);
    }
  }

  void setErrorEnabled(boolean enabled) {
    
    if (errorEnabled == enabled) {
      return;
    }

    
    cancelCaptionAnimator();

    if (enabled) {
      errorView = new AppCompatTextView(context);
      errorView.setId(R.id.textinput_error);
      if (VERSION.SDK_INT >= 17) {
        errorView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
      }
      if (typeface != null) {
        errorView.setTypeface(typeface);
      }
      setErrorTextAppearance(errorTextAppearance);
      setErrorViewTextColor(errorViewTextColor);
      setErrorContentDescription(errorViewContentDescription);
      errorView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(errorView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      addIndicator(errorView, ERROR_INDEX);
    } else {
      hideError();
      removeIndicator(errorView, ERROR_INDEX);
      errorView = null;
      textInputView.updateEditTextBackground();
      textInputView.updateTextInputBoxState();
    }
    errorEnabled = enabled;
  }

  boolean isErrorEnabled() {
    return errorEnabled;
  }

  boolean isHelperTextEnabled() {
    return helperTextEnabled;
  }

  void setHelperTextEnabled(boolean enabled) {
    
    if (helperTextEnabled == enabled) {
      return;
    }

    
    cancelCaptionAnimator();

    if (enabled) {
      helperTextView = new AppCompatTextView(context);
      helperTextView.setId(R.id.textinput_helper_text);
      if (VERSION.SDK_INT >= 17) {
        helperTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
      }
      if (typeface != null) {
        helperTextView.setTypeface(typeface);
      }
      helperTextView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(
          helperTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      setHelperTextAppearance(helperTextTextAppearance);
      setHelperTextViewTextColor(helperTextViewTextColor);
      addIndicator(helperTextView, HELPER_INDEX);
    } else {
      hideHelperText();
      removeIndicator(helperTextView, HELPER_INDEX);
      helperTextView = null;
      textInputView.updateEditTextBackground();
      textInputView.updateTextInputBoxState();
    }
    helperTextEnabled = enabled;
  }

  boolean errorIsDisplayed() {
    return isCaptionStateError(captionDisplayed);
  }

  boolean errorShouldBeShown() {
    return isCaptionStateError(captionToShow);
  }

  private boolean isCaptionStateError(@CaptionDisplayState int captionState) {
    return captionState == CAPTION_STATE_ERROR
        && errorView != null
        && !TextUtils.isEmpty(errorText);
  }

  boolean helperTextIsDisplayed() {
    return isCaptionStateHelperText(captionDisplayed);
  }

  boolean helperTextShouldBeShown() {
    return isCaptionStateHelperText(captionToShow);
  }

  private boolean isCaptionStateHelperText(@CaptionDisplayState int captionState) {
    return captionState == CAPTION_STATE_HELPER_TEXT
        && helperTextView != null
        && !TextUtils.isEmpty(helperText);
  }

  @Nullable
  CharSequence getErrorText() {
    return errorText;
  }

  CharSequence getHelperText() {
    return helperText;
  }

  @SuppressWarnings("ReferenceEquality") 
  void setTypefaces(Typeface typeface) {
    if (typeface != this.typeface) {
      this.typeface = typeface;
      setTextViewTypeface(errorView, typeface);
      setTextViewTypeface(helperTextView, typeface);
    }
  }

  private void setTextViewTypeface(@Nullable TextView captionView, Typeface typeface) {
    if (captionView != null) {
      captionView.setTypeface(typeface);
    }
  }

  @ColorInt
  int getErrorViewCurrentTextColor() {
    return errorView != null ? errorView.getCurrentTextColor() : -1;
  }

  @Nullable
  ColorStateList getErrorViewTextColors() {
    return errorView != null ? errorView.getTextColors() : null;
  }

  void setErrorViewTextColor(@Nullable ColorStateList errorViewTextColor) {
    this.errorViewTextColor = errorViewTextColor;
    if (errorView != null && errorViewTextColor != null) {
      errorView.setTextColor(errorViewTextColor);
    }
  }

  void setErrorTextAppearance(@StyleRes int resId) {
    this.errorTextAppearance = resId;
    if (errorView != null) {
      textInputView.setTextAppearanceCompatWithErrorFallback(errorView, resId);
    }
  }

  void setErrorContentDescription(@Nullable final CharSequence errorContentDescription) {
    this.errorViewContentDescription = errorContentDescription;
    if (errorView != null) {
      errorView.setContentDescription(errorContentDescription);
    }
  }

  @Nullable
  CharSequence getErrorContentDescription() {
    return errorViewContentDescription;
  }

  @ColorInt
  int getHelperTextViewCurrentTextColor() {
    return helperTextView != null ? helperTextView.getCurrentTextColor() : -1;
  }

  @Nullable
  ColorStateList getHelperTextViewColors() {
    return helperTextView != null ? helperTextView.getTextColors() : null;
  }

  void setHelperTextViewTextColor(@Nullable ColorStateList helperTextViewTextColor) {
    this.helperTextViewTextColor = helperTextViewTextColor;
    if (helperTextView != null && helperTextViewTextColor != null) {
      helperTextView.setTextColor(helperTextViewTextColor);
    }
  }

  void setHelperTextAppearance(@StyleRes int resId) {
    this.helperTextTextAppearance = resId;
    if (helperTextView != null) {
      TextViewCompat.setTextAppearance(helperTextView, resId);
    }
  }
}
