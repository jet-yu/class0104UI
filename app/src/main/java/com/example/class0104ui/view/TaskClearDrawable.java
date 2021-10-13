package com.example.class0104ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.class0104ui.R;

import java.sql.Struct;

/**
 * //Math.cos(double angle) angle的值以弧度为单位
 */
public class TaskClearDrawable extends Drawable {

    Context mContext;

    //anmator state
    private final int STATE_ORIGIN = 0;//初始状态
    private final int STATE_ROTATE = STATE_ORIGIN + 1;//外圈旋转
    private final int STATE_UP = STATE_ROTATE + 1;//上移
    private final int STATE_DOWN = STATE_UP + 1;//下移
    private final int STATE_FINISH = STATE_DOWN + 1;//结束

    private int mAnimState = STATE_ORIGIN;//状态

    String getState(final int state) {
        String result = "STATE_ORIGIN";
        switch (state) {
            case STATE_ORIGIN:
                result = "STATE_ORIGIN";
                break;
            case STATE_ROTATE:
                result = "STATE_ROTATE";
                break;
            case STATE_UP:
                result = "STATE_UP";
                break;
            case STATE_DOWN:
                result = "STATE_DOWN";
                break;
            case STATE_FINISH:
                result = "STATE_FINISH";
                break;
            default:
                break;
        }
        return result;
    }

    private final float PI_DEGREE = (float) (180.0f / Math.PI);//180度/π是1弧度对应多少度,这里表示一弧度的大小(57.30)


    //动画时长

    private final long DURATION_ROTATE = 1250;//外圈旋转时长
    private final long DURATION_CLEANNING = 250;//× 缩小至 0的时长

    private final long DURATION_POINT_UP = 250;// 点 往上移动的时长
    private final long DURATION_POINT_DOWN = 350;// 点 往下移动的时长

    private final long DURATION_FINISH = 200;// 短边缩放的时长
    private final long DURATION_CLEANNING_DELAY = 1000;// cleanning 时长

    private final long DURATION_ORIGIN_DELAY = 3000;// 返回初始状态的时长


    private final float DRAWABLE_WIDTH = 180.0f;//drawable_width 宽度
    private final float ROTATE_DEGREE_TOTAL = -1080.0f;//总共旋转的角度 即旋转3圈 6π

    private final float PAINT_WIDTH = 4.0f;//画×的笔的宽度
    private final float PAINT_WIDTH_OTHER = 1.0f;//画其他的笔的宽度

    private final float CROSS_LENGTH = 62.0f;//×的长度
    private final float CORSS_DEGREE = 45.0f / PI_DEGREE;//π/4 三角函数计算用 sin(π/4) = cos(π/4) = 0.707105

    private final float UP_DISTANCE = 24.0f;//往上移动的距离
    private final float DOWN_DISTANCE = 20.0f;//往下移动的距离


    private final float FORK_LEFT_LEN = 33.0f;//左短边长度
    private final float FORK_LEFT_DEGREE = 40.0f / PI_DEGREE;//左短边弧度
    private final float FORK_RIGHT_LEN = 50.0f;//右长边长度
    private final float FORK_RIGHT_DEGREE = 50.0f / PI_DEGREE;//右长边弧度

    private final float CIRCLE_RADIUS = 3.0f;//圆点半径

    private int mWidth, mHeight;

    private float mCleanningScale, mRotateDegreeScale;    //cleanning 缩放，旋转缩放
    private float mScale = 0.0f;
    private float mPaintWidth;//画笔宽度
    private float mPaintWidthOther;
    private float mViewScale;
    private float mCenterX, mCenterY;
    private float mCrossLen, oldCrossLen;
    private float mPointRadius;
    private float mForkLeftLen, mForkRightLen;
    private float mPointUpLen, mPointDownLen;

    private Paint mPaint;
    private Paint mLinePaint;
    private Bitmap mBgBitmap;
    private Bitmap mCircleBitmap;
    private TimeInterpolator fast_out_slow_in;
    private TimeInterpolator fast_out_linear_in;
    private AnimatorSet mAnimatorSet;

    public TaskClearDrawable(Context context, int width, int height) {
        this.mContext = context;
        this.mWidth = width;
        this.mHeight = height;
        this.mViewScale = width / DRAWABLE_WIDTH;


        Bitmap bitmapBG = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        mBgBitmap = Bitmap.createScaledBitmap(bitmapBG, mWidth, mHeight, true);
        if (bitmapBG != mBgBitmap) {
            bitmapBG.recycle();
        }

        Bitmap bitmapCircle = BitmapFactory.decodeResource(context.getResources(), R.drawable.circle);
        mCircleBitmap = Bitmap.createScaledBitmap(bitmapCircle, mWidth, mHeight, true);
        if (bitmapCircle != mCircleBitmap) {
            bitmapCircle.recycle();
        }

        mPaint = new Paint();
        mLinePaint = new Paint();

        mPaintWidth = PAINT_WIDTH * mViewScale;

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

//        x lenght
        mCrossLen = CROSS_LENGTH * mViewScale;//叉叉的长度
        mPointRadius = CIRCLE_RADIUS * mViewScale;
        mForkLeftLen = FORK_LEFT_LEN * mViewScale;
        mForkRightLen = FORK_RIGHT_LEN * mViewScale;
        mPointUpLen = UP_DISTANCE * mViewScale;
        mPointDownLen = DOWN_DISTANCE * mViewScale;

        mCleanningScale = 1.0f;
        mRotateDegreeScale = 0.0f;

        mPaintWidthOther = PAINT_WIDTH_OTHER * mViewScale;

        fast_out_slow_in = AnimationUtils.loadInterpolator(mContext, android.R.interpolator.fast_out_slow_in);
        fast_out_linear_in = AnimationUtils.loadInterpolator(mContext, android.R.interpolator.fast_out_linear_in);


    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xffffffff);
        mPaint.setStrokeWidth(mPaintWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void initEffectPaint() {
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(4);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        float x1, y1;
        float x2, y2;
        float x3, y3;
        float x4, y4;
        float length;
        float sin_45 = (float) Math.sin(CORSS_DEGREE);//45°的值

        initPaint();
        initEffectPaint();

        canvas.drawBitmap(mBgBitmap, 0, 0, mPaint);


        Log.i("draw", "draw: mAnimState= " + getState(mAnimState));
        switch (mAnimState) {
            case STATE_ORIGIN:
                length = mCrossLen / 2.0f * sin_45;
                x1 = mCenterX - length;
                y1 = mCenterY - length;
                x2 = mCenterX + length;
                y2 = mCenterY + length;
                x3 = mCenterX + length;
                y3 = mCenterY - length;
                x4 = mCenterX - length;
                y4 = mCenterY + length;
                drawPath(canvas, mPaint, x1, y1, x2, y2, x3, y3, x4, y4);
                canvas.drawBitmap(mCircleBitmap, 0, 0, null);
                break;
            case STATE_ROTATE:
                float degree = ROTATE_DEGREE_TOTAL * mRotateDegreeScale;
                Matrix matrix = new Matrix();
                matrix.setRotate(degree, mCenterX, mCenterY);
                canvas.drawBitmap(mCircleBitmap, matrix, null);

                length = mCrossLen / 2.0f * mCleanningScale * sin_45;
                x1 = mCenterX - length;
                y1 = mCenterY - length;
                x2 = mCenterX + length;
                y2 = mCenterY + length;
                x3 = mCenterX + length;
                y3 = mCenterY - length;
                x4 = mCenterX - length;
                y4 = mCenterY + length;
                drawPath(canvas, mPaint, x1, y1, x2, y2, x3, y3, x4, y4);
                break;
            case STATE_UP:
                mPaint.setStrokeWidth(mPaintWidthOther);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY - mPointUpLen * mScale, mPointRadius, mPaint);
                canvas.drawBitmap(mCircleBitmap, 0, 0, null);
                break;
            case STATE_DOWN:
                mPaint.setStrokeWidth(mPaintWidthOther);
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY - mPointUpLen + (mPointDownLen + mPointUpLen) * mScale, mPointRadius, mPaint);
                canvas.drawBitmap(mCircleBitmap, 0, 0, null);

                break;
            case STATE_FINISH:
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mPaintWidth);
                canvas.drawBitmap(mCircleBitmap, 0, 0, null);

                x1 = (float) (mCenterX - Math.abs(mScale * mForkLeftLen * Math.cos(FORK_LEFT_DEGREE)));
                y1 = (float) (mCenterY + mPointDownLen - mScale * mForkLeftLen * Math.sin(FORK_LEFT_DEGREE));
                x2 = mCenterX;
                y2 = mCenterY + mPointDownLen;
                x3 = mCenterX;
                y3 = mCenterY + mPointDownLen;
                x4 = (float) (mCenterX + mScale * mForkRightLen * Math.cos((FORK_RIGHT_DEGREE)));
                y4 = (float) (mCenterY + mPointDownLen - (mScale * mForkRightLen * Math.sin(FORK_RIGHT_DEGREE)));

                drawPath(canvas, mPaint, x1, y1, x2, y2, x3, y3, x4, y4);

                break;
        }
    }


    private void drawPath(Canvas canvas, Paint paint,
                          float x1, float y1, float x2, float y2,
                          float x3, float y3, float x4, float y4) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.moveTo(x3, y3);
        path.lineTo(x4, y4);

        canvas.drawPath(path, paint);
    }


    private ValueAnimator createAnimator(final int drawType,
                                         long duration, TimeInterpolator interpo) {
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                mAnimState = drawType;
                mScale = value;
                invalidateSelf();
            }
        });

        anim.setDuration(duration);
        anim.setInterpolator(interpo);
        return anim;
    }

    public void start() {
        if (mAnimatorSet != null) {
            stop();
        }

        ValueAnimator circleAnimator;
        ValueAnimator cleaningAnimator;

        circleAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        circleAnimator.setDuration(DURATION_ROTATE);
        circleAnimator.setInterpolator(fast_out_slow_in);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mAnimState = STATE_ROTATE;
                mRotateDegreeScale = value;
                mCleanningScale = 1.0f;
                invalidateSelf();
            }
        });

        cleaningAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        cleaningAnimator.setDuration(DURATION_CLEANNING);
        cleaningAnimator.setStartDelay(DURATION_ROTATE);
        cleaningAnimator.setInterpolator(fast_out_linear_in);
        cleaningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCleanningScale = (float) animation.getAnimatedValue();
                mAnimState = STATE_ROTATE;
                invalidateSelf();
            }
        });

        AnimatorSet beginAnimSet = new AnimatorSet();
        beginAnimSet.playTogether(circleAnimator, cleaningAnimator);

        //up animator
        ValueAnimator poiontUpAnim = createAnimator(
                STATE_UP, DURATION_POINT_UP, fast_out_slow_in);

        //down animator
        ValueAnimator pointDownAnim = createAnimator(
                STATE_DOWN, DURATION_POINT_DOWN, fast_out_slow_in);

        //right animator
        ValueAnimator finishAnim = createAnimator(
                STATE_FINISH, DURATION_FINISH, fast_out_slow_in);

        ValueAnimator delayAnim = ValueAnimator.ofFloat(1.0f, 0.0f);
        delayAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimState = STATE_ORIGIN;
                invalidateSelf();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        delayAnim.setDuration(DURATION_ORIGIN_DELAY);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(
                beginAnimSet, poiontUpAnim, pointDownAnim, finishAnim, delayAnim);
        mAnimatorSet.start();

    }

    private ValueAnimator createValueAnimator(final int drawType,
                                              long duration, TimeInterpolator interpo) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setInterpolator(interpo);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScale = (float) animation.getAnimatedValue();
                mAnimState = drawType;
                invalidateSelf();
            }
        });

        return valueAnimator;
    }

    public void stop() {
        if (mAnimatorSet != null) {
            if (isRunning()) {
                mAnimatorSet.cancel();
                mAnimatorSet = null;
            }
        }
        mAnimState = STATE_ORIGIN;
        invalidateSelf();
    }

    public boolean isRunning() {
        if (mAnimatorSet != null) {
            return mAnimatorSet.isRunning();
        }
        return false;
    }


    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {

        return PixelFormat.OPAQUE;
    }
}
