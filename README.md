# Console Log README
- zh_CN [简体中文](./README.md)
- en_US [English](./README.en_US.md)

![Build](https://github.com/Littledogdudu/ConsoleLog/workflows/Build/badge.svg)

<!-- Plugin description -->
**ConsoleLog**能够通过光标所在位置快速打印console.log语句，并在结束调试后一键删除

默认的快捷键：
- Alt+1: 插入 console.log()
- Alt+2: 删除所有 console.log()
- Alt+Shift+1: 注释掉所有 console.log()
- Alt+Shift+2: 解注释所有 console.log()

键盘映射名称：
- Alt+1: Console Log Plugin: Insert Console Log Message
- Alt+2: Console Log Plugin: Delete All Console Log Message
- Alt+Shift+1: Console Log Plugin: Comment All Console Log Message
- Alt+Shift+2: Console Log Plugin: Uncomment Console Log Message

- [x] 哈喽，你可以通过WebStorm的插件设置自定义你的打印模板哦，但是要注意尽量与众不同一点哦，不然可能会误删你不想删掉的console.log语句哦

运行这个插件需要把这个local方法的参数修改为你的WebStorm文件路径哦
![modifyLocal](https://github.com/Littledogdudu/ConsoleLog/blob/master/.github/readme/buildModifyLocal.png)

抱歉，暂时不支持jsp项目，该插件插入时可能只能插入在下一行，在没有语法错误的情况下，删除理论可以使用，具体原因是结构树都是XMLTOKEN，只能通过文本获取，麻烦且很多jsp项目也转到vue项目了，故不多做处理了  
可以在html代码上使用该插件，但是需要注意：插入表达式后出现语法错误时不会删除该语句，因为此时的PSI树结构是混乱的

> 灵感来源于vscode插件 [turbo console log](https://github.com/Chakroun-Anas/turbo-console-log)  
> 有新的主意可以在github上fork或提出issue或者发送到我的邮箱2378459785@qq.com哦
<!-- Plugin description end -->