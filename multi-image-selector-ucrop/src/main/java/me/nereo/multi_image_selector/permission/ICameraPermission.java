package me.nereo.multi_image_selector.permission;

public interface ICameraPermission {

        void showCamera();

        void showRationaleForCamera(IPermissionRequest request);

        void onCameraDenied();

        void onCameraNeverAskAgain(IPermissionRequest request);
}
