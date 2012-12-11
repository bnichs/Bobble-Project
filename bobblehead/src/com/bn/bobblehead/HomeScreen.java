package com.bn.bobblehead;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.bn.bobblehead.FaceSelectActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeScreen extends Activity {

	public static final File backFil = new File(
			Environment.getExternalStorageDirectory(), "bobblebackg.png");
	public static final File faceFil = new File(
			Environment.getExternalStorageDirectory(), "bobbleface.png");
	public static final File tmpFil = new File(
			Environment.getExternalStorageDirectory(), "bobbletmp.png");
	private static final int CAMERA_REQUEST = 1888;
	private static final int GALLERY_REQUEST = 1555;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		Button useCameraButton = (Button) this.findViewById(R.id.button1);
		useCameraButton.setOnClickListener(new View.OnClickListener() {

			// launch camera app to get img
			public void onClick(View v) {
				 Intent cameraIntent = new
				 Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				 cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(backFil));
				 startActivityForResult(cameraIntent, CAMERA_REQUEST);
				
				 /*Bitmap photo = BitmapFactory.decodeResource(getResources(),
						R.drawable.test);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
				byte[] b = baos.toByteArray();

				try {
					if (!backFil.exists()) {
						backFil.createNewFile();
					}
					FileOutputStream out = new FileOutputStream(backFil);
					photo.compress(Bitmap.CompressFormat.PNG, 90, out);

					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Intent intent = new Intent(HomeScreen.this,
						FaceSelectActivity.class);
				startActivity(intent);*/

			}
		});

		Button useGalleryButton = (Button) this.findViewById(R.id.button2);
		useGalleryButton.setOnClickListener(new View.OnClickListener() {

			// launch camera app to get img
			public void onClick(View v) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, GALLERY_REQUEST);

			}
		});

	}

	// this receives the camera's photo
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bitmap backg = null;
		String filePath = null;
		switch (requestCode) {

		case GALLERY_REQUEST:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);
				cursor.close();

			}
			break;
		case CAMERA_REQUEST:
			if (resultCode == RESULT_OK) {
				
				
				
				
				//backg = (Bitmap) data.getExtras().get("data");
				/*try {
					if (!backFil.exists()) {
						backFil.createNewFile();
					}
					FileOutputStream out = new FileOutputStream(backFil);
					backg.compress(Bitmap.CompressFormat.PNG, 90, out);

					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
				
				filePath=backFil.getPath();

			}
			break;

		}

		System.out.println(filePath);
		Intent intent = new Intent(this, FaceSelectActivity.class);
		intent.putExtra("backPath", filePath);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_home_screen, menu);
		return true;
	}
}
