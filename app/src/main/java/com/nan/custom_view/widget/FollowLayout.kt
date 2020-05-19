package com.nan.custom_view.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.max

/**
 * Author:jingnan
 * Time:2020-05-14/14
 */
class FollowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ViewGroup(context, attrs, defStyleAttr) {

    private val allViews = ArrayList<ArrayList<View>>()
    private val perLineMaxHeight = ArrayList<Int>()
    private var lineViews = ArrayList<View>()

    /**
     * 1. 调用measureChild方法递归测量子View
     * 2. 通过叠加每一行的高度，计算出最终高度
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //获得宽高的测量模式和测量值
//        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var totalLineWidth = 0
        var perLineMaxHeight = 0
        var totalHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
            val params = childView.layoutParams as MarginLayoutParams
            val childWidth = childView.measuredWidth + params.leftMargin + params.rightMargin
            val childHeight = childView.measuredHeight + params.topMargin + params.bottomMargin
            if (totalLineWidth + childWidth >= widthSize) {
                //统计总高度
                totalHeight += perLineMaxHeight
                //开启新的一行
                totalLineWidth = childWidth
                perLineMaxHeight = childHeight
            } else {
                //记录每一行的总宽度
                totalLineWidth += childWidth
                perLineMaxHeight = max(perLineMaxHeight, childHeight)
            }
            //当该View已经是最后一个View的时候，将改行醉倒高度添加到totalHeight中
            if (i == childCount - 1) {
                totalHeight += perLineMaxHeight
            }
        }
        heightSize = if (heightMode == MeasureSpec.EXACTLY) heightSize else totalHeight
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        allViews.clear()
        perLineMaxHeight.clear()
        var totalLineWidth = 0
        var lineMaxHeight = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            val params = childView.layoutParams as MarginLayoutParams
            val childWidth = childView.measuredWidth + params.leftMargin + params.rightMargin
            val childHeight = childView.measuredHeight + params.topMargin + params.bottomMargin
            if (totalLineWidth + childWidth >= width) {
                allViews.add(lineViews)
                perLineMaxHeight.add(lineMaxHeight)
                //开启新的一行
                totalLineWidth = 0
                lineMaxHeight = 0
                lineViews.clear()
            }
            totalLineWidth += childWidth
            lineViews.add(childView)
            lineMaxHeight = max(lineMaxHeight, childHeight)
        }
        //单独处理最后一行
        allViews.add(lineViews)
        perLineMaxHeight.add(lineMaxHeight)
        //遍历集合中所有的View并显示出来
        //表示一个View和父容器左边的距离
        var mLeft = 0
        //表示View和父容器顶部的距离
        var mTop = 0
        for (i in 0 until allViews.size) {
            lineViews = allViews[i]
            lineMaxHeight = perLineMaxHeight[i]
            for (j in 0 until lineViews.size) {
                val childView = lineViews[j]
                val params = childView.layoutParams as MarginLayoutParams
                val leftChild = mLeft + params.leftMargin
                val topChild = mTop + params.topMargin
                val rightChild = leftChild + childView.measuredWidth
                val bottomChild = topChild + childView.measuredHeight
                //四个参数分别表示View的左上角和右下角
                childView.layout(leftChild, topChild, rightChild, bottomChild)
                mLeft += (params.leftMargin + childView.measuredWidth + params.rightMargin)
            }
            mLeft = 0
            mTop += lineMaxHeight
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (i in 0 until childCount) {
            val child = getChildAt(i) as TextView
            println("child.text = " + child.text)
            Log.e("child.text", child.text.toString())
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return FlowLayoutParams(context, attrs)
    }

    class FlowLayoutParams(c: Context?, attrs: AttributeSet?) : MarginLayoutParams(c, attrs) {
    }
}