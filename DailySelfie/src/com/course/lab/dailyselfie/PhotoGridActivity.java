package com.course.lab.dailyselfie;

import static com.course.lab.dailyselfie.Constants.IMAGE_POSITION_EXTRA;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class PhotoGridActivity extends Activity
{
    private static final String CLASSNAME = PhotoGridActivity.class.getSimpleName();
    
    // 2 minutes
    private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;
    //private AppLifecycleCallback lifecycleCallback = new AppLifecycleCallback();
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_layout);
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        //getApplication().registerActivityLifecycleCallbacks(lifecycleCallback);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        GridView gridview = (GridView)findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                startViewPhotoActicity(position);
            }
        });
        setupSelfieAlarm();
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        //getApplication().unregisterActivityLifecycleCallbacks(lifecycleCallback);
    }
    
    private void setupSelfieAlarm()
    {
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationReceiverIntent = new Intent(getApplicationContext(), AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + INITIAL_ALARM_DELAY, pendingIntent);
    }

    private void startViewPhotoActicity(int position)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] position = '%d', starting intent to view photo..", CLASSNAME, "startViewPhotoActicity", position);
        }

        Intent intent = new Intent(this, ViewPhotoActivity.class);
        intent.putExtra(IMAGE_POSITION_EXTRA, position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_take_selfie:
                if(Logger.isDebugEnabled())
                {
                    Logger.debug("[%s.%s] displayPhoto(), action_take_selfie selected", CLASSNAME, "onOptionsItemSelected");
                }
                startTakePhotoActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startTakePhotoActivity()
    {
        Intent intent = new Intent(this, TakePhotoActivity.class);
        startActivity(intent);
    }
}
