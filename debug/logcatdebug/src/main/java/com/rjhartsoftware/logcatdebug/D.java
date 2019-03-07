package com.rjhartsoftware.logcatdebug;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;

@SuppressLint("SetTextI18n")
@SuppressWarnings({"HardCodedStringLiteral", "SameReturnValue", "unused", "SameParameterValue"})
public class D {

    private static final String TAG_FORMAT = "%s%s";
    private static final String TAG = "debug_";
    private static final int D = 0;
    private static final int W = 1;
    private static final int E = 2;
    public static final int SHOW_DEFAULT_LINES = -1;
    public static final int SHOW_ALL_LINES = -2;
    private static final int DEFAULT_LINES_AT_START = 4;
    private static final int DEFAULT_LINE_AT_END = 4;

    public static void init(String version, boolean debug) {
        DEBUG = (debug || version.contains("alpha"));
        BETA = (DEBUG || version.contains("beta"));
    }

    public static final DebugTag GENERAL = new DebugTag("general", true);

    public static boolean DEBUG = false;
    public static boolean BETA = false;
    private static int sDefaultStartLines = DEFAULT_LINES_AT_START;
    private static int sDefaultEndLines = DEFAULT_LINE_AT_END;

    public static void setLines(int lines_at_start, int lines_at_end) {
        sDefaultStartLines = lines_at_start;
        sDefaultEndLines = lines_at_end;
    }

    public static void log(DebugTag tag, String msg, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, D, null, tag.startLines(), tag.endLines());
        }
    }

    public static void warn(DebugTag tag, String msg, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, W, null, tag.startLines(), tag.endLines());
        }
    }

    public static void error(DebugTag tag, String msg, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, E, null, tag.startLines(), tag.endLines());
        }
    }

    public static void log(DebugTag tag, String msg, Throwable tr, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, D, tr, tag.startLines(), tag.endLines());
        }
    }

    public static void warn(DebugTag tag, String msg, Throwable tr, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, W, tr, tag.startLines(), tag.endLines());
        }
    }

    public static void error(DebugTag tag, String msg, Throwable tr, Object... args) {
        if (DEBUG) {
            internal_log(tag, msg, args, E, tr, tag.startLines(), tag.endLines());
        }
    }

    private static void internal_log(DebugTag tag, String msg, Object[] args, int level, Throwable tr, int showStart, int showEnd) {
        if (tag.mEnabled) {
            if (args != null && args.length > 0) {
                try {
                    msg = String.format(Locale.US, msg, args);
                } catch (Exception e) {
                    error(tag, "Unable to format string, showing raw msg:");
                }
            }
            String orig_lines[] = msg.split("\n");
            Queue<String> lines = new ArrayDeque<>();
            for(String orig_line : orig_lines) {
                String remaining = orig_line;
                while (!remaining.isEmpty()) {
                    if (remaining.length() > 1000) {
                        lines.add(remaining.substring(0, 1000));
                        remaining = remaining.substring(1000);
                    } else {
                        lines.add(remaining);
                        remaining = "";
                    }
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
        private final String mTag;
        private boolean mEnabled;
        private final int mLevel;
        private final int mStartLines;
        private final int mEndLines;

        @Deprecated
        public DebugTag(String tag, boolean enabled, boolean focus) {
            this(tag, enabled);
        }

        @Deprecated
        public DebugTag(String tag, boolean enabled, boolean focus, int level) {
            this(tag, enabled, level);
        }

        public DebugTag(String tag) {
            this(tag, true, 0, SHOW_DEFAULT_LINES, SHOW_DEFAULT_LINES);
        }

        public DebugTag(String tag, boolean enabled_by_default) {
            this(tag, enabled_by_default, 0, SHOW_DEFAULT_LINES, SHOW_DEFAULT_LINES);
        }

        public DebugTag(String tag, boolean enabled_by_default, int caller_level) {
            this(tag, enabled_by_default, caller_level, SHOW_DEFAULT_LINES, SHOW_DEFAULT_LINES);
        }

        public DebugTag(String tag, boolean enabled_by_default, int caller_level, int lines_at_start, int lines_at_end) {
            mTag = tag;
            mEnabled = enabled_by_default;
            mLevel = caller_level;
            mStartLines = lines_at_start;
            mEndLines = lines_at_end;
        }

        @Override
        public String toString() {
            return String.format(TAG_FORMAT, TAG, mTag);
        }

        public void enable() {
            setEnabled(true);
        }

        public void disable() {
            setEnabled(false);
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public int startLines() {
            if (mStartLines == SHOW_DEFAULT_LINES) {
                return sDefaultStartLines;
            } else if (mStartLines == SHOW_ALL_LINES) {
                return SHOW_ALL_LINES;
            } else {
                return mStartLines;
            }
        }

        public int endLines() {
            if (mEndLines == SHOW_DEFAULT_LINES) {
                return sDefaultEndLines;
            } else if (mStartLines == SHOW_ALL_LINES) {
                return SHOW_ALL_LINES;
            } else {
                return mEndLines;
            }
        }
    }

}