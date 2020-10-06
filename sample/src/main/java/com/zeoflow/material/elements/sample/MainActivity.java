package com.zeoflow.material.elements.sample;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zeoflow.material.elements.color.ColorEnvelope;
import com.zeoflow.material.elements.colorwheel.ColorWheel;
import com.zeoflow.material.elements.colorwheel.flag.FlagView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
{

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ColorWheel colorWheel = findViewById(R.id.colorWheel);
    colorWheel.setFlagView(new FlagView(this, R.layout.flag_bubble)
    {
      @Override
      public void onRefresh(ColorEnvelope colorEnvelope)
      {
        Log.d("color", String.valueOf(Objects.requireNonNull(colorWheel.getFlagView()).getVisibility()));
      }
    });

  }
}
