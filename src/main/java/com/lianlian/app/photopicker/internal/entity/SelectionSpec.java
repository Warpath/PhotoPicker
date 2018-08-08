package com.lianlian.app.photopicker.internal.entity;

import android.support.annotation.StyleRes;

import com.lianlian.app.photopicker.R;

import java.util.ArrayList;

/**
 * Created by warpath on 2018/6/8.
 */

public final class SelectionSpec {
    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }

    @StyleRes
    public int themeId;
    public int maxSelectable;
    public ArrayList<Image> selectedImageList;

    private SelectionSpec() {}

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = SelectionSpec.getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    private void reset() {
        themeId = R.style.PhotoPicker_Health;
        maxSelectable = 9;
        selectedImageList = null;
    }
}
