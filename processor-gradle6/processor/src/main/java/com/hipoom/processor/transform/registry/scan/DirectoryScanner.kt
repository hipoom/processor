package com.hipoom.processor.transform.registry.scan

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.TransformOutputProvider
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.copyToOutput
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.registry.InputResult
import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.registry.TRANSFORM_NAME
import java.io.File

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:25 上午
 */
object DirectoryScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_NAME, "DirScan")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun scanAndCopy2Output(configs: RegistryTransformConfig, directory: DirectoryInput, outputProvider: TransformOutputProvider): InputResult {
        logger.info("处理文件夹：${directory.file.absolutePath}")

        val res = InputResult()

        // 遍历当前文件夹下的所有类，并逐一处理
        directory.file?.listFiles()?.forEach { file ->
            scanDirectory(configs, file, res)
        }
        directory.copyToOutput(outputProvider)

        return res
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * @param save 用于保存这个目录的结果.
     */
    private fun scanDirectory(configs: RegistryTransformConfig, file: File, save: InputResult) {
        if (file.isDirectory) {
            logger.info("处理子文件夹: ${file.absolutePath}")
            file.listFiles()?.forEach {
                scanDirectory(configs, it, save)
            }
            return
        }

        // jar 包中可能有非代码文件，跳过这些非代码文件
        if (!file.path.endsWith(".class")) {
            return
        }

        if (file.path.endsWith("/R.class")) {
            return
        }

        // 获取文件流
        val inputStream = file.inputStream()

        // 处理这个类
        ClassHandler.handleClass(configs, inputStream, save)
    }
}