package com.course.lab.dailyselfie;

import java.io.File;

abstract class AlbumStorageDirFactory
{
    public abstract File getAlbumStorageDir(String albumName);
}
