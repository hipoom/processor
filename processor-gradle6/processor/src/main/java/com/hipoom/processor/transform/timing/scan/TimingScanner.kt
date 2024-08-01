@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.hipoom.processor.transform.timing.scan

import com.android.build.api.transform.JarInput
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.common.scan.AbsInputScanner
import com.hipoom.processor.common.scan.filter.defaultFileFilter
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.transform.timing.TimingConfig
import com.hipoom.processor.transform.timing.editor.DirectoryClassHandler
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * @author ZhengHaiPeng
 * @since 2024/7/30 22:57
 *
 */
class TimingScanner(private val timingConfig: TimingConfig?): AbsInputScanner() {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */
    override val logger: Logger
        get() = Logger.of(TRANSFORM_TIMING, "scan")



    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun needScanDirectory(): Boolean {
        return timingConfig?.enable == true
    }

    override fun getFileFilter() = defaultFileFilter

    override fun onVisitFile(fileInputStream: FileInputStream, outputDirectory: File){
        DirectoryClassHandler.handleClass(
            configs         = timingConfig,
            fileInputStream = fileInputStream,
            outputDirectory = outputDirectory
        )
    }

    override fun needScanJar(): Boolean {
        return timingConfig?.enable == true
    }

    override fun getJarEntryFilter() = null

    override fun onVisitNotChangedJar(jar: JarInput) {
        super.onVisitNotChangedJar(jar)
        handleJar(jar)
    }

    override fun onVisitAddedJar(jar: JarInput) {
        super.onVisitAddedJar(jar)
        handleJar(jar)
    }

    override fun onVisitRemovedJar(jar: JarInput) {
        super.onVisitRemovedJar(jar)
    }

    override fun onVisitChangedJar(jar: JarInput) {
        super.onVisitChangedJar(jar)
        handleJar(jar)
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 处理某一个 jar 文件。
     */
    private fun handleJar(jar: JarInput) {
        // 遍历这个 jar 包中的所有类，并逐一处理
        val jarFile = JarFile(jar.file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            onVisitEntry(jarFile, entry)
        }
    }

    private fun onVisitEntry(jarFile: JarFile, entry: JarEntry) {
        val entryName = entry.name
        if (entry.isDirectory) {
            return
        }

        if (!entryName.endsWith(".class")) {
            return
        }

        logger.info("处理 entry: $entryName")

        val inputStream = jarFile.getInputStream(entry)


        inputStream.close()
    }
}