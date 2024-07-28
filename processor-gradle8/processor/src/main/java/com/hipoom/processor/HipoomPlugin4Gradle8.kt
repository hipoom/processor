package com.hipoom.processor

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

/**
 * @author ZhengHaiPeng
 * @since 2024/7/27 23:43
 *
 */
class HipoomPlugin4Gradle8 : Plugin<Project> {

    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun apply(target: Project) {
        // 初始化全局配置
        project = target

        // 整个 plugin 对应的配置
        target.extensions.create(PLUGIN_NAME, PluginConfigs::class.java)

        // 这个插件，只能用于 app module，不能用于 java-library 或者 kotlin-library module.
        val hasAppPlugin: Boolean = target.plugins.hasPlugin(AppPlugin::class.java)
        if (!hasAppPlugin) {
            target.logger.log(LogLevel.ERROR, "hipoom 插件只能用于 application 类型的 module.")
            return
        }

        val appExtension = target.extensions.findByType(AppExtension::class.java)
        if (appExtension == null) {
            target.logger.log(LogLevel.ERROR, "从 project 中，没有找到 AppExtension 类型的扩展.")
            return
        }

        // 给全局缓存赋值
        com.hipoom.processor.appExtension = appExtension

    }
}