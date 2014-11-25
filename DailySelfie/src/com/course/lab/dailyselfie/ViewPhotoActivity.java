package com.course.lab.dailyselfie;

import static com.course.lab.dailyselfie.Constants.IMAGE_POSITION_EXTRA;

import java.lang.ref.WeakReference;

import com.squareup.picasso.Picasso;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

//@TargetApi(Build.VERSION_CODES.KITKAT)
public class ViewPhotoActivity extends Activity
{
    private static final String CLASSNAME = ViewPhotoActivity.class.getSimpleName();

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView mImageView;
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private int mImagePosition;

    // height and width of the ImageView, set once it's ready to be drawn and has been measured
    private int finalHeight = 0;
    private int finalWidth = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo_layout);

        mImagePosition = getIntent().getExtras().getInt(IMAGE_POSITION_EXTRA);
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] mImagePosition = '%d'", CLASSNAME, "onCreate", mImagePosition);
        }

        mImageView = (ImageView)findViewById(R.id.viewPhotoImageview);
        ViewTreeObserver vto = mImageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            @Override
            public boolean onPreDraw()
            {
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                finalHeight = mImageView.getMeasuredHeight();
                finalWidth = mImageView.getMeasuredWidth();

                if(Logger.isDebugEnabled())
                {
                    Logger.debug("[%s.%s] ImageView height: '%d', width: '%d'", CLASSNAME, "onPreDraw", finalHeight,
                            finalWidth);
                }

                loadPhotoPathAsync();
                return true;
            }
        });
    }
    
    private void loadPhotoPathAsync()
    {
        new PhotoLoadTask().execute(mImagePosition);
    }

    private void displayScaledPhoto()
    {
        if(this.mCurrentPhotoPath == null)
        {
            Logger.warn("[%s.%s] Image at position '%d' not found in album", CLASSNAME, "setPhotoPath", mImagePosition);
            Toast.makeText(this, "Image not found in album", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        
        BitmapTaskData data = new BitmapTaskData();
        data.filePath = mCurrentPhotoPath;
        data.width = finalWidth;
        data.height = finalHeight;

        new BitmapWorkerTask(mImageView).execute(data);
    }
    
    private void displayScaledPhotoPicasso()
    {
        if(this.mCurrentPhotoPath == null)
        {
            Logger.warn("[%s.%s] Image at position '%d' not found in album", CLASSNAME, "setPhotoPath", mImagePosition);
            Toast.makeText(this, "Image not found in album", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        
        Picasso.with(this)
            .load("file:" + mCurrentPhotoPath)
            .resize(finalWidth, finalHeight)
            .centerInside()
            .error(R.drawable.empty_photo)
            .placeholder(R.drawable.empty_photo)
            .skipMemoryCache()
            .into(mImageView);
    }
    

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? ImageView.VISIBLE
                : ImageView.INVISIBLE);
    }

    /**
     * Load a scaled bitmap from disk asynchronously
     */
    private class BitmapWorkerTask extends AsyncTask<BitmapTaskData, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        BitmapTaskData data = null;

        public BitmapWorkerTask(ImageView imageView)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(BitmapTaskData... params)
        {
            data = params[0];
            return BitmapUtils.decodeSampledBitmapFromResource(data.filePath, data.width, data.height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if(imageViewReference != null && bitmap != null)
            {
                final ImageView imageView = imageViewReference.get();
                if(imageView != null)
                {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class BitmapTaskData
    {
        String filePath;
        int width;
        int height;
    }

    
    /**
     * Get the path to the image based on the index asynchronously
     */
    private class PhotoLoadTask extends AsyncTask<Integer, Void, String>
    {
        @Override
        protected String doInBackground(Integer... params)
        {
            int position = params[0];
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] Loading photo from storage at position '%d'", "PhotoLoadTask", "doInBackground", position);
            }
            return DirUtils.getPhoto(position);
        }

        @Override
        protected void onPostExecute(String filePath)
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] Loaded file path '%s'", "PhotoLoadTask", "onPostExecute", filePath);
            }
            ViewPhotoActivity.this.mCurrentPhotoPath = filePath;
            //ViewPhotoActivity.this.displayScaledPhoto();
            ViewPhotoActivity.this.displayScaledPhotoPicasso();
        }
    }
}
