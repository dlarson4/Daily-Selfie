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
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.LruCache;

/**
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 * 
 * @author A078276
 */
public class BitmapUtils
{
    private static final String CLASSNAME = BitmapUtils.class.getSimpleName();

    Set<SoftReference<Bitmap>> mReusableBitmaps;
    private LruCache<String, BitmapDrawable> mMemoryCache;

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

        // // If we're running on Honeycomb or newer, try to use inBitmap.
        // if(Utils.hasHoneycomb())
        // {
        // addInBitmapOptions(options);
        // if(Logger.isDebugEnabled())
        // {
        // Logger.debug("[%s.%s] options.inBitmap after call to addInBitmapOptions '%s'", CLASSNAME,
        // "decodeSampledBitmapFromResource", options.inBitmap);
        // }
        // }
        
//        if (Utils.hasHoneycomb()) 
//        {
//            addInBitmapOptions(options, CacheManager.getImageCache());
//            
//            if(Logger.isDebugEnabled())
//            {
//                Logger.debug("[%s.%s] options.inBitmap after call to addInBitmapOptions '%s'", CLASSNAME,
//                        "decodeSampledBitmapFromResource", options.inBitmap);
//            }
//        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        final Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] options.inBitmap == bitmap? '%b' (%d))", CLASSNAME,
                    "decodeSampledBitmapFromResource", (options.inBitmap == bitmap), bitmap.hashCode());
        }

        // if(Utils.hasHoneycomb())
        // {
        // // logic from com.android.gallery3d.data.DecodeUtils:decodeUsingPool (Gallery2 app)
        // if (options.inBitmap != null && options.inBitmap != bitmap)
        // {
        // ReusableBitmaps.INSTANCE.add(bitmap);
        // options.inBitmap = null;
        // }
        // }

        return bitmap;
    }

    private static void addInBitmapOptions(BitmapFactory.Options options)
    {
        // inBitmap only works with mutable bitmaps, so force the decoder to return mutable bitmaps.
        options.inMutable = true;
        options.inSampleSize = 1;

        // Try to find a bitmap to use for inBitmap.
        Bitmap inBitmap = ReusableBitmaps.INSTANCE.getBitmapFromReusableSet(options);

        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] inBitmap returned from Set '%s'", CLASSNAME, "addInBitmapOptions", inBitmap);
        }

        if(inBitmap != null)
        {
            // If a suitable bitmap has been found, set it as the value of inBitmap.
            options.inBitmap = inBitmap;
        }
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

    // This method iterates through the reusable bitmaps, looking for one
    // to use for inBitmap:
    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options)
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
