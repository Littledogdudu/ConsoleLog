# Console Log README

[![JDK](https://img.shields.io/badge/JDK-17-blue.svg)](https://bell-sw.com/pages/downloads/#jdk-17-lts)
[![License](https://img.shields.io/badge/License-Apache-blue.svg)](LICENSE)
[![Commit](https://img.shields.io/github/last-commit/Littledogdudu/ConsoleLog?color=green)]()
[![Version](https://img.shields.io/github/release/Littledogdudu/ConsoleLog.svg?style=flat-square&maxAge=600)](https://github.com/Littledogdudu/ConsoleLog/releases)

**ConsoleLog**能够通过光标所在位置快速打印console.log语句，并在结束调试后一键删除  
更适用于WebStorm和IDEA的前端开发插件

默认的快捷键：
- Alt+1: 插入 console.log()
- Alt+2: 删除所有 console.log()
- Alt+Shift+1: 注释掉所有 console.log()
- Alt+Shift+2: 解注释所有 console.log()

键盘映射名称：
- Alt+1: Console Log Plugin: Insert Console Log Message
- Alt+2: Console Log Plugin: Delete All Console Log Message
- Ctrl+Alt+1: Console Log Plugin: Insert Console XXX Template Message
- Alt+Shift+1: Console Log Plugin: Comment All Console Log Message
- Alt+Shift+2: Console Log Plugin: Uncomment Console Log Message

### 已发布功能
#### 基础功能
- [x] 哈喽，你可以通过WebStorm的插件设置自定义你的打印模板哦，但是要注意尽量与众不同一点哦，不然可能会误删你不想删掉的console.log语句哦
- [x] 智能地插入打印语句，包智能的，嘿嘿🤭
- [x] 可以在插件设置中自行设置插入打印语句后光标是否自动跟随到打印语句末尾（默认启用）
- [x] 可以在插件设置中自行设置字符串使用双引号还是单引号包含（默认启用双引号）
- [x] 存在选中文本时（支持多光标区域选中）仅在选中区域内删除/注释/解注释（默认启用）
- [x] 除了单引号/双引号，加入反引号(`)的支持

#### 无变量打印功能（默认不启用）
- [x] 不选中文本时生成判断数据流向的语句及删除/注释/解注释的行为
- [x] 插入后自动修复行号（为避免过分的性能开销，后续也不会再扩展，比如删除时，代码变更时）
- [x] 提供修复行号功能（不默认设置快捷键，需要自行设置）

#### 侧边栏功能
- [x] 可以在插件设置中自行设置是否启用侧边栏，侧边栏显示当前打开文件的所有打印表达式（默认启用）
- [x] 侧边栏支持点击定位到对应行的打印语句
- [x] 侧边栏支持查询 所有打印表达式/不包含注释的打印表达式/仅符合插件规范格式的打印表达式/仅符合插件无变量生成规范格式的打印表达式
- [x] 侧边栏添加单击删除/跳转切换行为

> 可在[github](https://github.com/Littledogdudu/ConsoleLog/releases)或者[jetbrain marketplace](https://plugins.jetbrains.com/plugin/26574-console-log/versions)上下载对应心仪版本  
> 1.1.8版本是插件最主要功能的最佳版本，包括多光标支持、选中区域删除/注释/解注释的版本（未加入侧边栏的版本）   
> 1.2.2版本是当前加入侧边栏的支持的最佳版本

### 开发者
环境配置：JDK17和Gradle8.10；master-Java11分支（尚未加入侧边栏功能）可支持使用JDK11或JDK8开发，使用Gradle7.2

| 分支            | JDK版本   | Gradle版本 | 兼容的IDE版本  |
|---------------|---------|----------|-----------|
| master        | 17      | 8.10     | 2022.3及以上 |
| master-Java11 | 8或11或17 | 7.2      | 2020.3及以上 |

运行这个插件需要把这个local方法的参数修改为你的WebStorm文件路径哦  
![modifyLocal](https://gitee.com/yang_skysource/console-log/blob/master/.github/readme/buildModifyLocal.png)  
在一些网络下，gradle下载依赖可能出现失败的情况，

抱歉，暂时**不完全**支持jsp项目（注释和解注释无法使用），该插件插入时可能只能插入在下一行，在没有语法错误的情况下，删除理论可以使用

### 鸣谢
- 由igor.pavlenko提出PSI JS类型强制转换问题的bug
- 由yan.wt提出新的功能：支持在格式化字符串中添加文件名和行号
- 由JiGewusuoweiju提出新的功能：期望未选中文本的时候，依然可以打印默认信息
- 由1327947094提出新的功能：文件相对路径占位
- 由cscsyiku123提出新功能：添加 console.log, dir, table和其他选项（快捷键：Ctrl+Alt+1，使用模板选择）
- 由IFnGSiYu提出新的功能：单击侧边栏项跳转/删除的侧边栏按钮

> 灵感来源于vscode插件 [turbo console log](https://github.com/Chakroun-Anas/turbo-console-log)  
> 有新的主意可以在[github](https://github.com/Littledogdudu/ConsoleLog)上fork或提出[issue](https://github.com/Littledogdudu/ConsoleLog/issues)或者发送到我的邮箱2378459785@qq.com哦  
> 如果觉得插件对你的帮助很大很大，希望[github点个star](https://github.com/Littledogdudu/ConsoleLog)，真的感谢！

# 设置项简介

![setting.png](https://gitee.com/yang_skysource/console-log/blob/master/.github/readme/setting.png)

## 基础设置
### 自定义打印语句

#### 插入语句

你可以通过输入下面【符号】列对应的占位符来实时获取文本中对应的变量名/方法名/行号/文件名  
例如默认为：🚀 ~ \${methodName} ~ \${variableName}:&nbsp;  
你可以修改为：🚀 ~ \${filePath}/${fileName} ~ L(\${lineNumber}) ~ \${methodName} ~ \${variableName}:&nbsp;

### 未选中变量时的默认行为（默认关闭行为：什么也不做）

#### 当没有可打印变量时生成默认插入语句内容

默认不启用：当光标处没有可打印变量时，什么也不做  
启用后：当光标处没有可打印变量时，插入的打印语句将使用下方填写的默认插入语句模板

#### 默认插入语句

同插入语句，但是仅在启用上方的【当没有可打印变量是生成默认插入语句内容】按钮且当前光标没有可打印变量时调用

#### 当没有可打印变量时插入后光标自动跟随到log表达式末尾

默认不启用（建议）：当没有可打印变量时，插入的打印语句将自动跟随到生成的console.log的末尾  
启用后：插入的打印语句将自动跟随到生成的console.log的末尾

> 已知bug：当光标上方存在多行空行时使用此功能不仅会删除多余的空行，而且会导致打印的行号停留在光标之前的行，因为代码格式化导致的删除的关系，这个行号不正确

### LiveTemplate

#### 一键删除、注释或解注释时同样作用于console.table

默认启用：当使用一键删除、一键注释或一键解注释功能时，也可以作用于console.table  
禁用后：当使用一键删除、一键注释或一键解注释功能时将不会对编辑器中的所有console.table进行操作

console.table仅支持data一个入参，因没有额外标识识别，故一键删除、注释和解注释无法识别插件生成还是手动添加

### 占位符设置

#### ${fileName}

#### 打印的文件名是否需要后缀名

默认启用：\${fileName}占位符将会被替换为生成的console.log所在的文件名，且包含后缀名  
禁用后：\${fileName}占位符将会被替换为生成的console.log所在的文件名，但不在包含后缀名

#### ${filePath}

##### 文件所在路径是否根据基准文件名称截断

默认启用：\${filePath}占位符将会被替换为生成的console.log所在文件的相对路径，且根据【文件所在路径基准文件夹名称】截断
禁用后：\${filePath}占位符将会被替换为生成的console.log所在文件相对路径

##### 文件所在路径基准文件夹名称

默认为views：即\${filePath}占位符将会根据填写内容截断  
例如：当前所在相对路径为 src/views/blog/index.vue，则根据views截断后获得：blog/index.vue

多个文件夹名称可通过逗号分隔

##### 文件所在路径分隔符

路径分隔符

##### 文件所在路径是否包含基准文件夹名称

默认启用：当前所在相对路径为 src/views/blog/index.vue，则根据views截断后获得：views/blog/index.vue
禁用后：当前所在相对路径为 src/views/blog/index.vue，则根据views截断后获得：blog/index.vue

#### ${lineNumber}

##### 是否使用打印变量所在行号

默认不启用：\${lineNumber}占位符将会被替换为生成的console.log所在的行号  
启用后：\${lineNumber}占位符会被替换为需要打印的变量所在的行号

##### 是否开启插入自动修复行号

默认不启用：\${lineNumber}占位符不会自动更新，需手动更新  
启用后：\${lineNumber}占位符会在插入console.log语句时自动更新为正确行号

### 提升选中文本时的可操作性

#### 选中文本时仅在选中区域内删除/注释/解注释

默认启用：当选中文本时，删除/注释/解注释功能将仅在选中区域内生效  
禁用后：无论是否选中文本，删除/注释/解注释功能都会在整个文件内生效

## 格式设置

### 插入后光标后自动跟随到log表达式末尾

默认启用：插入后光标自动跟随到生成的console.log表达式末尾  
禁用后：光标停留在原本位置

### 字符串常量引用符号

#### 使用单引号/双引号/反引号

单选组，更改包裹console.log表达式文本所使用的引号类型

## 侧边栏设置

![sidebar.png](https://gitee.com/yang_skysource/console-log/blob/master/.github/readme/sidebar.png)

### 侧边栏顶部查询选项设置按钮

#### 展示/隐藏注释项（眼睛图标）

默认启用：侧边栏将查询所有打印表达式，包含注释项
禁用：侧边栏将查询所有打印表达式，不包含注释项

#### 启用/禁用针对性查找（小火箭图标/禁用图标）

默认启用：侧边栏将查询所有符合插件生成规范的打印表达式
禁用：侧边栏将不会筛选符合插件生成规范的打印表达式

#### 启用/禁用无变量针对性查找（>>>图标/禁用图标）

默认启用：侧边栏将查询所有符合无变量选中时插件生成的console.log表达式  
禁用：侧边栏将不会筛选无变量选中时插件生成的console.log表达式

#### 启用/禁用标签查找（书签图标）

启用：根据设置中设置的标签项继续查找，标签项为1级，表达式为2级（缩进2字符）  
默认禁用：禁用标签查找

#### 单击侧边栏项跳转/删除

启用：此时单击侧边栏下方的项会跳转到该项所对应的打印表达式
默认禁用：此时单击侧边栏下方的项会删除该项所对应的打印表达式

### 侧边栏基础设置

#### 是否启用侧边栏（重启生效）

默认启用：启用侧边栏  
禁用：禁用侧边栏  
此功能仅在设置应用后重启才会生效

#### 侧边栏字体大小

修改侧边栏搜索出来的每行表达式的字体大小

### 文件类型生效设置

#### 侧边栏查找不限定文件类型

默认启用：对当前打开的任何文件类型的文件都会启用侧边栏查找打印表达式  
禁用后：仅对当前打开的文件为选中文件类型的文件才会执行查找并显示在侧边栏

#### 侧边栏查找限定文件类型

该四个文件类型选项仅在【侧边栏查找不限定文件类型】选项禁用时可选，可选中多个文件类型  
选中Vue后，检查当前文件类型为Vue的文件则会查询该文件所有console.log语句  
选中JavaScript后，检查当前文件类型为JavaScript的文件则会查询该文件所有console.log语句  
选中TypeScript后，检查当前文件类型为TypeScript的文件则会查询该文件所有console.log语句  
选中Text后，检查当前文件为普通文本则会查询该文件所有console.log语句  

### 侧边栏按钮设置

#### 侧边栏搜索设置

##### 首次启动侧边栏时是否默认启用注释项查询

默认启用：每次打开IDE时，侧边栏将默认启用注释项查询按钮
禁用：每次打开IDE时，侧边栏将默认禁用注释项查询按钮

##### 首次启动侧边栏时是否默认启用针对性查询

默认启用：每次打开IDE时，侧边栏将默认启用针对性查询按钮
禁用：每次打开IDE时，侧边栏将默认禁用针对性查询按钮

##### 首次启动侧边栏时是否默认启用无变量针对性查询

默认启用：每次打开IDE时，侧边栏将默认启用无变量针对性查询按钮
禁用：每次打开IDE时，侧边栏将默认禁用无变量针对性查询按钮

#### 标签设置

##### 首次启动侧边栏时是否默认启用标签查找

启用后：每次打开IDE时，侧边栏将默认启用侧边栏标签查找（标签项在下面的设置项可以进行设置）  
默认禁用：侧边栏不会默认启用标签查找（也可通过侧边栏顶部的【启用标签查找】临时更改）

##### 侧边栏自定义标签查询项

自定义侧边栏查找的标签项，使用分号分割每个标签项，支持正则表达式

#### 单击侧边栏项时默认行为

##### 首次启动侧边栏时单击侧边栏默认跳转还是删除（启用则默认跳转，禁用则默认删除）

默认启用：每次打开IDE时，侧边栏将默认启用侧边栏项单击跳转按钮
禁用：每次打开IDE时，侧边栏将默认启用侧边栏项单击删除按钮

##### 单击侧边栏是否删除标签

启用：侧边栏单击侧边栏的标签项时，可以删除对应标签项
默认禁用：侧边栏单击侧边栏的标签项时，不可以删除对应标签项（单击标签项无反应）

---

<!-- Plugin description -->

**ConsoleLog** can quickly print console.log() in your code and free-hand

Default keymap as following:
- Alt+1: Console Log Plugin: Insert Console Log Message
- Ctrl+Alt+1: Console Log Plugin: Insert Console XXX Template Message
- Alt+2: Console Log Plugin: Delete All Console Log Message
- Alt+Shift+1: Console Log Plugin: Comment All Console Log Message
- Alt+Shift+2: Console Log Plugin: Uncomment Console Log Message

### Release Feature
#### basic feature
- [x] you can go to settings to set what message you want to show
- [x] Intelligently insert print statements, hei hei 🤭
- [x] You can set whether the cursor automatically follows to the end of the print statement after inserting the print statement in the plug-in settings (enabled by default)
- [x] You can set whether the string is included in double or single quotes in the plugin settings (double quotes are enabled by default)
- [x] When selecting Chinese book, delete annotation only in the selected area (enabled by default)
- [x] In addition to single and double quotation marks, support for backticks (') has been added

#### no variable feature(disabled default)
- [x] When the variable is not selected, a statement that determines the direction of data flow is generated, and the act of deleting/commenting/uncommenting is generated
- [x] Automatically repairs line numbers after insertion (in order to avoid excessive performance overhead, they will not be extended in the future, such as when deleting or changing code)
- [x] Provide the function of repairing line numbers (shortcut keys are not set by default, you need to set them by yourself)

#### sidebar feature
- [x] You can set whether to enable the sidebar in the plug-in settings, and the sidebar displays all print expressions of the currently open file (enabled by default)
- [x] The sidebar supports clicking on the print statement that is located to the corresponding line
- [x] The sidebar supports queries All print expressions that do not contain comments are only in the format of the plug-in specification

if you want to run this project, please modify the local path.  
![modifyLocal](https://github.com/Littledogdudu/ConsoleLog/blob/master/.github/readme/buildModifyLocal.png)

Sorry, jsp not support  
You can use the plugin on html code, but be careful: the statement is not removed if there is a syntax error after inserting the expression, because the PSI tree structure is chaotic at this point

### Thanks List
- Bug report on PSI JS type coercion issue by igor.pavlenko
- New feature proposed by yan.wt: support for adding file names and line numbers in formatted strings
- New feature proposed by JiGewusuoweiju: When the variable is undefined near the cursor, a statement that determines the direction of data flow is generated, and the act of deleting/commenting/uncommenting is generated
- New feature proposed by 1327947094: placeholder for file relative path
- New feature proposed by cscsyiku123：Added console.log, dir, table and other options
- New feature proposed by IFnGSiYu：sidebar button to jump/delete by clicking sidebar item

> The idea from vscode plugin [turbo console log](https://github.com/Chakroun-Anas/turbo-console-log)

# Setting description

![setting.png](.github/readme/setting.png)

## Basic Setting
### Customize the print sentence

#### Insert Sentence
You can obtain the corresponding variable name/method name/line number/file name in real-time in the text by entering the placeholder corresponding to the symbol column below  
For example, the default is: 🚀 ~ \$ {methodName} ~ \${variableName}:&nbsp;  
You can modify it to: 🚀 ~ \${filePath}/${fileName} ~ L(\${lineNumber}) ~ \${methodName} ~ \${variableName}:&nbsp;

### Default behavior when no variable is selected (default disabled behavior: do nothing)

#### Generate default insert statement content when there are no printable variables

Default not enabled: Nothing is done when there are no printable variables at the cursor  
After activation: When there are no printable variables at the cursor, the inserted print statement will use the default insert statement template filled in below

#### Default insertion statement:

The same as the insert statement, but only when the \[Generates default insert statement content when there are no printable variables\] button above is enabled and the current cursor has no printable variables

#### When there are no printable variables, the cursor automatically moves to the end of the log expression after insertion

Default not enabled (recommended): When there are no printable variables, the inserted print statement will automatically follow to the end of the generated console.log  
After activation: Inserted print statements are automatically followed to the end of the generated console.log

> Known bug: Using this function when there are multiple blank lines above the cursor will not only delete the extra blank lines, but also cause the printed line number to stay on the line before the cursor, which is incorrect due to the deletion caused by code formatting

### LiveTemplate

#### One-click delete, comment, or uncomment also applies to console.table

Enabled by default: When using the one-click delete, one-click comment, or one-click uncomment feature, it also applies to console.table.  
Disabled: When using the one-click delete, one-click comment, or one-click uncomment feature, it will not operate on all console.table in the editor.

console.table only supports a single 'data' parameter. Because there is no additional identifier to distinguish, the one-click delete, comment, and uncomment cannot recognize whether it was generated by the plugin or added manually.

### Placeholder Setting

#### ${fileName}

##### Does the printed file name require a file extension (this option applies to the ${filename} placeholder)

Default enabled: The \${fileName} placeholder will be replaced with the file name of the generated console.log, including the suffix  
After disabling: The \${fileName} placeholder will be replaced with the file name of the generated console.log, but it will not include the suffix

#### ${filePath}

##### Whether the file is truncated according to the benchmark file name based on its path (this option applies to the ${filePath} placeholder)

Default enabled: The ${filePath} placeholder will be replaced with the relative path of the file where the generated console.log is located, truncated according to the [Reference Folder Name for File Path]
If disabled: The ${filePath} placeholder will be replaced with the relative path of the file where the generated console.log is located

##### Base folder name of the file path

Default is 'views': The ${filePath} placeholder will be truncated based on the provided content
For example: If the current relative path is src/views/blog/index.vue, truncating based on 'views' will result in: blog/index.vue

Multiple folder names can be separated by commas

##### File path separator

Default is '/': The ${filePath} placeholder will be replaced with the relative path of the file where the generated console.log is located, separated by '/'

##### Does the file path contain the reference folder name

Default enabled: If the current relative path is src/views/blog/index.vue, truncating based on 'views' will result in: views/blog/index.vue
If disabled: If the current relative path is src/views/blog/index.vue, truncating based on 'views' will result in: blog/index.vue

#### ${lineNumber}

##### Whether to print the line number of the variable

Default not enabled: The \${lineNumber} placeholder will be replaced with the line number where the generated console.log is located  
After activation, the \${lineNumber} placeholder will be replaced with the line number of the variable that needs to be printed

##### Enable automatic correction of inserted line numbers?

Default not enabled: The \${lineNumber} placeholder will not be automatically updated and needs to be updated manually
After activation: The \${lineNumber} placeholder is automatically updated to the correct line number when the console.log statement is inserted

### Enhance the operability of the selected text

### when selecting code text, it will be deleted/comment/uncomment only within the selected area

Default enabled: When selecting text, the delete/comment/uncomment function will only take effect within the selected area  
After disabling: Regardless of whether text is selected or not, the delete/comment/uncomment function will take effect throughout the entire file

## Format Setting

### after insertion, the cursor automatically follows to the end of the log expression

Default enabled: After insertion, the cursor automatically follows to the end of the generated console.log expression  
After disabling: the cursor stays in its original position

### String constant reference symbols

#### single quotes/double quotes/back tick

radio group to change the type of quotation marks used to wrap console.log expression text

## Sidebar Setting

![sidebar.png](https://gitee.com/yang_skysource/console-log/blob/master/.github/readme/sidebar.png)

### Query option settings button at the top of the sidebar

#### show/hidden comment (eye icon)

Enabled by default: The sidebar will query all print expressions, including comment items
Disabled: The sidebar will query all print expressions and do not contain comment items

#### enable/disable targeted lookup (rocket/disable icon)

Enabled by default: The sidebar will query all print expressions that meet the plugin generation specifications.
Disabled: The sidebar will not filter print expressions that meet the plugin generation specifications.

#### Enable/Disable targeted search without variables (>>> icon/disabled icon)

Enabled by default: The sidebar will query all console.log expressions generated by the plugin when no variables are selected.
Disabled: The sidebar will not filter console.log expressions generated by the plugin when no variables are selected.

#### enable/disable tag lookup (bookmark icon)

Enabled: Continue to search according to the label items set in the settings, the label item is level 1, and the expression is level 2 (indented 2 characters)  
Disabled by default: Disables tag lookup

#### Click Sidebar Item to Navigate/Delete

Enabled: Clicking an item in the sidebar will navigate to the print expression corresponding to that item.
Disabled by default: Clicking an item in the sidebar will delete the print expression corresponding to that item.

### Sidebar basic settings

#### whether to enable the sidebar (restart takes effect)

Default Enabled: Enable Sidebar  
Disabled: Disable Sidebar  
This feature only takes effect after restarting the application after the settings are applied.

#### sidebar font size:

the font size of each line expression retrieved in the sidebar search.

### File type activation settings

#### sidebar lookup is not limited to file types

Default Enabled: The sidebar search will print expressions for any type of file currently opened.  
Disabled: The search will only execute and display in the sidebar for files of the selected file type currently opened.

#### sidebar looks for limited file types:

The four file type options can only be selected when the "Sidebar Search Unrestricted by File Type" option is disabled. Multiple file types can be selected.  
Select Vue: If the current file type is Vue, it will query all console.log statements in that file.  
Select JavaScript: If the current file type is JavaScript, it will query all console.log statements in that file.  
Select TypeScript: If the current file type is TypeScript, it will query all console.log statements in that file.  
Select Text: If the current file is plain text, it will query all console.log statements in that file.

### Sidebar Button Settings

#### Sidebar Search Button Setting

##### whether to display the comment sentence by default when you first launch the sidebar

Enabled by default: Each time the IDE is opened, the sidebar will have the comment query button enabled by default.
Disabled: Each time the IDE is opened, the sidebar will have the comment query button disabled by default.

##### whether to display the targeted sentence by default when you first launch the sidebar

Enabled by default: Each time the IDE is opened, the sidebar will have the targeted query button enabled by default.
Disabled: Each time the IDE is opened, the sidebar will have the targeted query button disabled by default.

##### whether to enable variable-free targeted queries by default when first launching the sidebar

Enabled by default: Each time the IDE is opened, the sidebar will have the variable-free targeted query button enabled by default.
Disabled: Each time the IDE is opened, the sidebar will have the variable-free targeted query button disabled by default.

#### Tag Button Setting

##### Default Enable Tag Search When Sidebar is Launched for the First Time

When enabled: Each time the IDE is opened, the sidebar will default to enabling tag search (the tag items can be set in the options below).  
Default Disabled: The sidebar will not default to enabling tag search (can also be temporarily changed by using "Enable Tag Search" at the top of the sidebar).

##### Custom Sidebar Tag Query Items

Customize the tag items for the sidebar search, separating each tag item with a semicolon. Regular expressions are supported.

#### Sidebar Label Jump Or Delete Button Setting

##### sidebar label jump or delete is enabled by default when you first launch the sidebar(enable is jump by default and disable is delete by default)

Default enabled: Each time the IDE is opened, the sidebar will default to enable the jump button for sidebar item clicks.
Disabled: Each time the IDE is opened, the sidebar will default to enable the delete button for sidebar item clicks.

##### whether clicking a sidebar item deletes a tag

Enabled: Clicking a tab item in the sidebar will delete the corresponding tab item.
Default disabled: Clicking a tab item in the sidebar will not delete the corresponding tab item (clicking the tab has no effect).

<!-- Plugin description end -->