<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# Console Log CHANGELOG
- zh_CN [简体中文](./CHANGELOG.md)
- en_US [English](./CHANGELOG.en_US.md)

## [Unreleased]

## [1.2.1] - 2025-06-14

### Contains the initial version of the sidebar feature that was never released
- You can set whether to enable the sidebar in the plugin settings, and the sidebar will show all print expressions for the currently open file (enabled by default)
- The sidebar supports clicking to locate the print statement of the corresponding line
- Sidebar support for queries All Print Expressions/Print Expressions without Comments/Only Print Expressions that conform to the plug-in's specification format

### Feature
- Optimized the sidebar style
- The sidebar supports custom label queries
- i18n

## [1.2.0] - 2025-05-02

### Feature
- You can set whether to enable the sidebar in the plug-in settings, and the sidebar displays all print expressions of the currently open file (enabled by default)
- The sidebar supports clicking on the print statement that is located to the corresponding line.
- Sidebar support query All print expressions without annotations are only those that conform to the plug-in specification format

## [1.1.5] - 2025-07-04
- Automatically repairs line numbers when inserted
- You can use shortcut keys to repair line numbers (you don't need to set the default shortcut keys, you need to set them by yourself)
- When the variable is undefined near the cursor, a statement that determines the direction of data flow is generated, and the act of deleting/commenting/uncommenting is generated

## [1.1.4] - 2025-06-30

### Feature
- When the variable is undefined near the cursor, a statement that determines the direction of data flow is generated, and the act of deleting/commenting/uncommenting is generated

## [1.1.3] - 2025-05-16

### Feature
- In addition to single quotes, backticks (') support has been added

### Fixed
- The issue of inserting catch statement blocks, if judgment expressions, and some function parameters into the position is fixed

## [1.1.2] - 2025-05-15

### Feature
- Support adding file names and line numbers in formatted strings(proposed by yan.wt)

## [1.1.1] - 2025-05-12

### Fix
- Fix bug🐛: PSI JS type casting issue (proposed by igor.pavlenko)

## [1.1.0] - 2025-05-04

### Feature
- When selecting Chinese book, delete annotation only in the selected area

## [1.0.6] - 2025-04-17

### Fix
- Insert Position Fix

### Optimized
- Settings UI Optimization
- Optimized performance of inserting expressions in single and double quotation marks

## [1.0.5] - 2025-04-17

### Fix
- version 2025 compatible

## [1.0.4] - 2025-04-05

### Add
- 🎉 Single/double quotation mark toggle has been added to the settings
- Fixed the bug of the variable insertion position

## [1.0.3] - 2025-02-19

### Fix
- For double quote escaping and ${xxx} replacement, see the replaceConsoleLog method of the InsertConsoleLogAction class.
- The insertion position of the while and for loops is more accurate

### Verify
- Plug-in Version Compatibility Interval Extended Forward (Verified)

## [1.0.2] - 2025-02-18

### Add
- you can set the cursor whether automatically follow the end of console.log expression 
after insert the expression in setting😊

## [1.0.1] - 2025-02-18

### Fix
- can't select variable when the cursor after the last word in variable
- optimize insert position

## [1.0.0] - 2025-02-8

- control where console.log() is inserted when cursor in the chain call
- automatically capture the prefix, such as "this."
- replace the quota to the escape quota in the console.log text  

## [0.0.1] - 2025-02-03

### Add

KeyMap:
- Alt+1: insert console.log()
- Alt+2: delete all console.log()
- Alt+Shift+1: comment all console.log()
- Alt+Shift+2: uncomment all console.log()

- [x] you can go to settings to set what message you want to show