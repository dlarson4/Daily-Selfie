package com.course.lab.dailyselfie;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Build.VERSION_CODES;

public enum ReusableBitmaps
{
    INSTANCE;

    private static final String CLASSNAME = ReusableBitmaps.class.getSimpleName();
    
    private Set<SoftReference<Bitmap>> mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());

    public void add(Bitmap bitmap)
    {
        mReusableBitmaps.add(new SoftReference<Bitmap>(bitmap));
    }
    
    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options)
    {
        Bitmap bitmap = null;

        if(mReusableBitmaps != null && !mReusableBitmaps.isEmpty())
        {
            synchronized(mReusableBitmaps)
            {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                Bitmap item;

                while(iterator.hasNext())
                {
                    item = iterator.next().get();

                    if(null != item && item.isMutable())
                    {
                        // Check to see it the item can be used for inBitmap.
                        if(canUseForInBitmap(item, options))
                        {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again.
                            iterator.remove();
                            break;
                        }
                    }
                    else
                    {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] targetOptions.outWidth: '%d',  targetOptions.outHeight: '%d', targetOptions.inSampleSize: '%d', byteCount: '%d', candidate.allocationByteCount: '%d'", 
                        CLASSNAME, "canUseForInBitmap", targetOptions.outWidth,  targetOptions.outHeight, targetOptions.inSampleSize, byteCount, candidate.getAllocationByteCount());
            }
            
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    private static int getBytesPerPixel(Config config)
    {
        if(config == Config.ARGB_8888)
        {
            return 4;
        }
        else if(config == Config.RGB_565)
        {
            return 2;
        }
        else if(config == Config.ARGB_4444)
        {
            return 2;
        }
        else if(config == Config.ALPHA_8)
        {
            return 1;
        }
        return 1;
    }

}
