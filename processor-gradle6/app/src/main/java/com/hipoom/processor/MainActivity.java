package com.hipoom.processor;

import java.util.Random;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.hipoom.Files;
import com.hipoom.performance.timing.TimingRecorder;
import com.hipoom.performance.timing.TimingRecorder.Frame;
import com.hipoom.performance.timing.TimingRecorder.Listener;

@TestAnnotation
public class MainActivity extends AppCompatActivity implements TestInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Test().test();

        TimingRecorder.onFramePopListener = new OnFramePopListener();
        Files.ensureDirectory(getFilesDir());

        new KotlinTest().thisMethodNeedBeInsertCode();
    }

    private static boolean randomBoolean() {
        return new Random().nextBoolean();
    }
}