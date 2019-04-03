package me.nereo.multi_image_selector.permission;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import me.nereo.multi_image_selector.R;

public class BasePermissionActivity extends AppCompatActivity {

    private AlertDialog builder;
    protected boolean isRun = true;
    private static final int REQUEST_SHOWCAMERA = 0;
    private static final String[] PERMISSION_SHOWCAMERA = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};//相机 存储权限

    private static final int REQUEST_SHOWPHONE = 1;
    private static final String[] PERMISSION_SHOWPHONE = new String[]{"android.permission.READ_PHONE_STATE"};//手机

    private static final int REQUEST_SETTINGS = 2;
    private static final String[] PERMISSION_SETTINGS = new String[]{"android.permission.WRITE_SETTINGS"};//系统设置权限

    //百度定位使用
    private static final int REQUEST_LOCATION = 3;
    private static final String[] PERMISSION_LOCATION = new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};//获取错略位置 获取精确位置

    private static final int REQUEST_EXTERNAL_STORAGE = 4;
    private static final String[] PERMISSION_READ_EXTERNAL_STORAGE_ = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};//存储权限


    //相机接口
    private ICameraPermission mICameraPermission;

    protected void setICameraPermission(ICameraPermission iCameraPermission) {
        mICameraPermission = iCameraPermission;
    }

    //手机接口
    private IPhonePermission mIPhonePermission;

    protected void setIPhonePermission(IPhonePermission iPhonePermission) {
        mIPhonePermission = iPhonePermission;
    }

    //定位接口
    private ILocationPermission mILocationPermission;

    protected void setILocationPermission(ILocationPermission iLocationPermission) {
        mILocationPermission = iLocationPermission;
    }

    /***
     * 请求系统设置权限
     */
    protected void requestSetting() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_SETTINGS)) {
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SETTINGS)) {
            } else {
                ActivityCompat.requestPermissions(this, PERMISSION_SETTINGS, REQUEST_SETTINGS);
            }
        }

    }

    /***
     * 是否存系统设置权限
     */
    protected boolean isSetting() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_SETTINGS) || Settings.System.canWrite(this)) {
            if (builder != null) {
                builder.cancel();
            }
            return true;
        }
        return false;
    }


    /***
     * 请求相机权限
     *
     *  此处提示信息需要修改 相机和存储权限提示
     */
    public void showCameraWithCheck() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_SHOWCAMERA)) {
            //用户允许相机权限
            if (mICameraPermission != null) {
                mICameraPermission.showCamera();
            }
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SHOWCAMERA)) {
                //为保证您正常的使用此项功能，需要获取您的相机使用权限，请允许
                if (mICameraPermission != null) {
                    mICameraPermission.showRationaleForCamera(new ShowPermissionRequest(this, REQUEST_SHOWCAMERA));
                }

            } else {
                ActivityCompat.requestPermissions(this,PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA);
            }
        }
    }

    protected void showExternalStorageWithCheck() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_READ_EXTERNAL_STORAGE_)) {
            //用户允许存储权限
            if (mICameraPermission != null) {
                mICameraPermission.showCamera();
            }
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_READ_EXTERNAL_STORAGE_)) {
                //为保证您正常的使用此项功能，需要获取您的相机使用权限，请允许
                if (mICameraPermission != null) {
                    mICameraPermission.showRationaleForCamera(new ShowPermissionRequest(this, REQUEST_EXTERNAL_STORAGE));
                }

            } else {
                //弹出系统授权申请
                ActivityCompat.requestPermissions(this, PERMISSION_READ_EXTERNAL_STORAGE_, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    /***
     * 请求定位权限
     */
    protected void showLocationWithCheck() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_LOCATION)) {
            //用户允许获取定位权限
            if (mILocationPermission != null) {
                mILocationPermission.getLocation();
            }
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_LOCATION)) {
                //为保证您正常的使用此项功能，需要获取您的位置信息使用权限，请允许
                if (mILocationPermission != null) {
                    mILocationPermission.showRationaleForLocation(new ShowPermissionRequest(this, REQUEST_LOCATION));
                }

            } else {
                //弹出系统授权申请
                ActivityCompat.requestPermissions(this, PERMISSION_LOCATION, REQUEST_LOCATION);
            }
        }
    }

    /***
     * 是否存在使用手机权限
     */
    protected boolean isUsePhone() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_SHOWPHONE)) {
            if (builder != null) {
                builder.cancel();
            }
            return true;
        }
        return false;
    }

    /***
     * 请求手机权限
     */
    protected void usePhoneWithCheck() {
        if (PermissionUtils.hasSelfPermissions(this, PERMISSION_SHOWPHONE)) {
            if (mIPhonePermission != null) {
                mIPhonePermission.allowsUsePhone();
            }
        } else {
            if (PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SHOWPHONE)) {
                if (mIPhonePermission != null) {
                    isRun = false;
                    mIPhonePermission.showRationaleForPhone(new ShowPermissionRequest(this, REQUEST_SHOWPHONE));
                }

            } else {
                isRun = false;
                ActivityCompat.requestPermissions(this, PERMISSION_SHOWPHONE, REQUEST_SHOWPHONE);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //用户允许相机权限
                    if (mICameraPermission != null) {
                        mICameraPermission.showCamera();
                    }
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_READ_EXTERNAL_STORAGE_)) {
                        //未取得您的相机使用权限，此功能无法使用。请前往应用权限设置打开去权限。
                        if (mICameraPermission != null) {
                            mICameraPermission.onCameraNeverAskAgain(new ShowPermissionRequest(this, REQUEST_SHOWCAMERA));
                        }
                    } else {
                        //为保证您正常的使用此项功能，需要获取您的相机使用权限，请允许
                        if (mICameraPermission != null) {
                            mICameraPermission.showRationaleForCamera(new ShowPermissionRequest(this, REQUEST_SHOWCAMERA));
                        }

                    }
                }
                break;
            case REQUEST_SHOWCAMERA:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //用户允许相机权限
                    if (mICameraPermission != null) {
                        mICameraPermission.showCamera();
                    }
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SHOWCAMERA)) {
                        //未取得您的相机使用权限，此功能无法使用。请前往应用权限设置打开去权限。
                        if (mICameraPermission != null) {
                            mICameraPermission.onCameraNeverAskAgain(new ShowPermissionRequest(this, REQUEST_SHOWCAMERA));
                        }
                    } else {
                        //为保证您正常的使用此项功能，需要获取您的相机使用权限，请允许
                        if (mICameraPermission != null) {
                            mICameraPermission.showRationaleForCamera(new ShowPermissionRequest(this, REQUEST_SHOWCAMERA));
                        }

                    }
                }
                break;
            case REQUEST_SHOWPHONE:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //用户允许手机权限
                    if (mIPhonePermission != null) {

                        mIPhonePermission.allowsUsePhone();
                    }
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SHOWPHONE)) {
                        //未取得您的电话使用权限，优品惠无法开启为您服务。请前往应用权限设置打开去权限。
                        if (mIPhonePermission != null) {
                            isRun = false;
                            mIPhonePermission.onPhoneNeverAskAgain(new ShowPermissionRequest(this, REQUEST_SHOWPHONE));
                        }
                    } else {
                        //为保证您正常的使用此项功能，获取您的手机使用权限，去允许
                        if (mIPhonePermission != null) {
                            isRun = false;
                            mIPhonePermission.showRationaleForPhone(new ShowPermissionRequest(this, REQUEST_SHOWPHONE));
                        }
                    }
                }
                break;
            case REQUEST_LOCATION:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    //用户允许使用定位权限
                    if (mILocationPermission != null) {
                        mILocationPermission.getLocation();
                    }
                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SHOWCAMERA)) {
                        if (mILocationPermission != null) {
                            //需在手机设置中打开优品惠的位置信息权限
                            mILocationPermission.onLocationDenied();
                        }
                    } else {
                        //为保证您正常的使用此项功能，需要获取您的位置信息使用权限，请允许
                        if (mILocationPermission != null) {
                            mILocationPermission.showRationaleForLocation(new ShowPermissionRequest(this, REQUEST_LOCATION));
                        }

                    }
                }
                break;
            case REQUEST_SETTINGS:
                if (PermissionUtils.verifyPermissions(grantResults)) {

                } else {
                    if (!PermissionUtils.shouldShowRequestPermissionRationale(this, PERMISSION_SETTINGS)) {
//                                                showRationaleDialog(R.string.open_setting_permission, true,true, new ShowPermissionRequest(this,REQUEST_SETTINGS));
                    }
                }
                break;
            default:
                break;
        }
    }

    private final class ShowPermissionRequest implements IPermissionRequest {
        private final WeakReference<BasePermissionActivity> weakTarget;
        private int REQUEST_CODE;

        private ShowPermissionRequest(BasePermissionActivity target, int requestCode) {
            this.weakTarget = new WeakReference<BasePermissionActivity>(target);
            this.REQUEST_CODE = requestCode;
        }

        @Override
        public void toOpen() {
            switch (REQUEST_CODE) {
                case REQUEST_SHOWPHONE:
                case REQUEST_SHOWCAMERA:
                    //打开系统权限设置
                    Intent intent = getAppDetailSettingIntent(BasePermissionActivity.this);
                    startActivity(intent);
                    break;
                case REQUEST_SETTINGS:
                    Intent intent1 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent1, REQUEST_SETTINGS);
                    break;
                default:
                    break;
            }


        }

        @Override
        public void proceed() {
            BasePermissionActivity target = weakTarget.get();
            if (target == null) return;
            //弹出系统授权申请
            switch (REQUEST_CODE) {
                case REQUEST_EXTERNAL_STORAGE:
                    ActivityCompat.requestPermissions(target, PERMISSION_READ_EXTERNAL_STORAGE_, REQUEST_EXTERNAL_STORAGE);
                    break;
                case REQUEST_SHOWCAMERA:
                    ActivityCompat.requestPermissions(target, PERMISSION_SHOWCAMERA, REQUEST_SHOWCAMERA);
                    break;
                case REQUEST_SHOWPHONE:
                    ActivityCompat.requestPermissions(target, PERMISSION_SHOWPHONE, REQUEST_SHOWPHONE);
                    break;
                case REQUEST_LOCATION:
                    ActivityCompat.requestPermissions(target, PERMISSION_LOCATION, REQUEST_LOCATION);
                    break;
                default:
                    break;
            }

        }

        @Override
        public void cancel() {
            //取消授权
            BasePermissionActivity target = weakTarget.get();
            if (target == null) return;
            switch (REQUEST_CODE) {
                case REQUEST_SHOWCAMERA:
                    if (target.mICameraPermission != null) {
                        //需在手机设置中打开优品惠的相机权限
                        target.mICameraPermission.onCameraDenied();
                    }
                    break;
                case REQUEST_LOCATION:
                    if (target.mILocationPermission != null) {
                        //需在手机设置中打开优品惠的位置信息权限
                        target.mILocationPermission.onLocationDenied();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /***
     * @param messageResId 弹框消息内容
     * @param request 回调
     */
    protected void showRationaleDialog(@StringRes int messageResId, IPermissionRequest request) {
        showRationaleDialog(messageResId, false, true, request);
    }

    /***
     * @param messageResId 弹框消息内容
     * @param toOpen 是否显示去打开（系统应用权限）
     * @param request 回调
     */
    protected void showRationaleDialog(@StringRes int messageResId, boolean toOpen, IPermissionRequest request) {
        showRationaleDialog(messageResId, toOpen, false, request);
    }

    /***
     * @param messageResId 弹框消息内容
     * @param toOpen 是否显示去打开（系统应用权限）
     * @param isShowCancel 是否显示取消按钮
     * @param request  回调
     */
    protected void showRationaleDialog(@StringRes int messageResId, final boolean toOpen, boolean isShowCancel, final IPermissionRequest request) {
        String value = "去允许";
        if (toOpen) {
            value = "去打开";
        }
        builder = new AlertDialog.Builder(this)
                .setTitle(R.string.mis_permission_dialog_title)
                .setMessage(this.getText(messageResId) + "")
                .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (toOpen) {
                            request.toOpen();
                        } else {
                            request.proceed();
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.mis_permission_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                        dialog.dismiss();
                    }
                })
                .create();

        if (!isShowCancel) {
            builder.setCancelable(false);
        }
        builder.show();

    }


    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    private Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }
}
