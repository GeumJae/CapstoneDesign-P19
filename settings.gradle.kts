pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // settings.gradle.kts 안의 repositories 블록에 추가
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "capstone-mbti"
include(":app")
