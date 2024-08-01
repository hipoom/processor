package com.hipoom.processor.common.scan.filter;

import java.util.jar.JarEntry;

import com.android.annotations.NonNull;

/**
 * @author ZhengHaiPeng
 * @since 2024/7/30 00:20
 */
public interface JarEntryFilter {

    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 是否需要忽略遍历时遇到的 jar 包中的 Entry.
     *
     * @param entry 需要判断的 JarEntry.
     * @return 如果需要忽略，返回 true.
     */
    boolean needIgnore(@NonNull JarEntry entry);
}
