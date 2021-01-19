package com.zeoflow.material.elements.color

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.zeoflow.material.elements.R
import com.zeoflow.material.elements.colorwheel.extensions.readBooleanCompat
import com.zeoflow.material.elements.colorwheel.extensions.writeBooleanCompat
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawable
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState
import com.zeoflow.material.elements.colorwheel.thumb.readThumbState
import com.zeoflow.material.elements.colorwheel.thumb.writeThumbState
import com.zeoflow.material.elements.colorwheel.utils.MAX_ALPHA
import com.zeoflow.material.elements.colorwheel.utils.ensureNumberWithinRange
import com.zeoflow.material.elements.colorwheel.utils.isTap
import com.zeoflow.material.elements.colorwheel.utils.setColorAlpha

open class ColorSeekBar @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val viewConfig = ViewConfiguration.get(context)
  private val gradientColors = IntArray(2)
  private val thumbDrawable = ThumbDrawable()
  private var colorSeeds = intArrayOf(
    Color.parseColor("#000000"),
    Color.parseColor("#ff0000"),
    Color.parseColor("#ff0040"),
    Color.parseColor("#ff0080"),
    Color.parseColor("#ff00bf"),
    Color.parseColor("#ff00ff"),
    Color.parseColor("#bf00ff"),
    Color.parseColor("#8000ff"),
    Color.parseColor("#4000ff"),
    Color.parseColor("#0000ff"),
    Color.parseColor("#0040ff"),
    Color.parseColor("#0080ff"),
    Color.parseColor("#00bfff"),
    Color.parseColor("#00ffff"),
    Color.parseColor("#00ffbf"),
    Color.parseColor("#00ff80"),
    Color.parseColor("#00ff40"),
    Color.parseColor("#00ff00"),
    Color.parseColor("#40ff00"),
    Color.parseColor("#80ff00"),
    Color.parseColor("#bfff00"),
    Color.parseColor("#ffff00"),
    Color.parseColor("#ffbf00"),
    Color.parseColor("#ff8000"),
    Color.parseColor("#ff4000"),
    Color.parseColor("#ff0000"),
      Color.parseColor("#ffffff")
  )
  private val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorSeeds)

  private lateinit var orientationStrategy: OrientationStrategy
  private var downX = 0f
  private var downY = 0f

  var offset = 0f
    set(offset) {
      field = ensureOffsetWithinRange(offset)
      calculateArgb()
    }

  var barSize = 0
    set(width) {
      field = width
      requestLayout()
    }

  var strokeSize = 0
    set(strokeSize) {
      field = strokeSize
      requestLayout()
    }

  var cornersRadius = 0f
    set(radius) {
      field = radius
      invalidate()
    }

  var orientation = Orientation.VERTICAL
    set(orientation) {
      field = orientation
      orientationStrategy = createOrientationStrategy()
      requestLayout()
    }

  var thumbColor
    get() = thumbDrawable.thumbColor
    set(value) {
      thumbDrawable.thumbColor = value
      invalidate()
    }

  var thumbStrokeColor
    get() = thumbDrawable.strokeColor
    set(value) {
      thumbDrawable.strokeColor = value
      invalidate()
    }

  var thumbColorCircleScale
    get() = thumbDrawable.colorCircleScale
    set(value) {
      thumbDrawable.colorCircleScale = value
      invalidate()
    }

  var thumbRadius
    get() = thumbDrawable.radius
    set(radius) {
      thumbDrawable.radius = radius
      requestLayout()
    }

  var argb = 0
    private set

  var colorChangeListener: ((Int) -> Unit)? = null

  var interceptTouchEvent = true

  init {
    parseAttributes(context, attrs, R.style.GradientSeekBarDefaultStyle)
  }

  private fun parseAttributes(context: Context, attrs: AttributeSet?, defStyle: Int) {
    context.obtainStyledAttributes(attrs, R.styleable.GradientSeekBar, 0, defStyle).apply {
      thumbColor = getColor(R.styleable.GradientSeekBar_asb_thumbColor, 0)
      thumbStrokeColor = getColor(R.styleable.GradientSeekBar_asb_thumbStrokeColor, 0)
      thumbColorCircleScale = getFloat(R.styleable.GradientSeekBar_asb_thumbColorCircleScale, 0f)
      thumbRadius = getDimensionPixelSize(R.styleable.GradientSeekBar_asb_thumbRadius, 0)
      barSize = getDimensionPixelSize(R.styleable.GradientSeekBar_asb_barSize, 0)
      strokeSize = getDimensionPixelSize(R.styleable.GradientSeekBar_asb_strokeSize, 0)
      cornersRadius = getDimension(R.styleable.GradientSeekBar_asb_barCornersRadius, 0f)
      offset = ensureOffsetWithinRange(getFloat(R.styleable.GradientSeekBar_asb_offset, 0f))
      orientation = Orientation.values()[getInt(R.styleable.GradientSeekBar_asb_orientation, 0)]
      recycle()
    }
  }

  private fun createOrientationStrategy() = when (orientation) {
    Orientation.VERTICAL -> VerticalStrategy()
    Orientation.HORIZONTAL -> HorizontalStrategy()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val dimens = orientationStrategy.measure(this, widthMeasureSpec, heightMeasureSpec)
    setMeasuredDimension(dimens.width(), dimens.height())
  }

  override fun onDraw(canvas: Canvas) {
    drawGradientRect(canvas)
    drawThumb(canvas)
  }

  private fun drawGradientRect(canvas: Canvas) {
    gradientDrawable.orientation = orientationStrategy.gradientOrientation
    gradientDrawable.bounds = orientationStrategy.calculateGradientBounds(this)
    gradientDrawable.cornerRadius = cornersRadius
    gradientDrawable.setStroke(strokeSize/4, thumbColor)
    gradientDrawable.draw(canvas)
  }

  private fun drawThumb(canvas: Canvas) {
    val coordinates = orientationStrategy.calculateThumbCoordinates(this, gradientDrawable.bounds)

    argb = pickColor(thumbX, width)
    thumbDrawable.indicatorColor = argb
    thumbDrawable.setCoordinates(coordinates.x, coordinates.y)
    thumbDrawable.draw(canvas)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> onActionDown(event)
      MotionEvent.ACTION_MOVE -> calculateOffsetOnMotionEvent(event)
      MotionEvent.ACTION_UP -> {
        calculateOffsetOnMotionEvent(event)
        if (isTap(event, downX, downY, viewConfig)) performClick()
      }
    }

    return true
  }

  private fun onActionDown(event: MotionEvent) {
    parent.requestDisallowInterceptTouchEvent(interceptTouchEvent)
    calculateOffsetOnMotionEvent(event)
    downX = event.x
    downY = event.y
  }

  private var thumbX: Float = 24f
  private fun calculateOffsetOnMotionEvent(event: MotionEvent) {
    event.x.let {
      thumbX = it
      invalidate()
    }
    offset = orientationStrategy.calculateOffsetOnMotionEvent(this, event, gradientDrawable.bounds)
  }

  private fun calculateArgb() {
    argb = pickColor(thumbX, width)
    fireListener()
    invalidate()
  }

  private fun pickColor(position: Float, canvasWidth: Int): Int {
    val value = (position - paddingStart) / (canvasWidth - (paddingStart + paddingEnd))
    when {
      value <= 0.0 -> return colorSeeds[0]
      value >= 1 -> return colorSeeds[colorSeeds.size - 1]
      else -> {
        var colorPosition = value * (colorSeeds.size - 1)
        val i = colorPosition.toInt()
        colorPosition -= i
        val c0 = colorSeeds[i]
        val c1 = colorSeeds[i + 1]

        val red = mix(Color.red(c0), Color.red(c1), colorPosition)
        val green = mix(Color.green(c0), Color.green(c1), colorPosition)
        val blue = mix(Color.blue(c0), Color.blue(c1), colorPosition)
        return Color.rgb(red, green, blue)
      }
    }
  }

  private fun mix(start: Int, end: Int, position: Float): Int {
    return start + Math.round(position * (end - start))
  }

  private fun fireListener() {
    colorChangeListener?.invoke(argb)
  }

  override fun onSaveInstanceState(): Parcelable {
    val superState = super.onSaveInstanceState()
    val thumbState = thumbDrawable.saveState()
    return ColorSeekBarState(superState, this, thumbState)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    if (state is ColorSeekBarState) {
      super.onRestoreInstanceState(state.superState)
      readColorSeekBarState(state)
    } else {
      super.onRestoreInstanceState(state)
    }
  }

  private fun readColorSeekBarState(state: ColorSeekBarState) {
    offset = state.offset
    barSize = state.barSize
    cornersRadius = state.cornerRadius
    orientation = Orientation.values()[state.orientation]
    interceptTouchEvent = state.interceptTouchEvent
    thumbDrawable.restoreState(state.thumbState)
  }

  enum class Orientation { VERTICAL, HORIZONTAL }
}

private class ColorSeekBarState : View.BaseSavedState {

  val offset: Float
  val barSize: Int
  val strokeSize: Int
  val cornerRadius: Float
  val orientation: Int
  val interceptTouchEvent: Boolean
  val thumbState: ThumbDrawableState

  constructor(superState: Parcelable?, view: ColorSeekBar, thumbState: ThumbDrawableState) : super(superState) {
    offset = view.offset
    barSize = view.barSize
    strokeSize = view.strokeSize
    cornerRadius = view.cornersRadius
    orientation = view.orientation.ordinal
    interceptTouchEvent = view.interceptTouchEvent
    this.thumbState = thumbState
  }

  constructor(source: Parcel) : super(source) {
    offset = source.readFloat()
    barSize = source.readInt()
    strokeSize = source.readInt()
    cornerRadius = source.readFloat()
    orientation = source.readInt()
    interceptTouchEvent = source.readBooleanCompat()
    thumbState = source.readThumbState()
  }

  override fun writeToParcel(out: Parcel, flags: Int) {
    super.writeToParcel(out, flags)
    out.writeFloat(offset)
    out.writeInt(barSize)
    out.writeInt(strokeSize)
    out.writeFloat(cornerRadius)
    out.writeInt(orientation)
    out.writeBooleanCompat(interceptTouchEvent)
    out.writeThumbState(thumbState, flags)
  }

  companion object CREATOR : Parcelable.Creator<ColorSeekBarState> {

    override fun createFromParcel(source: Parcel) = ColorSeekBarState(source)

    override fun newArray(size: Int) = arrayOfNulls<ColorSeekBarState>(size)
  }
}

val ColorSeekBar.currentColorAlpha get() = Color.alpha(argb)

inline fun ColorSeekBar.setAlphaChangeListener(crossinline listener: (Int, Int) -> Unit) {
  this.colorChangeListener = { color -> listener(color, this.currentColorAlpha) }
}

private fun ensureOffsetWithinRange(offset: Float) = ensureNumberWithinRange(offset, 0f, 1f)
