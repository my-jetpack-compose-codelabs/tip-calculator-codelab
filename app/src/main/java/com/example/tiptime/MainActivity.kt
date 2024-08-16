/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TipTimeLayout()
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    // 考虑我们的需求,需要在TipTimeLayout下的 text 显示tip的金额,所以将状态属性提取到 text 和 EditNumberField 共同的父级组件, 这就是状态提升
    var amountInput by remember { mutableStateOf("") }
    val amount = amountInput.toDoubleOrNull() ?: 0.0

    // 创建一个状态属性负责接收输入的小费百分比
    var tipInput by remember { mutableStateOf("") }
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0

    // 小费是否向上取整的 flag
    var roundUp by remember { mutableStateOf(false) }

    // 根据账单金额和小费百分比计算小费的实际值
    val tip = calculateTip(amount, tipPercent, roundUp)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .safeDrawingPadding()
            // 使Column可以垂直滑动,且会记忆滑动的位置状态,为了对应翻转屏幕时显示不全的情况
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(
                    bottom = 16.dp,
                    top = 40.dp
                )
                .align(alignment = Alignment.Start)
        )
        // 添加一个输入框,输入账单的价格
        EditNumberField(
            label = R.string.bill_amount,
            leadingIcon = R.drawable.money,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                // 指定键盘的 Done 键行为是 移动到下一个输入框
                imeAction = ImeAction.Next
            ),
            // 因为状态提升,state 属性不再定义在EditNumberField内,而是传递进去
            value = amountInput,
            onValueChange = { amountInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        )
        // 添加一个输入框,输入小费的百分比
        EditNumberField(
            label = R.string.how_was_the_service,
            leadingIcon = R.drawable.percent,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                // 指定键盘的 Done 键行为是 Done
                imeAction = ImeAction.Done
            ),
            value = tipInput,
            onValueChange = { tipInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
        )
        RoundTheTipRow(
            roundUp = roundUp,
            onCheckedChange = { roundUp = it },
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            // 因为 amountInput 提升了层级,所以这里也可获取到计算出来的 tip 了,我们这里不再使用定制而是tip的值
            text = stringResource(R.string.tip_amount, tip),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}

// 自定义一个输入框组件
@Composable
fun EditNumberField(
    // 添加一个形参, 来接收 strings.xml 中的字符资源的 id, 因为我们将会重用这个EditNumberField组件, 我们需要指定显示的字符串
    // 同时也添加了@StringRes注解,检查接收的不是一个随意的 int 值而是一个字符串资源 id
    @StringRes label: Int,
    // 这个形参接受的 Icon 资源的 id
    @DrawableRes leadingIcon: Int,
    // 添加一个形参, 接受键盘的配置
    keyboardOptions: KeyboardOptions,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        // label是 textfield 的占位文字,这里可以看到接受了一个函数,也就是说我们可以在这里写 if 判断,根据不同场显示不同的占位文字
        label = { Text(text = stringResource(id = label)) },
        // 设置TextField的leadingIcon
        leadingIcon = {
            // 这里使用了painter这个通用的接口, 其他的还有imageVector(矢量图)和bitmap(位图)的方式
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null
            )
        },
        value = value,
        // 用户输入后的回调方法,根据定义onValueChange: (String) -> Unit,所以我们需要更新amountInput, 且每次更新都会触发EditNumberField组件重组,因为amountInput是一个 state 属性
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        // 使TextField可以接受外部传入的键盘配置
        keyboardOptions = keyboardOptions
    )
}
// 创建一个控件,包含一个 text 和一个 switch 可以切换小费是否向上取整
@Composable
fun RoundTheTipRow(
    roundUp: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(id = R.string.round_up_tip))
        Switch(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End),
            checked = roundUp,
            onCheckedChange = onCheckedChange
        )
    }
}
/**
 * Calculates the tip based on the user input and format the tip amount
 * according to the local currency.
 * Example would be "$10.00".
 */
// 添加一个注解,使这个方法面向测试公开
@VisibleForTesting
// 修改可见性为internal, private 表只在本文件或者本类中课件, internal表在模块内可见, 我们为了要测试这个方法,需要改为internal
internal fun calculateTip(
    amount: Double,
    tipPercent: Double = 15.0,
    roundUp: Boolean
): String {
    var tip = tipPercent / 100 * amount
    if (roundUp) {
        // 使计算出来的小费数值向上取整
        tip = kotlin.math.ceil(tip)
    }
    return NumberFormat.getCurrencyInstance().format(tip)
}

@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme {
        TipTimeLayout()
    }
}
