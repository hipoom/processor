package com.hipoom.processor.transform.timing.scan

import com.android.build.api.transform.TransformInvocation
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.copyToOutput
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.timing.TRANSFORM_NAME
import com.hipoom.processor.transform.timing.TimingConfig

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:23 上午
 */
object InputScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger by lazy { Logger.of(TRANSFORM_NAME, "scan") }



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 扫描所有的输入，并将满足条件的类返回。
     */
    fun scanAndCopy2Output(config: TimingConfig, transformInvocation: TransformInvocation?) {
        // 遍历每一个输入
        transformInvocation?.inputs?.forEach { input ->
            // 遍历每一个文件夹
            val fileFilter = FileFilter { file ->
                // jar 包中可能有非代码文件，跳过这些非代码文件
                if (!file.path.endsWith(".class")) {
                    return@FileFilter true
                }
                // 所有的 R.class 都忽略掉
                if (file.path.endsWith("/R.class")) {
                    return@FileFilter true
                }
                return@FileFilter false
            }

            // TODO: 验证这里的 directory 会不会和子 directory 重复遍历
            input.directoryInputs.forEach { directory ->
                DirectoryScanner().apply {
                    this.fileFilter = fileFilter
                }.scan(directory) {
                    // TODO:
                }
                directory.copyToOutput(transformInvocation.outputProvider)
            }

            // 遍历每一个 Jar
            input.jarInputs.forEach { jar ->
                JarScanner().scan(
                    configs = config,
                    jar = jar
                )
                jar.copyToOutput(transformInvocation.outputProvider)
            }
        }

        logger.flush()
    }

}