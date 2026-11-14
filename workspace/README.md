# 简易计算器

一个使用 Kotlin 和原生 Android View 编写的轻量计算器。

## 功能

- 加、减、乘、除，自动处理运算优先级
- 小数、百分号、正负号
- 输入时实时预览结果
- 除零和非法表达式错误提示
- 支持屏幕键盘和实体键盘输入
- 屏幕旋转时保留计算状态
- 计算引擎与界面解耦，并包含单元测试

## 项目结构

```text
app/src/main/java/com/example/calculator/
├── CalculatorEngine.kt   # 独立的表达式解析与计算
└── MainActivity.kt        # 界面交互与状态管理
app/src/test/...           # CalculatorEngine 单元测试
```

## 构建

需要 JDK 17 和 Android SDK 35：

```bash
./gradlew assembleDebug
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

运行测试：

```bash
./gradlew testDebugUnitTest
```

## 使用说明

- `%` 是后缀运算符，例如 `200 × 10% = 20`。
- 点击 `C` 清除全部输入，点击 `⌫` 删除最后一个字符。
- `±` 切换当前数字的正负号。

项目还附带 GitHub Actions 工作流，推送仓库后会运行单元测试并上传 APK 构建产物。
