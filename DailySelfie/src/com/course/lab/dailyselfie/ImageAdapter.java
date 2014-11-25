package com.course.lab.dailyselfie;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
    private Context mContext;
    private List<String> imagePaths = null;

    public ImageAdapter(Context c)
    {
        this.mContext = c;
        this.imagePaths = new ArrayList<String>();
        loadAlbumImagePaths();
    }

    private void loadAlbumImagePaths()
    {
        imagePaths = DirUtils.getPhotos();
    }

    public int getCount()
    {
        return imagePaths == null ? 0 : imagePaths.size();
    }

    public Object getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView;
        if(convertView == null)
        {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
        }
        else
        {
            imageView = (ImageView)convertView;
        }

        imageView.setImageURI(Uri.parse(imagePaths.get(position)));
        return imageView;
    }

}
