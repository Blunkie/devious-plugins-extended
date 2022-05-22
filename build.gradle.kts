import ProjectVersions.openosrsVersion

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    checkstyle
    kotlin("jvm") version "1.6.21"
}

project.extra["GithubUrl"] = "https://github.com/unethicalite/unethicalite-plugins-release"
project.extra["GithubUserName"] = "unethicalite"
project.extra["GithubRepoName"] = "unethicalite-plugins-release"

apply<BootstrapPlugin>()

allprojects {
    group = "dev.unethicalite"

    project.extra["PluginProvider"] = "unethicalite"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/WTvTbSPknJ"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    apply<JavaPlugin>()
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "checkstyle")

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.unethicalite.net/releases/")
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = uri("https://repo.unethicalite.net/snapshots/")
            mavenContent {
                snapshotsOnly()
            }
        }
    }

    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)

        compileOnly("com.openosrs:runelite-api:$openosrsVersion+")
        compileOnly("com.openosrs:runelite-client:$openosrsVersion+")
        compileOnly("com.openosrs:http-api:$openosrsVersion+")

        compileOnly(Libraries.guice)
        compileOnly(Libraries.javax)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "11"
        }
    }
}
