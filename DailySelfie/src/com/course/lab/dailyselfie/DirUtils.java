package com.course.lab.dailyselfie;

import static com.course.lab.dailyselfie.Constants.ALBUM_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Build;
import android.os.Environment;

public class DirUtils
{
    private static final String CLASSNAME = DirUtils.class.getSimpleName();
    private static AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private DirUtils()
    {
    }

    static
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        }
        else
        {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    private static String getAlbumName()
    {
        return ALBUM_NAME;
    }
    
    public static boolean isExternalStorageWritable()
    {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    
    public static File getAlbumDir()
    {
        File storageDir = null;
        //if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        if(isExternalStorageWritable())
        {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if(storageDir != null)
            {
                if(!storageDir.mkdirs())
                {
                    if(!storageDir.exists())
                    {
                        Logger.warn("[%s.%s] Failed to create album directory = '%s'", CLASSNAME, "getAlbumDir", storageDir);
                        return null;
                    }
                }
            }
        }
        else
        {
            if(Logger.isDebugEnabled())
            {
                Logger.debug("[%s.%s] External storage is not mounted READ/WRITE", CLASSNAME, "getAlbumDir");
            }
        }
        return storageDir;
    }
    
    /**
     * Returns the absolute path for each file/image in the album directory
     * 
     * @return
     */
    public static List<String> getPhotos()
    {
        File albumDir = getAlbumDir();
        List<String> photoPaths = new ArrayList<String>();
        
        if(albumDir == null || !albumDir.exists() || !albumDir.isDirectory())
        {
            Logger.warn("[%s.%s] Unable to locate album directory '%s'", CLASSNAME, "getPhotos", albumDir);
            
            return photoPaths;
        }

        File[] albumImages = albumDir.listFiles();
        for(File f : albumImages)
        {
            photoPaths.add(f.getAbsolutePath());
        }
        
        Collections.sort(photoPaths); // natural order sort should work for ordering by date/age
        
        return photoPaths;
    }
    
    /**
     * Returns the absolute path for the file/image in the album directory at the specified position
     * or null
     * 
     * @param position
     * @return
     */
    public static String getPhoto(int position)
    {
        List<String> photoPaths = getPhotos();
        if(photoPaths.size() < position)
        {
            return null;
        }
        return photoPaths.get(position);
    }

}
