# PsiPositionUtil 单元测试说明

## 概述

本目录下的测试用于验证 `PsiPositionUtil`（`src/main/java/com/sky/consolelog/utils/PsiPositionUtil.java`）
插入位置计算逻辑的正确性。该工具类负责在 WebStorm 中按 Alt+1 插入 console.log 语句时，
根据光标所在 PSI 元素类型计算出 console.log 的插入位置（`ScopeOffset`）。

## 测试文件

| 文件 | 说明 |
| --- | --- |
| `PsiPositionUtilTest.java` | 主测试类：75 个用例，覆盖 `getScopeOffsetByType` 全部语法分支及 `InsertConsoleLogAction.findScopeOffset` 向上查找逻辑 |
| `FakePsiElement.java` | 测试基础设施：模拟 IntelliJ JavaScript/TypeScript PSI 树的假 `PsiElement` |

## 测试方式

由于本地 WebStorm 2026.2 的 light test 应用无法自动加载 JavaScript 插件
（IPGP 2.5.0 + local IDE 组合下，JS 插件依赖的 bundled module 如 `intellij.platform.smRunner`
无法在测试 JVM 中注册为已安装模块），本测试不依赖 IDE 运行时，
而是通过 `FakePsiElement` **手工建模 WebStorm 真实的 JS/TS PSI 树结构**，
以纯 JUnit 方式精确验证定位逻辑。测试无需启动 IDE，运行极快（< 1 秒）。

每个用例都会断言 `ScopeOffset` 的全部 5 个字段：
`insertEndOffset`（插入偏移量）、`needTab`（是否需制表符对齐）、
`isDefault`（是否默认换行）、`needBegLine`（句首是否换行）、`needEndLine`（末尾是否换行）。

## 覆盖范围

### 一、变量语句 JSVarStatement（const/let/var）
- 带分号 / ASI 无分号 / 多声明符 / 对象解构 / 数组解构 / TS 类型注解
- for 循环、for-in、for-of 中的变量声明（特殊处理：插入到变量声明末尾 + tab 对齐）

### 二、赋值表达式 JSAssignmentExpression
- 简单赋值 / ASI / 赋值调用表达式 / 成员赋值 `obj.x = 1` / 链式赋值 `a = b = 1`

### 三、块作用域语句（插入到 `{` 之后）
- if（带块/无块/空块）、else、else-if、switch、try、while、for、for-in/for-of
- 对象方法 `JSFunctionProperty`、类方法、函数声明 `JSFunction`、函数表达式 `JSFunctionExpression`
- 类 `JSClass`、catch 块 `JSCatchBlock`

### 四、TypeScript 函数（.ts 文件）
- `TypeScriptFunction` / `TypeScriptFunctionExpression` / `TypeScriptFunctionProperty`

### 五、case 子句（插入到冒号之后）
- `case 1:` / `case TARGET:` / `default:`

### 六、do-while（插入到块体 `}` 之前，`needBegLine=false`）

### 七、调用表达式 JSCallExpression
- 作为语句（默认末尾）/ 位于 if 条件（块内）
- `foo().then(() => {...})` 箭头回调 / `foo().then(function(){...})` 函数回调（`instanceof JSFunctionExpression`）/ 无回调退化

### 八、表达式语句 JSExpressionStatement
- `ElMessage({...});`（分号后）/ ASI

### 九、箭头函数 JSArrowFunction
- 块体 / 表达式体（退化默认）/ TS async 箭头

### 十、JSX 元素
- 函数内 / 箭头函数内 / 类 render 方法内（向上查找最近作用域）/ 模块顶层（退化）

### 十一、return 语句（插入到 return 之前，避免不可达代码）

### 十二、对象字面量（插入到 `{` 之后）
- 普通 / 空对象 / 嵌套对象

### 十三、Vue/HTML 模板标签（XmlTag）
- `<script>` 块内插入 / 无块退化

### 十四、未知类型（返回 null）
- `JSReferenceExpression` / `JSIdentifier` / `JSBinaryExpression`
- **已知局限**：`TypeScriptClass`（.ts 带类型参数的类）当前不在 switch 中，返回 null，
  测试已记录该行为，如需支持请在 `PsiPositionUtil` 增加 `TypeScriptClass` 分支

### 十五、公开默认方法
- `getDefault` / `getAlignDefault` / `getUndefinedDefault(caret)`

### 十六、findScopeOffset 端到端（InsertConsoleLogAction 向上查找）
- 变量 / 调用 / 箭头函数体内 / 条件 / return / 无匹配退化 / TS 类内

## 测试发现的代码问题

1. **`PsiPositionUtil.getJSVarStatement` NPE 隐患（已修复）**：
   原代码 `element.getParent().toString()` 在父节点为 null（元素为根节点）时抛 NPE，
   已改为先判空再调用。修复不影响原有行为。

## 运行方式

```bash
# 运行全部测试
./gradlew test

# 只运行本测试类
./gradlew test --tests "com.sky.consolelog.utils.PsiPositionUtilTest"
```

## 关于 tailvy-mcp

原需求提到使用 tailvy-mcp 获取测试集可能值。经检查：
- 当前环境未配置 tailvy-mcp 工具（工具列表、npm registry、pi 配置中均不存在）
- 测试集已根据 JS/TS 语言语法知识 + WebStorm 2026.2 PSI 模型（从本地 IDE 的
  `intellij.javascript.psi.impl.jar` / `intellij.javascript.parser.jar` 反查确认的
  元素类型，如 `TypeScriptClass`、`JavaScriptFileType` 等）完整枚举
