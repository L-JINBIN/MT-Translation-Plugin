# MT翻译插件

MT管理器官方翻译插件源码

## 项目说明

项目使用 Android Studio 进行开发，在默认文件结构的基础上，删除多余的文件和配置，仅保留插件开发所需要的相关内容，并集成了一键打包功能。

插件源码文件在下面四个路径：

- plugin/src/main/resources/manifest.json
- plugin/src/main/java
- plugin/src/main/assets
- plugin/libs

如果需要重新开发自己的模块，清空上面 4 个路径下的文件内容即可。

## 一键打包

在 Android Studio 上方选择 BuildPlugin 并点击运行，或者在项目根目录下执行 ./gradlew plugin:buildPlugin，即可自动打包文件并保存到 plugin/build/outputs/mtp/plugin.mtp
