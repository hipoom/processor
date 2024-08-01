package com.hipoom.processor.common.scan

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import java.io.File

typealias OutputJarFile = File

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:28 上午
 *
 */
class JarScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of("main", "JarScan")

    /**
     * 没有变化的 Jar 包的处理逻辑。
     */
    var notChangedJarHandler: ((JarInput, OutputJarFile)->Unit)? = null

    /**
     * 新增的 Jar 包的处理逻辑。
     */
    var addedJarHandler: ((JarInput, OutputJarFile)->Unit)? = null

    /**
     * 移除的 Jar 包的处理逻辑。
     */
    var removedJarHandler: ((JarInput, OutputJarFile)->Unit)? = null

    /**
     * 有变化的 Jar 包的处理逻辑。
     */
    var changedJarHandler: ((JarInput, OutputJarFile)->Unit)? = null



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun scan(inputJar: JarInput, outputJar: File) {
        logger.info("处理 Jar 包：${inputJar.file.absolutePath}")
        scanJar(inputJar, outputJar)
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun scanJar(inputJar: JarInput, outputJar: File) {
        when (inputJar.status) {
            // 未改变的 Jar 包，从缓存中读取其信息
            Status.NOTCHANGED -> {
                notChangedJarHandler?.invoke(inputJar, outputJar)
                return
            }

            // 新增的 Jar 包，直接处理，并添加到缓存
            Status.ADDED -> {
                addedJarHandler?.invoke(inputJar, outputJar)
                return
            }

            // 移除的 Jar 包
            Status.REMOVED -> {
                removedJarHandler?.invoke(inputJar, outputJar)
                return
            }

            // 有改变的 Jar 包，从缓存移除，重新分析
            Status.CHANGED -> {
                changedJarHandler?.invoke(inputJar, outputJar)
                return
            }
            else -> {}
        }
    }

}