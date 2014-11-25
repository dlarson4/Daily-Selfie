package com.course.lab.dailyselfie;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


public class GalleryPickerAdapter extends BaseAdapter
{
    private static final String CLASSNAME = GalleryPickerAdapter.class.getSimpleName();
    
    public static final int TYPE_THUMBNAIL = 1;
    public static final int TYPE_MICROTHUMBNAIL = 2;

    private ArrayList<String> mItems = new ArrayList<String>();
    //private LayoutInflater mInflater;
    //private LoadStyle loadStyle;
    private Context context;

    private LruCache<String, Bitmap> mMemoryCache;
    
//    private enum LoadStyle
//    {
//        Sync, Async;
//    }

//    GalleryPickerAdapter(LayoutInflater inflater, LruCache<String, Bitmap> memoryCache)
//    {
//        this.mInflater = inflater;
//        this.loadStyle = LoadStyle.Async;
//        this.mMemoryCache = memoryCache;
//    }
    
    GalleryPickerAdapter(Context context, LruCache<String, Bitmap> memoryCache)
    {
        this.context = context;
        //this.loadStyle = LoadStyle.Async;
        this.mMemoryCache = memoryCache;
    }

    public void addAll(List<String> photos)
    {
        mItems.addAll(photos);
    }

    public void updateDisplay()
    {
        notifyDataSetChanged();
    }

    public void clear()
    {
        mItems.clear();
    }

    public int getCount()
    {
        return mItems == null ? 0 : mItems.size();
    }

    public Object getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        //View view;
        ViewHolder holder;
        
        if(convertView == null)
        {
            //convertView = mInflater.inflate(R.layout.gallery_picker_item, null);
            convertView = LayoutInflater.from(context).inflate(R.layout.gallery_picker_item, null);
            
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        }
        else
        {
            //view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        
        //ImageView imageView = (ImageView)view.findViewById(R.id.thumbnail);
        ImageView imageView = holder.imageView;        

        //int targetSize = (int)mInflater.getContext().getResources().getDimension(R.dimen.image_thumbnail_size);
        int targetSize = (int)context.getResources().getDimension(R.dimen.image_thumbnail_size);
        final String filePath = mItems.get(position);
        
        //loadBitmap(filePath, targetSize, imageView);
        loadBitmapPicasso(filePath, imageView);
        
        return convertView;
    }

    
    static class ViewHolder
    {
        ImageView imageView;
    }
    
    private void loadBitmapPicasso(String path, ImageView imageView)
    {
        Picasso.with(context)
            .load("file:" + path)
            .resizeDimen(R.dimen.image_thumbnail_size, R.dimen.image_thumbnail_size)
            .centerInside()
            .error(R.drawable.empty_photo)
            .placeholder(R.drawable.empty_photo)
            .into(imageView);
    }

    /**
     * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
     * 
     */
    private void loadBitmap(String path, int targetSize, ImageView imageView)
    {
        Bitmap cachedBitmap = null; 
        if(mMemoryCache != null)
        {
            cachedBitmap = mMemoryCache.get(createCacheKey(path, targetSize, targetSize));
        }
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] cachedBitmap = '%s'", CLASSNAME, "loadBitmap", cachedBitmap);
        }
        
        if(cachedBitmap != null)
        {
            imageView.setImageBitmap(cachedBitmap);
        }
        else
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] No cached Bitmap found, loading from disk", CLASSNAME, "loadBitmap");
            }
            
            final TaskData data = new TaskData();
            data.filePath = path;
            data.width = targetSize;
            data.height = targetSize;
    
            if(cancelPotentialWork(data, imageView))
            {
                final BitmapWorkerTaskForPicker task = new BitmapWorkerTaskForPicker(imageView);
                //final Resources resources = mInflater.getContext().getResources();
                final Resources resources = context.getResources();
    
                Bitmap mPlaceHolderBitmap = BitmapFactory.decodeResource(resources, R.drawable.empty_photo);
    
                final AsyncDrawable asyncDrawable = new AsyncDrawable(resources, mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(data);
            }
        }
    }
    
    /**
     * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
     * 
     */
    static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<BitmapWorkerTaskForPicker> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTaskForPicker bitmapWorkerTask)
        {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTaskForPicker>(bitmapWorkerTask);
        }

        public BitmapWorkerTaskForPicker getBitmapWorkerTask()
        {
            return bitmapWorkerTaskReference.get();
        }
    }

    private class BitmapWorkerTaskForPicker extends AsyncTask<TaskData, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        TaskData data = null;
        private long startTime;

        public BitmapWorkerTaskForPicker(ImageView imageView)
        {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            startTime = System.currentTimeMillis();
        }

        @Override
        protected Bitmap doInBackground(TaskData... params)
        {
            data = params[0];
            return BitmapUtils.decodeSampledBitmapFromResource(data.filePath, data.width, data.height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if(isCancelled())
            {
                bitmap = null;
            }

            if(imageViewReference != null && bitmap != null)
            {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTaskForPicker bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if(this == bitmapWorkerTask && imageView != null)
                {
                    if(Logger.isDebugEnabled())
                    {
                        Logger.debug("[%s.%s] Adding bitmap to cache", CLASSNAME, "onPostExecute");
                    }
                    if(GalleryPickerAdapter.this.mMemoryCache != null)
                    {
                        final String key = createCacheKey(data.filePath, data.width, data.height); 
                        GalleryPickerAdapter.this.mMemoryCache.put(key, bitmap);
                    }
                    
                    imageView.setImageBitmap(bitmap);
                }
            }
            
            if(Logger.isDebugEnabled())
            {
                final long endTime = System.currentTimeMillis();
                String duration = String.valueOf((endTime - startTime) / 1000.00);
                Logger.debug("[%s.%s] bitmap load time = '%s'", CLASSNAME, "onPostExecute", duration);
            }
        }
    }

    /**
     * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
     * 
     */
    private boolean cancelPotentialWork(TaskData data, ImageView imageView)
    {
        final BitmapWorkerTaskForPicker bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if(bitmapWorkerTask != null)
        {
            final TaskData bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if(bitmapData == null || bitmapData != data)
            {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            }
            else
            {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
     * 
     */
    private BitmapWorkerTaskForPicker getBitmapWorkerTask(ImageView imageView)
    {
        if(imageView != null)
        {
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable)
            {
                final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class TaskData
    {
        String filePath;
        int width;
        int height;
    }
    
    private String createCacheKey(String path, int width, int height)
    {
        return new StringBuilder().append(path).append("__").append(width).append("__").append(height).toString();
    }

}
