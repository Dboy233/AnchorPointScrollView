package com.example.scrollviewnode

import android.content.res.Resources

/**
 * @author DBoy 2021/2/1
 * <br/>    文件描述 :
 */
object SizeUtils {

    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}