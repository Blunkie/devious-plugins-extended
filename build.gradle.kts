import ProjectVersions.openosrsVersion

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    `java-library`
    checkstyle
}

project.extra["GithubUrl"] = "https://github.com/unethicalite/unethicalite-plugins-release"
project.extra["GithubUserName"] = "unethicalite"
project.extra["GithubRepoName"] = "unethicalite-plugins-release"

apply<BootstrapPlugin>()

subprojects {
    group = "dev.unethicalite"

    project.extra["PluginProvider"] = "unethicalite"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/WTvTbSPknJ"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    apply<JavaPlugin>()
    apply(plugin = "java-library")
//    apply(plugin = "checkstyle")

    repositories {
//        jcenter {
//            content {
//                excludeGroupByRegex("com\\.openosrs.*")
//            }
//        }
//
//        exclusiveContent {
//            forRepository {
//                mavenLocal()
//            }
//
//            filter {
//                includeGroupByRegex("com\\.openosrs.*")
//            }
//        }


        maven {
            url = uri("https://maven.pkg.github.com/unethicalite/unethicalite")
            credentials {
                username = "buracc"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)

        compileOnly("com.openosrs:runelite-api:$openosrsVersion+")
        compileOnly("com.openosrs:runelite-client:$openosrsVersion+")

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
    }
}
