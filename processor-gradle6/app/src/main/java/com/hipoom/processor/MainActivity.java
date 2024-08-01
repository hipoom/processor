package com.hipoom.processor;

import java.util.Random;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

@TestAnnotation
public class MainActivity extends AppCompatActivity implements TestInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Test().test();
    }



    private static boolean randomBoolean() {
        return new Random().nextBoolean();
    }
}