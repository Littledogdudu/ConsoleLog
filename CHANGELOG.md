<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# Console Log CHANGELOG
- zh_CN [简体中文](./CHANGELOG.md)
- en_US [English](./CHANGELOG.en_US.md)

## [Unreleased]

## [1.0.0] - 2025-02-08

- 控制链式调用插入位置
- 当光标（未选中）所在变量名前包含this.等前缀时自动捕获
- 如果文本中存在包含双引号的变量或方法名，把双引号替换为转义双引号：\\"

## [0.0.1] - 2025-02-03

### Add

以下是快捷键及对应说明：
- Alt+1: 插入所有符合规范的console.log表达式
- Alt+2: 删除所有符合规范的console.log表达式
- Alt+Shift+1: 注释所有符合规范的console.log表达式
- Alt+Shift+2: 解注释所有符合规范的console.log表达式

- [x] 可以通过设置中的插件设置页面设置想要展示的console.log文本