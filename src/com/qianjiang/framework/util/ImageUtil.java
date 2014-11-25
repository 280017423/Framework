package com.qianjiang.framework.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;

public class ImageUtil {
	private static final String TAG = "ImageUtil";

	private ImageUtil() {
	}

	/**
	 * 保存bitmap到本地相册
	 * 
	 * @param context
	 *            上下文
	 * @param title
	 *            标题
	 * @param bitmap
	 *            图片
	 * @return
	 */
	public static String addToTouchActiveAlbum(Context context, String title, Bitmap bitmap) {
		ContentValues values = new ContentValues();
		values.put(MediaColumns.TITLE, title);
		values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
		values.put(MediaColumns.MIME_TYPE, "image/jpeg");
		values.put(ImageColumns.DESCRIPTION, "abcd");
		ContentResolver contentResolver = context.getContentResolver();
		String stringUrl = null;
		Uri uri = null;
		try {
			uri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
			if (bitmap != null) {
				OutputStream imageOut = contentResolver.openOutputStream(uri);
				try {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
				} finally {
					imageOut.close();
					long id = ContentUris.parseId(uri);
					// Wait until MINI_KIND thumbnail is generated.
					Bitmap miniThumb = Images.Thumbnails.getThumbnail(contentResolver, id, Images.Thumbnails.MINI_KIND,
							null);
					// This is for backward compatibility.
					StoreThumbnail(contentResolver, miniThumb, id, 50F, 50F, Images.Thumbnails.MICRO_KIND);
				}
			} else {
				EvtLog.w(TAG, "Failed to create thumbnail, removing original");
				contentResolver.delete(uri, null, null);
				uri = null;
			}
			stringUrl = uri.toString();
		} catch (Exception e) {
			EvtLog.w(TAG, "Failed to insert image" + e);
			if (uri != null) {
				contentResolver.delete(uri, null, null);
				uri = null;
			}
		}
		if (uri != null) {
			stringUrl = uri.toString();
		}
		return stringUrl;
	}

	private static Bitmap StoreThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
		// create the matrix to scale it
		Matrix matrix = new Matrix();
		float scaleX = width / source.getWidth();
		float scaleY = height / source.getHeight();
		matrix.setScale(scaleX, scaleY);
		Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
		ContentValues values = new ContentValues(4);
		values.put(Images.Thumbnails.KIND, kind);
		values.put(Images.Thumbnails.IMAGE_ID, (int) id);
		values.put(Images.Thumbnails.HEIGHT, thumb.getHeight());
		values.put(Images.Thumbnails.WIDTH, thumb.getWidth());
		Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
		try {
			OutputStream thumbOut = cr.openOutputStream(url);

			thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
			thumbOut.close();
			return thumb;
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		}
	}

	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}
		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}
}
