package com.example.scrollview

import org.junit.Test
import kotlin.math.min

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    @Test
    fun 最接近值() {
        val list = arrayListOf<Int>(-1, -2, -3, 14, 5, 62, 7, 80, 9, 100, 200, 500, 1123)
        //寻找与tag最近的两个值
        val tag = 0L
        //tag左边值
        var leftVal: Int = Int.MIN_VALUE
        //tag右边值
        var rightVal: Int = Int.MAX_VALUE
        //首先排序
        list.sort()

        for (value in list) {
            //当前值小于Tag
            if (tag >= value) {
                if (tag - value == min(tag - value, tag - leftVal)) {
                    leftVal = value
                }
            } else {
                //当前值大于Tag
                if (value - tag == min(value - tag, rightVal - tag)) {
                    rightVal = value
                }
            }
        }

        println(" left=$leftVal tag=$tag  right=$rightVal")
    }

}