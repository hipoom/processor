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
     */
    boolean needIgnore(@NonNull JarEntry entry);
}
