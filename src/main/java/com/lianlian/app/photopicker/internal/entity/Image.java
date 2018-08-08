package com.lianlian.app.photopicker.internal.entity;

import java.io.Serializable;

/**
 * This class is for the images of the system Created by warpath on 2017/11/6.
 */

public class Image implements Serializable {

    private long mId;
    private long mSize;
    private String mAlbumName;
    private String mPath;

    public Image() {

    }

    public Image(long id, long size, String albumName, String path) {
        this.mId = id;
        this.mSize = size;
        this.mAlbumName = albumName;
        this.mPath = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        Image image = (Image) o;
        if (this.mId == image.getId()) {
            return true;
        }
        if (this.mPath.equals(image.getPath())) {
            return true;
        }

        return false;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }
}
