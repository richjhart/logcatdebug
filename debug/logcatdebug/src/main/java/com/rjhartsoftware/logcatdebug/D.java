package com.rjhartsoftware.logcatdebug;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Queue;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"HardCodedStringLiteral", "SameReturnValue", "unused", "SameParameterValue"})
public class D {

    public static void init(String version, boolean debug) {
        DEBUG = (debug || version.contains("alpha"));
        BETA = (DEBUG || version.contains("beta"));
    }

    public static final DebugTag GENERAL = new DebugTag("general", true, true);

    public static boolean DEBUG = false;
    public static boolean BETA = false;

    private static final String TAG_FORMAT = "%s%s";
    private static final String TAG_HIDDEN = "all_debug_";
    private static final String TAG_SHOWN = "main_debug_";
    private static final int D = 0;
    private static final int W = 1;
    private static final int E = 2;

    public static void log(DebugTag tag, String msg) {
        if (DEBUG) {
            log(tag, msg, D, null, 4, 4);
        }
    }

    public static void warn(DebugTag tag, String msg) {
        if (DEBUG) {
            log(tag, msg, W, null, 4, 4);
        }
    }

    public static void error(DebugTag tag, String msg) {
        if (DEBUG) {
            log(tag, msg, E, null, 4, 4);
        }
    }

    public static void log(DebugTag tag, String msg, Throwable tr) {
        if (DEBUG) {
            log(tag, msg, D, tr, 4, 4);
        }
    }

    public static void warn(DebugTag tag, String msg, Throwable tr) {
        if (DEBUG) {
            log(tag, msg, W, tr, 4, 4);
        }
    }

    public static void error(DebugTag tag, String msg, Throwable tr) {
        if (DEBUG) {
            log(tag, msg, E, tr, 4, 4);
        }
    }

    private static void log(DebugTag tag, String msg, int level, Throwable tr, int showStart, int showEnd) {
        if (tag.mEnabled) {
            Queue<String> lines = new ArrayDeque<>();
            String remaining = msg;
            while (!remaining.isEmpty()) {
                if (remaining.length() > 1000) {
                    lines.add(remaining.substring(0, 1000));
                    remaining = remaining.substring(1000);
                } else {
                    lines.add(remaining);
                    remaining = "";
                }
            }
            if (showStart < 0) {
                showStart = lines.size();
            }
            if (showEnd < 0) {
                showEnd = lines.size();
            }
            String caller = caller(tag.mLevel);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < caller.length(); i++) {
                sb.append("\u00A0");
            }
            while (showStart > 0 && !lines.isEmpty()) {
                print(tag.toString(), caller, lines.peek(), (lines.size() == 1 ? tr : null), level);
                lines.poll();
                caller = sb.toString();
                showStart--;
            }
            if (!lines.isEmpty()) {
                if (showEnd < lines.size()) {
                    print(tag.toString(), caller, "...", tr, level);
                    caller = sb.toString();
                }
                while (showEnd > 0 && !lines.isEmpty()) {
                    print(tag.toString(), caller, lines.peek(), (lines.size() == 1 ? tr : null), level);
                    lines.poll();
                    caller = sb.toString();
                    showEnd--;
                }
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private static void print(String tag, String prefix, String main, Throwable tr, int level) {
        String msg = String.format("%s%s", prefix, main);
        switch (level) {
            case D:
                if (tr != null) {
                    Log.d(tag, msg, tr);
                } else {
                    Log.d(tag, msg);
                }
                break;
            case W:
                if (tr != null) {
                    Log.w(tag, msg, tr);
                } else {
                    Log.w(tag, msg);
                }
                break;
            case E:
                if (tr != null) {
                    Log.e(tag, msg, tr);
                } else {
                    Log.e(tag, msg);
                }
                break;
        }
    }

    private static String caller(int level) {
        Throwable t = new Exception();
        if (t.getStackTrace().length > 0) {
            String this_file = t.getStackTrace()[0].getFileName();
            if (this_file == null) {
                this_file = "(unknown file): ";
            }
            int count = 0;
            for (StackTraceElement ste : t.getStackTrace()) {
                if (!this_file.equals(ste.getFileName())) {
                    count++;
                    if (count > level) {
                        return String.format(Locale.US, "(%s:%d).%s(): ", ste.getFileName(), ste.getLineNumber(), ste.getMethodName());
                    } else {
                        this_file = ste.getFileName();
                    }
                }
            }
        }
        return "(unknown source): ";
    }

    public static boolean isLogging(DebugTag tag) {
        return DEBUG && tag.mEnabled;
    }

    public static final class DebugTag {
        private final boolean mFocus;
        private final String mTag;
        private final boolean mEnabled;
        private final int mLevel;

        public DebugTag(String tag, boolean enabled, boolean focus) {
            this(tag, enabled, focus, 0);
        }

        public DebugTag(String tag, boolean enabled, boolean focus, int level) {
            mFocus = focus;
            mTag = tag;
            mEnabled = enabled;
            mLevel = level;
        }

        @Override
        public String toString() {
            return String.format(TAG_FORMAT, (mFocus ? TAG_SHOWN : TAG_HIDDEN), mTag);
        }
    }

}