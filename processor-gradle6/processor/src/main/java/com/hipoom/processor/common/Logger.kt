@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.hipoom.processor.common

import com.hipoom.processor.pluginBuildDirectory
import com.hipoom.processor.project
import org.gradle.api.logging.LogLevel
import java.io.File
import java.text.SimpleDateFormat

/**
 * 日志记录类。
 * 每一个 transform 可以有多个自己的 Logger 对象。
 * 通过 Logger.of() 方法创建 Logger 对象。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/27 21:27
 */
class Logger {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    companion object;

    private val file: File

    private val sb = StringBuilder()



    /* ======================================================= */
    /* Constructors or Instance Creator                        */
    /* ======================================================= */

    constructor(file: File) {
        this.file = file

        // 删除旧日志
        if (file.exists()) {
            file.delete()
        }

        // 新建日志文件
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
    }



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun info(msg: Any?): Logger {
        if (this != defLogger) {
            defLogger.info(msg)
        }

        val now = sdf.format(System.currentTimeMillis())
        synchronized(sb) {
            val content = "[$now] $msg"
            project.logger.log(LogLevel.INFO, content)
            sb.append(content).append("\n")
        }
        return this
    }

    fun warn(msg: Any?): Logger {
        if (this != defLogger) {
            defLogger.warn(msg)
        }

        val now = sdf.format(System.currentTimeMillis())
        synchronized(sb) {
            val content = "[$now] $msg"
            project.logger.log(LogLevel.WARN, content)
            sb.append(content).append("\n")
        }
        return this
    }

    fun flush() {
        synchronized(sb) {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.appendText(sb.toString())
            sb.clear()
        }
    }

}


/**
 * 缓存所有 Logger 对象。
 */
private val loggers = HashMap<String, Logger>()

@Suppress("SimpleDateFormat")
val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

/**
 * 创建，或者从缓存中取，对应的 Logger 对象。
 */
fun Logger.Companion.of(transformName: String, moduleName: String): Logger {
    val loggerKey = "$transformName-$moduleName"

    // 如果已存在，返回
    if (loggers.containsKey(loggerKey)) {
        return loggers[loggerKey]!!
    }

    // 如果首次调用，新建加入到缓存，并返回
    val logger = Logger(File(pluginBuildDirectory, "$transformName/log/$moduleName.txt"))
    loggers[loggerKey] = logger
    return logger
}

fun Logger.Companion.flushAll() {
    loggers.values.forEach { it.flush() }
    project.logger.log(LogLevel.INFO, "所有日志已被 flush.")
}

val defLogger = Logger.of("main", "main")