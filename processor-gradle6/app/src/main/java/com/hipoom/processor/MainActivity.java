package com.hipoom.processor;

import java.util.Set;

import android.os.Bundle;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatCallback;
import com.hipoom.registry.Registry;

@TestAnnotation
public class MainActivity extends AppCompatActivity implements TestInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Set<Class<?>> classes = Registry.getClassesImplements(AppCompatCallback.class);
        for (Class<?> c : classes) {
            Log.i("ZHP_TEST", "c: " + c);
        }
        Log.i("ZHP_TEST", "-------------------");
    }
}