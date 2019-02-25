package com.aseemsethi.shopnearme;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private Integer[] imageIds = {
            R.drawable.ic_menu_camera, R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_manage, R.drawable.ic_menu_send,
            R.drawable.ic_menu_share, R.drawable.ic_menu_slideshow
    };

    public GridAdapter(Context c) {
        context = c;
    }

    public int getCount() {
        return imageIds.length;
    }

    public Object getItem(int position) {
        return imageIds[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        ImageView iview;
        if (view == null) {
            iview = new ImageView(context);
            iview.setLayoutParams(new GridView.LayoutParams(150,200));
            iview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iview.setPadding(5, 5, 5, 5);
        } else {
            iview = (ImageView) view;
        }
        //iview.setBackgroundResource(R.drawable.grid_row_border);
        iview.setImageResource(imageIds[position]);
        return iview;
    }
}