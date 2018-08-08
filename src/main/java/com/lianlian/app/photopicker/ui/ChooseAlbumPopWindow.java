package com.lianlian.app.photopicker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lianlian.app.photopicker.internal.entity.Album;
import com.lianlian.app.photopicker.R;

import java.util.List;

/**
 * Created by warpath on 2017/11/6.
 */

public class ChooseAlbumPopWindow extends PopupWindow
        implements PopupWindow.OnDismissListener, AdapterView.OnItemClickListener {

    private MyAdapter mAdapter;
    private int mCurSelectedPosition = 0;
    private OnChooseAlbumListener mChooseAlbumListener;

    public ChooseAlbumPopWindow(View contentView, int width, int height, List<Album> albums,
            OnChooseAlbumListener listener) {
        super(contentView, width, height, false);
        setAnimationStyle(R.style.hm_pwChooseAlbumAnim);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);
        setOnDismissListener(this);
        setFocusable(true);
        setOutsideTouchable(true);

        mChooseAlbumListener = listener;

        ListView listView = (ListView) contentView.findViewById(R.id.lv_listview);
        mAdapter = new MyAdapter(contentView.getContext(), albums);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    public void setDatas(List<Album> albums) {
        mAdapter.setDatas(albums);
    }

    @Override
    public void onDismiss() {
        mChooseAlbumListener.onDismiss();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCurSelectedPosition = position;
        mChooseAlbumListener.onChooseAlbum((Album) parent.getAdapter().getItem(position), position);
        dismiss();
    }

    public interface OnChooseAlbumListener {

        public void onChooseAlbum(Album album, int position);

        public void onDismiss();
    }

    class MyAdapter extends BaseAdapter {

        private List<Album> mAlbums;
        private LayoutInflater mInflater;
        private Context mContext;
        private int mPlaceHolderResId;

        public MyAdapter(Context context, List<Album> albums) {
            mAlbums = albums;
            mInflater = LayoutInflater.from(context);
            mContext = context;
            TypedArray ta = ((Activity)context).getTheme()
                    .obtainStyledAttributes(new int[]{R.attr.chooseAlbum_menu_placeHolderDrawable});
            mPlaceHolderResId = ta.getResourceId(0, R.drawable.hm_bg_chooseimage_default);
            ta.recycle();
        }

        public void setDatas(List<Album> albums) {
            mAlbums = albums;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mAlbums.size();
        }

        @Override
        public Object getItem(int position) {
            return mAlbums.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.hm_item_choose_album, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.cover = (ImageView) convertView.findViewById(R.id.iv_cover);
                viewHolder.count = (TextView) convertView.findViewById(R.id.tv_count);
                viewHolder.mIvSelected = (ImageView) convertView.findViewById(R.id.iv_selected);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Album album = mAlbums.get(position);
            viewHolder.name.setText(album.getName());
            if (position == 0) {
                viewHolder.count.setVisibility(View.GONE);
            } else {
                viewHolder.count.setText(mContext.getString(R.string.hm_text_album_num, album.getCount()));
                viewHolder.count.setVisibility(View.VISIBLE);
            }
            viewHolder.mIvSelected.setVisibility(mCurSelectedPosition == position ? View.VISIBLE : View.GONE);
            Glide.with(mContext).load(PhotoPickerActivity.FILE_PROTOCOL + album.getCoverImagePath()).centerCrop()
                    .placeholder(mPlaceHolderResId)
                    .error(mPlaceHolderResId)
                    .into(viewHolder.cover);
            return convertView;
        }
    }

    class ViewHolder {

        public ImageView cover;
        public TextView name;
        public TextView count;
        public ImageView mIvSelected;
    }


}
