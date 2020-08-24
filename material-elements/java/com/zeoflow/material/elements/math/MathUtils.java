
package com.zeoflow.material.elements.math;


public final class MathUtils {

  
  public static final float DEFAULT_EPSILON = 0.0001f;

  private MathUtils() {}

  
  public static float dist(float x1, float y1, float x2, float y2) {
    final float x = (x2 - x1);
    final float y = (y2 - y1);
    return (float) Math.hypot(x, y);
  }

  
  public static float lerp(float start, float stop, float amount) {
    return (1 - amount) * start + amount * stop;
  }

  
  public static boolean geq(float a, float b, float epsilon) {
    return a + epsilon >= b;
  }

  
  public static float distanceToFurthestCorner(
      float pointX,
      float pointY,
      float rectLeft,
      float rectTop,
      float rectRight,
      float rectBottom) {
    return max(
        dist(pointX, pointY, rectLeft, rectTop),
        dist(pointX, pointY, rectRight, rectTop),
        dist(pointX, pointY, rectRight, rectBottom),
        dist(pointX, pointY, rectLeft, rectBottom));
  }

  
  private static float max(float a, float b, float c, float d) {
    return a > b && a > c && a > d ? a : b > c && b > d ? b : c > d ? c : d;
  }

  
  public static float floorMod(float x, int y) {
    int r = (int) (x / y);
    
    if (Math.signum(x) * y < 0 && (r * y != x)) {
      r--;
    }
    return x - r * y;
  }

  
  public static int floorMod(int x, int y) {
    int r = x / y;
    
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return x - r * y;
  }
}
