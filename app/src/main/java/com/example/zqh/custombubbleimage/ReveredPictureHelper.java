package com.example.zqh.custombubbleimage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by zqh on 2017/1/20.
 */
public class ReveredPictureHelper {


    /**
     * 获取原图的的Rect
     *
     * @param org
     * @return
     */
    private static Rect getRectByByte(byte[] org) {
        Rect rect = new Rect();
        ByteBuffer byteBuffer = ByteBuffer.wrap(org).order(ByteOrder.nativeOrder());
        rect.set(byteBuffer.getInt(3 * 4), byteBuffer.getInt(5 * 4), byteBuffer.getInt(4 * 4), byteBuffer.getInt(6 * 4));
        return rect;
    }


    /**
     * 获取气泡背景框，可以翻转
     *
     * @return
     */
    public static Drawable getReversedBg(String drawableName,Context context) {
        Bitmap bgdrawable = getHcBitmap(context,
                "com.handcent.sms.skin.christmas20172", drawableName);
        Bitmap new_bitmap;
        Rect rect3 = new Rect();

        byte[] imgbyts_normal = bgdrawable.getNinePatchChunk();
        byte[] imgbyts_change;//变化后的图片字节数组
        Rect rect4 = getRectByByte(imgbyts_normal);//原始的Rect
        new_bitmap = reverseBitmap(bgdrawable, 0);//翻转图片获得新的图片
        imgbyts_change = getReverImgByte(ByteBuffer.wrap(imgbyts_normal),new_bitmap.getWidth()).array();//翻转图片的字节数组得到反转后字节数组
        //因为翻转，所有
        rect3.set(rect4.right, rect4.top, rect4.left, rect4.bottom);
        NinePatchDrawable ninePatchDrawable =
                new NinePatchDrawable(context.getResources(), new_bitmap,
                        imgbyts_change, rect3, null);
        return ninePatchDrawable;
    }


    /**
     * 气泡图片
     *
     * @param bgdrawable
     * @param bitmap_in
     * @return
     */
    public Bitmap getRoundCornerImage(Context context,Bitmap bgdrawable, Bitmap bitmap_in, int whitchWay) {
        int width = bitmap_in.getWidth();
        int height = bitmap_in.getHeight();
        Bitmap roundConcerImage = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        Rect rectF = new Rect(0, 0, bitmap_in.getWidth(), bitmap_in.getHeight());
        paint.setAntiAlias(true);

        Bitmap new_bitmap;
        Rect rect3 = new Rect();
        byte[] imgbyts_normal = bgdrawable.getNinePatchChunk();
        byte[] imgbyts_change;//变化后的图片字节数组
        Rect rect4 = getRectByByte(imgbyts_normal);//原始的Rect
        if (whitchWay == 0) {//右边
            new_bitmap = reverseBitmap(bgdrawable, 0);//翻转图片获得新的图片
            imgbyts_change = getReverImgByte(ByteBuffer.wrap(imgbyts_normal),bgdrawable.getWidth()).array();//翻转图片的字节数组得到反转后字节数组
            //因为翻转，所有
            rect3.set(rect4.right, rect4.top, rect4.left, rect4.bottom);
        } else if (whitchWay == 1) {//左边
            new_bitmap = bgdrawable;
            imgbyts_change = imgbyts_normal;
            rect3 = rect4;
        } else {
            return null;
        }

        NinePatchDrawable ninePatchDrawable =
                new NinePatchDrawable(context.getResources(), new_bitmap,
                        imgbyts_change, rect3, null);
        ninePatchDrawable.setBounds(rect);
        ninePatchDrawable.draw(canvas);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap_in, rectF, rect, paint);
        return roundConcerImage;
    }


    /**
     * 图片翻转
     *
     * @param bmp
     * @param flag 0为水平翻转；1：为垂直翻转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
//        if (ImageCache.get("reverseBitmap") != null) {
//            return ImageCache.get("reverseBitmap");
//        }
        float[] floats = null;
        switch (flag) {
            case 0: // 水平反转
                floats = new float[]{
                        -1f, 0f, 0f,
                        0f, 1f, 0f,
                        0f, 0f, 1f};
                break;
            case 1: // 垂直反转
                floats = new float[]{
                        1f, 0f, 0f,
                        0f, -1f, 0f,
                        0f, 0f, 1f};
                break;
        }

        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), matrix, true);
        }
//        ImageCache.put("reverseBitmap", bmp);
        return bmp;
    }

    /**
     * 图片的字节数组进行翻转后的数据翻转
     *
     * @param org 原始图片
     * @return
     */
    public static ByteBuffer getReverImgByte(ByteBuffer org, int imgWidth) {
        //Docs check the NinePatchChunkFile
        ByteBuffer buffer = ByteBuffer.allocate(84).order(ByteOrder.nativeOrder());
        //was translated
        org = org.order(ByteOrder.nativeOrder());


        buffer.putInt(org.getInt());
        //skip
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());

        //padding
        int left = org.getInt();
        int right = org.getInt();
        buffer.putInt(right);
        buffer.putInt(left);
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());

        //skip 4 bytes
        buffer.putInt(org.getInt());

        int left1 = org.getInt();//+22
        int right1 = org.getInt();//+22

        int right2 = imgWidth - left1;
        int left2 = imgWidth - right1;

        buffer.putInt(left2); // left
        buffer.putInt(right2); // right
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());

        int color_1 = org.getInt();//无色
        int color_2 = org.getInt();//有色
        int color_3 = org.getInt();//有色

        buffer.putInt(color_3);
        buffer.putInt(color_2);
        buffer.putInt(color_1);


        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());
        buffer.putInt(org.getInt());

        Log.i("newButter", bufferToHex(buffer.array()));
        return buffer;
    }

    /**
     * 字节数组打印
     *
     * @param buffer
     * @return
     */
    private static String bufferToHex(byte[] buffer) {
        final char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        if (buffer != null) {
            int length = buffer.length;
            if (length > 0) {
                StringBuilder hex = new StringBuilder(2 * length);
                int split = 0;
                for (int i = 0; i < length; ++i) {
                    byte l = (byte) (buffer[i] & 0x0F);
                    byte h = (byte) ((buffer[i] & 0xF0) >> 4);

                    hex.append(hexChars[h]);
                    hex.append(hexChars[l]);
                    split++;
                    if (split == 4) {
                        hex.append(" ");
                        split = 0;
                    }
                }

                return hex.toString();
            } else {
                return "";
            }
        }

        return null;
    }


    /**
     * 根据资源名获取资源图片
     * @param cnt
     * @param packName
     * @param DrawableName
     * @return
     */
    public static Bitmap getHcBitmap(Context cnt, String packName, String DrawableName) {
        Resources res = null;
        try {
            res = cnt.getPackageManager()
                    .getResourcesForApplication(packName);
            int resID = res.getIdentifier(DrawableName,
                    "drawable", packName);
            if (resID > 0) {
                return BitmapFactory.decodeResource(res, resID);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            res=cnt.getResources();
            int resID = res.getIdentifier(DrawableName,
                    "drawable", cnt.getPackageName());
            return BitmapFactory.decodeResource(res, resID);
        }
        return null;
    }
}
