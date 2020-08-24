

package com.zeoflow.material.elements.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.text.TextPaint;
import com.zeoflow.material.elements.resources.TextAppearance;
import com.zeoflow.material.elements.resources.TextAppearanceFontCallback;

import java.lang.ref.WeakReference;


@RestrictTo(LIBRARY_GROUP)
public class TextDrawableHelper {

  private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

  private final TextAppearanceFontCallback fontCallback =
      new TextAppearanceFontCallback() {
        @Override
        public void onFontRetrieved(@NonNull Typeface typeface, boolean fontResolvedSynchronously) {
          if (fontResolvedSynchronously) {
            return;
          }
          textWidthDirty = true;
          TextDrawableDelegate textDrawableDelegate = delegate.get();
          if (textDrawableDelegate != null) {
            textDrawableDelegate.onTextSizeChange();
          }
        }

        @Override
        public void onFontRetrievalFailed(int reason) {
          textWidthDirty = true;
          
          TextDrawableDelegate textDrawableDelegate = delegate.get();
          if (textDrawableDelegate != null) {
            textDrawableDelegate.onTextSizeChange();
          }
        }
      };

  private float textWidth;
  private boolean textWidthDirty = true;
  @Nullable private WeakReference<TextDrawableDelegate> delegate = new WeakReference<>(null);
  @Nullable private TextAppearance textAppearance;

  
  public TextDrawableHelper(@Nullable TextDrawableDelegate delegate) {
    setDelegate(delegate);
  }

  
  public void setDelegate(@Nullable TextDrawableDelegate delegate) {
    this.delegate = new WeakReference<>(delegate);
  }

  @NonNull
  public TextPaint getTextPaint() {
    return textPaint;
  }

  public void setTextWidthDirty(boolean dirty) {
    textWidthDirty = dirty;
  }

  public boolean isTextWidthDirty() {
    return textWidthDirty;
  }

  
  public float getTextWidth(String text) {
    if (!textWidthDirty) {
      return textWidth;
    }

    textWidth = calculateTextWidth(text);
    textWidthDirty = false;
    return textWidth;
  }

  private float calculateTextWidth(@Nullable CharSequence charSequence) {
    if (charSequence == null) {
      return 0f;
    }
    return textPaint.measureText(charSequence, 0, charSequence.length());
  }

  
  @Nullable
  public TextAppearance getTextAppearance() {
    return textAppearance;
  }

  
  public void setTextAppearance(@Nullable TextAppearance textAppearance, Context context) {
    if (this.textAppearance != textAppearance) {
      this.textAppearance = textAppearance;
      if (textAppearance != null) {
        textAppearance.updateMeasureState(context, textPaint, fontCallback);

        TextDrawableDelegate textDrawableDelegate = delegate.get();
        if (textDrawableDelegate != null) {
          textPaint.drawableState = textDrawableDelegate.getState();
        }
        textAppearance.updateDrawState(context, textPaint, fontCallback);
        textWidthDirty = true;
      }

      TextDrawableDelegate textDrawableDelegate = delegate.get();
      if (textDrawableDelegate != null) {
        textDrawableDelegate.onTextSizeChange();
        textDrawableDelegate.onStateChange(textDrawableDelegate.getState());
      }
    }
  }

  public void updateTextPaintDrawState(Context context) {
    textAppearance.updateDrawState(context, textPaint, fontCallback);
  }

  
  public interface TextDrawableDelegate {
    
    @NonNull
    int[] getState();

    
    void onTextSizeChange();

    
    boolean onStateChange(int[] state);
  }
}
