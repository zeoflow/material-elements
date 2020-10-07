package com.zeoflow.material.elements.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zeoflow.material.elements.colorwheel.ColorWheel;
import com.zeoflow.material.elements.gradientseekbar.GradientSeekBar;

public class MainActivity extends AppCompatActivity
{

  private GradientSeekBar mGradientSeekBar;
  private ColorWheel mColorWheel;
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mColorWheel = findViewById(R.id.colorWheel);
    mGradientSeekBar = findViewById(R.id.gradientSeekBar);
    mColorWheel.setColorChangeListener();
        mColorWheel.colorChangeListener((Function1<Integer, Unit>) color ->
        {
          mGradientSeekBar.setStartColor(color);
          colorSelected = color;
          return null;
        });
  }

}
