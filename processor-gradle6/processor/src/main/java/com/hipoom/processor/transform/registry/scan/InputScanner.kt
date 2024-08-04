package com.hipoom.processor.transform.registry.scan

import com.android.build.api.transform.TransformInvocation
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.registry.InputResult
import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.registry.TRANSFORM_REGISTRY

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:23 上午
 */
object InputScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger by lazy { Logger.of(TRANSFORM_REGISTRY, "scan") }



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 扫描所有的输入，并将满足条件的类返回。
     */
    fun scan(config: RegistryTransformConfig, transformInvocation: TransformInvocation?): ScanResult {

        val res = ScanResult()

        // 遍历每一个输入
        transformInvocation?.inputs?.forEach { input ->

            // 遍历每一个文件夹
            input.directoryInputs.forEach { directory ->
                val one: InputResult = DirectoryScanner.scanAndCopy2Output(
                    configs = config,
                    directory = directory,
                    outputProvider = transformInvocation.outputProvider
                )
                res.annotation2Classes.mergeValueSet(one.annotation)
                res.interface2Classes.mergeValueSet(one.interfaces)
            }

            // 遍历每一个 Jar
            input.jarInputs.forEach { jar ->
                val one = JarScanner.scanAndCopy2Output(
                    configs = config,
                    jar = jar,
                    outputProvider = transformInvocation.outputProvider
                )
                res.annotation2Classes.mergeValueSet(one.annotation)
                res.interface2Classes.mergeValueSet(one.interfaces)
                res.jar2Res[jar] = one
            }

        }

        logger.flush()

        return res
    }

}