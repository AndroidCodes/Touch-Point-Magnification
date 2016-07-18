package com.example.touchpointmagnification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class MagnifyView {

    private static final int BGCOLOR = Color.parseColor("#000000");  // Black Color code
    private static final int BGCOLORCONSTANT = Color.parseColor("#00000000");  // Transparent Color Code
    private static final int MAGNIFIER_ZOOM_IN_FACTOR = 2;

    private boolean isTopCircle = true;

    private int mScreenWidth;

    private float x1, y1, newX1, newY1;

    private float ratio = 1.0f;

    private Path mPath2Magnifier;

    private Rect mRectDestClip, rectDest, rectSrc;

    private Bitmap bitmapMagnifier, bitmapOriginalCircular, mBitmapMagnify;

    private Canvas mCanvasClip, mCanvasOriginalCircular;


    public MagnifyView(Context context) {
        mScreenWidth = context.getApplicationContext().getResources().getDisplayMetrics().widthPixels;

        mBitmapMagnify = BitmapFactory.decodeResource(context.getApplicationContext().getResources(),
                R.drawable.glass);

        rectSrc = new Rect();

        rectDest = new Rect(0, 0, (mScreenWidth / MAGNIFIER_ZOOM_IN_FACTOR) - 20
                , (mScreenWidth / MAGNIFIER_ZOOM_IN_FACTOR) - 20);

        mRectDestClip = new Rect(0, 0, rectDest.width(), rectDest.height());

        bitmapMagnifier = Bitmap.createBitmap(rectDest.width(), rectDest.height(), Config.ARGB_8888);
        mCanvasClip = new Canvas(bitmapMagnifier);

        bitmapOriginalCircular = Bitmap.createBitmap(rectDest.width(), rectDest.height(), Config.ARGB_8888);
        mCanvasOriginalCircular = new Canvas(bitmapOriginalCircular);

        mPath2Magnifier = new Path();
    }

    public void customMagnifyView(Canvas canvas1, int mLineWidth, Paint mPaintLine, Bitmap mBWBitmap, int width, int height
            , float scaleFactor, Bitmap mOriginalBmp) {

        if (rectDest.left == 0) {
            rectDest.offsetTo(20, 120);
        }

        if (height / MAGNIFIER_ZOOM_IN_FACTOR < rectDest.bottom) {
            isTopCircle = true;
        } else if (height / MAGNIFIER_ZOOM_IN_FACTOR > rectDest.top) {
            isTopCircle = false;
        }

        if (rectDest.contains((int) x1, (int) y1) && !isTopCircle) {

			/*Log.i("TOP CIRCLE FALSE >>>>>> ", "" + (canvas1.getWidth() - (rectDest.width() + 20)));
            Log.i("TOP CIRCLE FALSE >>>>>> ", "" + (canvas1.getHeight() - (rectDest.height() + 120)));*/

            rectDest.offsetTo(canvas1.getWidth() - (rectDest.width() + 20), canvas1.getHeight()
                    - (rectDest.height() + 120));
        }

        if (rectDest.contains((int) x1, (int) y1) && isTopCircle) {
            rectDest.offsetTo(20, 120);
        }

        int srcRectRadius = (int) (((float) (rectDest.width() / 4)) * ratio);
        rectSrc = new Rect(((int) newX1) - srcRectRadius, ((int) newY1) - srcRectRadius,
                ((int) newX1) + srcRectRadius, ((int) newY1) + srcRectRadius);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        mCanvasClip.drawColor(BGCOLORCONSTANT, Mode.CLEAR);
        mCanvasClip.drawCircle((float) mRectDestClip.centerX(), (float) mRectDestClip.centerY(),
                (float) (mRectDestClip.width() / MAGNIFIER_ZOOM_IN_FACTOR), paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        mCanvasClip.drawBitmap(mBWBitmap, rectSrc, mRectDestClip, paint);

        Paint paintMagnifierPath = new Paint(mPaintLine);
        paintMagnifierPath.setStrokeWidth(((((float) mLineWidth) * (1.0f / ratio)) * 2.0f) / scaleFactor);

        float scaleX = newX1 * 2.0f;
        float scaleY = newY1 * 2.0f;

        if (mPath2Magnifier.isEmpty()) {

            mPath2Magnifier.moveTo(scaleX, scaleY);

        } else {

            mPath2Magnifier.lineTo(scaleX, scaleY);

        }

        mPath2Magnifier.offset(-scaleX, -scaleY);
        mPath2Magnifier.offset((float) (rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR)
                , (float) (rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR));

        mCanvasClip.drawPath(mPath2Magnifier, paintMagnifierPath);
        mPath2Magnifier.offset((float) (-(rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR)),
                (float) (-(rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR)));
        mPath2Magnifier.offset(scaleX, scaleY);

        Paint colorPaint = new Paint();
        colorPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OVER));

        circularCropOroginalBitmap(mOriginalBmp);

        mCanvasClip.drawBitmap(bitmapOriginalCircular, null, mRectDestClip, colorPaint);
        mCanvasClip.drawBitmap(mBitmapMagnify, null, mRectDestClip, null);

        paint.setXfermode(null);
        paint.setColor(BGCOLOR);
        canvas1.drawCircle((float) rectDest.centerX(), (float) rectDest.centerY()
                , (float) (rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR), paint);

		/*Log.i("rectDest.centerX() >>>>>>> ", "" + rectDest.centerX());
		Log.i("rectDest.centerY() >>>>>>> ", "" + rectDest.centerY());
		Log.i("rectDest.center >>>>>>> ", "" + (float) (rectDest.width() / MAGNIFIER_ZOOM_IN_FACTOR));*/

        canvas1.drawBitmap(bitmapMagnifier, null, rectDest, null);

    }

    private void circularCropOroginalBitmap(Bitmap mOriginalBmp) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        mCanvasOriginalCircular.drawColor(BGCOLORCONSTANT, Mode.SRC_OVER);
        mCanvasOriginalCircular.drawCircle((float) mRectDestClip.centerX(),
                (float) mRectDestClip.centerY(),
                (float) (mRectDestClip.width() / MAGNIFIER_ZOOM_IN_FACTOR), paint);

        //Log.i("mCanvasOriginalCircular >>>>>>>> ", "" + (float) (mRectDestClip.width() / MAGNIFIER_ZOOM_IN_FACTOR));

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        mCanvasOriginalCircular.drawBitmap(mOriginalBmp, rectSrc, mRectDestClip, paint);

    }

    public void getExactTouchPoint(float x1, float y1, int Display_width, int Display_height,
                                   float rotate, float rotateTemp, float scaleFactor, float tx,
                                   float ty) {

		/*Log.v("HI",
				new StringBuilder(String.valueOf(x1)).append(", ").append(y1)
						.append(", ").append(rotate).append(", ")
						.append(rotateTemp).toString());*/
        this.x1 = x1;
        this.y1 = y1;
        newX1 = x1 - tx;
        newY1 = y1 - ty;

        Matrix matrix = new Matrix();
        matrix.postRotate(360.0f - (rotate + rotateTemp),
                (float) (Display_width / MAGNIFIER_ZOOM_IN_FACTOR),
                (float) (Display_height / MAGNIFIER_ZOOM_IN_FACTOR));
        matrix.postScale(1.0f / scaleFactor, 1.0f / scaleFactor,
                (float) (Display_width / MAGNIFIER_ZOOM_IN_FACTOR),
                (float) (Display_height / MAGNIFIER_ZOOM_IN_FACTOR));

        float[] fs = new float[MAGNIFIER_ZOOM_IN_FACTOR];
        fs[0] = newX1;
        fs[1] = newY1;
        matrix.mapPoints(fs);
        newX1 = fs[0];
        newY1 = fs[1];

    }

    public void resetMagnifyPath() {
        mPath2Magnifier.reset();
    }

    public void recycleBitmap() {
        if (mBitmapMagnify != null) {
            mBitmapMagnify.recycle();
            mBitmapMagnify = null;
        }
        if (bitmapMagnifier != null) {
            bitmapMagnifier.recycle();
            bitmapMagnifier = null;
        }
        if (bitmapOriginalCircular != null) {
            bitmapOriginalCircular.recycle();
            bitmapOriginalCircular = null;
        }
    }
}
