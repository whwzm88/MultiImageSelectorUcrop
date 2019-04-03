# MultiImageSelectorUcrop

## 简介
本项目是将https://github.com/lovetuzitong/MultiImageSelector(图片选择框架)
和https://github.com/Yalantis/uCrop(图片裁切框架)进行了整合
在以上框架原有的功能基础上
新增
图片选择时预览
不可视化裁切
多图选择裁切

目前的缺点是
部分多张大图片裁切时,会出现耗时长的问题,后续将持续优化改进


## 如何使用
#### 1、添加权限
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.CAMERA"/>
	
#### 2、添加7.0以上文件访问授权
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="包名.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths"/>
        </provider>
		
		
#### 3、在res/xml下面创建一个filepaths.xml文件
		<?xml version="1.0" encoding="utf-8"?>
		<paths xmlns:android="http://schemas.android.com/apk/res/android">
			<external-path path="." name="camera_photos" />
		</paths>

#### 4、API调用
			MultiImageSelector.create()
				.showCamera(true) // show camera or not. true by default
				//编辑时只能选择一张图,添加时最多选9张
				.count(9 - imgs.size()) // max select image size, 9 by default. used width #.multi()
				.multi()// single mode
				.origin(new ArrayList<String>()) // original select data set, used width #.multi()
				.ucrop(false)
				.cropShow(true)
				.maxSide(2000)
				.minRatio("16/9")
				.maxRatio("3/4")
				.dpi(70)
				.start(MainActivity.this, SELECT_PHOTO_DATA);
				
#### 5、接收返回的图片路径列表(开启裁剪返回的裁剪图,不开启裁剪返回原图)
			@Override
			protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);
				if(requestCode == REQUEST_IMAGE){
					if(resultCode == RESULT_OK){
						// Get the result list of select image paths
						List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
						// do your logic ....
					}
				}
			}
				
## 混淆配置
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }