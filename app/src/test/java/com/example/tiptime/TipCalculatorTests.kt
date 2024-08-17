// 本地测试的 package 需要和项目的package的代码路径并列, 且包名相同
// 项目package路径: tip-calculator/app/src/main/java/com/example/tiptime
// 插桩测试package路径: tip-calculator/app/src/test/java/com/example/tiptime
package com.example.tiptime

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat

// 本地测试用于测试应用中的小段代码, 不依赖于设备
// 我们这里主要测试calculateTip这个计算小费的方法可能正常的运行
class TipCalculatorTests {
    // 使用@Test注解, 提示编译器这是一个测试的方法
    @Test
    // 测试方法的命名请确保能清楚地说明测试的内容和预期
    // 测试方法不关注实现方式,而是着眼于输入和输出是否符合预期
    fun calculateTip_20PercentNoRoundup() {
        // calculateTip方法接受三个参数(amount: Double, tipPercent: Double,roundUp: Boolean), 我们先不考虑向上取整的 flag
        val amount = 10.00
        val tipPercent = 20.00

        // 根据calculateTip方法的return 的代码,获取到我们期望的calculateTip方法返回值
        val expectedTip = NumberFormat.getCurrencyInstance().format(2)
        // 获取到实际方法的返回值
        val actualTip = calculateTip(amount, tipPercent, false)
        // 使用断言比较期待值和实际值是否一致, assertEquals需要import org.junit.Assert.assertEquals, 其他的还有 assertNotEqual, assertTrue 等其他断言
        assertEquals(expectedTip, actualTip)
    }
}