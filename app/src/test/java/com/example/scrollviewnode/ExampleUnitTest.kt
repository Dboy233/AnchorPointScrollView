package com.example.scrollviewnode

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
        var value1: Int = Int.MIN_VALUE
        //tag右边值
        var value2: Int = Int.MAX_VALUE

        for (value in list) {
            if (tag >= value) {
                if (tag - value == min(tag - value, tag - value1)) {
                    value1 = value
                }
            } else {
                if (value - tag == min(value - tag, value2 - tag)) {
                    value2 = value
                }
            }
        }
    }

    @Test
    fun 测试计算() {
        var v1 = Int.MIN_VALUE
        var v2 = 20L;
        var v3 = v2 - v1;
        print(v3)
    }


}