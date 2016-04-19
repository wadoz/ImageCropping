package com.rec.imagecropping;

import static com.rec.imagecropping.editoractivity.EditorSaveConstants.RESTORE_SAVED_BITMAP;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.rec.imagecropping.editoractivity.BrightnessActivity;
import com.rec.imagecropping.editoractivity.CropActivity;
import com.rec.imagecropping.editoractivity.FilterActivity;
import com.rec.imagecropping.editoractivity.RotateActivity;
import com.rec.imagecropping.graphics.ImageProcessor;
import com.rec.imagecropping.utils.BitmapScalingUtil;
import com.rec.imagecropping.utils.ImageScannerAdapter;
import com.rec.imagecropping.utils.SaveToStorageUtil;
import com.rec.imagecropping.utils.SendMailUtil;

public class EditorActivity extends Activity implements OnClickListener
		 {


	private static final int EDITOR_FUNCTION = 1;
	private static final int AUTHORIZE_FACEBOOK = 2;

	private ImageView imageView;

	// Top bar buttons
	private ImageButton brightnessButton;
	private ImageButton cropButton;
	private ImageButton rotateButton;
	private ImageButton filtersButton;

	// Bottom bar buttons
	private ImageButton backButton;

	private ImageButton saveButton;

	private String savedImagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		initComponents();
		initImageView();
	}

	private void initComponents() {
		brightnessButton = (ImageButton) findViewById(R.id.brightness_button);
		brightnessButton.setOnClickListener(this);
		cropButton = (ImageButton) findViewById(R.id.crop_button);
		cropButton.setOnClickListener(this);
		rotateButton = (ImageButton) findViewById(R.id.rotate_button);
		rotateButton.setOnClickListener(this);
		filtersButton = (ImageButton) findViewById(R.id.filters_button);
		filtersButton.setOnClickListener(this);
		backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(this);

		saveButton = (ImageButton) findViewById(R.id.save_button);
		saveButton.setOnClickListener(this);
	}

	private void initImageView() {
		String imageUri = getIntent().getStringExtra(
				getString(R.string.image_uri_flag));
		imageView = (ImageView) findViewById(R.id.image_view);

		final Object data = getLastNonConfigurationInstance();
		if (data == null) {
			openBitmap(imageUri);
		} else {
			restoreBitmap();
		}
	}



	private void restoreBitmap() {
		Bitmap b = ImageProcessor.getInstance().getBitmap();
		if (b != null) {
			imageView.setImageBitmap(b);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Bundle saveObject = new Bundle();
		saveObject.putInt("Bitmap", RESTORE_SAVED_BITMAP);
		return saveObject;
	}

	private void openBitmap(String imageUri) {
		Bitmap b;
		try {
			b = BitmapScalingUtil.bitmapFromUri(this, Uri.parse(imageUri));
			if (b != null) {
				Log.i("REC Photo Editor", "Opened Bitmap Size: " + b.getWidth()
						+ " " + b.getHeight());
			}
			ImageProcessor.getInstance().setBitmap(b);
			imageView.setImageBitmap(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.brightness_button:
			brightnessButtonClicked();
			break;
		case R.id.crop_button:
			cropButtonClicked();
			break;
		case R.id.rotate_button:
			rotateButtonClicked();
			break;
		case R.id.filters_button:
			filtersButtonClicked();
			break;
		case R.id.back_button:
			backButtonClicked();
			break;
		case R.id.save_button:
			saveButtonClicked();
			break;
		default:
			break;
		}
	}

	private void backButtonClicked() {
		finish();
	}



/*	private boolean imageIsAlreadySaved() {
		return savedImagePath != null && !savedImagePath.equals("");
	}*/


	private void saveButtonClicked() {
		saveImage();
		Toast.makeText(this, R.string.photo_saved_info, Toast.LENGTH_LONG)
				.show();
	}

	private void saveImage() {
		savedImagePath = SaveToStorageUtil.save(ImageProcessor.getInstance()
				.getBitmap(), this);
		ImageScannerAdapter adapter = new ImageScannerAdapter(this);
		adapter.scanImage(savedImagePath);
		ImageProcessor.getInstance().resetModificationFlag();
	}

	private void cropButtonClicked() {
		runEditorActivity(CropActivity.class);
	}

	private void brightnessButtonClicked() {
		runEditorActivity(BrightnessActivity.class);
	}

	private void filtersButtonClicked() {
		runEditorActivity(FilterActivity.class);
	}


	private void rotateButtonClicked() {
		runEditorActivity(RotateActivity.class);
	}

	private void runEditorActivity(Class<?> activityClass) {
		Intent i = new Intent(this, activityClass);
		startActivityForResult(i, EDITOR_FUNCTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case EDITOR_FUNCTION:
			if (resultCode == RESULT_OK) {
				imageView.setImageBitmap(ImageProcessor.getInstance()
						.getBitmap());
			}
			break;
		default:
			break;
		}
	}


	private Runnable createPostRotateAction() {
		final Runnable postRotateAction = new Runnable() {
			public void run() {
				imageView.setImageBitmap(ImageProcessor.getInstance()
						.getBitmap());
				imageView.invalidate();
			}
		};
		return postRotateAction;
	}


}
