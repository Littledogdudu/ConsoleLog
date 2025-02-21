<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# Console Log CHANGELOG
- zh_CN [简体中文](./CHANGELOG.md)
- en_US [English](./CHANGELOG.en_US.md)

## [Unreleased]

## [1.0.3] - 2025-02-19

### Fix
- For double quote escaping and ${xxx} replacement, see the replaceConsoleLog method of the InsertConsoleLogAction class.

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