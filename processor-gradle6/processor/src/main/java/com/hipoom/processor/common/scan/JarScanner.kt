package com.hipoom.processor.common.scan

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of

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
    var notChangedJarHandler: ((JarInput)->Unit)? = null

    /**
     * 新增的 Jar 包的处理逻辑。
     */
    var addedJarHandler: ((JarInput)->Unit)? = null

    /**
     * 移除的 Jar 包的处理逻辑。
     */
    var removedJarHandler: ((JarInput)->Unit)? = null

    /**
     * 有变化的 Jar 包的处理逻辑。
     */
    var changedJarHandler: ((JarInput)->Unit)? = null



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun scan(jar: JarInput) {
        logger.info("处理 Jar 包：${jar.file.absolutePath}")
        scanJar(jar)
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun scanJar(jar: JarInput) {
        when (jar.status) {
            // 未改变的 Jar 包，从缓存中读取其信息
            Status.NOTCHANGED -> {
                notChangedJarHandler?.invoke(jar)
                return
            }

            // 新增的 Jar 包，直接处理，并添加到缓存
            Status.ADDED -> {
                addedJarHandler?.invoke(jar)
                return
            }

            // 移除的 Jar 包
            Status.REMOVED -> {
                removedJarHandler?.invoke(jar)
                return
            }

            // 有改变的 Jar 包，从缓存移除，重新分析
            Status.CHANGED -> {
                changedJarHandler?.invoke(jar)
                return
            }
            else -> {}
        }
    }

}