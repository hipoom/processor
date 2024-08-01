package com.hipoom.processor.transform.timing.config

/**
 * @author ZhengHaiPeng
 * @since 2024/7/29 21:43
 */
open class BlackListConfig {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    /**
     * 前缀。
     * 全类名满足以 [prefix] 开头的所有类，都不会参与插桩。
     */
    var prefix: Set<String>? = null

    /**
     * 全类名中，包含 [contians] 关键字的都不会参与插桩。
     */
    var contains: Set<String>? = null
}