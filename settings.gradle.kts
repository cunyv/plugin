pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()

        // 如果网络有问题，打开下面两个阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "plugin"

rootProject.name = "plugin"