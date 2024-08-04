package com.hipoom.processor.common


import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import javassist.CtClass
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter


/**
 * 获取这个类的所有接口。
 */
internal fun CtClass.getInterfacesSafely(needTrackSuperClassForInterface: Boolean): Array<CtClass> {
    try {
        val selfInterfaces = this.interfaces
        // 如果需要递归查询父类
        return if (needTrackSuperClassForInterface) {
            selfInterfaces + (this.superclass?.getInterfacesSafely(true) ?: emptyArray())
        }
        // 如果不需要递归查询父类
        else {
            selfInterfaces
        }
    } catch (e: Exception) {
        e.printStackTrace()
        defLogger.info("获取接口时异常：\n" + e.toTraceString())
    }
    return emptyArray()
}

fun Throwable.toTraceString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return sw.toString()
}


fun DirectoryInput.copyToOutput(outputProvider: TransformOutputProvider): File {
    // 获取输出路径
    val output = outputProvider.getContentLocation(
        name,
        contentTypes,
        scopes,
        Format.DIRECTORY
    )

    // 将修改后的文件夹复制到输出路径
    FileUtils.copyDirectory(file, output)

    return output
}


fun JarInput.copyToOutput(outputProvider: TransformOutputProvider): File {
    // 获取输出路径
    val output = outputProvider.getContentLocation(
        file.absolutePath,
        contentTypes,
        scopes,
        Format.JAR
    )
    // 虽然不处理 Jar 中的类，但也需要复制到输出目录
    FileUtils.copyFile(file, output)

    return output
}

/**
 * 直接将所有 inputs 都拷贝到 outputs 中。
 * 不做修改。
 */
fun TransformInvocation.copyInputsToOutputs() {
    this.inputs?.forEach { input ->
        // 遍历每一个文件夹
        input.directoryInputs.forEach { directory ->
            directory.copyToOutput(this.outputProvider)
        }

        // 遍历每一个 Jar
        input.jarInputs.forEach { jar ->
            jar.copyToOutput(this.outputProvider)
        }
    }
}

fun measureTime(r: Runnable): Long {
    val begin = System.currentTimeMillis()
    r.run()
    val end = System.currentTimeMillis()
    return end - begin
}
