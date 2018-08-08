package com.lianlian.app.photopicker.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lianlian.app.photopicker.internal.entity.Album;
import com.lianlian.app.photopicker.internal.entity.Image;
import com.lianlian.app.photopicker.R;
import com.lianlian.app.photopicker.internal.entity.SelectionSpec;
import com.markupartist.android.widget.ActionBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by warpath on 2018/1/18.
 */

public class PhotoPickerActivity extends AppCompatActivity implements View.OnClickListener,
        ChooseAlbumPopWindow.OnChooseAlbumListener {
    public static final String RESULT_SELECTED_IMAGE = "result_selected_image";
    public static final String FILE_PROTOCOL = "file://";
    public static final int MAX_SELECTED_NUM = 9;
    private static final int MESSAGE_START = 0;
    private static final int REQ_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    private ActionBar mActionBar;
    private RelativeLayout mRlBottomLayout;
    private TextView mTvChooseAlbum;
    private TextView mTvImageNum;
    private GridView mGridView;
    private TextView mTvConfirm;

    private ArrayList<Image> mAllImages; //all images
    private ArrayList<Image> mCurImages; //the current display images;
    private ArrayList<Image> mSelectedImages; //the selected images;
    private ArrayList<Album> mAlbums; //all albums
    private MyAdapter mAdapter;
    private ChooseAlbumPopWindow mChooseAlbumPopWindow;
    private Album mCurAlbum;

    private int mImageViewHeight = 360;
    private int mMaxSelectedNum;

    private MyHandler mHandler = new MyHandler(this);
    private SelectionSpec mSpec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                performShowPermissionRationale();
            } else {
                performRequestPermission();
            }
        } else {
            continueOnCreate();
        }
    }

    private void performRequestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQ_PERMISSIONS_READ_EXTERNAL_STORAGE);
    }

    private void performShowPermissionRationale() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.need_read_external_storage_permission)
                .setNegativeButton(R.string.permission_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.permission_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performRequestPermission();
                    }
                })
                .create()
                .show();
    }

    private void continueOnCreate() {
        mSpec = SelectionSpec.getInstance();
        setTheme(mSpec.themeId);
        mMaxSelectedNum = mSpec.maxSelectable == 0 ? MAX_SELECTED_NUM : mSpec.maxSelectable;
        setContentView(R.layout.activity_photo_picker);

        initView();

        mAllImages = new ArrayList<Image>();
        mAlbums = new ArrayList<Album>();
        mSelectedImages = new ArrayList<Image>();
        mCurImages = new ArrayList<Image>();

        if (mSpec.selectedImageList != null && mSpec.selectedImageList.size() != 0) {
            mSelectedImages = mSpec.selectedImageList;
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        mImageViewHeight =
                (width - 5 * getResources().getDimensionPixelSize(R.dimen.hm_chooseimage_gridview_verticalSpacing)) / 4;

        mAdapter = new MyAdapter();
        mGridView.setAdapter(mAdapter);

        mChooseAlbumPopWindow =
                new ChooseAlbumPopWindow(LayoutInflater.from(this).inflate(R.layout.hm_pw_choose_album, null),
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.7), mAlbums, this);

        queryImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                continueOnCreate();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MESSAGE_START);
    }

    public void updateFullScreenSelect(int position, boolean checked) {
        if (position < 0) {
            return;
        }
        View v = mGridView.getChildAt(position - mGridView.getFirstVisiblePosition());
        if (v != null) {
            Object object = v.getTag();
            if (object != null) {
                ViewHolder viewHolder = (ViewHolder) object;
                if (checked) {
                    viewHolder.mIvCheckBox.setSelected(false);
                } else {
                    viewHolder.mIvCheckBox.setSelected(true);
                }
            }
        }
    }

    public boolean checkedChanged(Image image, boolean isChecked) {
        int selectedSize = mSelectedImages.size();
        if (isChecked) {
            if (selectedSize == mMaxSelectedNum) {
                Toast.makeText(this, getString(R.string.hm_toast_reach_max_select_num, mMaxSelectedNum),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!mSelectedImages.contains(image)) {
                mSelectedImages.add(image);
            }
        } else {
            mSelectedImages.remove(image);
        }
        selectedSize = mSelectedImages.size();
        mActionBar.setTitle(getString(R.string.hm_title_selected_num, selectedSize));
        if (selectedSize > 0) {
            mTvConfirm.setEnabled(true);
        } else {
            mTvConfirm.setEnabled(false);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_bottom_layout) {
            mChooseAlbumPopWindow.showAsDropDown(v);
            backgroundAlpha(this, 0.3f);
        } else if (id == R.id.iv_checkbox) {
            int position = (int) v.getTag(R.id.hm_id_choose_image_position);
            ViewHolder viewHolder = (ViewHolder) v.getTag(R.id.hm_id_choose_image_viewholder);
            Image image = mCurImages.get(position);
            if (mSelectedImages.contains(image)) {
                if (checkedChanged(image, false)) {
                    viewHolder.mIvCheckBox.setSelected(false);
                }
            } else {
                if (checkedChanged(image, true)) {
                    viewHolder.mIvCheckBox.setSelected(true);
                }
            }
        } else if (id == R.id.iv_image) {
            int position = (int) v.getTag(R.id.hm_id_choose_image_position_real);
            SelectPicFullScreenFragment selectPicFullScreenFragment =
                    SelectPicFullScreenFragment.newInstance(new SelectPicFullScreenFragment.SelectOperationCallback() {
                        @Override
                        public void onSelectedAndSure() {
                            Intent intent = new Intent();
                            intent.putExtra(PhotoPickerActivity.RESULT_SELECTED_IMAGE, mSelectedImages);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    });
            selectPicFullScreenFragment.setAllImages(mCurImages);
            selectPicFullScreenFragment.setSelectedImages(mSelectedImages);
            selectPicFullScreenFragment.setCurrentPosition(position);
            selectPicFullScreenFragment.show(getSupportFragmentManager(), "selectPicFullScreenFragment");
        }
    }

    private void initView() {
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.toolbar_textAction_textColor});
        int textActionColor = ta.getColor(0, getResources().getColor(R.color.yellow));
        ta.recycle();
        mActionBar = (ActionBar) findViewById(R.id.view_actionbar);
        mActionBar.setHomeAsUpAction(new ActionBar.HomeAsUpAction(this, R.drawable.icon_back));
        mActionBar.setTitle(R.string.title_photo_picker);
        mTvConfirm = mActionBar.addTextAction(new SendAction(0));
        mTvConfirm.setEnabled(false);
        mTvConfirm.setTextColor(textActionColor);

        mRlBottomLayout = (RelativeLayout) findViewById(R.id.rl_bottom_layout);
        mTvChooseAlbum = (TextView) findViewById(R.id.tv_choose_album);
        mTvImageNum = (TextView) findViewById(R.id.tv_image_num);
        mGridView = (GridView) findViewById(R.id.gv_images);
        mRlBottomLayout.setOnClickListener(this);
    }

    private void queryImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                                MediaStore.Images.Media.DATE_MODIFIED + " DESC ");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int position = cursor.getPosition();
                        Long id = cursor.getLong(0);
                        String albumName = cursor.getString(2);
                        Image image = new Image(id, cursor.getLong(1), albumName, cursor.getString(3));
                        mAllImages.add(image);
                        Album newAlbum = new Album(albumName, image.getPath(), false);
                        if (mAlbums.size() == 0) {
                            mCurAlbum = new Album(getString(R.string.hm_text_all_image), image.getPath(), true);
                            mAlbums.add(mCurAlbum);
                        }
                        if (mAlbums.contains(newAlbum)) {
                            Album album = mAlbums.get(mAlbums.indexOf(newAlbum));
                            album.addImageIndex(position);
                        } else {
                            newAlbum.addImageIndex(position);
                            mAlbums.add(newAlbum);
                        }
                    }
                    cursor.close();
                }
                mHandler.sendEmptyMessage(MESSAGE_START);
            }
        }).start();
    }

    @Override
    public void onChooseAlbum(Album album, int position) {
        if (mCurAlbum.equals(album)) {
            //do nothing
            return;
        }

        mCurAlbum = album;
        mTvChooseAlbum.setText(album.getName());
        mCurImages.clear();
        if (position == 0) {
            mCurImages.addAll(mAllImages);
        } else {
            mCurImages.addAll(getAlbumImages(album));
        }
        mAdapter.notifyDataSetChanged();
        mTvImageNum.setText(getString(R.string.hm_text_album_num, mCurImages.size()));
    }

    @Override
    public void onDismiss() {
        backgroundAlpha(this, 1f);
    }

    private static class MyHandler extends Handler {

        private WeakReference<PhotoPickerActivity> mWeakReferenceActivity;

        public MyHandler(PhotoPickerActivity activity) {
            mWeakReferenceActivity = new WeakReference<PhotoPickerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PhotoPickerActivity activity = mWeakReferenceActivity.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case MESSAGE_START:
                    activity.mCurImages.addAll(activity.mAllImages);
                    activity.mAdapter.notifyDataSetChanged();
                    activity.mChooseAlbumPopWindow.setDatas(activity.mAlbums);
                    activity.mTvImageNum
                            .setText(activity.getString(R.string.hm_text_album_num, activity.mCurImages.size()));
                    activity.mTvImageNum.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    private List<Image> getAlbumImages(Album album) {
        List<Image> result = new ArrayList<>();
        List<Integer> imageIndexs = album.getImageIndexs();
        for (Integer integer : imageIndexs) {
            result.add(mAllImages.get(integer));
        }
        return result;
    }

    /**
     * 改变window背景透明度
     */
    private void backgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
    }

    public class SendAction extends ActionBar.AbstractAction {

        public SendAction(int drawable) {
            super(drawable);
        }

        @Override
        public void performAction(View view) {
            Intent intent = new Intent();
            intent.putExtra(PhotoPickerActivity.RESULT_SELECTED_IMAGE, mSelectedImages);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public String getText() {
            return getResources().getString(R.string.hm_choose_image_finish);
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCurImages.size();
        }

        @Override
        public Object getItem(int position) {
            return mCurImages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mCurImages.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView =
                        LayoutInflater.from(PhotoPickerActivity.this).inflate(R.layout.hm_item_choose_image, null);
                viewHolder = new ViewHolder();
                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.iv_image);
                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) viewHolder.mImageView.getLayoutParams();
                params.height = mImageViewHeight;
                viewHolder.mImageView.setLayoutParams(params);

                viewHolder.mIvCheckBox = (ImageView) convertView.findViewById(R.id.iv_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Image image = mCurImages.get(position);
            Glide.with(PhotoPickerActivity.this).load(FILE_PROTOCOL + image.getPath())
                    .placeholder(R.drawable.hm_bg_chooseimage_default).error(R.drawable.hm_bg_chooseimage_default)
                    .centerCrop().into(viewHolder.mImageView);
            if (mSelectedImages.contains(image)) {
                viewHolder.mIvCheckBox.setSelected(true);
            } else {
                viewHolder.mIvCheckBox.setSelected(false);
            }
            viewHolder.mIvCheckBox.setTag(R.id.hm_id_choose_image_position, position);
            viewHolder.mIvCheckBox.setTag(R.id.hm_id_choose_image_viewholder, viewHolder);
            viewHolder.mIvCheckBox.setOnClickListener(PhotoPickerActivity.this);
            viewHolder.mImageView.setTag(R.id.hm_id_choose_image_position_real, position);
            viewHolder.mImageView.setOnClickListener(PhotoPickerActivity.this);
            return convertView;
        }
    }

    class ViewHolder {

        public ImageView mImageView;
        public ImageView mIvCheckBox;
    }
}
