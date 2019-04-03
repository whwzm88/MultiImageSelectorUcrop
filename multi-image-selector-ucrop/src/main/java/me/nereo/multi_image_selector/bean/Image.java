package me.nereo.multi_image_selector.bean;

import android.text.TextUtils;

/**
 * 图片实体
 * Created by Nereo on 2015/4/7.
 */
public class Image {
    public String path;
    public String name;
    public long time;
    public String cropPath;
    public Image(String path, String name, long time){
        this.path = path;
        this.name = name;
        this.time = time;
    }

    public String getCropPath() {
        return cropPath;
    }

    public void setCropPath(String cropPath) {
        this.cropPath = cropPath;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return TextUtils.equals(this.path, other.path);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
