package com.rjhartsoftware.debug;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rjhartsoftware.logcatdebug.D;

public class MainActivity extends AppCompatActivity {

    private static final D.DebugTag TEST = new D.DebugTag("test");
    private static final D.DebugTag TEST_2 = new D.DebugTag("test2", true);
    private static final D.DebugTag SHOW_ALL = new D.DebugTag("test3", true, 0, D.SHOW_ALL_LINES, D.SHOW_ALL_LINES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        D.init(BuildConfig.VERSION_NAME, BuildConfig.DEBUG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        D.log(D.GENERAL, "General comment");
        D.log(TEST, "Test comment");
        D.log(TEST_2, "Test comment 2");
        TEST.disable();
        D.log(TEST, "This shouldn't appear");
        TEST.enable();
        D.log(TEST, "This should appear");
        D.log(TEST, "This is a long message that should be cut\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
        D.log(SHOW_ALL, "This is a long message that should be fully displayed\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
        D.setLines(D.SHOW_ALL_LINES, D.SHOW_ALL_LINES);
        D.log(TEST, "This is a long message that should now be fully displayed\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12");
        D.log(TEST, "This %s %d %s", "is a formatted message with", 3, "arguments");
    }
}
