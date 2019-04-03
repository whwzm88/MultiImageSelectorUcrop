package me.wuzhimin.multiimggeselectorucrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Copyright (C) 2015 The huimai mobile client Project
 * All right reserved.
 *
 * @author: wuzhimin@huimai365.com
 * @date: 2019/4/1 14:46
 * @Description:
 */
public class ImageResultAdapter extends BaseAdapter {
    private Context context;
    private List<String> imgs;

    public ImageResultAdapter(Context context, List<String> imgs) {
        this.context = context;
        this.imgs = imgs;
    }

    @Override
    public int getCount() {
        return imgs != null ? imgs.size() : 0;
    }

    @Override
    public String getItem(int pos) {
        return imgs.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        Holder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.img_layout, null);
            holder = new Holder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        Picasso.with(context)
                .load(new File(getItem(pos)))
                .transform(new PicassoTransformation(holder.imageView, 1))
                .into(holder.imageView);
        return view;
    }

    final class Holder {
        private ImageView imageView;

        public Holder(View view) {
            imageView = view.findViewById(R.id.iv_img);
        }

    }
}
