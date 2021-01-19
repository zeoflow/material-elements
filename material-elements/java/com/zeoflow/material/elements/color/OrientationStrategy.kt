package com.zeoflow.material.elements.color

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent

internal interface OrientationStrategy {

  val gradientOrientation: GradientDrawable.Orientation

  fun measure(view: ColorSeekBar, widthSpec: Int, heightSpec: Int): Rect

  fun calculateGradientBounds(view: ColorSeekBar): Rect

  fun calculateOffsetOnMotionEvent(view: ColorSeekBar, event: MotionEvent, barBounds: Rect): Float

  fun calculateThumbCoordinates(view: ColorSeekBar, barBounds: Rect): PointF
}
