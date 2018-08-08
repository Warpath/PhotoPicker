package com.lianlian.app.photopicker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lianlian.app.photopicker.internal.entity.Image;
import com.lianlian.app.photopicker.R;

import java.util.List;


/**
 * Created by warpath on 2017/11/6.
 */

public class SelectPicFullScreenFragment extends DialogFragment implements View.OnClickListener {

    ViewPager mViewPager;
    ImageView mIvTitleBack;
    ImageView mIvSelectPic;
    TextView mTvTitle;
    TextView mTvSure;

    private List<Image> mAllImages;
    private SelectPicImagePagerAdapter mAdapter;
    private SelectOperationCallback mSelectOperationCallback;
    private List<Image> mSelectedImages;
    private int mCurrentPosition;

    public static SelectPicFullScreenFragment newInstance(SelectOperationCallback selectCallback) {
        SelectPicFullScreenFragment selectPicFullScreenFragment = new SelectPicFullScreenFragment();
        selectPicFullScreenFragment.setSelectOperationCallback(selectCallback);
        return selectPicFullScreenFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hm_dlg_select_pic_full_screen, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_select_pic_full_screen);
        mIvTitleBack = (ImageView) view.findViewById(R.id.iv_select_pic_full_screen_back);
        mIvSelectPic = (ImageView) view.findViewById(R.id.iv_select_pic);
        mTvTitle = (TextView) view.findViewById(R.id.tv_select_pic_full_screen_title);
        mTvSure = (TextView) view.findViewById(R.id.tv_select_pic_full_screen_sure);
        mIvTitleBack.setOnClickListener(this);
        mIvSelectPic.setOnClickListener(this);
        mTvSure.setOnClickListener(this);

        mAdapter = new SelectPicImagePagerAdapter(getActivity(), mAllImages);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mTvTitle.setText(getString(R.string.hm_select_pic_count, mCurrentPosition + 1, mAllImages.size()));
                initChecked();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(mCurrentPosition, false);
        changeSureTextColor();
        return view;
    }

    public void setAllImages(List<Image> images) {
        mAllImages = images;
    }

    public void setSelectedImages(List<Image> images) {
        mSelectedImages = images;
    }

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
    }

    public void setSelectOperationCallback(SelectOperationCallback selectOperationCallback) {
        mSelectOperationCallback = selectOperationCallback;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_select_pic_full_screen_back) {
            dismiss();
        } else if (id == R.id.iv_select_pic) {
            Image image = mAllImages.get(mCurrentPosition);
            PhotoPickerActivity activity = ((PhotoPickerActivity) getActivity());
            if (mSelectedImages.contains(image)) {
                if (activity.checkedChanged(image, false)) {
                    activity.updateFullScreenSelect(mCurrentPosition, false);
                    mIvSelectPic.setSelected(false);
                }
            } else {
                if (activity.checkedChanged(image, true)) {
                    activity.updateFullScreenSelect(mCurrentPosition, true);
                    mIvSelectPic.setSelected(true);
                }
            }
            changeSureTextColor();
        } else if (id == R.id.tv_select_pic_full_screen_sure) {
            if (mSelectOperationCallback != null) {
                mSelectOperationCallback.onSelectedAndSure();
            }
            this.dismiss();
        }
    }

    private void changeSureTextColor() {
        if (mSelectedImages != null && mSelectedImages.size() > 0) {
            mTvSure.setEnabled(true);
        } else {
            mTvSure.setEnabled(false);
        }
    }

    private void initChecked() {
        Image image = mAllImages.get(mCurrentPosition);
        if (mSelectedImages.contains(image)) {
            mIvSelectPic.setSelected(true);
        } else {
            mIvSelectPic.setSelected(false);
        }
    }

    public interface SelectOperationCallback {

        void onSelectedAndSure();
    }

    private class SelectPicImagePagerAdapter extends PagerAdapter {

        private List<Image> mImages;
        private Context mContext;

        public SelectPicImagePagerAdapter(Context context, List<Image> images) {
            mContext = context;
            mImages = images;
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ViewPagerItemView itemView = new ViewPagerItemView(mContext);
            itemView.setData(mImages.get(position));
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ViewPagerItemView itemView = (ViewPagerItemView) object;
            container.removeView(itemView);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class ViewPagerItemView extends FrameLayout {

        private ImageView mImageView;
        private Image mImage;
        private int mPlaceHolderResId;

        public ViewPagerItemView(Context context) {
            super(context);
            setupViews();
        }

        public ViewPagerItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray ta = ((Activity)context)
                    .obtainStyledAttributes(new int[]{R.attr.fullScreen_image_placeHolderDrawable});
            mPlaceHolderResId = ta.getResourceId(0, R.drawable.hm_bg_chooseimage_default);
            ta.recycle();
            setupViews();
        }

        private void setupViews() {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(R.layout.hm_vp_image_itemview, null);
            mImageView = (ImageView) view.findViewById(R.id.iv_image_itemview);
            addView(view);
        }

        public void setData(Image image) {
            this.mImage = image;
            Glide.with(this.mImageView.getContext())
                    .load(PhotoPickerActivity.FILE_PROTOCOL + mImage.getPath()).fitCenter()
                    .placeholder(mPlaceHolderResId)
                    .error(mPlaceHolderResId)
                    .into(this.mImageView);
        }
    }
}
