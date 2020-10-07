package com.zeoflow.material.elements.sample.cw.utils

internal const val PI = Math.PI.toFloat()

internal fun toRadians(degrees: Float) = degrees / 180f * PI

internal fun toDegrees(radians: Float) = radians * 180f / PI
