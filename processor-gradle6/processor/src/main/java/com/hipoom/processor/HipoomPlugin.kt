package com.hipoom.processor

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.hipoom.Files
import com.hipoom.processor.transform.registry.RegistryTransform
import com.hipoom.processor.transform.timing.TimingTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import java.io.File
import java.text.SimpleDateFormat

/**
 * 搜集指定 注解、 接口，并注册到 Registry 中的插件。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/27 20:53
 */
class HipoomPlugin : Plugin<Project> {

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
            log("hipoom 插件只能用于 application 类型的 module.")
            return
        }

        val appExtension = target.extensions.findByType(AppExtension::class.java)
        if (appExtension == null) {
            log("从 project 中，没有找到 AppExtension 类型的扩展.")
            return
        }

        // 给全局缓存赋值
        com.hipoom.processor.appExtension = appExtension

        // 注册 transform: RegistryTransform
        appExtension.registerTransform(RegistryTransform())
        log("注册 RegistryTransform 完成.")

        // 注册 transform: TimingTransform
        appExtension.registerTransform(TimingTransform())
        log("注册 TimingTransform 完成.")
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    @Suppress("SimpleDateFormat")
    private fun log(msg: String) {
        // DEBUG: 下面这段代码会把日志保存到本地文件
        val workspace = File("/Users/zhp/Workspace")
        if (!workspace.exists()) {
            return
        }
        val logFile = File(workspace, "hipoom-plugin-log.txt")
        Files.createNewFileIfNotExist(logFile)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val line = "[" + sdf.format(System.currentTimeMillis()) + "] " + msg + "\n"
        logFile.appendText(line)
        // DEBUG: 上面这段代码会把日志保存到本地文件

        project.logger.log(LogLevel.INFO, msg)
    }
}