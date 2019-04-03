package me.nereo.multi_image_selector.permission;


public interface IPermissionRequest {

    void toOpen();

    void proceed();

    void cancel();
}
