package com.example.scrollviewnode

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.widget.NestedScrollView
import java.util.*
import kotlin.math.min

/**
 * @author DBoy 2021/2/1
 * <br/>    文件描述 :
 */
class ScrollViewPoint(context: Context, attrs: AttributeSet?) :
    @JvmOverloads NestedScrollView(context, attrs), NestedScrollView.OnScrollChangeListener {

    /**
     * 注册的View点
     */
    private val registerViews = mutableListOf<View>()

    /**
     * 自己的绝对位置坐标
     */
    private var mPos: ViewPos? = null

    /**
     * 滚动偏移量
     */
    private var mScrollOffset = 0

    /**
     * 外部的滚动监听
     */
    private var mUserListener: OnScrollChangeListener? = null

    /**
     * 最大可滚动距离
     */
    private var mMaxScrollY = -1

    /**
     * 当前锚点位置
     */
    private var mViewPoint = -1

    /**
     * 显示定点触发线 画笔
     */
    private val mDebugLinePaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            strokeWidth = 5f
        }
    }

    /**
     * 滚动触发锚点回调
     */
    var onViewPointChangeListener: OnViewPointChangeListener? = null

    /**
     * 自动修复滚动底部
     * 如果true->当滚动到底部后，index坐标修改为最后一个锚点ViewIndex
     */
    var isFixBottom = false


    /**
     * 是否显示debug 定点触发线
     */
    var isShowDebugLine = false


    init {
        super.setOnScrollChangeListener(this)
    }

    override fun setOnScrollChangeListener(userListener: OnScrollChangeListener?) {
        mUserListener = userListener
    }

    /**
     * 获取最大滑动距离
     */
    fun getMaxScrollY(): Int {
        if (mMaxScrollY != -1) {
            return mMaxScrollY
        }
        if (childCount == 0) {
            // Nothing to do.
            return -1
        }
        val child = getChildAt(0)
        val lp = child.layoutParams as LayoutParams
        val childSize = child.height + lp.topMargin + lp.bottomMargin
        val parentSpace = height - paddingTop - paddingBottom
        mMaxScrollY = 0.coerceAtLeast(childSize - parentSpace)
        return mMaxScrollY
    }

    /**
     * 添加滚动监听的锚点View
     * 会对view进行排序，由上到下坐标排序
     */
    fun addScrollView(vararg views: View) {
        views.forEach {
            val view = findViewById<View>(it.id)
            if (view == null) {
                val missingId = rootView.resources.getResourceName(it.id)
                throw NoSuchElementException("没有找到这个ViewId相关的VIew $missingId")
            }
        }
        registerViews.clear()
        registerViews.addAll(views)
    }

    /**
     * 添加滚动监听的锚点ViewId
     * 会对view进行排序，由上到下坐标排序
     */
    fun addScrollView(vararg viewIds: Int) {
        val views = Array(viewIds.size) { index ->
            val view = findViewById<View>(viewIds[index])
            if (view == null) {
                val missingId = rootView.resources.getResourceName(viewIds[index])
                throw NoSuchElementException("没有找到这个ViewId相关的VIew $missingId")
            }
            view
        }
        registerViews.clear()
        registerViews.addAll(views)
    }

    /**
     * 滚动到指定下表的View前提是
     * 使用过[addScrollView]并添加了相同Size的View数量
     */
    @JvmOverloads
    fun scrollToViewIndex(index: Int, offset: Int = 0) {
        val view = registerViews[index]
        scrollToView(view, offset)
    }

    /**
     * 移动到哪个VIew
     * [view] 需要滚动到的View
     * 这个View必须是[ScrollViewPoint]的内部View
     * [offset] 大于0，view与顶部接触前触发
     * [offset] 小于0，view与顶部接触之后触发
     * [offset]会和[mScrollOffset]累加
     */
    @JvmOverloads
    fun scrollToView(view: View?, offset: Int = 0) {
        view ?: return
        scrollToView(view.id, offset)
    }

    /**
     * 移动到哪个VIew
     * [viewId] 需要滚动到的View的Id
     * 这个View必须是[ScrollViewPoint]的内部View
     * [offset] 大于0，view与顶部接触前触发
     * [offset] 小于0，view与顶部接触之后触发
     * [offset]会和[mScrollOffset]累加
     */
    @JvmOverloads
    fun scrollToView(viewId: Int, offset: Int = 0) {
        val moveToView = findViewById<View>(viewId)
        moveToView ?: return
        //获取自己的绝对xy坐标
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        //获取View的绝对坐标
        val viewLocation = IntArray(2)
        moveToView.getLocationOnScreen(viewLocation)
        //坐标相减得到要滚动的距离
        val moveViewY = viewLocation[1] - parentLocation[1]
        //加上偏移坐标量，得到最终要滚动的距离
        val needScrollY = (moveViewY - (offset + mScrollOffset))
        //如果是0，那就没必要滚动了，说明坐标已经重合了
        if (moveViewY == 0) return
        smoothScrollBy(0, needScrollY)
    }


    /**
     * 设置计算滚动偏移量
     * [offset] 当进行滚动计算的时候的偏移量
     * 大于零：view与顶部之间提前触发
     * 小于零：view与顶部重合以后触发
     */
    fun setScrollOffset(offset: Int) {
        mScrollOffset = offset
        mPos = updateViewPos(this)
        mPos!!.Y += mScrollOffset
    }

    override fun onScrollChange(
        v: NestedScrollView?,
        scrollX: Int,
        scrollY: Int,
        oldScrollX: Int,
        oldScrollY: Int
    ) {
        //用户回调
        mUserListener?.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY)
        //计算逻辑
        computeView()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        //最大可滚动距离重置 在第一次获取此数据时重新计算
        mMaxScrollY = -1
        //布局完成之后进行注册view排序根据Y轴坐标排序
        registerViews.sortBy {
            val viewPos = updateViewPos(it)
            viewPos.Y
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //最大可滚动距离重置 在第一次获取此数据时重新计算
        mMaxScrollY = -1
        //大小改变时，更新自己的坐标位置
        mPos = updateViewPos(this)
        mPos!!.Y += mScrollOffset
    }

    /**
     * 更新自己的绝对位置信息
     */
    private fun updateViewPos(view: View): ViewPos {
        //获取自己的绝对xy坐标
        val parentLocation = IntArray(2)
        view.getLocationOnScreen(parentLocation)
        return ViewPos(view, parentLocation[0], parentLocation[1])
    }

    /**
     * 获取根View的绝对坐标位置；
     * 用于计算相对左边偏移比例
     */
    private fun getRootViewPos(): ViewPos? {
        if (childCount == 0) return null
        val rootView = getChildAt(0)
        val parentLocation = IntArray(2)
        rootView.getLocationOnScreen(parentLocation)
        return ViewPos(null, parentLocation[0], parentLocation[1])
    }


    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        //绘制定点触发线
        if (isShowDebugLine) {
            canvas?.drawLine(
                0f,
                mScrollOffset.toFloat() + scrollY,
                width.toFloat(),
                mScrollOffset.toFloat() + scrollY,
                mDebugLinePaint
            )
        }
    }


    /**
     * 计算View的位置
     */
    private fun computeView() {
        mPos ?: return
        if (registerViews.isEmpty()) return
        //判断是否滚动到底部了
        val isScrollBottom = scrollY == getMaxScrollY()
        //检索相邻两个View
        //前一个View缓存
        var previousView = ViewPos(null, 0, Int.MIN_VALUE)
        //下一个View缓存
        var nextView = ViewPos(null, 0, Int.MAX_VALUE)
        //当前滚动的View下标仅添加的View个数
        var scrollIndex = -1
        //通过遍历注册的View，找到当前与定点触发位置相邻的前后两个View和坐标位置
        //[这个查找算法查看 [com.example.scrollviewnode.ExampleUnitTest]
        registerViews.forEachIndexed { index, it ->
            val viewPos = updateViewPos(it)
            if (mPos!!.Y >= viewPos.Y) {
                if (mPos!!.Y.toLong() - viewPos.Y == min(
                        mPos!!.Y.toLong() - viewPos.Y,
                        mPos!!.Y.toLong() - previousView.Y
                    )
                ) {
                    scrollIndex = index
                    previousView = viewPos
                }
            } else {
                if (viewPos.Y - mPos!!.Y.toLong() == min(
                        viewPos.Y - mPos!!.Y.toLong(),
                        nextView.Y - mPos!!.Y.toLong()
                    )
                ) {
                    nextView = viewPos
                }
            }
        }
//=========================前后View滚动差值
        //距离上一个View需要滚动的距离/与上一个View之间的距离
        var previousViewDistance = 0
        //距离下一个View需要滚动的距离/与下一个View之间的距离
        var nextViewDistance = 0

        if (previousView.view != null) {
            previousViewDistance = mPos!!.Y - previousView.Y
        } else {
            //没有前一个View，这就是第一个
            if (scrollIndex == -1) {
                scrollIndex = 0
            }
        }

        if (nextView.view != null) {
            nextViewDistance = nextView.Y - mPos!!.Y
        } else {
            //没有最后一个View，这就是最后一个
            if (scrollIndex == -1) {
                scrollIndex = registerViews.size - 1
            }
        }

        //当滚动到底部的时候 判断修改滚动下标强制为最后一个锚点View
        if (isScrollBottom && isFixBottom) {
            scrollIndex = registerViews.size - 1
        }

//===============前后View逃离进入百分比
        //距离前一个View百分比值
        var previousRatio = 0.0f
        //距离下一个View百分比值
        var nextRatio = 0.0f
        //前后两个View的差值
        var viewDistanceDifference = 0
        //根View的坐标值
        val rootPos = getRootViewPos()
        //计算最相邻两个View的Y坐标差值距离[viewDistanceDifference]
        if (previousView.view != null && nextView.view != null) {
            viewDistanceDifference = nextView.Y - previousView.Y
        } else if (rootPos != null) {
            if (previousView.view == null && nextView.view != null) {
                //没有前一个View
                //那么到达第一个View的 距离 = 下一个View - 跟布局顶部坐标
                viewDistanceDifference = nextView.Y - rootPos.Y
            } else if (nextView.view == null && previousView.view != null) {
                //没有下一个View
                //此时前一个View是最后一个注册的锚点view，
                //距离 = 底部Y坐标 - 前一个ViewY坐标
                val bottomY = rootPos.Y + getMaxScrollY() //最大滚动距离
                viewDistanceDifference = bottomY - previousView.Y
            }
        }

//=====================计算百分比值
        if (nextViewDistance != 0) {
            //下一个View的距离/总距离=前一个view的逃离百分比
            previousRatio = nextViewDistance.toFloat() / viewDistanceDifference
            //反之是下一个View的进入百分比
            nextRatio = 1f - previousRatio
            if (previousViewDistance == 0) {
                //如果还不到第一个锚点View 将不存在第一个View的逃离百分比；
                //此时的previousRatio是顶部坐标的逃离百分比
                previousRatio = 0f
            }
        } else if (previousViewDistance != 0) {
            //同理。前一个View的距离/总距离=下一个View的逃离百分比
            nextRatio = previousViewDistance.toFloat() / viewDistanceDifference
            //反之 是前一个View的进入百分比
            previousRatio = 1f - nextRatio
            if (nextViewDistance == 0) {
                //如果锚点计算已经到达最后一个View 将不存在下一个View的进入百分比
                //此时的nextRatio是底部坐标的进入百分比及到达不可滚动时的百分比
                nextRatio = 0f
            }
        }


//==============数据回调

        //触发锚点变化回调
        if (mViewPoint != scrollIndex) {
            mViewPoint = scrollIndex
            onViewPointChangeListener?.onPointChange(mViewPoint, isScrollBottom)
        }

        //触发滚动距离改变回调
        onViewPointChangeListener?.onScrollPointChange(
            previousViewDistance,
            nextViewDistance,
            scrollIndex
        )

        //触发 逃离进入百分比变化回调
        if (previousRatio in 0f..1f && nextRatio in 0f..1f) {
            //只有两个值在正确的范围之内才能进行处理否则打印异常信息
            onViewPointChangeListener?.onScrollPointChangeRatio(
                previousRatio,
                nextRatio,
                scrollIndex,
                previousViewDistance,
                isScrollBottom
            )
        } else {
            Log.e(
                TAG, "computeView:" +
                        "\n previousRatio = $previousRatio" +
                        "\n nextRatio = $nextRatio"
            )
        }

    }


    /**
     * 保存View的位置信息
     */
    data class ViewPos(val view: View?, var X: Int, var Y: Int)


    interface OnViewPointChangeListener {
        /**
         * [previousDistance]上一个View距离滚动顶部的距离 数值为正
         *
         * [nextDistance] 下一个View距离滚动顶部的距离 数值为正
         *
         * [index] 当前滚动到的view 介于当前View与下一个View之间的坐标，注册的view会进行Y轴坐标排序
         */
        fun onScrollPointChange(previousDistance: Int, nextDistance: Int, index: Int)

        /**
         *通知滚动点的逃离和进入比例
         *
         * [previousFleeRatio]->[1.0-0.0] 上一个锚点View逃离当前定点位置/向上滚动 的百分比占比
         * 当还未到达第一个锚点View时，此Value=0
         *
         * [nextEnterRatio]->[0.0-1.0] 下一个锚点View进入当前定点位置的百分比占比
         * 已经到达最后一个坐锚点View后，此Value=0
         *
         * [index] 当前锚点下下标
         *
         * [scrollPixel] 每次到下一个锚点时所滚动的距离 即距离上一个锚点View的距离
         *
         * [isScrollBottom] 判断是否滚到底部了，有时候最后一个锚点位置不是滚动布局的底部
         * 可能永远不会滚动到这个位置。所单独去判断吧
         */
        fun onScrollPointChangeRatio(
            previousFleeRatio: Float,
            nextEnterRatio: Float,
            index: Int,
            scrollPixel: Int,
            isScrollBottom: Boolean
        )

        /**
         * 点位置改变，只改变时触发，不会一直触发
         *
         *  [index] 当前锚点下下标
         */
        fun onPointChange(index: Int, isScrollBottom: Boolean)
    }

    companion object {
        const val TAG = "ScrollViewPoint"
    }

}