package me.nereo.multi_image_selector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 图片选择器
 * Created by nereo on 16/3/17.
 */
public class MultiImageSelector implements Serializable {

    private static final long serialVersionUID = 8646926609133793033L;
    public static final String EXTRA_RESULT = MultiImageSelectorActivity.EXTRA_RESULT;
    private boolean mShowCamera = true;
    private int mMaxCount = 9;
    private int mMode = MultiImageSelectorActivity.MODE_MULTI;
    private ArrayList<String> mOriginData;
    private static MultiImageSelector sSelector;
    private boolean mCrop = false;
    //裁剪边界
    private int mMaxSide;
    //最小裁剪比例
    private String mMinRatio;
    //最大裁剪比例
    private String mMaxRatio;
    //压缩质量
    private int mDpi;
    //剪切回显
    private boolean mCropShow = false;

    @Deprecated
    private MultiImageSelector(Context context){

    }

    private MultiImageSelector(){}

    @Deprecated
    public static MultiImageSelector create(Context context){
        if(sSelector == null){
            sSelector = new MultiImageSelector(context);
        }
        return sSelector;
    }

    public static MultiImageSelector create(){
        if(sSelector == null){
            sSelector = new MultiImageSelector();
        }
        return sSelector;
    }

    public MultiImageSelector showCamera(boolean show){
        mShowCamera = show;
        return sSelector;
    }

    public MultiImageSelector count(int count){
        mMaxCount = count;
        return sSelector;
    }

    public MultiImageSelector single(){
        mMode = MultiImageSelectorActivity.MODE_SINGLE;
        return sSelector;
    }
    public MultiImageSelector multi(){
        mMode = MultiImageSelectorActivity.MODE_MULTI;
        return sSelector;
    }

    public MultiImageSelector origin(ArrayList<String> images){
        mOriginData = images;
        return sSelector;
    }

    public MultiImageSelector ucrop(boolean ucrop){
        mCrop = ucrop;
        return sSelector;
    }

    public MultiImageSelector maxSide(int maxSide){
        mMaxSide = maxSide;
        return sSelector;
    }

    public MultiImageSelector minRatio(String minRatio){
        mMinRatio = minRatio;
        return sSelector;
    }

    public MultiImageSelector maxRatio(String maxRatio){
        mMaxRatio = maxRatio;
        return sSelector;
    }

    public MultiImageSelector dpi(int dpi){
        mDpi = dpi;
        return sSelector;
    }

    public MultiImageSelector cropShow(boolean cropShow){
        mCropShow = cropShow;
        return sSelector;
    }

    public boolean ismShowCamera() {
        return mShowCamera;
    }

    public int getmMaxCount() {
        return mMaxCount;
    }

    public int getmMode() {
        return mMode;
    }

    public ArrayList<String> getmOriginData() {
        return mOriginData;
    }

    public boolean ismCrop() {
        return mCrop;
    }

    public int getmMaxSide() {
        return mMaxSide;
    }

    public String getmMinRatio() {
        return mMinRatio;
    }

    public String getmMaxRatio() {
        return mMaxRatio;
    }

    public int getmDpi() {
        return mDpi;
    }

    public boolean ismCropShow() {
        return mCropShow;
    }

    public void start(Activity activity, int requestCode){
        final Context context = activity;
        if(hasPermission(context)) {
            activity.startActivityForResult(createIntent(context), requestCode);
        }else{
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    public void start(Fragment fragment, int requestCode){
        final Context context = fragment.getContext();
        if(hasPermission(context)) {
            fragment.startActivityForResult(createIntent(context), requestCode);
        }else{
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermission(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            // Permission was added in API Level 16
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private Intent createIntent(Context context){
        Intent intent = new Intent(context, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_IMAGE_SELECTOR, this);
        return intent;
    }
}
