### App 权限检查 ###


**目录结构**
* app测试使用
* plugin为权限检查插件
* maven为plugin发布到本地的路径
* 根目录的permission.json用于放置客户端合法的权限


**用法**
* 项目根路径下的 builde.gradle `buildscrip添加maven{url uri('maven')}`
* 项目根路径下的 builde.gradle `dependencies{classpath 'com.dz:check.permission:1.0.1'}`
* app下的builde.gradle 添加 `apply plugin: 'com.plugin.permissions.safe'`
* app下的builde.gradle 添加 `permissionConfig {permissionPath  "${project.rootDir}${File.separator}permission.json"}'`
