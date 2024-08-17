// 和本地测试同样的, 插桩测试的package的路径也需要和项目的代码路径层级平行,且包名相同
// 项目package路径: tip-calculator/app/src/main/java/com/example/tiptime
// 插桩测试package路径: tip-calculator/app/src/androidTest/java/com/example/tiptime
package com.example.tiptime

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.tiptime.ui.theme.TipTimeTheme
import org.junit.Rule
import org.junit.Test
import java.text.NumberFormat

// 插桩测试是要依赖于虚拟设备或者物理设备
class TipUITests {
    // 插桩测试会测试实际实例及其界面, 所以需要我们先设置界面, 就是类似于 onCreate 的方法中的设置,之后我们才能执行编写的插桩测试
    // 创建一个规则,使用这个规则我们可以操作 compose 的 ui
    @get:Rule
    val composeTestRule = createComposeRule()

    // 使用@Test告诉编译器这是个测试方法,且编译器知道androidTest路径下是插桩测试, test路径下的是本地测试
    @Test
    fun calculate_20_percent_tip() {
        composeTestRule.setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout()
                }
            }
        }
        // 查找一个文本为"Bill Amount"的Compose节点(node),然后输入"10"
        composeTestRule.onNodeWithText("Bill Amount")
            .performTextInput("10")
        // 查找一个文本为"Tip Percentage"的Compose节点(node),然后输入"20"
        composeTestRule.onNodeWithText("Tip Percentage")
            .performTextInput("20")
        // 创建一个期待的结果
        val exceptedTip = NumberFormat.getCurrencyInstance().format(2)
        // 找到一个文本是"Tip Amount: $exceptedTip"的节点,断言存在,否则输出入"No node with this text was found"
        composeTestRule
            .onNodeWithText("Tip Amount: $exceptedTip")
            .assertExists("No node with this text was found")
    }
}