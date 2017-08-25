package easyimagepick.foronia.com.library.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import easyimagepick.foronia.com.library.R;
import easyimagepick.foronia.com.library.adapter.CircleRecyclerAdapter;
import easyimagepick.foronia.com.library.customview.ListImageDirPopupWindow;
import easyimagepick.foronia.com.library.model.ImageLoaderBean;
import easyimagepick.foronia.com.library.util.DividerGridItemDecoration;
import easyimagepick.foronia.com.library.util.ImageBeanUtil;

/**
 * Created by Foronia on 2017/8/17 0017.
 */

public class CircleAlbumActivity extends Activity implements View.OnClickListener {

    //top
    private TextView tv_center;
    private ImageView iv_back;

    /**
     * 用于小图片的加载
     */
    private RecyclerView mRecyclerView;
    /**
     * 适配器
     */
    private CircleRecyclerAdapter mAdpater;
    /**
     * 图片数据源
     */
    private List<String> mImgs;
    /**
     * 相册目录集合
     */
    private List<ImageLoaderBean> mImageLoaderBeanList=new ArrayList<>();
    /**
     * 所有图片
     */
    private List<String> alllist=new ArrayList<>();
    private int imgsum;

    /**
     * 相册名
     */
    private TextView mDirName;
    /**
     * 已选择的数量
     */
    private TextView mCount;
    /**
     * 已选中的图片默认为0
     */
    public static int SELECT_COUNT = 0;

    /**
     * 预览选中图片的按钮
     */
    private TextView mButton;
    /**
     * 确认自己选中的所有值
     */
    private Button mConfirm;
    /**
     * 加载进度条
     */
    private ProgressDialog mProgressDialog;

    /**
     * 当前界面显示图片数量最多的文件夹
     */
    private File mCurrentDir;
    private int mMaxCount;
    /**
     * 数据加载完成
     */
    private static final int DATA_LOAD_COMPLETE=0x110;

    private int limit;

    private ListImageDirPopupWindow mDirPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_circle_album);
        init_Datas();
        init_View();
        set_Datas();
        init_Event();
    }

    private void init_Datas() {
        /**
         * 开启线程加载图片
         * 1.检查SD卡是否存在
         * 2.利用ContentProvier扫描手机中的图片
         */
        if (!checkIsHaveSDCard()) {
            Toast.makeText(CircleAlbumActivity.this, "当前存储卡不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            limit=getIntent().getIntExtra("limit",0)==0?9:getIntent().getIntExtra("limit",0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
        new Thread() {
            @Override
            public void run() {
                /**
                 * 获取系统所有图片的URI
                 */
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                //得到contentResolver
                ContentResolver cr = CircleAlbumActivity.this.getContentResolver();
                Cursor cursor = cr.query(mImgUri,
                        null,
                        MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED+ " desc");
                Set<String> mDirPaths =new HashSet<String>();
                while (cursor.moveToNext()){
                    //获取图片的绝对路径
                    String path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    alllist.add(path);
                    //获取图片的父文件的目录
                    File parentFile =new File(path).getParentFile();
                    if(parentFile == null){//如果为空 跳过
                        continue;
                    }

                    //获取图片的父文件的目录的路径
                    String dirpath=parentFile.getAbsolutePath();
                    ImageLoaderBean imageLoaderBean=null;

                    //如果set中已经存在 跳出
                    if(mDirPaths.contains(dirpath)){
                        continue;
                    }else {
                        mDirPaths.add(dirpath);
                        imageLoaderBean=new ImageLoaderBean();
                        imageLoaderBean.setMdirpath(dirpath);
                        imageLoaderBean.setFirstImgPath(path);
                        imageLoaderBean.setMname(new File(dirpath).getName());
                    }

                    if(parentFile.list() ==null || parentFile.list().length==0){
                        continue;
                    }
                    //过滤下文件夹中的jpg，jpeg和png
                    int picnum=parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            filename=filename.toLowerCase();
                            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                                return true;
                            return false;
                        }
                    }).length;

                    //相册中图片的数量
                    imageLoaderBean.setMcount(picnum);

                    mImageLoaderBeanList.add(imageLoaderBean);

                    imgsum+=picnum;
                }
                ImageLoaderBean imageLoaderBean=new ImageLoaderBean();
                imageLoaderBean.setMcount(alllist.size());
                imageLoaderBean.setMname("全部图片");
                imageLoaderBean.setMdirpath("全部图片");
                imageLoaderBean.setFirstImgPath(alllist.get(0));
                mImageLoaderBeanList.add(0,imageLoaderBean);
                //扫描完成
                cursor.close();
                //通知Handler
                handler.sendEmptyMessage(DATA_LOAD_COMPLETE);
            }
        }.start();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DATA_LOAD_COMPLETE:
                    mProgressDialog.dismiss();
                    //绑定数据到View中
                    dataBindView();
                     initDirPopupWindow();
                    break;
            }
        }
    };

    private void initDirPopupWindow() {
        mDirPopupWindow = new ListImageDirPopupWindow(this, mImageLoaderBeanList);
        mDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });
        mDirPopupWindow.setDirSelectOnListener(new ListImageDirPopupWindow.OnDirSelectListener() {
            @Override
            public void onSelect(ImageLoaderBean folderBean) {
                if(folderBean.getMdirpath().equals("全部图片")){
                    mImgs = alllist;
                    Collections.sort(mImgs, new FileComparator());
                    mAdpater = new CircleRecyclerAdapter(CircleAlbumActivity.this, mImgs,"全部图片");
                    mRecyclerView.setAdapter(mAdpater);
                    mAdpater.setOnItemClickListener(new CircleRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onLook(int position) {
                        }

                        @Override
                        public void onCheck(int size) {
                            mCount.setText(size + "张");
                        }

                        @Override
                        public void onUnCheck(int size) {
                            mCount.setText(size + "张");
                        }

                        @Override
                        public void onLongClick(int position) {

                        }

                        @Override
                        public void onImageCount(int count) {
                            if (count > 0) {
                                mConfirm.setBackgroundColor(Color.parseColor("#F49A28"));
                                mConfirm.setTextColor(Color.parseColor("#ffffff"));
                                mButton.setTextColor(Color.parseColor("#F49A28"));
                            } else {
                                mConfirm.setBackgroundColor(Color.parseColor("#d1d1d1"));
                                mConfirm.setTextColor(Color.parseColor("#b0b0b0"));
                                mButton.setTextColor(Color.parseColor("#b0b0b0"));
                            }
                        }
                    });
                    mDirName.setText("全部图片");
                    tv_center.setText("全部图片");
                    mDirPopupWindow.dismiss();
                }else {
                    mCurrentDir = new File(folderBean.getMdirpath());
                    mImgs = Arrays.asList(
                            mCurrentDir.list(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String filename) {
                                    filename=filename.toLowerCase();
                                    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png"))
                                        return true;
                                    return false;
                                }
                            }));
                    Collections.sort(mImgs, new FileComparator());
                    ImageBeanUtil.setAbsolutePath(mCurrentDir.getAbsolutePath());
                    mAdpater = new CircleRecyclerAdapter(CircleAlbumActivity.this, mImgs, mCurrentDir.getAbsolutePath());
                    mRecyclerView.setAdapter(mAdpater);
                    mAdpater.setOnItemClickListener(new CircleRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onLook(int position) {
                        }

                        @Override
                        public void onCheck(int size) {
                            mCount.setText(size + "张");
                        }

                        @Override
                        public void onUnCheck(int size) {
                            mCount.setText(size + "张");
                        }

                        @Override
                        public void onLongClick(int position) {

                        }

                        @Override
                        public void onImageCount(int count) {
                            if (count > 0) {
                                mConfirm.setBackground(ContextCompat.getDrawable(CircleAlbumActivity.this,R.drawable.btn_radius_orange));
                                mConfirm.setTextColor(Color.parseColor("#ffffff"));
                                mButton.setTextColor(ContextCompat.getColor(CircleAlbumActivity.this,R.color.colorBlue));
                            } else {
                                mConfirm.setBackgroundColor(Color.parseColor("#d1d1d1"));
                                mConfirm.setTextColor(Color.parseColor("#b0b0b0"));
                                mButton.setTextColor(Color.parseColor("#b0b0b0"));
                            }
                        }
                    });
                    mDirName.setText(mCurrentDir.getName());
                    tv_center.setText(mCurrentDir.getName());
                    mDirPopupWindow.dismiss();
                }
            }
        });
    }


    private void dataBindView() {
        if(imgsum<=0){
            Toast.makeText(CircleAlbumActivity.this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 第一次绑定所有的图片进来
         */
        mImgs=alllist;
        /**
         * 对图片进行排序
         */
        Collections.sort(mImgs, new FileComparator());
        mAdpater=new CircleRecyclerAdapter(this,mImgs,"全部图片");
        CircleRecyclerAdapter.setImg_Size(limit);
        mRecyclerView.setAdapter(mAdpater);
        mAdpater.setOnItemClickListener(new CircleRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onLook(int position) {
            }

            @Override
            public void onCheck(int size) {
                mCount.setText(size + "张");
            }

            @Override
            public void onUnCheck(int size) {
                mCount.setText(size + "张");
            }

            @Override
            public void onLongClick(int position) {

            }

            @Override
            public void onImageCount(int count) {
                if(count>0){
                    mConfirm.setBackgroundColor(Color.parseColor("#F49A28"));
                    mConfirm.setTextColor(Color.parseColor("#ffffff"));
                    mButton.setTextColor(Color.parseColor("#F49A28"));
                }else {
                    mConfirm.setBackgroundColor(Color.parseColor("#d1d1d1"));
                    mConfirm.setTextColor(Color.parseColor("#b0b0b0"));
                    mButton.setTextColor(Color.parseColor("#b0b0b0"));
                }
            }
        });
        mDirName.setText("全部图片");
        tv_center.setText("全部图片");
        mCount.setText(ImageBeanUtil.getImgCount() + "张");
    }

    private boolean checkIsHaveSDCard() {
        /**
         * 获得外部存储卡是否可用
         */
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private void init_View() {
        mButton = (TextView) findViewById(R.id.circle_imgload_btn);
        mDirName = (TextView) findViewById(R.id.circle_imgload_name);
        mCount = (TextView) findViewById(R.id.circle_imgload_count);
        mRecyclerView = (RecyclerView) findViewById(R.id.circle_imgload_img);
        mConfirm= (Button) findViewById(R.id.circle_confirm_btn);

        iv_back = (ImageView)findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        tv_center = (TextView)findViewById(R.id.tv_center);

    }

    private void set_Datas() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(CircleAlbumActivity.this, 3));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(CircleAlbumActivity.this));

    }

    private void init_Event() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageBeanUtil.getImgCount()>0){
                    Intent intent=new Intent(CircleAlbumActivity.this,CircleImgDetailActivity.class);
                    intent.putExtra("position",0);
                    intent.putExtra("action","select");
                    startActivity(intent);
                    overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                }
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageBeanUtil.getImgCount()>0){
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                }
            }
        });
        mDirName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDirPopupWindow.setAnimationStyle(R.style.dir_popupwindow_anim);
                mDirPopupWindow.showAsDropDown(mDirName, 0, 0);
                lightOff();
            }
        });
    }

    /**
     * 内容区域变亮
     */
    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * 内容区域变暗
     */
    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_back) {
            finish();
        }
    }


    class FileComparator implements Comparator<String> {
        @Override
        public int compare(String lhs, String rhs) {
            if(new File(lhs).lastModified()<new File(rhs).lastModified()){
                return 1;//最后修改的照片在前
            }else{
                return -1;
            }
        }
    }
}
