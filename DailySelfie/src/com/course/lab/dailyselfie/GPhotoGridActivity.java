package com.course.lab.dailyselfie;

import static com.course.lab.dailyselfie.Constants.IMAGE_POSITION_EXTRA;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class GPhotoGridActivity extends Activity
{
    private static final String CLASSNAME = GPhotoGridActivity.class.getSimpleName();

    // 2 minutes
    private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;

    private GridView mGridView;
    private GalleryPickerAdapter mAdapter = null;

    private LruCache<String, Bitmap> mMemoryCache;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallerypicker);
        mGridView = (GridView)findViewById(R.id.albums);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap bitmap)
            {
                final int bitmapSize = getBitmapSize(bitmap) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }
        };
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat) onward this returns
     * the allocated memory size of the bitmap which can be larger than the actual bitmap data byte count (in the case
     * it was re-used).
     * 
     * @param value
     * @return size in bytes
     */
    @TargetApi(VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap)
    {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if(VersionUtils.hasKitKat())
        {
            return bitmap.getAllocationByteCount();
        }

        if(VersionUtils.hasHoneycombMR1())
        {
            return bitmap.getByteCount();
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAdapter = new GalleryPickerAdapter(this, mMemoryCache);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                startViewPhotoActicity(position);
            }
        });

        if(DirUtils.isExternalStorageWritable())
        {
            new PhotoLoadTask().execute();
            setupSelfieAlarm();
        }
        else
        {
            Logger.warn("[%s.%s] No external storage found", CLASSNAME, "onStart");
            Toast.makeText(this, this.getResources().getString(R.string.no_external_storage), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // mMemoryCache.evictAll();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mMemoryCache.evictAll();
    }

    private void setupSelfieAlarm()
    {
        AlarmManager mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent notificationReceiverIntent = new Intent(getApplicationContext(), AlarmNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationReceiverIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + INITIAL_ALARM_DELAY, pendingIntent);
    }

    private void startViewPhotoActicity(int position)
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] position = '%d', starting intent to view photo..", CLASSNAME,
                    "startViewPhotoActicity", position);
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
                    Logger.debug("[%s.%s] displayPhoto(), action_take_selfie selected", CLASSNAME,
                            "onOptionsItemSelected");
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

    private class PhotoLoadTask extends AsyncTask<String, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(String... params)
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] Loading photos from storage", "PhotoLoadActivity", "doInBackground");
            }
            return DirUtils.getPhotos();
        }

        @Override
        protected void onPostExecute(List<String> photos)
        {
            GPhotoGridActivity.this.setupAdapter(photos);
        }
    }

    private void setupAdapter(List<String> photos)
    {
        mAdapter.addAll(photos);
        mAdapter.updateDisplay();
    }

}
