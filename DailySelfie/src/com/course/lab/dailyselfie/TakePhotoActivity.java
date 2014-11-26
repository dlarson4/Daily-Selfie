package com.course.lab.dailyselfie;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

public class TakePhotoActivity extends Activity
{
    private static final String CLASSNAME = TakePhotoActivity.class.getSimpleName();
    
    private static final int ACTION_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_photo_layout);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] dispatching intent to take photo", CLASSNAME, "onCreate");
        }
        dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
    }
    
    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = DirUtils.getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException
    {
        return createImageFile();
    }

    private void addPicToGallery()
    {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch(actionCode)
        {
            case ACTION_TAKE_PHOTO:
            {
                File f = null;
                try
                {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                }
                catch(IOException e)
                {
                    Logger.error("[%s.%s] Error creating file for photo", e, CLASSNAME, "dispatchTakePictureIntent");
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;
            }
            default:
            {
                break;
            }
        }

        if(isIntentAvailable(this, takePictureIntent))
        {
            startActivityForResult(takePictureIntent, actionCode);
        }
        else
        {
            String msg = getResources().getString(R.string.no_camera_apps_found);
            Logger.warn("[%s.%s] msg", CLASSNAME, "dispatchTakePictureIntent");
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleNewCameraPhoto()
    {
        if(mCurrentPhotoPath != null)
        {
            addPicToGallery();
            mCurrentPhotoPath = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case ACTION_TAKE_PHOTO:
            {
                if(Logger.isDebugEnabled())
                {
                    Logger.debug("[%s.%s] Result code '%d'", CLASSNAME, "onActivityResult", resultCode);
                }
                if(resultCode == RESULT_OK)
                {
                    handleNewCameraPhoto();
                }
                else if(resultCode == RESULT_CANCELED)
                {
                    deleteImageFile();
                }
                if(Logger.isDebugEnabled())
                {
                    Logger.debug("[%s.%s] Returning to previous activity", CLASSNAME, "onActivityResult");
                }
                finish(); // return to previous activity
                break;
            } 
        }
    }
    
    private void deleteImageFile()
    {
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] Attempting to delete '%s'", CLASSNAME, "deleteImageFile", mCurrentPhotoPath);
        }
        if(mCurrentPhotoPath != null)
        {
            File f = new File(mCurrentPhotoPath);
            if(f.exists() && f.canWrite())
            {
                boolean result = f.delete();
                if(Logger.isDebugEnabled())
                {
                    Logger.debug("[%s.%s] File was deleted? '%b'", CLASSNAME, "deleteImageFile", result);
                }
            }
        }
    }

    /**
     * Indicates whether the specified action can be used as an intent. This method queries the package manager for
     * installed packages that can respond to an intent with the specified action. If no suitable package is found, this
     * method returns false. http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     * 
     * @return True if an Intent with the specified action can be sent and responded to, false otherwise.
     */
    private boolean isIntentAvailable(Context context, final Intent intent)
    {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
}
