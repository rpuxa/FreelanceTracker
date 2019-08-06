package com.robobobo.apps.aws.freelancetracker

import android.util.Log

const val MONTH_IN_MILLIS = 1000L * 60 * 60 * 24 * 30
const val GLOBAL_TAG = "FreelanceTrackerDebug"

fun log(vararg objects: Any?) = Log.d(GLOBAL_TAG, objects.joinToString(" "))