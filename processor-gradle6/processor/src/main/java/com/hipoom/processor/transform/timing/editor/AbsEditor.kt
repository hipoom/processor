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

        log("needIgnoreClassWithName", "判断是否匹配白名单")
        val isMatchWhitelist = isMatchWhitelist(configs, className)
        // 如果没有命中白名单，则忽略这个类
        if (!isMatchWhitelist) {
            log("needIgnoreClassWithName", "没有命中白名单.")
            decreaseIndent()
            return true
        }

        log("needIgnoreClassWithName", "判断是否命中黑名单")
        val isMatchBlacklist = isMatchBlacklist(configs, className)
        if (isMatchBlacklist) {
            log("needIgnoreClassWithName", "命中黑名单，忽略这个类.")
            decreaseIndent()
            return true
        }

        // 对应这个功能的所有代码都不需要插桩。
        val isTimingRecord = className.startsWith("com.hipoom.performance.timing.")
        if (isTimingRecord) {
            log("needIgnoreClassWithName", "忽略 TimingRecorder 类.")
            decreaseIndent()
            return true
        }

        decreaseIndent()
        return false
    }

    /**
     * 是否匹配白名单。
     *
     * 如果没有配置白名单，或者白名单配置的是空，那么表明想要对所有类插桩。则返回 true;
     */
    private fun isMatchWhitelist(configs: TimingConfig?, className: String): Boolean {
        increaseIndent()

        // 如果没有配置白名单，那么等价于所有都要插桩。 所以想判断是否需要忽略，只需要在 whitelist 不为 null or empty 的时候判断。
        val whitelist = configs?.whitelist ?: return true

        // prefix 和 contains 都是空的时候，说明用户没有配置，没有配置则表示所有。
        if (whitelist.prefix.isNullOrEmpty() && whitelist.contains.isNullOrEmpty()) {
            log("isMatchWhitelist", "白名单中 prefix 和 contains 都是空的.")
            decreaseIndent()
            return true
        }

        // 如果配置了白名单，则需要匹配其中任意一个。
        val isMatchPrefix = whitelist.prefix?.any { className.startsWith(it) } ?: false
        if (isMatchPrefix) {
            log("isMatchWhitelist", "白名单中 prefix 命中.")
            decreaseIndent()
            return true
        }

        decreaseIndent()
        return configs.whitelist.contains?.any { className.contains(it) } ?: false
    }

    /**
     * 是否命中黑名单了。
     */
    private fun isMatchBlacklist(configs: TimingConfig?, className: String): Boolean {
        increaseIndent()

        // 前缀是否命中黑名单
        val isPrefixMatched = configs?.blacklist?.prefix?.any {
            className.startsWith(it)
        } ?: false

        if (isPrefixMatched) {
            log("isMatchBlacklist", "前缀符合忽略条件.")
            decreaseIndent()
            return true
        }

        // 是否命中关键字
        val isContainsKeyWord = configs?.blacklist?.contains?.any {
            className.contains(it)
        } ?: false

        if (isContainsKeyWord) {
            log("needIgnoreClassWithName", "包含需要忽略的关键字，符合忽略条件.")
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