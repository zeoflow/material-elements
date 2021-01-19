package com.zeoflow.material.elements.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zeoflow.material.elements.color.ColorSeekBar;
import com.zeoflow.material.elements.gradientseekbar.GradientSeekBar;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity
{

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    LinearLayout llHomer = findViewById(R.id.llHomer);
    ColorSeekBar csbPicker = findViewById(R.id.csb_picker);
    GradientSeekBar gsbPicker = findViewById(R.id.gsb_picker);
    gsbPicker.setColorChangeListener(color ->
    {
      llHomer.setBackgroundColor(color);
      return null;
    });
    csbPicker.setColorChangeListener(color ->
    {
      gsbPicker.setColors(Color.argb(0, Color.red(color), Color.green(color), Color.blue(color)), color);
      return null;
    });
//    ColorWheel mColorWheel = findViewById(R.id.colorWheel);
//    mColorWheel.colorChangeListener(new Function1<Integer, Unit>()
//    {
//      @Override
//      public Unit invoke(Integer color)
//      {
//        mGradientSeekBar.setStartColor(color);
//        colorSelected = color;
//        return null;
//      }
//    });
  }

}
