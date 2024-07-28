package com.hipoom.processor.common

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 11:03 上午
 */
object JarEntryReplaceUtils {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of("main", "replace")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 将 [jar] 包中的 [entryName] 文件替换为 [entryBytes].
     * 这个方法会修改 [jar] 文件的内容，而不是生成一个新的 jar.
     */
    fun replace(jar: File, entryName: String, entryBytes: ByteArray) {
        logger.info("[replace] jar: " + jar.absolutePath)
        logger.info("[replace] entryName: $entryName")

        // 临时文件
        val temp = File(jar.absolutePath + ".tmp")

        // 临时文件.JarOutputStream
        val output = JarOutputStream(temp.outputStream())

        val old = JarFile(jar)

        // 遍历旧 jar 中的每一个元素，如果名字不匹配，直接复制到 output，如果名字匹配 entry，则用传入的 entryBytes 复制到 output.
        val entries = old.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()

            // 找到后，
            if (entry.name == entryName) {
                // 写入新的 entry
                val newEntry = JarEntry(entryName)
                output.putNextEntry(newEntry)
                output.write(entryBytes)
                continue
            }

            // 拷贝元素
            output.putNextEntry(entry)
            old.getInputStream(entry).copyTo(output)
        }

        output.close()
        jar.delete() && temp.renameTo(jar)
    }

}