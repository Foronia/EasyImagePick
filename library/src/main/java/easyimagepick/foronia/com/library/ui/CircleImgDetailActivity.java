package easyimagepick.foronia.com.library.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.util.DepthPageTransformer;
import easyimagepick.foronia.com.library.util.ImageBeanUtil;
import easyimagepick.foronia.com.library.util.SDCardSaveImgUtil;
import easyimagepick.foronia.com.library.util.ZoomImageView;

import static easyimagepick.foronia.com.library.util.StringUtil.ADDICON;
import static easyimagepick.foronia.com.library.util.StringUtil.INTENTLIST;

/**
 * Created by Foronia on 2017/8/17 0017.
 */

public class CircleImgDetailActivity extends Activity{

    private PopupWindow pop = null;
    private LinearLayout ll_popup;
    private View parentview;
    private Bitmap bitmap;
    private String path;
    private File mFile;
    private List<String> imglist=new ArrayList<String>();
    private String mDirPath;
    private int positon=0;
    private String action="";
    private TextView img_count;
    private ViewPager img_detail_vp;
    private PagerAdapter mAdapter;
    private ImageView[] mImageViews;
    private SDCardSaveImgUtil sdCardSaveImgUtil;
    private SystemBarTintManager tintManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        parentview=LayoutInflater.from(this).inflate(R.layout.activity_circle_img_detail,null);
        setContentView(R.layout.activity_circle_img_detail);
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        //设置颜色和透明度
        tintManager.setTintColor(Color.parseColor("#000000"));
        init_datas();
        init_View();
        set_datas();
        init_Event();
        sdCardSaveImgUtil=SDCardSaveImgUtil.getInstance();
    }

    private void init_datas() {
        Intent intent =getIntent();
        try {
            positon=intent.getIntExtra("position", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (intent.getStringArrayListExtra(INTENTLIST)!=null){
            imglist=intent.getStringArrayListExtra(INTENTLIST);
            if (imglist.size()!=0&&imglist.get(imglist.size()-1).equals(ADDICON)){
                imglist.remove(imglist.size()-1);
            }
        }else {
            imglist.addAll(ImageBeanUtil.getSelectImg());
        }

        mImageViews=new ImageView[imglist.size()];

    }

    private void init_View() {
        img_detail_vp= (ViewPager) findViewById(R.id.img_detail_vp);
        img_count= (TextView) findViewById(R.id.img_count);
        mAdapter=new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                ZoomImageView imageView=new ZoomImageView(CircleImgDetailActivity.this);
                Glide.with(CircleImgDetailActivity.this)
                        .load(imglist.get(position))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
                container.addView(imageView);
                mImageViews[position]=imageView;
                imageView.setOnLongClicklistenr(new ZoomImageView.OnImageLongClickListener() {
                    @Override
                    public void OnImageLongClick() {
                        showpopuwindow(position);
                    }

                    @Override
                    public boolean OnSingleClick() {
                        handler.sendEmptyMessage(0x220);
                        return true;
                    }
                });
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews[position]);
            }

            @Override
            public int getCount() {
                return imglist.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }
        };
    }

    //Activity 中获取屏幕高度
    private int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point.x;
    }

    //Activity 中获取屏幕高度
    private int getScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        // int screenWidth = point.x;
        return point.y;
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


    private void showpopuwindow(final int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_circle_popupwindows, null);
        pop = new PopupWindow(CircleImgDetailActivity.this);
        ll_popup = (LinearLayout) view.findViewById(R.id.save_img_popup);
        ll_popup.startAnimation(AnimationUtils.loadAnimation(CircleImgDetailActivity.this, R.anim.activity_translate_in));
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        RelativeLayout parent = (RelativeLayout) view.findViewById(R.id.parent);
        Button bt1 = (Button) view
                .findViewById(R.id.item_circle_popupwindows_save);
        Button bt2 = (Button) view
                .findViewById(R.id.item_circle_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadImageSimpleTarget(position);
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.clearAnimation();
            }
        });
        int navigationBar=0;
        if(checkDeviceHasNavigationBar(getApplicationContext())){
            navigationBar=getNavigationBarHeight(getApplicationContext());
        }
        pop.showAtLocation(parentview, Gravity.BOTTOM, 0, navigationBar);
    }
    private SimpleTarget target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(final Bitmap bitmap, GlideAnimation glideAnimation) {
            //这里我们拿到回掉回来的bitmap，可以加载到我们想使用到的地方
            if(bitmap!=null){
                path=sdCardSaveImgUtil.getFilePath()+System.currentTimeMillis()+".jpg";
                mFile=new File(path);
                if (mFile.exists()) {
                    mFile.delete();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream out = new FileOutputStream(mFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                            handler.sendEmptyMessage(1000);
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1001);
                        }
                    }
                }).start();
            }else {
                handler.sendEmptyMessage(1001);
            }

        }
    };
    private void loadImageSimpleTarget(int position) {
        Glide.with( this ) // could be an issue!
                .load( imglist.get(position))
                .asBitmap()   //强制转换Bitmap
                .into( target );
    }

    private void saveImgFile() {
        try {

            if(bitmap!=null){
                path=sdCardSaveImgUtil.getFilePath()+System.currentTimeMillis()+".jpg";
                mFile=new File(path);
                if (mFile.exists()) {
                    mFile.delete();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream out = new FileOutputStream(mFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                            handler.sendEmptyMessage(1000);
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1001);
                        }
                    }
                }).start();
            }else {
                handler.sendEmptyMessage(1001);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1000:
                    Toast.makeText(CircleImgDetailActivity.this, "图片保存在ATOMimg相册中", Toast.LENGTH_SHORT).show();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mFile)));
                    break;
                case 1001:
                    Toast.makeText(CircleImgDetailActivity.this, "图片保存失败，请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
                case 0x220:
                    finish();
                    overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                    break;
            }
        }
    };

    private static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }

        return hasNavigationBar;

    }

    private static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }


    private void set_datas() {
        img_detail_vp.setAdapter(mAdapter);
        img_detail_vp.setCurrentItem(positon);
        img_detail_vp.setOffscreenPageLimit(3);
        img_detail_vp.setPageTransformer(true, new DepthPageTransformer());
        img_count.setText((positon + 1) + "/" + imglist.size());
        img_detail_vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                img_count.setText((position + 1) + "/" + imglist.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void init_Event() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            handler.sendEmptyMessage(0x220);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bitmap=null;
    }
}
