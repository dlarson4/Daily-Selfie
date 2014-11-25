package com.course.lab.dailyselfie;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppLifecycleCallback implements Application.ActivityLifecycleCallbacks
{
    private static final String CLASSNAME = AppLifecycleCallback.class.getSimpleName();
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityCreated", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Created);
    }

    @Override
    public void onActivityStarted(Activity activity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityStarted", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Started);
    }

    @Override
    public void onActivityResumed(Activity activity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityResumed", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Resumed);
    }

    @Override
    public void onActivityPaused(Activity activity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityPaused", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Paused);
    }

    @Override
    public void onActivityStopped(Activity activity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityStopped", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Stopped);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState)
    {
    }

    @Override
    public void onActivityDestroyed(Activity activity)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] hashCode = '%d'", CLASSNAME, "onActivityDestroyed", hashCode());
        }
        AppStatus.INSTANCE.setStatus(AppStatus.Status.Destroyed);
    }

}
