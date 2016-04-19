package com.rec.imagecropping;

import com.rec.imagecropping.graphics.ImageProcessor;

import android.app.Application;

public class Platform extends Application {

	public static String userUID;
	public static String[] facebookPermissions = { 
		"offline_access",
		"publish_stream", 
		"photo_upload" };

	@Override
	public void onCreate() {
		super.onCreate();
		ImageProcessor.getInstance().setApplicationCotnext(
				getApplicationContext());
	}
}
