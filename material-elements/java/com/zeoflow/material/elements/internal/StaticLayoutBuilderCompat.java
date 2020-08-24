

package com.zeoflow.material.elements.internal;

import static androidx.core.util.Preconditions.checkNotNull;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import java.lang.reflect.Constructor;


@RestrictTo(Scope.LIBRARY_GROUP)
final class StaticLayoutBuilderCompat {

  private static final String TEXT_DIR_CLASS = "android.text.TextDirectionHeuristic";
  private static final String TEXT_DIRS_CLASS = "android.text.TextDirectionHeuristics";
  private static final String TEXT_DIR_CLASS_LTR = "LTR";
  private static final String TEXT_DIR_CLASS_RTL = "RTL";

  private static boolean initialized;

  @Nullable private static Constructor<StaticLayout> constructor;
  @Nullable private static Object textDirection;

  private CharSequence source;
  private final TextPaint paint;
  private final int width;
  private int start;
  private int end;

  private Alignment alignment;
  private int maxLines;
  private boolean includePad;
  private boolean isRtl;
  @Nullable private TextUtils.TruncateAt ellipsize;

  private StaticLayoutBuilderCompat(CharSequence source, TextPaint paint, int width) {
    this.source = source;
    this.paint = paint;
    this.width = width;
    this.start = 0;
    this.end = source.length();
    this.alignment = Alignment.ALIGN_NORMAL;
    this.maxLines = Integer.MAX_VALUE;
    this.includePad = true;
    this.ellipsize = null;
  }

  
  @NonNull
  public static StaticLayoutBuilderCompat obtain(
      @NonNull CharSequence source, @NonNull TextPaint paint, @IntRange(from = 0) int width) {
    return new StaticLayoutBuilderCompat(source, paint, width);
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setAlignment(@NonNull Alignment alignment) {
    this.alignment = alignment;
    return this;
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setIncludePad(boolean includePad) {
    this.includePad = includePad;
    return this;
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setStart(@IntRange(from = 0) int start) {
    this.start = start;
    return this;
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setEnd(@IntRange(from = 0) int end) {
    this.end = end;
    return this;
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setMaxLines(@IntRange(from = 0) int maxLines) {
    this.maxLines = maxLines;
    return this;
  }

  
  @NonNull
  public StaticLayoutBuilderCompat setEllipsize(@Nullable TextUtils.TruncateAt ellipsize) {
    this.ellipsize = ellipsize;
    return this;
  }

  
  public StaticLayout build() throws StaticLayoutBuilderCompatException {
    if (source == null) {
      source = "";
    }


    int availableWidth = Math.max(0, width);
    CharSequence textToDraw = source;
    if (maxLines == 1) {
      textToDraw = TextUtils.ellipsize(source, paint, availableWidth, ellipsize);
    }

    end = Math.min(textToDraw.length(), end);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (isRtl) {
        alignment = Alignment.ALIGN_OPPOSITE;
      }
      
      
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              textToDraw, start, end, paint, availableWidth);
      builder.setAlignment(alignment);
      builder.setIncludePad(includePad);
      TextDirectionHeuristic textDirectionHeuristic = isRtl
          ? TextDirectionHeuristics.RTL
          : TextDirectionHeuristics.LTR;
      builder.setTextDirection(textDirectionHeuristic);
      if (ellipsize != null) {
        builder.setEllipsize(ellipsize);
      }
      builder.setMaxLines(maxLines);
      return builder.build();
    }

    createConstructorWithReflection();
    
    try {
      return checkNotNull(constructor)
          .newInstance(
              textToDraw,
              start,
              end,
              paint,
              availableWidth,
              alignment,
              checkNotNull(textDirection),
              1.0f,
              0.0f,
              includePad,
              null,
              availableWidth,
              maxLines);
    } catch (Exception cause) {
      throw new StaticLayoutBuilderCompatException(cause);
    }
  }

  
  private void createConstructorWithReflection() throws StaticLayoutBuilderCompatException {
    if (initialized) {
      return;
    }

    try {
      final Class<?> textDirClass;
      boolean useRtl = isRtl && Build.VERSION.SDK_INT >= VERSION_CODES.M;
      if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
        textDirClass = TextDirectionHeuristic.class;
        textDirection = useRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR;
      } else {
        ClassLoader loader = StaticLayoutBuilderCompat.class.getClassLoader();
        String textDirClassName = isRtl ? TEXT_DIR_CLASS_RTL : TEXT_DIR_CLASS_LTR;
        textDirClass = loader.loadClass(TEXT_DIR_CLASS);
        Class<?> textDirsClass = loader.loadClass(TEXT_DIRS_CLASS);
        textDirection = textDirsClass.getField(textDirClassName).get(textDirsClass);
      }

      final Class<?>[] signature =
          new Class<?>[] {
            CharSequence.class,
            int.class,
            int.class,
            TextPaint.class,
            int.class,
            Alignment.class,
            textDirClass,
            float.class,
            float.class,
            boolean.class,
            TextUtils.TruncateAt.class,
            int.class,
            int.class
          };

      constructor = StaticLayout.class.getDeclaredConstructor(signature);
      constructor.setAccessible(true);
      initialized = true;
    } catch (Exception cause) {
      throw new StaticLayoutBuilderCompatException(cause);
    }
  }

  public StaticLayoutBuilderCompat setIsRtl(boolean isRtl) {
    this.isRtl = isRtl;
    return this;
  }

  static class StaticLayoutBuilderCompatException extends Exception {

    StaticLayoutBuilderCompatException(Throwable cause) {
      super("Error thrown initializing StaticLayout " + cause.getMessage(), cause);
    }
  }
}
