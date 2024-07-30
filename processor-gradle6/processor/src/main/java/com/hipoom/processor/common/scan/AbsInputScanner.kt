package com.hipoom.processor.common.scan

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.copyToOutput
import com.hipoom.processor.common.of
import com.hipoom.processor.common.scan.filter.FileFilter
import com.hipoom.processor.common.scan.filter.JarEntryFilter
import java.io.FileInputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:23 上午
 */
abstract class AbsInputScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger by lazy { Logger.of("main", "scan") }



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 扫描所有的输入，并将满足条件的 .class 文件，以及所有的 jar，通过模板方法回调给子类。
     * Directory 和 Jar 都会在回调完模板方法后，copy 到 output 中。
     */
    fun scanAndCopyToOutput(transformInvocation: TransformInvocation?) {
        // 遍历每一个输入
        transformInvocation?.inputs?.forEach { input ->
            // 遍历这个 input 下的所有文件夹。
            scanDirectory(transformInvocation, input)
            // 遍历每一个 Jar
            scanJar(transformInvocation, input)
        }

        logger.flush()
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 遍历文件夹。
     */
    private fun scanDirectory(transformInvocation: TransformInvocation, input: TransformInput) {
        // 创建遍历器
        val scanner = DirectoryScanner()
        scanner.fileFilter = getFileFilter()

        // 遍历每一个 directory
        input.directoryInputs.forEach { directory ->

            // 遍历这个 directory 下的所有文件
            scanner.scan(directory) {
                // 将遇到的每一个文件，返回给子类去处理
                onVisitFile(it)
            }

            // 将处理好的结果，拷贝到输出目的地
            directory.copyToOutput(transformInvocation.outputProvider)
        }
    }

    private fun scanJar(transformInvocation: TransformInvocation, input: TransformInput) {
        val scanner = JarScanner()
        scanner.notChangedJarHandler = { onVisitNotChangedJar(it) }
        scanner.addedJarHandler = { onVisitAddedJar(it) }
        scanner.removedJarHandler = { onVisitRemovedJar(it) }
        scanner.changedJarHandler = { onVisitChangedJar(it) }

        input.jarInputs.forEach { jar ->
            scanner.scan(
                jar = jar
            )
            jar.copyToOutput(transformInvocation.outputProvider)
        }
    }



    /* ======================================================= */
    /* Protected Methods                                       */
    /* ======================================================= */

    /**
     * 是否需要遍历文件夹。
     */
    protected open fun needScanDirectory(): Boolean {
        return true
    }

    /**
     * 遍历文件夹时的文件过滤器。
     */
    protected open fun getFileFilter(): FileFilter? {
        return null
    }

    /**
     * 遍历文件夹遇到文件时的回调。
     * 如果文件已经被 FileFilter 过滤了，则不会回调到这里。
     */
    protected open fun onVisitFile(fileInputStream: FileInputStream) {

    }

    /**
     * 是否需要遍历 Jar。
     */
    protected open fun needScanJar(): Boolean {
        return true
    }

    /**
     * 遍历 Jar 包时的 Entry 过滤器。
     */
    protected open fun getJarEntryFilter(): JarEntryFilter? {
        return null
    }

    /**
     * 遍历 jar 时，遇到了没有变化的 jar.
     * 如果文件已经被 JarFilter 过滤了，则不会回调到这里。
     */
    protected open fun onVisitNotChangedJar(jar: JarInput) {

    }

    /**
     * 遍历 jar 时，遇到了新增的 jar.
     * 如果文件已经被 JarFilter 过滤了，则不会回调到这里。
     */
    protected open fun onVisitAddedJar(jar: JarInput) {

    }

    /**
     * 遍历 jar 时，遇到了移除的 jar.
     * 如果文件已经被 JarFilter 过滤了，则不会回调到这里。
     */
    protected open fun onVisitRemovedJar(jar: JarInput) {

    }

    /**
     * 遍历 jar 时，遇到了有变化的 jar.
     * 如果文件已经被 JarFilter 过滤了，则不会回调到这里。
     */
    protected open fun onVisitChangedJar(jar: JarInput) {

    }

}