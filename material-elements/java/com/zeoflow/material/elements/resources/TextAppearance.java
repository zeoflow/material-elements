

package com.zeoflow.material.elements.resources;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.content.res.ResourcesCompat.FontCallback;
import androidx.core.provider.FontsContractCompat.FontRequestCallback;
import android.text.TextPaint;
import android.util.Log;


@RestrictTo(Scope.LIBRARY_GROUP)
public class TextAppearance {

  private static final String TAG = "TextAppearance";

  
  private static final int TYPEFACE_SANS = 1;
  private static final int TYPEFACE_SERIF = 2;
  private static final int TYPEFACE_MONOSPACE = 3;

  public final float textSize;
  @Nullable public final ColorStateList textColor;
  @Nullable public final ColorStateList textColorHint;
  @Nullable public final ColorStateList textColorLink;
  public final int textStyle;
  public final int typeface;
  @Nullable public final String fontFamily;
  public final boolean textAllCaps;
  @Nullable public final ColorStateList shadowColor;
  public final float shadowDx;
  public final float shadowDy;
  public final float shadowRadius;

  @FontRes private final int fontFamilyResourceId;

  private boolean fontResolved = false;
  private Typeface font;

  
  public TextAppearance(@NonNull Context context, @StyleRes int id) {
    TypedArray a = context.obtainStyledAttributes(id, R.styleable.TextAppearance);

    textSize = a.getDimension(R.styleable.TextAppearance_android_textSize, 0f);
    textColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColor);
    textColorHint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorHint);
    textColorLink =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorLink);
    textStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL);
    typeface = a.getInt(R.styleable.TextAppearance_android_typeface, TYPEFACE_SANS);
    int fontFamilyIndex =
        MaterialResources.getIndexWithValue(
            a,
            R.styleable.TextAppearance_fontFamily,
            R.styleable.TextAppearance_android_fontFamily);
    fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0);
    fontFamily = a.getString(fontFamilyIndex);
    textAllCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
    shadowColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_shadowColor);
    shadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0);
    shadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0);
    shadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0);

    a.recycle();
  }

  
  @VisibleForTesting
  @NonNull
  public Typeface getFont(@NonNull Context context) {
    if (fontResolved) {
      return font;
    }

    
    if (!context.isRestricted()) {
      try {
        font = ResourcesCompat.getFont(context, fontFamilyResourceId);
        if (font != null) {
          font = Typeface.create(font, textStyle);
        }
      } catch (UnsupportedOperationException | Resources.NotFoundException e) {
        
      } catch (Exception e) {
        Log.d(TAG, "Error loading font " + fontFamily, e);
      }
    }

    
    createFallbackFont();
    fontResolved = true;

    return font;
  }

  
  public void getFontAsync(
      @NonNull Context context, @NonNull final TextAppearanceFontCallback callback) {
    if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
      getFont(context);
    } else {
      
      createFallbackFont();
    }

    if (fontFamilyResourceId == 0) {
      
      fontResolved = true;
    }

    if (fontResolved) {
      callback.onFontRetrieved(font, true);
      return;
    }

    
    try {
      ResourcesCompat.getFont(
          context,
          fontFamilyResourceId,
          new FontCallback() {
            @Override
            public void onFontRetrieved(@NonNull Typeface typeface) {
              font = Typeface.create(typeface, textStyle);
              fontResolved = true;
              callback.onFontRetrieved(font, false);
            }

            @Override
            public void onFontRetrievalFailed(int reason) {
              fontResolved = true;
              callback.onFontRetrievalFailed(reason);
            }
          },
           null);
    } catch (Resources.NotFoundException e) {
      
      fontResolved = true;
      callback.onFontRetrievalFailed(FontRequestCallback.FAIL_REASON_FONT_NOT_FOUND);
    } catch (Exception e) {
      Log.d(TAG, "Error loading font " + fontFamily, e);
      fontResolved = true;
      callback.onFontRetrievalFailed(FontRequestCallback.FAIL_REASON_FONT_LOAD_ERROR);
    }
  }

  
  public void getFontAsync(
      @NonNull Context context,
      @NonNull final TextPaint textPaint,
      @NonNull final TextAppearanceFontCallback callback) {
    
    updateTextPaintMeasureState(textPaint, getFallbackFont());

    getFontAsync(
        context,
        new TextAppearanceFontCallback() {
          @Override
          public void onFontRetrieved(
              @NonNull Typeface typeface, boolean fontResolvedSynchronously) {
            updateTextPaintMeasureState(textPaint, typeface);
            callback.onFontRetrieved(typeface, fontResolvedSynchronously);
          }

          @Override
          public void onFontRetrievalFailed(int i) {
            callback.onFontRetrievalFailed(i);
          }
        });
  }

  
  public Typeface getFallbackFont() {
    createFallbackFont();
    return font;
  }

  private void createFallbackFont() {
    
    if (font == null && fontFamily != null) {
      font = Typeface.create(fontFamily, textStyle);
    }

    
    if (font == null) {
      switch (typeface) {
        case TYPEFACE_SANS:
          font = Typeface.SANS_SERIF;
          break;
        case TYPEFACE_SERIF:
          font = Typeface.SERIF;
          break;
        case TYPEFACE_MONOSPACE:
          font = Typeface.MONOSPACE;
          break;
        default:
          font = Typeface.DEFAULT;
          break;
      }
      font = Typeface.create(font, textStyle);
    }
  }

  
  public void updateDrawState(
      @NonNull Context context,
      @NonNull TextPaint textPaint,
      @NonNull TextAppearanceFontCallback callback) {
    updateMeasureState(context, textPaint, callback);

    textPaint.setColor(
        textColor != null
            ? textColor.getColorForState(textPaint.drawableState, textColor.getDefaultColor())
            : Color.BLACK);
    textPaint.setShadowLayer(
        shadowRadius,
        shadowDx,
        shadowDy,
        shadowColor != null
            ? shadowColor.getColorForState(textPaint.drawableState, shadowColor.getDefaultColor())
            : Color.TRANSPARENT);
  }

  
  public void updateMeasureState(
      @NonNull Context context,
      @NonNull TextPaint textPaint,
      @NonNull TextAppearanceFontCallback callback) {
    if (TextAppearanceConfig.shouldLoadFontSynchronously()) {
      updateTextPaintMeasureState(textPaint, getFont(context));
    } else {
      getFontAsync(context, textPaint, callback);
    }
  }

  
  public void updateTextPaintMeasureState(
      @NonNull TextPaint textPaint, @NonNull Typeface typeface) {
    textPaint.setTypeface(typeface);

    int fake = textStyle & ~typeface.getStyle();
    textPaint.setFakeBoldText((fake & Typeface.BOLD) != 0);
    textPaint.setTextSkewX((fake & Typeface.ITALIC) != 0 ? -0.25f : 0f);

    textPaint.setTextSize(textSize);
  }
}
