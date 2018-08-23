package com.rjhartsoftware.debug;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rjhartsoftware.logcatdebug.D;

public class MainActivity extends AppCompatActivity {

    private static final D.DebugTag TEST = new D.DebugTag("test", true, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        D.log(D.GENERAL, "General comment");
        D.log(TEST, "Test comment");
    }
}
