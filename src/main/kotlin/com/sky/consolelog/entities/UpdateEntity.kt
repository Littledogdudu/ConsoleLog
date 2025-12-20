package com.sky.consolelog.entities

/**
 * 用于更新编辑器中指定位置文本的专用实体类
 *
 * 当前用于更新行数
 *
 * @author SkySource
 * @Date: 2025/7/3 19:33
 */
class UpdateEntity {
    constructor(startOffset: Int, endOffset: Int, text: String) {
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.text = text
    }

    var startOffset: Int = 0
    var endOffset: Int = 0
    var text: String = ""
}