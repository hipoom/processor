package com.hipoom.processor.common.scan.filter;

import java.io.File;

import com.android.annotations.NonNull;

/**
 * @author ZhengHaiPeng
 * @since 2024/7/30 00:06
 */
public interface FileFilter {

    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 是否需要忽略遍历时遇到的文件。
     */
    boolean needIgnore(@NonNull File file);

}
