package com.example.touchpointmagnification;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

public class Blur_Activity extends Activity {

	Bitmap bmp_gallery;

	EraseView iv_image;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blur_activity);

		iv_image = (EraseView) findViewById(R.id.iv_image);

		Intent get_intent = getIntent();
		if (get_intent != null) {
			String image_uri_path = get_intent.getStringExtra("gallery_intent");
			Uri image_uri = Uri.parse(image_uri_path);
			
			final int screen_width = Blur_Activity.this.getResources().getDisplayMetrics().
					widthPixels;
			final int screen_height = Blur_Activity.this.getResources().getDisplayMetrics().
					heightPixels;

			bmp_gallery = getBitmapFromUri(Blur_Activity.this, screen_width, screen_height, true,
					image_uri, null);
		}

		iv_image.setLoadListener(new OnViewLoadListener() {

			@Override
			public void onViewInflated() {
				// TODO Auto-generated method stub
				try {
					if (bmp_gallery != null) {
						iv_image.setPickedBitmap(bmp_gallery);
						return;
					}
					
					finish();

				} catch (OutOfMemoryError e) {

					if (bmp_gallery != null) {
						bmp_gallery.recycle();
						bmp_gallery = null;
						System.gc();
					}
					
					finish();
				}
			}
		});
	}

	public static Bitmap getBitmapFromUri(Context context, int screenWidth,
			int screenHeight, boolean isGalleryON, Uri galleryUri,
			String imagePath) {
		
		String tempImagepath = imagePath;
		if (tempImagepath == null) {
			if (isGalleryON) {
				Cursor query = context.getContentResolver().query(galleryUri, new String[] { "_data" }, null, null, null);
				if (query.moveToFirst()) {
					tempImagepath = query.getString(query.getColumnIndexOrThrow("_data"));
				}
				query.close();
			} else {
				tempImagepath = galleryUri.getPath();
			}
		}
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(tempImagepath, options);
		int attributeInt = 0;
		try {
			attributeInt = new ExifInterface(tempImagepath).getAttributeInt("Orientation", 1);
		} catch (IOException e) {
		}
		options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight, attributeInt);
		options.inJustDecodeBounds = false;

		Bitmap b = rotateImage(BitmapFactory.decodeFile(tempImagepath, options), tempImagepath);

		return b;
	}

	public static int calculateInSampleSize(Options bitmapFactoryOptions,
			int width, int height, int attributeInt) {
		int outHeight = bitmapFactoryOptions.outHeight;
		int outWidth = bitmapFactoryOptions.outWidth;
		int sampleSize = 1;
		if ((outHeight > width || outWidth > height)
				&& (attributeInt == 8 || attributeInt == 6)) {
			int round = Math.round(((float) outHeight) / ((float) width));
			int round2 = Math.round(((float) outWidth) / ((float) height));
			if (round < round2) {
				sampleSize = round;
			} else {
				sampleSize = round2;
			}
		} else if (outHeight > height || outWidth > width) {
			int round3 = Math.round(((float) outHeight) / ((float) height));
			int round4 = Math.round(((float) outWidth) / ((float) width));
			if (round3 < round4) {
				sampleSize = round3;
			} else {
				sampleSize = round4;
			}
		}
		if (sampleSize > 16) {
			sampleSize = 16;
		} else if (sampleSize > 8) {
			return 8;
		} else {
			if (sampleSize > 4) {
				return 4;
			}
			if (sampleSize > 2) {
				return 2;
			}
		}
		return sampleSize;
	}

	public static Bitmap rotateImage(Bitmap bitmap, String filePath) {
		try {
			int orientation = new ExifInterface(filePath).getAttributeInt(
					"Orientation", 1);
			Matrix matrix = new Matrix();
			float angle = 0.0f;
			if (orientation == 6) {
				angle = 90.0f;
			} else if (orientation == 3) {
				angle = 180.0f;
			} else if (orientation == 8) {
				angle = 270.0f;
			}
			if (angle == 0.0f) {
				return bitmap;
			}
			matrix.postRotate(angle);
			Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return resultBitmap;
		} catch (Exception e) {
			return bitmap;
		}
	}
}
