package com.example.scrollviewnode

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.scrollviewnode.databinding.ActivityMainBinding
import com.example.scrollviewnode.databinding.ViewIndicatorTitleLayoutBinding
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView

class MainActivity : AppCompatActivity(), ScrollViewPoint.OnViewPointChangeListener {

    val viewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initIndicator()
        initScrollPoint()
    }

    private fun initIndicator() {
        var titles = mutableListOf<String>(
            "简介",
            "使用说明",
            "评论消息",
            "其他推荐"
        )
        var commonNavigator = CommonNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int = titles.size

            override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                var titleView = CommonPagerTitleView(context)
                var titleItem =
                    ViewIndicatorTitleLayoutBinding.inflate(LayoutInflater.from(context))
                titleItem.titleView.text = titles[index]
                titleView.setOnClickListener {
                    viewBinding.pointScrollView.scrollToViewIndex(index)
                }
                titleView.onPagerTitleChangeListener =
                    object : CommonPagerTitleView.OnPagerTitleChangeListener {
                        override fun onSelected(index: Int, totalCount: Int) {
                            titleItem.titleView.setTextColor(Color.BLACK)
                        }

                        override fun onDeselected(index: Int, totalCount: Int) {
                            titleItem.titleView.setTextColor(Color.GRAY)
                        }

                        override fun onLeave(
                            index: Int,
                            totalCount: Int,
                            leavePercent: Float,
                            leftToRight: Boolean
                        ) {
                        }

                        override fun onEnter(
                            index: Int,
                            totalCount: Int,
                            enterPercent: Float,
                            leftToRight: Boolean
                        ) {
                        }

                    }
                titleView.addView(titleItem.root)
                return titleView;
            }

            override fun getIndicator(context: Context?): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.setColors(Color.RED)
                indicator.lineHeight = SizeUtils.dp2px(3f).toFloat()
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineWidth = SizeUtils.dp2px(26f).toFloat()
                indicator.roundRadius = SizeUtils.dp2px(1f).toFloat()
                return indicator
            }

        }
        viewBinding.indicator.navigator = commonNavigator
    }

    private fun initScrollPoint() {
        viewBinding.pointScrollView.addScrollView(
            viewBinding.briefIntroduction,
            viewBinding.comment,
            viewBinding.other,
            viewBinding.useExplain
        )
        viewBinding.pointScrollView.onViewPointChangeListener = this
        viewBinding.pointScrollView.isFixBottom = true
        viewBinding.pointScrollView.isShowDebugLine = true
        viewBinding.pointScrollView.setScrollOffset(resources.getDimensionPixelOffset(R.dimen.indicator_size))
    }

    override fun onScrollPointChange(previousDistance: Int, nextDistance: Int, index: Int) {

    }

    override fun onScrollPointChangeRatio(
        previousFleeRatio: Float,
        nextEnterRatio: Float,
        index: Int,
        scrollPixel: Int,
        isScrollBottom: Boolean
    ) {
        //这里是从顶部滑到第一个锚点View时显示隐藏指示器
        if (index == 0) {
            viewBinding.indicator.visibility =
                if (previousFleeRatio == 0f && nextEnterRatio != 0f) View.GONE else View.VISIBLE
        }

        var ratio = 0f;
        if (previousFleeRatio == 0f || nextEnterRatio == 0f || isScrollBottom) {
            ratio = 0f
        } else {
            ratio = nextEnterRatio
        }
        viewBinding.indicator.onPageScrolled(index, ratio, scrollPixel)
    }

    override fun onPointChange(index: Int, isScrollBottom: Boolean) {
        viewBinding.indicator.onPageSelected(index)
    }

}