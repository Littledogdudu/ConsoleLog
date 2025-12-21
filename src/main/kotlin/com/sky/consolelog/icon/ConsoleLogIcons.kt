package com.sky.consolelog.icon

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * 图标加载工具类
 *
 * @author SkySource
 * @Date: 2025/10/6 13:43
 */
object ConsoleLogIcons {
    /** Console Log Side Window logos. */
    object ToolWindowIcons {
        @JvmField
        val UnComment: Icon = load("/icons/eye-closed-16.svg")

        @JvmField
        val Comment: Icon = load("/icons/eye-16-selected.svg")

        @JvmField
        val UnSpec: Icon = load("/icons/x-circle-16.svg")

        @JvmField
        val Spec: Icon = load("/icons/rocket-16-selected.svg")

        @JvmField
        val UnLevel: Icon = load("/icons/bookmark-slash-16.svg")

        @JvmField
        val Level: Icon = load("/icons/bookmark-16-selected.svg")

        @JvmField
        val Delete: Icon = load("/icons/click-to-delete.svg")

        @JvmField
        val Jump: Icon = load("/icons/click-to-jump.svg")

        @JvmField
        val nonVarSpec: Icon = load("/icons/non-var-spec.svg")
    }


    @JvmStatic
    fun load(path: String): Icon {
        return IconLoader.getIcon(path, ConsoleLogIcons::class.java)
    }
}