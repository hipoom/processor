package com.hipoom.performance.timing;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Stack;

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 21:50
 */
public class TimingRecorder {

   /* ======================================================= */
   /* Fields                                                  */
   /* ======================================================= */

   /**
    * 记录调用的栈。
    */
   private static final ThreadLocal<Stack<Frame>> stacks = new ThreadLocal<>();



   /* ======================================================= */
   /* Public Methods                                          */
   /* ======================================================= */

   public static void push(@NonNull String methodDescription) {
      Frame frame = new Frame();
      frame.beginTime = SystemClock.elapsedRealtime();
      frame.methodDescription = methodDescription;
      Stack<Frame> stack = stacks.get();
      if (stack == null) {
         stack = new Stack<>();
         stacks.set(stack);
      }
      stack.push(frame);
   }

   public static void pop() {
      final long now = SystemClock.elapsedRealtime();
      Stack<Frame> stack = stacks.get();
      if (stack == null) {
         return;
      }

      Frame frame = stack.pop();
      long duration = now - frame.beginTime;
      Log.i("Timing", "方法 " + frame.methodDescription + " 执行耗时 " + duration + ".");
   }



   /* ======================================================= */
   /* Inner Class                                             */
   /* ======================================================= */

   private static class Frame {

      long beginTime;

      String methodDescription;

   }

}
