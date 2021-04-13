package com.zeoflow.material.elements.colorwheel.utils;

import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class TouchUtils
{

    public static boolean isTap(MotionEvent lastEvent, float initialX, float initialY, ViewConfiguration config)
    {
        long duration = lastEvent.getEventTime() - lastEvent.getDownTime();
        double distance = Math.hypot(lastEvent.getX() - initialX, lastEvent.getY() - initialY);
        return duration < ViewConfiguration.getTapTimeout() && distance < config.getScaledTouchSlop();
    }

}
