

package com.zeoflow.material.elements.resources;

import com.google.android.material.R;


public class TextAppearanceConfig {

  private static boolean shouldLoadFontSynchronously;

  
  public static void setShouldLoadFontSynchronously(boolean flag) {
    shouldLoadFontSynchronously = flag;
  }

  
  public static boolean shouldLoadFontSynchronously() {
    return shouldLoadFontSynchronously;
  }
}
