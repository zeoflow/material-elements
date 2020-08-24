
package com.zeoflow.material.elements.internal;

import com.google.android.material.R;

import android.os.Build;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Locale;


@RestrictTo(Scope.LIBRARY_GROUP)
public class ManufacturerUtils {
  private static final String LGE = "lge";
  private static final String SAMSUNG = "samsung";
  private static final String MEIZU = "meizu";

  private ManufacturerUtils() {}

  
  public static boolean isMeizuDevice() {
    return Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).equals(MEIZU);
  }

  
  public static boolean isLGEDevice() {
    return Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).equals(LGE);
  }

  
  public static boolean isSamsungDevice() {
    return Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).equals(SAMSUNG);
  }

  
  public static boolean isDateInputKeyboardMissingSeparatorCharacters() {
    return isLGEDevice() || isSamsungDevice();
  }
}
