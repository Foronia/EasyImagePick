package easyimagepick.foronia.com.library.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by Foronia on 2016/8/5.
 */
public class ImageLoader {

    //单例模式
    private static ImageLoader mInstance;

    /**
     * 图片缓存的核心对象
     */
    private LruCache<String,Bitmap> mLruCache;

    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 默认线程数
     */
    private static final int DEFAULT_THREED_COUNT=5;

    private Type mType= Type.LIFO;

    /**
     * 任务堆栈
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮训线程用于获取任务到任务池
     */
    private Thread mPoolThread;

    /**
     * 后台消息处理handler
     */
    private Handler mPoolThreadHandler;
    /**
     * 前台通知Uihandler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private Semaphore mSemaphoreThreadPool;

    private Runnable task;

    public Runnable getTask() {
        if(mType== Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if(mType== Type.LIFO){
            return mTaskQueue.removeLast();
        }
        return task;
    }


    public enum Type{
        FIFO,LIFO
    }



    public ImageLoader(int defaultThreedCount, Type type) {
        init(defaultThreedCount,type);
    }

    private void init(int defaultThreedCount, Type type) {
        //初始化线程
        mPoolThread=new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //线程池取出一个任务进行执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();

            }
        };

        mPoolThread.start();


        int memorysize=getMemorySize()/8;

        //初始化图片缓存
        mLruCache=new LruCache<String, Bitmap>(memorysize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        //创建线程池
        mThreadPool= Executors.newFixedThreadPool(defaultThreedCount);

        //初始化任务堆栈
        mTaskQueue=new LinkedList<Runnable>();

        //模式
        mType=type;

        //后台同时进行线程的信号量
        mSemaphoreThreadPool=new Semaphore(defaultThreedCount);
    }

    /**
     * 获取应用的最大内存
     * @return
     */
    public int getMemorySize() {
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        return maxMemory;
    }



    public void LoadImage(final String path, final ImageView imageView){
        imageView.setTag(path);
        if(mUIHandler == null){
            mUIHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取得到的图片，为ImageView回调设置图片
                    ImgBeanHolder holder= (ImgBeanHolder) msg.obj;
                    Bitmap bitmap=holder.bitmap;
                    ImageView imageView=holder.imageView;
                    String path=holder.path;
                    if(imageView.getTag().toString().equals(path)){
                        imageView.setImageBitmap(bitmap);
                        Animation animation=new AlphaAnimation(0,1);
                        animation.setDuration(1000);
                        imageView.startAnimation(animation);
                    }
                }
            };
        }

        Bitmap bitmap=getBitmapFromLruCache(path);

        if(bitmap !=null){
            refreshBitmap(path,imageView,bitmap);
        }else {
            addTask(new Runnable(){

                @Override
                public void run() {
                    ImageSize imageSize=getImageViewSize(imageView);
                    //压缩图片
                    Bitmap bm=decodeSampledBitmapFromPath(path,imageSize.width,imageSize.height);
                    //3.把图片加入到缓存
                    addBitmapToLruCache(path, bm);
                    refreshBitmap(path, imageView, bm);

                    mSemaphoreThreadPool.release();
                }
            });
        }

    }

    private void refreshBitmap(String path, ImageView imageView, Bitmap bitmap) {
        Message message=Message.obtain();
        ImgBeanHolder holder=new ImgBeanHolder();
        holder.bitmap=bitmap;
        holder.path=path;
        holder.imageView=imageView;
        message.obj=holder;
        mUIHandler.sendMessage(message);
    }

    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        //获取图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = caculateInSampleSize(options, width, height);

        //使用获取到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }
    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqwidth
     * @param reqheight
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
     * 根据ImageView 获取适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize=new ImageSize();
        DisplayMetrics displayMetrics=imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams=imageView.getLayoutParams();
        int width=imageView.getWidth();
        if(width<=0){
            try {
                width=layoutParams.width;
            } catch (Exception e) {
                width=0;
            }
        }

        if(width<=0){
            width=getImageViewFieldValue(imageView,"mMaxWidth");
        }

        if(width<=0){
            width=displayMetrics.widthPixels;
        }

        int height=imageView.getHeight();
        if(height<=0){
            try {
                height=layoutParams.height;
            } catch (Exception e) {
                height=0;
            }
        }

        if(height<=0){
            height=getImageViewFieldValue(imageView,"mMaxHeight");
        }

        if(height<=0){
            height=displayMetrics.heightPixels;
        }

        imageSize.width=width;
        imageSize.height=height;
        return imageSize;
    }





    /**
     * 通过反射获取最大值
     * @param object
     * @param fieldName
     * @return
     */
    private int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }


    /**
     * 同步
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null) {

                mSemaphorePoolThreadHandler.acquire();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }



    private Bitmap getBitmapFromLruCache(String path) {
        return mLruCache.get(path);
    }


    private class ImageSize {
        int width;
        int height;
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }


    public static ImageLoader getIntance(){
        if(mInstance==null){
            /**
             * 保证同一时刻只有一个线程访问该方法
             */
            synchronized (ImageLoader.class){
                if(mInstance==null){
                    mInstance=new ImageLoader(DEFAULT_THREED_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 设置线程数和加载模式
     * @param count
     * @param type Type.FIFO Type.LIFO
     * @return
     */
    public static ImageLoader getIntance(int count,Type type){
        if(mInstance==null){
            /**
             * 保证同一时刻只有一个线程访问该方法
             */
            synchronized (ImageLoader.class){
                if(mInstance==null){
                    mInstance=new ImageLoader(count,type);
                }
            }
        }
        return mInstance;
    }
}
