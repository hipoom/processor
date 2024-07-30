package com.hipoom.processor.common.scan

import com.android.build.api.transform.DirectoryInput
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.common.scan.filter.FileFilter
import java.io.File
import java.io.FileInputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:25 上午
 */
class DirectoryScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_TIMING, "DirScan")

    /**
     * 文件过滤器，返回 true 表示需要被拦截，不执行下一步。
     */
    var fileFilter: FileFilter? = null



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun scan(directory: DirectoryInput, handler: (FileInputStream)->Unit) {
        logger.info("处理文件夹：${directory.file.absolutePath}")

        // 遍历当前文件夹下的所有类，并逐一处理
        directory.file?.listFiles()?.forEach { file ->
            scanDirectory(file, handler)
        }
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * @param save 用于保存这个目录的结果.
     */
    private fun scanDirectory(file: File, handler: (FileInputStream)->Unit) {
        // 如果当前是个目录，递归遍历子文件
        if (file.isDirectory) {
            logger.info("处理子文件夹: ${file.absolutePath}")
            file.listFiles()?.forEach {
                scanDirectory(it, handler)
            }
            return
        }

        // 判断当前 file 是否需要忽略
        val filter = fileFilter
        if (filter != null && filter.needIgnore(file)) {
            return
        }

        // 获取文件流
        val inputStream = file.inputStream()

        // 处理这个类
        handler(inputStream)
    }
}