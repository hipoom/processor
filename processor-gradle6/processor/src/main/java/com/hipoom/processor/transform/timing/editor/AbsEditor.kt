package com.hipoom.processor.transform.timing.editor

import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.transform.timing.TimingConfig
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier

/**
 * @author ZhengHaiPeng
 * @since 2024/8/1 23:47
 *
 */
abstract class AbsEditor {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    protected val logger by lazy { Logger.of(TRANSFORM_TIMING, getLoggerName()) }

    private var indent = ""



    /* ======================================================= */
    /* Protected Methods                                       */
    /* ======================================================= */

    /**
     * 判断 [className] 对应的类，是否需要忽略掉。例如 android、 java、 kotlin 自带的一些类，不需要再插桩了。
     */
    protected fun needIgnoreClassWithName(configs: TimingConfig?, className: String): Boolean {
        increaseIndent()

        val isPrefixMatched = configs?.blacklist?.prefix?.any {
            className.startsWith(it)
        } ?: false

        if (isPrefixMatched) {
            log("needIgnoreClassWithName", "前缀符合忽略条件.")
            decreaseIndent()
            return true
        }

        val isContainsKeyWord = configs?.blacklist?.contains?.any {
            className.contains(it)
        } ?: false

        if (isContainsKeyWord) {
            log("needIgnoreClassWithName", "包含需要忽略的关键字，符合忽略条件.")
            decreaseIndent()
            return true
        }

        val isTimingRecord = className.startsWith("com.hipoom.performance.timing.TimingRecorder")
        if (isTimingRecord) {
            log("needIgnoreClassWithName", "忽略 TimingRecorder 类.")
            decreaseIndent()
            return true
        }

        decreaseIndent()
        return false
    }

    /**
     * @return 是否修改了 [ctMethod] .
     */
    protected fun onVisitMethod(ctClass: CtClass, ctMethod: CtMethod): Boolean {
        val name = ctMethod.name
        log("onVisitMethod", "处理: $name")
        increaseIndent()

        if (ctMethod.isEmpty) {
            log("onVisitMethod", "忽略空方法")
            decreaseIndent()
            return false
        }

        if (Modifier.isNative(ctMethod.modifiers)) {
            log("onVisitMethod", "忽略 native 方法")
            decreaseIndent()
            return false
        }

        if (ctClass.isFrozen) {
            ctClass.defrost()
        }

        // 开始编辑这个方法
        onEditMethod(ctClass, ctMethod)
        log("onVisitMethod", "插入完毕.")

        decreaseIndent()
        return true
    }

    protected fun increaseIndent() {
        indent = "$indent    "
    }

    protected fun decreaseIndent() {
        indent = indent.substring(0, indent.length - 4)
    }

    protected fun log(methodName: String, msg: String) {
        logger.info("$indent |-- [$methodName] $msg")
    }

    protected abstract fun getLoggerName(): String



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 给 [ctClass].[ctMethod] 方法插入代码。
     */
    private fun onEditMethod(ctClass: CtClass, ctMethod: CtMethod) {
        try {
            val paramTypesDes = ctMethod.parameterTypes.map { it.simpleName }.joinToString { it }.removeSuffix(", ")

            // <ClassName>.<MethodName>([<ParamType1>, <ParamType2>, ...])
            val methodDescription = ctClass.simpleName + "." + ctMethod.name + "(${paramTypesDes})"

            ctMethod.insertBefore("com.hipoom.performance.timing.TimingRecorder.push(\"${methodDescription}\");")
            ctMethod.insertAfter("com.hipoom.performance.timing.TimingRecorder.pop();")
        } catch (e: Exception) {
            log("onVisitMethod", "异常：" + e.message)
        }
    }
}