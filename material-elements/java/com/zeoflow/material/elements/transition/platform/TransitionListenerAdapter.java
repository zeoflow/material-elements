


package com.zeoflow.material.elements.transition.platform;

import android.transition.Transition;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
abstract class TransitionListenerAdapter implements Transition.TransitionListener {

  @Override
  public void onTransitionStart(Transition transition) {}

  @Override
  public void onTransitionEnd(Transition transition) {}

  @Override
  public void onTransitionCancel(Transition transition) {}

  @Override
  public void onTransitionPause(Transition transition) {}

  @Override
  public void onTransitionResume(Transition transition) {}
}
