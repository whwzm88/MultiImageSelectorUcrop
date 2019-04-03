# MultiImageSelectorUcrop

## 简介
本项目是将https://github.com/lovetuzitong/MultiImageSelector | https://github.com/Yalantis/uCrop 
(图片选择框架)和(图片裁切框架)进行了整合
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
				//是否显示拍照
				.showCamera(true) // show camera or not. true by default
				//可选图片数量
				.count(9 - imgs.size()) // max select image size, 9 by default. used width #.multi()
				//多图选择 单图为single
				.multi()// single mode
				.origin(new ArrayList<String>()) // original select data set, used width #.multi()
				//是否开启裁切
				.ucrop(false)
				//裁切时回显,调试用,目前不可用
				.cropShow(true)
				//图片最大的宽度或高度(目前最大宽度和高度一个参数控制)
				.maxSide(2000)
				//最小的图片裁切比例
				.minRatio("16/9")
				//最大的图片裁切比例
				.maxRatio("3/4")
				//图片的裁切质量
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