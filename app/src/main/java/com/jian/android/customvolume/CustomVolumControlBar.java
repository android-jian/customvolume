package com.jian.android.customvolume;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dell on 2017/6/3.
 */

public class CustomVolumControlBar extends View {

    /**
     * 第一圈的颜色
     */
    private int mFirstColor;

    /**
     * 第二圈的颜色
     */
    private int mSecondColor;

    /**
     * 圈的宽度
     */
    private int mCircleWidth;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 当前进度 默认为3
     */
    private int mCurrentCount=3;

    /**
     * 中间的图片
     */
    private Bitmap mCenterIcon;

    /**
     * 每个块块间的间隙
     */
    private int mSplitSize;

    /**
     * 块块的个数
     */
    private int mCount;

    private Rect mRect;

    public CustomVolumControlBar(Context context) {
        this(context,null);
    }

    public CustomVolumControlBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    /**
     * 必要的初始化，获得一些自定义的值
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CustomVolumControlBar(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        TypedArray a=context.getTheme().obtainStyledAttributes(attrs,R.styleable.CustomVolumControlBar, defStyleAttr, 0);
        int n=a.getIndexCount();

        for (int i=0;i<n;i++){

            int attr=a.getIndex(i);
            switch (attr){

                case R.styleable.CustomVolumControlBar_firstColor:
                    mFirstColor=a.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.CustomVolumControlBar_secondColor:
                    mSecondColor=a.getColor(attr,Color.CYAN);
                    break;
                case R.styleable.CustomVolumControlBar_centerIcon:
                    mCenterIcon= BitmapFactory.decodeResource(getResources(),a.getResourceId(attr,0));
                    break;
                case R.styleable.CustomVolumControlBar_circleWidth:
                    mCircleWidth=a.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_PX,20,getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomVolumControlBar_dotCount:
                    mCount=a.getInt(attr,20);
                    break;
                case R.styleable.CustomVolumControlBar_splitSize:
                    mSplitSize=a.getInt(attr,20);
                    break;
            }
        }
        a.recycle();
        mPaint=new Paint();
        mRect=new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setAntiAlias(true);     //消除锯齿
        mPaint.setStrokeWidth(mCircleWidth);    //设置圆环的宽度
        mPaint.setStrokeCap(Paint.Cap.ROUND);   //设置线段断点形状为圆头
        mPaint.setStyle(Paint.Style.STROKE);    //设置空心
        int centre=getWidth()/2;         //获取圆心的x坐标
        int radius=centre-mCircleWidth/2;   //半径

        /**
         * 画块块
         */
        drawOval(canvas,centre,radius);

        int realRadius=radius-mCircleWidth/2;         //获得内圆的半径

        /**
         * 内切正方形的距离顶部 = mCircleWidth + relRadius - √2 / 2
         */
        mRect.left = (int) (realRadius - Math.sqrt(2) * 1.0f / 2 * realRadius) + mCircleWidth;

        /**
         * 内切正方形的距离左边 = mCircleWidth + relRadius - √2 / 2
         */
        mRect.top = (int) (realRadius - Math.sqrt(2) * 1.0f / 2 * realRadius) + mCircleWidth;
        mRect.bottom = (int) (mRect.left + Math.sqrt(2) * realRadius);
        mRect.right = (int) (mRect.left + Math.sqrt(2) * realRadius);

        /**
         * 如果图片比较小，那么根据图片的尺寸放置到正中心
         */
        if (mCenterIcon.getWidth()<Math.sqrt(2)*realRadius){
            mRect.left = (int) (mRect.left + Math.sqrt(2) * realRadius * 1.0f / 2 - mCenterIcon.getWidth() * 1.0f / 2);
            mRect.top = (int) (mRect.top + Math.sqrt(2) * realRadius * 1.0f / 2 - mCenterIcon.getHeight() * 1.0f / 2);
            mRect.right = (int) (mRect.left + mCenterIcon.getWidth());
            mRect.bottom = (int) (mRect.top + mCenterIcon.getHeight());
        }

        //绘图
        canvas.drawBitmap(mCenterIcon,null,mRect,mPaint);
    }

    /**
     * 根据参数画出每个小块
     * @param canvas
     * @param centre
     * @param redius
     */
    private void drawOval(Canvas canvas,int centre,int redius){

        /**
         * 根据需要画的块块个数以及间隙计算出每个块块所占的比例*360
         */
        float itemSize=(270*1.0f-mCount*mSplitSize)/mCount;

        /**
         * 用于定义圆弧的形状和大小的界线
         */
        RectF oval=new RectF(centre-redius,centre-redius,centre+redius,centre+redius);

        mPaint.setColor(mFirstColor);
        for (int i=0;i<mCount;i++){
            canvas.drawArc(oval,i*(itemSize+mSplitSize)+140,itemSize,false,mPaint);     //根据进度画圆弧
        }

        mPaint.setColor(mSecondColor);
        for (int i=0;i<mCurrentCount;i++){
            canvas.drawArc(oval,i*(itemSize+mSplitSize)+140,itemSize,false,mPaint);     //根据进度画圆弧
        }

    }

    /**
     * 当前数量+1
     */
    public void volumUp(){

        if (mCurrentCount<mCount){
            mCurrentCount++;
            postInvalidate();
        }
    }

    /**
     * 当前数量-1
     */
    public void volumDown(){

        if (mCurrentCount>0){
            mCurrentCount--;
            postInvalidate();
        }
    }

    private int xDown,xUp;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                xDown= (int) event.getY();
                break;

            case MotionEvent.ACTION_UP:
                xUp= (int) event.getY();
                if (xUp>xDown){
                    volumDown();
                }else {
                    volumUp();
                }
                break;

            default:
                break;
        }
        return true;
    }
}
