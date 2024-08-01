package com.hipoom.processor

import android.util.Log
import com.hipoom.performance.timing.TimingRecorder


class OnFramePopListener : TimingRecorder.Listener {

    override fun onFramePop(frame: TimingRecorder.Frame) {
        Log.i(
            "ZHP_TEST",
            frame.methodDescription + " 执行完毕，耗时: " + (frame.endTime - frame.beginTime) + " 毫秒."
        )
    }

}