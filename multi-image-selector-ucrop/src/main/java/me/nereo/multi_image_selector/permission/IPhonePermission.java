package me.nereo.multi_image_selector.permission;

public interface IPhonePermission {

        void allowsUsePhone();

        void showRationaleForPhone(IPermissionRequest request);

        void onPhoneNeverAskAgain(IPermissionRequest request);
}
