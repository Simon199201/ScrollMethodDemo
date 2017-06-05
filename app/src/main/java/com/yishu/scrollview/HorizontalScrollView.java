package com.yishu.scrollview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

/**
 * 横向布局控件
 * 模拟经典滑动冲突
 * 我们此处使用ScrollView来模拟ViewPager,那么必须手动处理滑动冲突，否则内外两层只能有一层滑动，那就是滑动冲突。另外内部左右滑动，外部上下滑动也同样属于该类
 */
public class HorizontalScrollView extends ViewGroup {
    private static final String TAG = "ScrollView";
    //记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    private WindowManager wm;
    //子View的个数
    private int mChildCount;
    private int mScreenWidth;
    //自定义控件横向宽度
    private int mMeasureWidth;
    //滑动加载下一个界面的阈值
    private int mCrital;
    //滑动辅助类
    private Scroller mScroller;
    //当前展示的子View的索引
    private int showViewIndex;

    public HorizontalScrollView(Context context) {
        this(context, null);
    }

    public HorizontalScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        //读取屏幕相关的长宽
        wm = ((Activity) context).getWindowManager();
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mCrital = mScreenWidth / 4;
        mScroller = new Scroller(context);
        showViewIndex = 1;
    }

    /**
     * 重新事件拦截机制
     * 我们分析了view的事件分发，我们知道点击事件的分发顺序是 通过父布局分发，如果父布局没有拦截，即onInterceptTouchEvent返回false，
     * 才会传递给子View。所以我们就可以利用onInterceptTouchEvent()这个方法来进行事件的拦截。来看一下代码
     * 此处使用外部拦截法
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        boolean intercept = false;
        switch (ev.getAction()) {
            //按下事件不要拦截,否则后续事件都会给ViewGroup处理
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercept = true;
                }
                Log.e(TAG, "onInterceptTouchEvent: ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                //如果是横向移动就进行拦截,否则不拦截
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                Log.e(TAG, "onInterceptTouchEvent: ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                Log.e(TAG, "onInterceptTouchEvent: ACTION_UP");
                break;
        }
        mLastX = x;
        mLastY = y;
        return intercept;
    }

    /**
     * 重新计算子View的高度和宽度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth;
        int measureHeight;
        mChildCount = getChildCount();
        //测量子View
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpaceMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpaceMode = MeasureSpec.getMode(heightMeasureSpec);

        //获取横向的padding值
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        final View childView = getChildAt(0);
        /**
         * 如果子View的数量是0,就读取LayoutParams中数据
         * 否则就对子View进行测量
         * 此处主要是针对wrap_content这种模式进行处理，因为默认情况下
         * wrap_content等于match_parent
         */
        if (mChildCount == 0) {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setMeasuredDimension(layoutParams.width, layoutParams.height);
            } else {
                setMeasuredDimension(0, 0);
            }
        } else if (heightSpaceMode == MeasureSpec.AT_MOST && widthSpaceMode == MeasureSpec.AT_MOST) {
            measuredWidth = childView.getMeasuredWidth() * mChildCount;
            measureHeight = getChildMaxHeight();
            //将两侧的padding值加上去
            measuredWidth = paddingLeft + measuredWidth + paddingRight;
            setMeasuredDimension(measuredWidth, measureHeight);
        } else if (heightSpaceMode == MeasureSpec.AT_MOST) {
            measureHeight = getChildMaxHeight();
            setMeasuredDimension(widthSpaceSize, measureHeight);
        } else if (widthSpaceMode == MeasureSpec.AT_MOST) {
            measuredWidth = childView.getMeasuredWidth() * mChildCount;
            measuredWidth = paddingLeft + measuredWidth + paddingRight;
            setMeasuredDimension(measuredWidth, heightSpaceSize);
        }
    }


    /**
     * 获取子View中最大高度
     *
     * @return
     */
    private int getChildMaxHeight() {
        int maxHeight = 0;
        for (int i = 0; i < mChildCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                int height = childView.getMeasuredHeight();
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }
        return maxHeight;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        for (int i = 0; i < mChildCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth, childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                /**
                 * scrollX是指ViewGroup的左侧边框和当前内容左侧边框之间的距离
                 */
                int scrollX = getScrollX();
                if (scrollX - deltaX > 0) {
                    scrollBy(-deltaX, 0);
                    Log.e(TAG, "onTouchEvent: ACTION_MOVE if" + deltaX);
                } else {
                    Log.e(TAG, "onTouchEvent: ACTION_MOVE  else" + deltaX);

                }
                break;
            case MotionEvent.ACTION_UP:
                scrollX = getScrollX();
                int dx;
                //计算滑动的差值,如果超过1/4就滑动到下一页
                int subScrollX = scrollX - ((showViewIndex - 1) * mScreenWidth);
                if (Math.abs(subScrollX) >= mCrital) {
                    boolean next = scrollX > (showViewIndex - 1) * mScreenWidth;
                    if (showViewIndex < 3 && next) {
                        showViewIndex++;
                    } else {
                        showViewIndex--;
                    }
                }
                dx = (showViewIndex - 1) * mScreenWidth - scrollX;
                smoothScrollByDx(dx);
                break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }


    /**
     * 缓慢滚动到指定位置
     *
     * @param dx
     */
    private void smoothScrollByDx(int dx) {
        //在1000毫秒内滑动dx距离，效果就是慢慢滑动
        mScroller.startScroll(getScrollX(), 0, dx, 0, 1000);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}