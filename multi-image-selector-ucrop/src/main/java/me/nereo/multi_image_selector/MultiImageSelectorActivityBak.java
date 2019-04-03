package me.nereo.multi_image_selector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;

import java.io.File;
import java.util.ArrayList;

import me.nereo.multi_image_selector.permission.BasePermissionActivity;
import me.nereo.multi_image_selector.permission.ICameraPermission;
import me.nereo.multi_image_selector.permission.IPermissionRequest;
import me.nereo.multi_image_selector.utils.BitmapUtil;

/**
 * Multi image selector
 * Created by Nereo on 2015/4/7.
 * Updated by nereo on 2016/1/19.
 * Updated by nereo on 2016/5/18.
 */
public class MultiImageSelectorActivityBak extends BasePermissionActivity
        implements MultiImageSelectorFragment.Callback, ICameraPermission, UCropFragmentCallback {

    // Single choice
    public static final int MODE_SINGLE = 0;
    // Multi choice
    public static final int MODE_MULTI = 1;

    /**
     * Whether show camera，true by default
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    public static final String EXTRA_IMAGE_SELECTOR = "image_selector";
    /**
     * Result data set，ArrayList&lt;String&gt;
     */
    public static final String EXTRA_RESULT = "select_result";
    // Default image size
    private static final int DEFAULT_IMAGE_SIZE = 9;

    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> cropResultList = new ArrayList<>();
    private Button mSubmitButton;
    private int mDefaultCount = DEFAULT_IMAGE_SIZE;
    private MultiImageSelectorFragment mulselectFragment;
    private MultiImageSelector imageSelector;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MIS_NO_ACTIONBAR);
        setContentView(R.layout.mis_activity_default);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setICameraPermission(this);
        final Intent intent = getIntent();
        imageSelector = (MultiImageSelector) intent.getSerializableExtra(EXTRA_IMAGE_SELECTOR);
        mDefaultCount = imageSelector.getmMaxCount();
        final int mode = imageSelector.getmMode();
        final boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        if (mode == MODE_MULTI && imageSelector.getmOriginData() != null) {
            resultList = imageSelector.getmOriginData();
        }
        findViewById(R.id.fragment_container).setVisibility(imageSelector.ismCropShow() ? View.VISIBLE : View.INVISIBLE);
        mSubmitButton = (Button) findViewById(R.id.commit);
        if (mode == MODE_MULTI) {
            updateDoneText(resultList);
            mSubmitButton.setVisibility(View.VISIBLE);
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (resultList != null && resultList.size() > 0) {
                        if (imageSelector != null && imageSelector.ismCrop()) {
                            batchCrop();
                        } else {
                            Intent data = new Intent();
                            data.putStringArrayListExtra(EXTRA_RESULT, imageSelector != null && imageSelector.ismCrop() ? cropResultList : resultList);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    } else {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_COUNT, mDefaultCount);
            bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_MODE, mode);
            bundle.putBoolean(MultiImageSelectorFragment.EXTRA_SHOW_CAMERA, isShow);
            bundle.putStringArrayList(MultiImageSelectorFragment.EXTRA_DEFAULT_SELECTED_LIST, resultList);
            mulselectFragment = (MultiImageSelectorFragment) Fragment.instantiate(this, MultiImageSelectorFragment.class.getName(), bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.image_grid, mulselectFragment)
                    .commit();
        }

    }

    private void batchCrop() {
        if (cropResultList.size() < resultList.size()) {
            CropCache cache = new CropCache();
            cache.setPath(resultList.get(cropResultList.size()));
            cache.setAction("batchCrop");
            setupFragment(initUcrop(cache));
        } else {
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, imageSelector != null && imageSelector.ismCrop() ? cropResultList : resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void showCamera() {
        if (mulselectFragment != null) {
            mulselectFragment.showCamera();
        }
    }

    @Override
    public void showRationaleForCamera(IPermissionRequest request) {
        //为保证您正常的使用此项功能，需要获取您的相机使用权限，请允许
        showRationaleDialog(R.string.mis_permission_rationale_write_storage, request);
    }

    @Override
    public void onCameraDenied() {
        Toast.makeText(this, "需在手机设置中打开惠买的相机、存储空间权限", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCameraNeverAskAgain(IPermissionRequest request) {
        //未取得您的相机使用权限，此功能无法使用。请前往应用权限设置打开去权限。
        showRationaleDialog(R.string.mis_open_camera_permission, true, request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Update done button by select image data
     *
     * @param resultList selected image data
     */
    private void updateDoneText(ArrayList<String> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(R.string.mis_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.mis_action_button_string,
                getString(R.string.mis_action_done), size, mDefaultCount));
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (imageSelector != null && imageSelector.ismCrop()) {
            CropCache cache = new CropCache();
            cache.setPath(path);
            cache.setAction("onSingleImageSelected");
            setupFragment(initUcrop(cache));
        } else {
            resultList.add(path);
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
            updateDoneText(resultList);
        }
    }

    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        updateDoneText(resultList);
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            // notify system the image has change
            if (imageSelector != null && imageSelector.ismCrop()) {
                CropCache cache = new CropCache();
                cache.setPath(imageFile.getAbsolutePath());
                cache.setAction("onCameraShot");
                setupFragment(initUcrop(cache));
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

                resultList.add(imageFile.getAbsolutePath());
                Intent data = new Intent();
                data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    @Override
    public void loadingProgress(boolean showLoader) {
    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        CropCache cropCache = (CropCache) mSubmitButton.getTag();
        if(result.mResultCode==UCrop.RESULT_ERROR){
            resultList.remove(cropCache.getPath());
            //剪裁出现异常
            Toast.makeText(this, "剪裁出现异常"+result.mResultData.toString(), Toast.LENGTH_LONG).show();
            batchCrop();
            return;
        }
        if ("onSingleImageSelected".equals(cropCache.getAction())) {
            resultList.add(cropCache.getPath());
            cropResultList.add(cropCache.cropPath);
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, cropResultList);
            setResult(RESULT_OK, data);
            finish();
        } else if ("batchCrop".equals(cropCache.getAction())) {
            cropResultList.add(cropCache.cropPath);
            removeFragmentFromScreen();
            batchCrop();
        } else if ("onCameraShot".equals(cropCache.getAction())) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(cropCache.getCropPath()))));
            resultList.add(cropCache.getPath());
            cropResultList.add(cropCache.cropPath);
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_RESULT, cropResultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private UCropFragment fragment;

    public UCrop initUcrop(CropCache cropCache) {
        String localUrl = cropCache.getPath();
        int rztioType = BitmapUtil.getRztio(localUrl, imageSelector.getmMinRatio(), imageSelector.getmMaxRatio());
        String[] min = imageSelector.getmMinRatio().split("/");
        String[] max = imageSelector.getmMaxRatio().split("/");
        String extName = BitmapUtil.getExtensionName(localUrl);
        File destinationFileName = new File(getCacheDir(), System.currentTimeMillis() + "." + extName);
        cropCache.setCropPath(destinationFileName.getAbsolutePath());
        mSubmitButton.setTag(cropCache);
        UCrop uCrop = UCrop.of(Uri.fromFile(new File(localUrl)), Uri.fromFile(destinationFileName));
        if (rztioType == 1) {
//                    Log.e("OPTIONS", String.format("裁切比(%1$dx%2$d)", Integer.valueOf(min[1]), Integer.valueOf(min[0])));
            //按照9/16裁切
            uCrop = uCrop.withAspectRatio(Float.valueOf(min[1]), Float.valueOf(min[0]));
        } else if (rztioType == 2) {
//                    Log.e("OPTIONS", String.format("裁切比(%1$dx%2$d)", Integer.valueOf(max[1]), Integer.valueOf(max[0])));
            //按照4/3裁切
            uCrop = uCrop.withAspectRatio(Float.valueOf(max[1]), Float.valueOf(max[0]));
        } else {
//                    Log.e("OPTIONS", "裁切原始比例");
            uCrop = uCrop.useSourceImageAspectRatio();
        }
        if (BitmapUtil.isMoreThanSide(localUrl, imageSelector.getmMaxSide())) {
            //只有当本地图宽高大于最大边界时,才设置,否则压缩后的图片高度小1px
            //设置最大宽高和压缩质量
            uCrop = uCrop.withMaxResultSize(imageSelector.getmMaxSide(), imageSelector.getmMaxSide());
        }
        UCrop.Options options = new UCrop.Options();
        if (!TextUtils.isEmpty(extName) && "PNG".equals(extName.toUpperCase())) {
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        } else {
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        }
        options.setCompressionQuality(imageSelector.getmDpi());
        options.setHideBottomControls(true);
        //设置是否允许操作裁剪框
        options.setFreeStyleCropEnabled(true);
        uCrop = uCrop.withOptions(options);
        return uCrop;
    }

    public void setupFragment(UCrop uCrop) {
        fragment = uCrop.getFragment(uCrop.getIntent(this).getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment, UCropFragment.TAG)
                .commitAllowingStateLoss();
        delayCrop();
    }

    private void delayCrop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fragment.cropAndSaveImage();
            }
        }, 500);
    }

    public void removeFragmentFromScreen() {
        getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss();
    }

    class CropCache {
        private String path;
        private String cropPath;
        private String action;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getCropPath() {
            return cropPath;
        }

        public void setCropPath(String cropPath) {
            this.cropPath = cropPath;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
