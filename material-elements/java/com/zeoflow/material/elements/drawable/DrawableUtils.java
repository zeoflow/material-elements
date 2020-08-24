

package com.zeoflow.material.elements.drawable;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.XmlRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


@RestrictTo(Scope.LIBRARY_GROUP)
public final class DrawableUtils {

  private DrawableUtils() {}

  
  @Nullable
  public static PorterDuffColorFilter updateTintFilter(
      @NonNull Drawable drawable,
      @Nullable ColorStateList tint,
      @Nullable PorterDuff.Mode tintMode) {
    if (tint == null || tintMode == null) {
      return null;
    }

    final int color = tint.getColorForState(drawable.getState(), Color.TRANSPARENT);
    return new PorterDuffColorFilter(color, tintMode);
  }

  @NonNull
  public static AttributeSet parseDrawableXml(
      @NonNull Context context, @XmlRes int id, @NonNull CharSequence startTag) {
    try {
      XmlPullParser parser = context.getResources().getXml(id);

      int type;
      do {
        type = parser.next();
      } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
      if (type != XmlPullParser.START_TAG) {
        throw new XmlPullParserException("No start tag found");
      }

      if (!TextUtils.equals(parser.getName(), startTag)) {
        throw new XmlPullParserException("Must have a <" + startTag + "> start tag");
      }

      AttributeSet attrs = Xml.asAttributeSet(parser);

      return attrs;
    } catch (XmlPullParserException | IOException e) {
      NotFoundException exception =
          new NotFoundException("Can't load badge resource ID #0x" + Integer.toHexString(id));
      exception.initCause(e);
      throw exception;
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  public static void setRippleDrawableRadius(@Nullable RippleDrawable drawable, int radius) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable.setRadius(radius);
    } else {
      try {
        @SuppressLint("PrivateApi")
        Method setMaxRadiusMethod =
            RippleDrawable.class.getDeclaredMethod("setMaxRadius", int.class);
        setMaxRadiusMethod.invoke(drawable, radius);
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("Couldn't set RippleDrawable radius", e);
      }
    }
  }
}
