# 使用方法
如果你使用的 gradle 版本号在 6.x, 7.x, 8.x 内，那么你需要在根目录的 `build.gradle` 文件中，添加以下配置：

```groovy
buildscript {
    // 自定义 gradle 插件
    dependencies {
        // 注意把版本号，替换为你需要的版本
        classpath "com.hipoom:processor:0.0.1"
    }
}
```

# 版本号选择
如果你的 gradle 版本号是 8.x 的版本，你需要使用 `-for-Gradle8` 后缀的版本号，例如：
```groovy
"com.hipoom:processor:0.0.1-for-Gradle8"
```
