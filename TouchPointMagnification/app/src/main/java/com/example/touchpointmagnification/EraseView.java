package com.example.touchpointmagnification;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

public class EraseView extends View {

    private Paint colorPaint;
    private boolean isMagnifier = false;

    private Bitmap mBlurBitmap;
    private Context mContext;

    private int mLineWidth = 50;
    private OnViewLoadListener mLoadListener;
    private MagnifyView mMagnifyView;
    private Bitmap user_selected_bitmap;
    private Paint mPaintLine;
    private Path mPath;
    private float scale_factor = 1.0f;

    private int mUserSelectedColor = 0;
    private int screen_width, screen_height;
    private float mX;
    private float mY;

    private ArrayList<Drawing> pathsList = new ArrayList<Drawing>();
    private float rotate = 0.0f;
    private float rotateTemp = 0.0f;

    private float tx = 0.0f;
    private float ty = 0.0f;

    private float x1;
    private float y1;

    public EraseView(Context ct) {
        super(ct);
        mContext = ct;
        init(ct);
    }

    public EraseView(Context ct, AttributeSet attrs) {
        super(ct, attrs);
        mContext = ct;
        init(ct);
    }

    public EraseView(Context ct, AttributeSet attrs, int defStyle) {
        super(ct, attrs, defStyle);
        mContext = ct;
        init(ct);
    }

    public static Bitmap fitToViewByRect(Bitmap bmp, int screenWidth,
                                         int screenHeight) {
        RectF defaultRect = new RectF(0.0f, 0.0f, (float) bmp.getWidth(), (float) bmp.getHeight());
        RectF screenRect = new RectF(0.0f, 0.0f, (float) screenWidth, (float) screenHeight);
        Matrix defToScreenMatrix = new Matrix();
        defToScreenMatrix.setRectToRect(defaultRect, screenRect, ScaleToFit.CENTER); // This Will Scale Image From
        // defaultRect To screenRect
        Bitmap newbmp = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888); // Created Bitmap Of
        // Original Image Which Is Translated In Matrix
        new Canvas(newbmp).drawBitmap(bmp, defToScreenMatrix, null); // Created
        // Canavas
        // Of
        // Translated
        // Original
        // User
        // Selected
        // Bitmap
        // i.e
        // newbmp
        if (bmp != null) {
            bmp.recycle();
        }
        return newbmp;
    }

    public void setLoadListener(OnViewLoadListener listener) {
        mLoadListener = listener;
    }

    @SuppressLint({"NewApi"})
    private void init(Context context) {
        mMagnifyView = new MagnifyView(context);
        mContext = context;
        mPath = new Path();
        mPaintLine = new Paint();
        mPaintLine.setStyle(Style.STROKE);
        mPaintLine.setStrokeJoin(Join.ROUND);
        mPaintLine.setStrokeCap(Cap.ROUND);
        mPaintLine.setStrokeWidth((float) mLineWidth);

        mPaintLine.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        mPaintLine.setColor(0);

        if (VERSION.SDK_INT >= 11) {
            setLayerType(1, null);
        }

        colorPaint = new Paint();
        colorPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OVER));
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

        if (width > 0 && height > 0 && mLoadListener != null) {
            mLoadListener.onViewInflated();
            mLoadListener = null;
        }
        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    protected void onDraw(Canvas canvas) {

        if (!(user_selected_bitmap == null || user_selected_bitmap.isRecycled())) {

            canvas.save();
            canvas.translate(tx, ty);
            canvas.scale(scale_factor, scale_factor, (float) (canvas.getWidth() / 2),
                    (float) (canvas.getHeight() / 2));

			/*Log.i("canavas width >>>>> ", "" + (float) (canvas.getWidth() / 2));
            Log.i("canavas height >>>>> ", "" + (float) (canvas.getHeight() / 2));*/

            canvas.rotate(rotate + rotateTemp, (float) (canvas.getWidth() / 2),
                    (float) (canvas.getHeight() / 2));
            canvas.drawBitmap(user_selected_bitmap, 0.0f, 0.0f, null);

            Iterator<Drawing> it = pathsList.iterator();
            while (it.hasNext()) {
                Drawing d = (Drawing) it.next();

				/*Log.i("D_Color >>>>>> ", "" + d.getColor());
				Log.i("D_StrokeWidth >>>>>> ", "" + d.getStrokeWidth());
				Log.i("D_Paint >>>>> ", "" + d.getPaint());
				Log.i("D_Path >>>>> ", "" + d.getPath());*/

                Path p = d.getPath();

                //Log.i("Path >>>> ", "" + p);

                d.getPaint().setStrokeWidth((float) d.getStrokeWidth());
                canvas.drawPath(p, d.getPaint());
            }

            //mPaintLine.setStrokeWidth((float) mLineWidth);
            canvas.drawPath(mPath, mPaintLine);
            canvas.restore();

            extractOriginalPixels(canvas);

        }

        if (isMagnifier) {

            mMagnifyView.getExactTouchPoint(x1, y1, getWidth(), getHeight(), rotate, rotateTemp,
                    scale_factor, tx, ty);
            mMagnifyView.customMagnifyView(canvas, mLineWidth, mPaintLine, user_selected_bitmap,
                    getWidth(), getHeight(), scale_factor, mBlurBitmap);

        }

        //Log.v("xyz", getWidth() + " canvas " + getHeight());

        super.onDraw(canvas);
    }

    private void extractOriginalPixels(Canvas canvas) {

        canvas.save();
        canvas.translate(tx, ty);
        canvas.scale(scale_factor, scale_factor, (float) (canvas.getWidth() / 2),
                (float) (canvas.getHeight() / 2));
        canvas.rotate(rotate + rotateTemp, (float) (canvas.getWidth() / 2),
                (float) (canvas.getHeight() / 2));

        if (mBlurBitmap != null) {

            canvas.drawBitmap(mBlurBitmap, 0.0f, 0.0f, colorPaint);

        }

        canvas.restore();

    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {

        x1 = event.getX();

        y1 = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN /* 0 */:

                isMagnifier = true;

                mPath.reset();
                mPath.moveTo(x1, y1);
                mX = x1;
                mY = y1;
                mMagnifyView.resetMagnifyPath();

                invalidate();

                break;

            case MotionEvent.ACTION_UP /* 1 */:

                isMagnifier = false;

                mPath.lineTo(mX, mY);
                pathsList.add(new Drawing(mPath, mLineWidth, mUserSelectedColor, mPaintLine));
                mPath = new Path();
                user_selected_bitmap = getErasedBitmap();
                pathsList.clear();

                invalidate();

                mMagnifyView.resetMagnifyPath();

                break;

            case MotionEvent.ACTION_MOVE /* 2 */:

                mPath.lineTo(x1, y1);

                mX = x1;

                mY = y1;

                invalidate();

                break;
        }

        return true;

    }

    public void clearCanvas() {
        pathsList.clear();
        invalidate();
        init(mContext);
    }

    public void setPickedBitmap(Bitmap bitmap) {
        screen_width = getWidth();
        screen_height = getHeight();
        user_selected_bitmap = fitToViewByRect(bitmap, screen_width, screen_height);

        new BlurOperation().execute();
    }

    public void onDestroy() {
        if (mBlurBitmap != null) {
            mBlurBitmap.recycle();
            mBlurBitmap = null;
        }
        if (user_selected_bitmap != null) {
            user_selected_bitmap.recycle();
            user_selected_bitmap = null;
        }
        mMagnifyView.recycleBitmap();
        System.gc();
    }

    public Bitmap getErasedBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(screen_width, screen_height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(user_selected_bitmap, 0.0f, 0.0f, null);

        Iterator<Drawing> it = pathsList.iterator();
        while (it.hasNext()) {
            Drawing d = (Drawing) it.next();
            Path p = d.getPath();
            d.getPaint().setStrokeWidth(((float) d.getStrokeWidth()) * (1.0f / scale_factor));
            geCalculatedPath(p, 360.0f - rotate, -tx, -ty, 1.0f / scale_factor,
                    (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));

            canvas.drawPath(p, mPaintLine);

        }

        mPaintLine.setStrokeWidth(((float) mLineWidth) * (1.0f / scale_factor));
        geCalculatedPath(mPath, 360.0f - rotate, -tx, -ty, 1.0f / scale_factor,
                (float) (canvas.getWidth() / 2), (float) (canvas.getHeight() / 2));

        canvas.drawPoint(mX, mY, mPaintLine);

        canvas.drawPath(mPath, mPaintLine);

        return bitmap;
    }

    private void geCalculatedPath(Path path, float angle, float trX, float trY,
                                  float scale, float cx, float cy) {
        path.offset(trX, trY, path);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, cx, cy);
        matrix.postScale(scale, scale, cx, cy);
        path.transform(matrix);
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    private class BlurOperation extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;

        private BlurOperation() {
        }

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        protected String doInBackground(Void... params) {
            mBlurBitmap = fastblur(user_selected_bitmap, 20);
            return null;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            invalidate();
        }
    }
}
