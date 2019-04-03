package me.nereo.multi_image_selector.permission;

public interface ILocationPermission {

        void getLocation();

        void showRationaleForLocation(IPermissionRequest request);

        void onLocationDenied();

}
