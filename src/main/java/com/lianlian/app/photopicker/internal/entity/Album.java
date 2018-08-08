package com.lianlian.app.photopicker.internal.entity;

import java.util.ArrayList;

/**
 * As the image dir of the system Created by warpath on 2017/11/6.
 */

public class Album {

    private String mName;
    private String mCoverImagePath;
    private ArrayList<Integer> mImageIndexs;
    private boolean mIsAllImages = false;

    public Album(String name, String coverImagePath, boolean isAllImages) {
        mName = name;
        mCoverImagePath = coverImagePath;
        mImageIndexs = new ArrayList<>();
        mIsAllImages = isAllImages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        Album album = (Album) o;

        return album.getName().equals(this.mName);
    }

    /**
     * return the image numbers
     */
    public int getCount() {
        return mImageIndexs.size();
    }

    public void addImageIndex(int position) {
        mImageIndexs.add(position);
    }

    public String getName() {
        return mName;
    }

    public String getCoverImagePath() {
        return mCoverImagePath;
    }

    public ArrayList<Integer> getImageIndexs() {
        return mImageIndexs;
    }

    public boolean isAllImages() {
        return mIsAllImages;
    }

}
