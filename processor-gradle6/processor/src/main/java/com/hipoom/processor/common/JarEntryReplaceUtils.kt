package com.hipoom.processor.common

import com.hipoom.processor.transform.timing.TimingTransform
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception
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

    private val logger = Logger.of("common", "replace")



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

        // 拼装成一个 map 走只有 1 个元素的批量替换
        replaceAll(jar = jar, needReplaceEntries = mapOf(entryName to entryBytes))
    }

    /**
     * 批量替换 [jar] 包中的所有 [needReplaceEntries] 元素。
     * 其中，[needReplaceEntries] 的 Key 是 entryName，Value 是 entryBytes.
     *
     * 这个方法会修改 [jar] 文件的内容，而不是生成一个新的 jar.
     */
    fun replaceAll(jar: File, needReplaceEntries: Map<String, ByteArray>) {
        logger.info("[replaceAll] jar: " + jar.absolutePath)

        needReplaceEntries.keys.joinToString { "\n" + it }.let {
            logger.info("[replaceAll]     需要替换的类包括:$it")
        }

        if (!jar.exists()) {
            logger.info("[replaceAll]     输入的文件不存在：$jar")
            return
        }

        // 临时文件
        val temp = File(jar.absolutePath + ".tmp")

        try {
            // 临时文件.JarOutputStream
            val output = JarOutputStream(temp.outputStream())

            val old = JarFile(jar)

            // 遍历旧 jar 中的每一个元素，如果名字不匹配，直接复制到 output，如果名字匹配 entry，则用传入的 entryBytes 复制到 output.
            val entries = old.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                // 如果 entry 需要被替换
                if (needReplaceEntries.containsKey(entry.name)) {
                    // 写入新的 entry
                    val newEntry = JarEntry(entry.name)
                    output.putNextEntry(newEntry)
                    needReplaceEntries[entry.name]?.let { output.write(it) }
                    continue
                }

                // 如果 entry 不需要被替换，拷贝元素
                output.putNextEntry(entry)
                old.getInputStream(entry).copyTo(output)
            }

            output.close()
            jar.delete() && temp.renameTo(jar)
        } catch (e: Exception) {
            val stringWriter = StringWriter()
            val writer = PrintWriter(stringWriter)
            e.printStackTrace(writer)
            logger.warn("[replaceAll]    【异常】：$stringWriter")
        }
    }
}