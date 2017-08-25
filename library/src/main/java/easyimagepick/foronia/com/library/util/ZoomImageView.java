package easyimagepick.foronia.com.library.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by Foronia on 2016/7/25.
 */
public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener,View.OnTouchListener {

    private boolean mOnce=false;
    /**
     * 初始化时缩放的值
     */
    private float mInitScale;
    /**
     * 双击放大到达的值
     */
    private float mMidScale;
    /**
     * 放大的最大值
     */
    private float mMaxScale;

    /**
     * 缩放手势处理类
     * 捕获用户多点触控时的比例
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private Matrix mScaleMatrix;

    //自由移动

    /**
     * 记录上一次多点触控的数量
     * @param context
     */
    private int mLastPointerCount;
    //上一次多点触控的中心位置
    private float mlastX;
    private float mlastY;
    //滑动的最小距离
    private int mTouchSlop;

    private boolean isCanDrag;

    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    //双击放大和缩小
    private GestureDetector mGestureDetector;

    public  OnImageLongClickListener mListener;

    private boolean isAutoScale;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init
        mScaleMatrix=new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector=new ScaleGestureDetector(context,this);
        //捕获触摸监听
        setOnTouchListener(this);
        mTouchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(isAutoScale)
                    return true;
                float x=e.getX();
                float y=e.getY();
                if(getScale()<mMidScale){
                    /*mScaleMatrix.postScale(mMidScale/getScale(),mMidScale/getScale(),x,y);
                    setImageMatrix(mScaleMatrix);*/
                    postDelayed(new AutoScaleRunnable(mMidScale,x,y),16);
                    isAutoScale=true;
                }else {
                    /*mScaleMatrix.postScale(mInitScale/getScale(),mInitScale/getScale(),x,y);
                    setImageMatrix(mScaleMatrix);*/
                    postDelayed(new AutoScaleRunnable(mInitScale,x,y),16);
                    isAutoScale=true;
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
               // Toast.makeText(context, "长按保存", Toast.LENGTH_SHORT).show();
                mListener.OnImageLongClick();
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return mListener.OnSingleClick();
            }
        });
    }

    private class  AutoScaleRunnable implements Runnable {
        /**
         * 缩放的目标值
         */
        private  float mTargetScale;
        //缩放的中心点
        private  float x;
        private  float y;

        private final  float BIGGER=1.07f;
        private final  float SMALL=0.93f;

        private  float tmpScale;

        public AutoScaleRunnable(float mTargetScale, float x, float y) {
            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;
            if(getScale()<mTargetScale){

                tmpScale=BIGGER;
            }

            if(getScale()>mTargetScale){
                tmpScale=SMALL;
            }
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenter();
            setImageMatrix(mScaleMatrix);
            float currentScale=getScale();
            if((tmpScale>1.0f&& currentScale<mTargetScale) || (tmpScale<1.0f && currentScale>mTargetScale)){
                postDelayed(this,16);
            }else {
                float scale=mTargetScale/currentScale;
                mScaleMatrix.postScale(scale,scale,x,y);
                checkBorderAndCenter();
                setImageMatrix(mScaleMatrix);
                isAutoScale=false;
            }
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 全局的布局完成调用的方法
     * 获取ImageView加载完成的图片
     */
    @Override
    public void onGlobalLayout() {
        if(!mOnce){
            //得到控件的宽和高
            int width=getWidth();
            int height=getHeight();
            //判断是否有图片
            Drawable d=getDrawable();
            if(d==null){
                return;
            }
            //得到图片的宽和高
            int dw=d.getIntrinsicWidth();
            int dh=d.getIntrinsicHeight();
            float scale=1.0f;
            /**
             *图片的宽度大于控件的宽度 图片的高度小于控件的高度
             */
            if(dw>width && dh<height){
             scale=width*1.0f/dw;
            }
            /**
             *图片的宽度小于控件的宽度 图片的高度大于控件的高度
             */
            if(dh>height && dw<width){
                scale=height*1.0f/dh;
            }
            /**
             *图片的宽度大于控件的宽度 图片的高度大于控件的高度
             */
            if(dh>height && dw>width){
                scale=Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }
            /**
             * 图片的宽度小于控件的宽度 图片的高度小于控件的高度
             */
            if(dw<width && dh<height){
                scale=Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }
            /**
             * 得到了初始化时缩放的比例
             */
            mInitScale=scale;
            mMaxScale=mInitScale*4;
            mMidScale=mInitScale*2;

            //将图片移动至控件的中心
            int dx=getWidth()/2-dw/2;
            int dy=getHeight()/2-dh/2;
            //平移
            mScaleMatrix.postTranslate(dx,dy);
            //缩放中心点为控件中心位置
            mScaleMatrix.postScale(mInitScale,mInitScale,width/2,height/2);
            setImageMatrix(mScaleMatrix);
            mOnce=true;

        }
    }

    /**
     * 获取当期图片的缩放值
     * @return
     */
    public float getScale(){
        float[] values=new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    //缩放的区间 initScale mMaxScale
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale=getScale();
        float scaleFactor=detector.getScaleFactor();
        if(getDrawable()==null){
            return true;
        }
        //缩放范围的控制
        if((scale<mMaxScale && scaleFactor>1.0f) ||(scale>mInitScale && scaleFactor<1.0f)){
           if(scaleFactor*scale<mInitScale) {
               scaleFactor=mInitScale/scale;
           }
            if(scale*scaleFactor>mMaxScale){
                scaleFactor=mMaxScale/scale;
            }
            //设置缩放中心为多点触控的中心位置
            mScaleMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusY());
            checkBorderAndCenter();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    /**
     * 获取图片放大缩小以后的宽和高，以及l,r,t,b
     * @return
     */
    private RectF getMatrixRectF(){
        Matrix matrix=mScaleMatrix;
        RectF rectF=new RectF();
        Drawable d=getDrawable();
        if(d!=null){
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 在缩放的时候进行边界控制以及我们的位置的控制
     */
    private void checkBorderAndCenter() {
        //获取缩放后图片的矩阵
        RectF rect=getMatrixRectF();
        float deltaX=0;
        float deltaY=0;
        int width=getWidth();
        int height=getHeight();
        if(rect.width() >=width){
            if(rect.left>0){
                deltaX=-rect.left;
            }

            if(rect.right<width){
                deltaX=width-rect.right;
            }
        }
        if(rect.height() >=height){
            if(rect.top >0){
                deltaY=-rect.top;
            }

            if(rect.bottom <height){
                deltaY=height-rect.bottom;
            }
        }
        //如果宽度或者高度小于控件的宽或者高 让其居中
        if(rect.width()<width){
            deltaX=width/2f-rect.right+rect.width()/2f;
        }
        if(rect.height()<height){
            deltaY=height/2f-rect.bottom+rect.height()/2f;
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * onScaleBegin 缩放的时候捕获事件
     * @param detector
     * @return
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //如果双击的时候捕获事件
        if(mGestureDetector.onTouchEvent(event))
            return true;

        //把触摸事件传给mScaleGestureDetector处理
        mScaleGestureDetector.onTouchEvent(event);

        float x=0;
        float y=0;
        //多点触控手指的数量
        int pointerCount = event.getPointerCount();
        for(int i=0;i<pointerCount;i++){
            x+=event.getX();
            y+=event.getY();
        }
        //计算多点触控中心点
        x/=pointerCount;
        y/=pointerCount;

        if(mLastPointerCount !=pointerCount){
            //当手指发生变化时应该进行重新判断
            isCanDrag=false;
            mlastX=x;
            mlastY=y;
        }
        mLastPointerCount=pointerCount;
        RectF rectF=getMatrixRectF();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //if(rectF.width()>getWidth()+0.01 || rectF.height()>getHeight()+0.01){
                if(rectF.width()>getWidth()+0.01){
                    if(getParent() instanceof ViewPager){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                }
                break;
            case MotionEvent.ACTION_MOVE:

               // if(rectF.width()>getWidth()+0.01 || rectF.height()>getHeight()+0.01){
              if(rectF.width()>getWidth()+0.01 ){
                    if(getParent() instanceof ViewPager){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                float dx=x-mlastX;
                float dy=y-mlastY;

                if(!isCanDrag){
                    isCanDrag=isMoveAction(dx,dy);
                }

                if(isCanDrag){
                    if(getDrawable()!=null){
                        isCheckLeftAndRight=isCheckTopAndBottom=true;
                        //如果宽度小于控件宽度不允许横向移动
                        if(rectF.width()<getWidth()){
                            isCheckLeftAndRight=false;
                            dx=0;
                        }
                        //如果高度小于控件高度 不允许纵向移动
                        if(rectF.height()<getHeight()){
                            isCheckTopAndBottom=false;
                            dy=0;
                        }

                        mScaleMatrix.postTranslate(dx,dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mlastX=x;
                mlastY=y;
                break;
            case MotionEvent.ACTION_UP:
                mLastPointerCount=0;
                break;
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount=0;
                break;
        }
        return true;
    }

    /**
     * 当移动时，进行边界检测
     */
    private void checkBorderWhenTranslate() {
        RectF rectF=getMatrixRectF();
        float deltaX=0;
        float deltaY=0;
        int width=getWidth();
        int height=getHeight();
        if(rectF.top>0 && isCheckTopAndBottom){
            deltaY=-rectF.top;
        }

        if(rectF.bottom<height && isCheckTopAndBottom){
            deltaY=height-rectF.bottom;
        }

        if(rectF.left>0 && isCheckLeftAndRight){
            deltaX=-rectF.left;
        }

        if(rectF.right<width && isCheckLeftAndRight){
            deltaX=width-rectF.right;
        }

        mScaleMatrix.postTranslate(deltaX,deltaY);
    }

    /**
     * 判断是否为Move
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx*dx+dy*dy)>mTouchSlop;
    }

    public void setOnLongClicklistenr(OnImageLongClickListener listener){
        if(mListener==null){
           mListener=listener;
        }
    }

    public interface OnImageLongClickListener{
        void OnImageLongClick();
        boolean OnSingleClick();
    }
}
