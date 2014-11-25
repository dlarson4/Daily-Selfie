package com.course.lab.dailyselfie;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.WARN;
import android.util.Log;

public class Logger
{
    public static final String TAG = "DailySelfie";
    
    public static final boolean DEBUG_ENABLED = com.course.lab.dailyselfie.BuildConfig.DEBUG;
    public static final boolean INFO_ENABLED = true;
    public static final boolean WARN_ENABLED = true;
    public static final boolean ERROR_ENABLED = true;
    
    public static boolean isDebugEnabled()
    {
        return DEBUG_ENABLED;
    }

    public static void debug(String msg, Object... args)
    {
        log(DEBUG, msg, null, args);
    }

    public static void info(String msg, Object... args)
    {
        log(INFO, msg, null, args);
    }

    public static void warn(String msg, Object... args)
    {
        log(WARN, msg, null, args);
    }

    public static void warn(String msg, Throwable t, Object... args)
    {
        log(WARN, msg, t, args);
    }

    public static void error(String msg, Object... args)
    {
        log(ERROR, msg, null, args);
    }

    public static void error(String msg, Throwable t, Object... args)
    {
        log(ERROR, msg, t, args);
    }

    private static void log(int level, String msg, Throwable t, Object... args)
    {
        switch(level)
        {
            case DEBUG:
            {
                if(DEBUG_ENABLED)
                {
                    Log.d(TAG, String.format(msg, args));
                }
                break;
            }
            case INFO:
            {
                if(INFO_ENABLED)
                {
                    Log.i(TAG, String.format(msg, args));
                }
                break;
            }
            case WARN:
            {
                if(WARN_ENABLED)
                {
                    if(t == null)
                    {
                        Log.w(TAG, String.format(msg, args));
                    }
                    else
                    {
                        Log.w(TAG, String.format(msg, args), t);
                    }
                }
                break;
            }
            case ERROR:
            {
                if(t == null)
                {
                    Log.e(TAG, String.format(msg, args));
                }
                else
                {
                    Log.e(TAG, String.format(msg, args), t);
                }
                break;
            }
        }
    }

}
