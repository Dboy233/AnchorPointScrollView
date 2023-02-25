# ScrollViewPonit

### 锚点定位滚动View

像MarkDown的锚点定位的功能一样。

给`ScrollView`添加锚点定位功能

#### 实现功能：
- 添加锚点View
- 监听滚动变化
- 响应滚动变化
- 主动滚动到指定View位置

| 演示  |
| :--: |
| <img src="ScreenRecord-2021-04-09-11-32-50.gif" width="360" height="680" />   |

### 使用说明

  拷贝文件：[AnchorPointScrollView.kt](https://github.com/Dboy233/AnchorPointScrollView/blob/master/app/src/main/java/com/example/scrollview/AnchorPointScrollView.kt)到你的项目中使用。


```kotlin
  
  //添加锚点View 这个View必须是ScrollViewPoint的子View；
  //且在ScrollViewPoint中可以使用findViewById寻找到
  addScrollView(...views)
  
  //设置锚点变化监听器
  //当添加了锚点View之后。滚动的百分比和距离锚点View下表位置会进行通知
  setOnViewPointChangeListener(OnViewPointChangeListener)
  
  //滚动到指定View位置可以是任意的View包括非添加的锚点View
  scrollToView(View,Int)
  
  //滚动到指定View ID 位置
  scrollToView(Int,Int)
   
  //滚动到指定Index位置的View
  //使用这个方法Index的大小不能超过添加的锚点View个数。起始从0开始
  scrollToViewIndex(Int,Int)
  
  //设置锚点View触发的定点位置偏移量
  //大于0 位置向屏幕下方移动
  //小于0 位置向屏幕上方移动
  setScrollOffset(Int)
  
  //绘制锚点View触发Index改变的定点线
  //如果设置了setScrollOffset，可以使用此方法显示触发位置，以便调整
  isShowDebugLine = true
  
  //自动修复滚动底部
  //如果true->当滚动到底部后，index坐标修改为最后一个锚点ViewIndex
  isFixBottom = true
```
