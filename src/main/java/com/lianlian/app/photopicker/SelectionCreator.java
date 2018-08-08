package com.lianlian.app.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.lianlian.app.photopicker.internal.entity.Image;
import com.lianlian.app.photopicker.internal.entity.SelectionSpec;
import com.lianlian.app.photopicker.ui.PhotoPickerActivity;

import java.util.ArrayList;

/**
 * Fluent API for building photo select specification
 */

public final class SelectionCreator {
    private final PhotoPicker mPhotoPicker;
    private final SelectionSpec mSelectionSpec;

    /**
     * Constructs a new specification builder on the context.
     * @param photoPicker a requester context wrapper.
     */
    SelectionCreator(PhotoPicker photoPicker) {
        mPhotoPicker = photoPicker;
        mSelectionSpec = SelectionSpec.getCleanInstance();
    }

    /**
     * Theme for photo selecting Activity
     * <p>
     *     There is one built-in themes
     *     1.com.lianlian.app.photopicker.R.style.PhotoPicker_Health
     *     you can define a custom theme derived from the above one or other themes.
     * @param themeId
     * @return
     */
    public SelectionCreator theme(@StyleRes int themeId) {
        mSelectionSpec.themeId = themeId;
        return this;
    }

    /**
     * Maximun selectable count
     * @param maxSelectable maxSelectable Maximum selectable count. Default value is 1.
     * @return
     */
    public SelectionCreator maxSelectable(int maxSelectable) {
        if (maxSelectable < 1)
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        mSelectionSpec.maxSelectable = maxSelectable;
        return this;
    }

    /**
     * Show the images has been selected
     * @param selectedImageList
     * @return
     */
    public SelectionCreator selectedImageList(ArrayList<Image> selectedImageList) {
        mSelectionSpec.selectedImageList = selectedImageList;
        return this;
    }

    /**
     * Start to select photo and wait for result
     * @param requestCode requestCode Identity of the request Activity or Fragment.
     */
    public void forResult(int requestCode) {
        Activity activity = mPhotoPicker.getActivity();
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, PhotoPickerActivity.class);

        Fragment fragment = mPhotoPicker.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }
}
