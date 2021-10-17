package com.example.class0104ui.scroll;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

public class OverScrollerEdgeEffectView extends ViewGroup {

    public static final String TAG = OverScrollerEdgeEffectView.class.getSimpleName();
    private final OverScroller mScroller;
    private final float mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private MotionEvent preMotion;
    private EdgeEffect edgeEffectTop, edgeEffectBottom;
    private int containHeight = 0;//实际高度
    private VelocityTracker velocityTracker;

    public OverScrollerEdgeEffectView(Context context) {
        this(context, null);
    }

    public OverScrollerEdgeEffectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverScrollerEdgeEffectView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OverScrollerEdgeEffectView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mScroller = new OverScroller(context);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop() * 0.8f;
        mMinimumVelocity = vc.getScaledMinimumFlingVelocity();
        mMaximumVelocity = vc.getScaledMaximumFlingVelocity();

        edgeEffectTop = new EdgeEffect(context);
        edgeEffectTop.setColor(Color.DKGRAY);

        edgeEffectBottom = new EdgeEffect(context);
        edgeEffectBottom.setColor(Color.GREEN);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        containHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            containHeight += child.getMeasuredHeight();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0, i * child.getMeasuredHeight(), child.getMeasuredWidth(), (i + 1) * child.getMeasuredHeight());
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        edgeEffectTop.setSize(w, h);
        edgeEffectBottom.setSize(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        if (!edgeEffectTop.isFinished()) {
            canvas.save();
            edgeEffectTop.draw(canvas);
            canvas.restore();
            invalidate();
        }

        if (!edgeEffectBottom.isFinished()) {
            canvas.save();
            canvas.translate(-getWidth(), 0);
            canvas.rotate(180, getWidth(), 0);
            canvas.translate(0, -containHeight);
            edgeEffectBottom.draw(canvas);
            canvas.restore();
            invalidate();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        onTouchEvent(event);
        if (action == MotionEvent.ACTION_DOWN) {
            preMotion = MotionEvent.obtain(event);
        }
        if (action == MotionEvent.ACTION_MOVE && mTouchSlop < calculateMoveDistance(event, preMotion)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);

                mScroller.forceFinished(true);
                preMotion = MotionEvent.obtain(event);
                edgeEffectTop.finish();
                edgeEffectBottom.finish();
                invalidate();//postInvalidateOnAnimation(this);
                break;

            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float v = event.getY() - preMotion.getY();
                Log.e(TAG, "onTouchEvent: sy[" + getScrollY() + "]h[" + getHeight() + "]ch[" + containHeight + "]");

                Log.e("abc0", "event.getY=" + (int) event.getY()
                        + " preMotion.getY=" + (int) preMotion.getY()
                        + " v=" + (int) v
                        + " getScrollY()=" + getScrollY()
                        + " getScrollY() + v =" + (getScrollY() + (int) v));

                if (getScrollY() + v < 0) {
                    Log.e("abc1", "event.getY(): [" + event.getY() + "] preMotion.getY[" + preMotion.getY() + "]");
                    edgeEffectTop.onPull(Math.abs(v / getHeight()), event.getX() / getWidth());
                }
                if (getScrollY() + getHeight() + v > containHeight) {
                    Log.e("abc2", "event.getY(): [" + event.getY() + "] preMotion.getY[" + preMotion.getY() + "]");
                    edgeEffectBottom.onPull(Math.abs(v / getHeight()), 1f - event.getX() / getWidth());
                }

                Log.e("abc3", " v=" + (int) v + " containHeight=" + containHeight + " getHeight()=" + getHeight() + " getScrollY()=" + getScrollY());
                if ((v > 0 && getScrollY() + v > 0)//向下

                        || (v < 0 && containHeight - getHeight() - v > getScrollY())//向上
                ) {

                    scrollBy(0, -(int) v);
                }

                invalidate();//postInvalidateOnAnimation();
                preMotion = MotionEvent.obtain(event);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                float vu = event.getY() - preMotion.getY();
                float velocityY = velocityTracker.getYVelocity();
                //TODO fling or springBack
                if (Math.abs(velocityY) > mMinimumVelocity) {
                    Log.e("ACTION_UP0", "Math.abs(velocityY) > mMinimumVelocity");
                    //fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY)
                    mScroller.fling(0, getScrollY() - (int) vu, 0, -(int) velocityY, 0, 0, 0, containHeight - getHeight(), 0, 400);
                }

                if (getScrollY() < 0 || getScrollY() + getHeight() > containHeight) {
                    Log.e("ACTION_UP1", getScrollY() < 0 ? "getScrollY() < 0" : "getScrollY() + getHeight() > containHeight");
                    mScroller.springBack(0, getScrollY(), 0, getWidth(), 0, containHeight - getHeight());
                }

                //TODO release
                velocityTracker.recycle();
                velocityTracker = null;
                preMotion.recycle();
                edgeEffectTop.onRelease();
                edgeEffectBottom.onRelease();
                invalidate();//postInvalidateOnAnimation();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        //动画执行完返回false
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            scrollTo(0, y);
            //TODO Absorb Velocity
//            if(edgeEffectTop.isFinished() && getScrollY()< 0 ){
//                edgeEffectTop.onAbsorb((int) mScroller.getCurrVelocity());
//            }
//
//            if(edgeEffectBottom.isFinished() && getScrollY()+getHeight()> containHeight){
//                edgeEffectBottom.onAbsorb((int) mScroller.getCurrVelocity());
//            }
            invalidate();//postInvalidateOnAnimation();
        }
    }


    /**
     * 计算两点
     */
    private float calculateMoveDistance(MotionEvent event1, MotionEvent event2) {
        if (event1 == null || event2 == null) {
            return 0f;
        }
        float disX = Math.abs(event1.getRawX() - event2.getRawX());
        float disY = Math.abs(event1.getRawX() - event2.getRawX());
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

}
