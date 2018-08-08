package com.lianlian.app.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.lianlian.app.photopicker.internal.entity.Image;
import com.lianlian.app.photopicker.ui.PhotoPickerActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by warpath on 2018/6/8.
 */

public final class PhotoPicker {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private PhotoPicker(Activity activity) {
        this(activity, null);
    }

    private PhotoPicker(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private PhotoPicker(Activity activity, Fragment fragment) {
        mContext = new WeakReference<Activity>(activity);
        mFragment = new WeakReference<Fragment>(fragment);
    }

    /**
     * Obtain user selected image list in the starting Activity or Fragment.
     * @param data Intent passed by onActivityResult(int, int, Intent)
     * @return User selected image' list
     */
    public static List<Image> obtainResult(Intent data) {
        return (List<Image>) data.getSerializableExtra(PhotoPickerActivity.RESULT_SELECTED_IMAGE);
    }

    /**
     * Start Matisse from an Activity.
     * <p>
     * This Activity's {@link Activity#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     * @param activity
     * @return
     */
    public static PhotoPicker from(Activity activity) {
        return new PhotoPicker(activity);
    }

    /**
     * Start Matisse from a Fragment.
     * <p>
     * This Fragment's {@link Fragment#onActivityResult(int, int, Intent)} will be called when user
     * finishes selecting.
     * @param fragment
     * @return
     */
    public static PhotoPicker from(Fragment fragment) {
        return new PhotoPicker(fragment);
    }

    public SelectionCreator builder() {
        return new SelectionCreator(this);
    }

    @NonNull
    Activity getActivity() {
        return mContext.get();
    }

    @NonNull
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }
}
