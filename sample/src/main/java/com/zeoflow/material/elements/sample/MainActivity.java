package com.zeoflow.material.elements.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zeoflow.material.elements.sample.cw.ColorWheel;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity
{

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ColorWheel mColorWheel = findViewById(R.id.colorWheel);
    mColorWheel.colorChangeListener(new Function1<Integer, Unit>()
    {
      @Override
      public Unit invoke(Integer color)
      {
        mGradientSeekBar.setStartColor(color);
        colorSelected = color;
        return null;
      }
    });
  }

}
