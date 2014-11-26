package com.course.lab.dailyselfie;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 * 
 * @author A078276
 */
public class BitmapUtils
{
    private static final String CLASSNAME = BitmapUtils.class.getSimpleName();

    Set<SoftReference<Bitmap>> mReusableBitmaps;

    private BitmapUtils()
    {
        if(VersionUtils.hasHoneycomb())
        {
            mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight)
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize_(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] options.inBitmap == bitmap? '%b' (%d))", CLASSNAME,
                    "decodeSampledBitmapFromResource", (options.inBitmap == bitmap), bitmap.hashCode());
        }
        return bitmap;
    }

    public static int calculateInSampleSize_(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth)
        {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() == targetOptions.outWidth && candidate.getHeight() == targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    static int getBytesPerPixel(Config config)
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
