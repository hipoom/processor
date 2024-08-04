@file:Suppress("ConvertSecondaryConstructorToPrimary")

package com.hipoom.processor.transform.timing.scan

import com.android.build.api.transform.JarInput
import com.hipoom.processor.common.JarEntryReplaceUtils
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.common.scan.AbsInputScanner
import com.hipoom.processor.common.scan.filter.defaultFileFilter
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.transform.timing.TimingConfig
import com.hipoom.processor.transform.timing.editor.DirectoryClassHandler
import com.hipoom.processor.transform.timing.editor.JarEntryClassHandler
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
        get() = Logger.of(TRANSFORM_TIMING, "scan/scan")



    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun needScanDirectory(): Boolean {
        return timingConfig?.enable == true
    }

    override fun getFileFilter() = defaultFileFilter

    override fun onVisitFile(fileInputStream: FileInputStream, outputDirectory: File) {
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

    override fun onVisitAddedJar(jar: JarInput, outputJar: File) {
        super.onVisitAddedJar(jar, outputJar)
        handleJar(outputJar)
    }

    override fun onVisitNotChangedJar(jar: JarInput, outputJar: File) {
        super.onVisitNotChangedJar(jar, outputJar)
        handleJar(outputJar)
    }

    override fun onVisitChangedJar(jar: JarInput, outputJar: File) {
        super.onVisitChangedJar(jar, outputJar)
        handleJar(outputJar)
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 处理某一个 jar 文件。
     */
    private fun handleJar(outputJarFile: File) {
        logger.info("[handleJar] --->")

        // 遍历这个 jar 包中的所有类，并逐一处理
        val jarFile = JarFile(outputJarFile)
        val entries = jarFile.entries()

        // 保存需要替换的 entries
        val needReplaceEntries = HashMap<String, ByteArray>()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val bytes = onVisitEntry(jarFile, entry)
            if (bytes != null) {
                needReplaceEntries[entry.name] = bytes
            }
        }

        // 替换掉所有修改过的类
        JarEntryReplaceUtils.replaceAll(outputJarFile, needReplaceEntries)

        logger.flush()
    }

    private fun onVisitEntry(jarFile: JarFile, entry: JarEntry): ByteArray? {
        logger.info("处理 jar: " + jarFile.name)
        val entryName = entry.name
        if (entry.isDirectory) {
            logger.info("忽略文件夹")
            return null
        }

        if (!entryName.endsWith(".class")) {
            logger.info("不是一个 .class 文件，忽略")
            return null
        }

        logger.info("处理 entry: $entryName")

        val inputStream = jarFile.getInputStream(entry)

        val bytes = JarEntryClassHandler().handleClass(
            configs = timingConfig,
            inputStream = inputStream
        )

        inputStream.close()

        return bytes
    }
}