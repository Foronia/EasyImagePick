package easyimagepick.foronia.com.library.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Foronia on 2016/9/5.
 */
public class SDCardSaveImgUtil {
    private static SDCardSaveImgUtil instance;
    /**
     * SD卡目录
     */
    private static String SD_DIR="";
    /**
     * SD卡下文件夹名称
     */
    private static final String FILE_DIR_NAME="/ATOMimg/";
    /**
     * 文件完整路径
     */
    private static String filepath;

    private Context mContext;

    private SDCardSaveImgUtil(Context context) {
        mContext=context;
        if(isHasSdcard()){
            SD_DIR= Environment.getExternalStorageDirectory().getPath();
            createFileDir();
        }else {
            SD_DIR= Environment.getDataDirectory().getPath();
            createFileDir();
        }
    }

    public static SDCardSaveImgUtil getInstance() {
        if(instance==null){
            synchronized (SDCardSaveImgUtil.class){
                if(instance==null){
                    instance=new SDCardSaveImgUtil(MyApplication.getContext());
                }
            }

        }
        return instance;
    }

    //创建目录
    private void createFileDir(){
        File dDir=new File(SD_DIR);
        if(!dDir.exists()){
            dDir.mkdirs();
        }
        File destDir = new File(getFilePath());
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }



    //获取的文件夹的路径
    public String getFilePath(){
        return SD_DIR+FILE_DIR_NAME;
    }

    //判断是否有sd卡
    private boolean isHasSdcard(){
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    //删除Atom已存在文件
    public  void deleteExistFile(String filename){
        filepath=getFilePath()+filename;
        File mfile=new File(filepath);
        if(mfile.exists()){
            mfile.delete();
        }
    }


    /**
     * 图片加文字水印
     * @param text
     * @param bmp
     * @return
     */
    public Bitmap drawTextToBitmap(String text, Bitmap bmp){

        //获取图片的宽高
        int width=bmp.getWidth();
        int height=bmp.getHeight();
        float scalsize=(float) (width*height)/(float)(750*1334);
        float sclesize2= (float) Math.sqrt(scalsize);

        //绘制同等大小的bitmap
        Bitmap bitmap= Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        //bitmap加到画布上
        Canvas canvas=new Canvas(bitmap);
        canvas.drawBitmap(bmp,0,0,null);
        Paint paint =new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#ffffff"));
        paint.setTextSize(30*sclesize2);
        paint.setAntiAlias(true);
        Rect bounds = new Rect();
        paint.getTextBounds(text,0,text.length(),bounds);
        paint.setAlpha(200);
        paint.setShadowLayer(1,2,2,0x4c000000);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.drawText(text,width-bounds.width()-20,height-20,paint);
        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();
        return bitmap;
    }


    /**
     * 高效加载bitmap
     */
    public Bitmap loadImgPathToBitmap(String filepath, int reqwidth, int reqheight){
        //获取图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);
        options.inSampleSize = caculateInSampleSize(options, reqwidth, reqheight);
        //使用获取到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }


    /**
     * Atom文件夹中根据文件名加载图片
     * @param filename
     * @param reqwidth
     * @param reqheight
     * @return
     */
    public Bitmap loadImgBitmap(String filename, int reqwidth, int reqheight){
        //获取图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(getFilePath()+filename, options);
        options.inSampleSize = caculateInSampleSize(options, reqwidth, reqheight);
        //使用获取到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(getFilePath()+filename, options);
    }

    /**
     * 计算SampleSize
     * @param options bitmap的尺寸
     * @param reqwidth  期望的宽度
     * @param reqheight  期望的高度
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqwidth, int reqheight) {
        int width = options.outWidth;
        int hegiht = options.outHeight;
        int inSampleSize = 1;
        if (width > reqwidth || hegiht > reqheight) {

            int widthRadio = Math.round(width * 1.0f / reqwidth);
            int heightRadio = Math.round(hegiht * 1.0f / reqheight);
            inSampleSize = Math.max(widthRadio, heightRadio);

        }
        return inSampleSize;
    }
    /**
     * 计算SampleSize
     * @param options bitmap的尺寸
     * @param reqwidth  期望的宽度
     * 仅对宽度有要求的
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqwidth) {
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqwidth) {
            inSampleSize = (width/reqwidth)+1;
        }
        return inSampleSize;
    }



    /**
     * Content Uri 转 path
     * @param uri
     * @return
     */
    public String changeUriToFilePath(final Uri uri) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = mContext.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    //加载固定宽度bitmap
    public Bitmap loadFixedBitmap(String path, int width){
        Bitmap bitmap=null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = caculateInSampleSize(options, width);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }



    /**
     * Uri 转bitmap
     * @param uri
     * @return
     */
    public Bitmap changeUriToBitmap(Uri uri){
        Bitmap bitmap=null;
        try {
            bitmap= BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * bitmap 转成文件
     */
    public void changeBitmapToFile(Bitmap bm, String filename) throws IOException {
        String path =getFilePath()+filename;
        File dirFile = new File(path);
        if(dirFile.exists()){
            dirFile.delete();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dirFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        bos.flush();
        bos.close();
    }

    public Uri changeStringToUri(String filename){
        File mFile=new File(getFilePath()+filename);
        return Uri.fromFile(mFile);
    }

    /**
     * 加水印的压缩图片
     * @param text
     * @param fileUri
     * @return
     * @throws Exception
     */
    public File scal(String text, Uri fileUri) throws Exception {
        String path = fileUri.getPath();
        File outputFile = new File(path);
        File newoutputFile = null;
        long fileSize = outputFile.length();
        final long fileMaxSize = 300 * 400;
        if (fileSize >= fileMaxSize) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int height = options.outHeight;
            int width = options.outWidth;

            double scale = Math.sqrt((float) fileSize / fileMaxSize);
            options.outHeight = (int) (height / scale);
            options.outWidth = (int) (width / scale);
            options.inSampleSize = (int) (scale + 0.5);
            // options.inSampleSize = (int) (scale);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            bitmap=drawTextToBitmap(text,bitmap);
            FileOutputStream fos = null;
            newoutputFile=new File(getFilePath()+ UUID.randomUUID()+".jpg");
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85, fos);
            fos.flush();
            fos.close();
            try {
                if(bitmap!=null){
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }else{

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newoutputFile;
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmap=drawTextToBitmap(text,bitmap);
            FileOutputStream fos = null;
            newoutputFile=new File(getFilePath()+ UUID.randomUUID()+".jpg");
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85, fos);
            fos.flush();
            fos.close();
            try {
                if(bitmap!=null){
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }else{

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newoutputFile;
    }


    /**
     *
     * @param fileUri 图片uri路径压缩图片
     * @return
     */
    public File scal(Uri fileUri) throws Exception {
        String path = fileUri.getPath();
        File outputFile = new File(path);
        long fileSize = outputFile.length();
        final long fileMaxSize = 300 * 400;
        if (fileSize >= fileMaxSize) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int height = options.outHeight;
            int width = options.outWidth;

            double scale = Math.sqrt((float) fileSize / fileMaxSize);
            options.outHeight = (int) (height / scale);
            options.outWidth = (int) (width / scale);
            options.inSampleSize = (int) (scale + 0.5);
            options.inJustDecodeBounds = false;

            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            FileOutputStream fos = null;
            File newoutputFile = null;

            newoutputFile=new File(getFilePath()+ UUID.randomUUID()+".jpg");
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();

            try {
                if(bitmap!=null){

                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }else{

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newoutputFile;
        }
        return outputFile;
    }

    /**
     * 压缩图片仅对图片宽度有要求
     * @param text 水印文字
     * @return  输出的文件
     */
    public File scalImage(String text, Uri fileUri){
        Bitmap bitmap=loadFixedBitmap(fileUri.getPath(),720);
        bitmap=drawTextToBitmap(text,bitmap);
       // bitmap=drawWarterMark(text);
        File newoutputFile = null;
        FileOutputStream fos = null;
        newoutputFile=new File(getFilePath()+ UUID.randomUUID()+".jpg");
        try {
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(bitmap!=null){
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }else{

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newoutputFile;
    }


    public File scal(String text, Uri fileUri, String filename) throws Exception {
        String path = fileUri.getPath();
        File outputFile = new File(path);
        File newoutputFile = null;
        long fileSize = outputFile.length();
        final long fileMaxSize = 300 * 400;
        if (fileSize >= fileMaxSize) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int height = options.outHeight;
            int width = options.outWidth;

            double scale = Math.sqrt((float) fileSize / fileMaxSize);
            options.outHeight = (int) (height / scale);
            options.outWidth = (int) (width / scale);
            options.inSampleSize = (int) (scale + 0.5);

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            bitmap=drawTextToBitmap(text,bitmap);
            FileOutputStream fos = null;
            newoutputFile=new File(getFilePath()+filename+".jpg");
            if(newoutputFile.exists()){
                newoutputFile.delete();
            }
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85, fos);
            fos.flush();
            fos.close();
            try {
                if(bitmap!=null){
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }else{

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newoutputFile;
        }else {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmap=drawTextToBitmap(text,bitmap);
            FileOutputStream fos = null;
            newoutputFile=new File(getFilePath()+filename+".jpg");
            fos = new FileOutputStream(newoutputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,85, fos);
            fos.flush();
            fos.close();
            try {
                if(bitmap!=null){
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }else{

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newoutputFile;
    }



}
