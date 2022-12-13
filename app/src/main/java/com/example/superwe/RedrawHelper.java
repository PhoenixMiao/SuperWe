package com.example.superwe;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RedrawHelper {
    private final static String TAG = "RedrawHelper";

    public static File getFileByUri(Uri uri, Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA }, buff.toString(), null, null);
                int index = 0;
                int dataIdx;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2以后
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }

    public static Uri redrawScreenshot(Context context,Uri uri) {
        Bitmap bitmap = null;
//        if (file == null) {
//            return "";
//        }
//        FileInputStream fis;
//        try {
//            fis = new FileInputStream(file);
//            bitmap = BitmapFactory.decodeStream(fis);
//            fis.close();
//            if (bitmap != null) {
//                System.out.println("bitmap is not null");
//            } else {
//                System.out.println("bitmap is null");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap bitmaps = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bitmaps);


        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(80);
        paint.setStrokeWidth(10);
        canvas.drawText("微信",55,2150,paint);
        canvas.drawText("发现",600,2150,paint);
        canvas.drawRect(55,2200,225,2340,paint);
        canvas.drawRect(600,2200,770,2340,paint);

//        Date date = new Date();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String fileName = df.format(date)+".png";
//
//        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
//        String filePath = sdCardPath + File.separator + "Superwe" + File.separator + "Screenshot" + File.separator + fileName;
        Uri returnUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmaps, null,null));
//        savePic(bitmaps,filePath);

        return returnUri;
    }

    private static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            File file = new File(strFileName);

            File fileParent = file.getParentFile();

            if (!fileParent.exists()) {

                fileParent.mkdirs();
            }
            file.createNewFile();
            fos = new FileOutputStream(new File(strFileName));
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


