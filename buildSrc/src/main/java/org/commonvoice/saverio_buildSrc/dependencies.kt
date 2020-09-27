package org.commonvoice.saverio_buildSrc

object Versions {
    const val ktlint = "0.38.1"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:4.0.1"

    object Kotlin {
        private const val version = "1.4.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0-alpha05"
        const val fragment = "androidx.fragment:fragment:1.0.0"
        const val activityKtx = "androidx.activity:activity-ktx:1.2.0-alpha08"
        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.1"
        const val coreKtx = "androidx.core:core-ktx:1.5.0-alpha02"
        const val legacySupport = "androidx.legacy:legacy-support-v4:1.0.0"

        object Work {
            private const val version = "2.4.0"
            const val runtime = "androidx.work:work-runtime:$version"
            const val runtimeKtx = "androidx.work:work-runtime-ktx:$version"
        }

        object Test {
            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"
            const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
            const val junitExt = "androidx.test.ext:junit:1.1.2"
        }

        object Lifecycle {
            private const val version = "2.2.0"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:$version"
            const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            const val viewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$version"
        }

        object Room {
            private const val version = "2.2.5"
            const val common = "androidx.room:room-common:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val compiler = "androidx.room:room-compiler:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Navigation {
            private const val version = "2.3.0"
            const val fragment = "androidx.navigation:navigation-ui:$version"
            const val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
            const val ui = "androidx.navigation:navigation-ui:$version"
            const val uiKtx = "androidx.navigation:navigation-ui-ktx:$version"
        }
    }

    object Google {
        const val material = "com.google.android.material:material:1.2.1"
        const val googleServicesPluginClassPath = "com.google.gms:google-services:4.3.3"
        const val ossLicencesPluginClassPath = "com.google.android.gms:oss-licenses-plugin:0.10.2"
        const val ossLicences = "com.google.android.gms:play-services-oss-licenses:17.0.0"
        const val firebaseCrashlyticsPluginClassPath =
            "com.google.firebase:firebase-crashlytics-gradle:2.0.0"
        const val firebaseCore = "com.google.firebase:firebase-core:17.2.3"
        const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics:17.0.0"
        const val firebasePerformance = "com.google.firebase:firebase-perf:19.0.7"
        const val firebasePerformancePluginClassPath = "com.google.firebase:perf-plugin:1.3.1"
        const val firebaseAnalytics = "com.google.firebase:firebase-analytics:17.5.0"
        const val firebaseMessaging = "com.google.firebase:firebase-messaging:20.2.4"
        const val gson = "com.google.code.gson:gson:2.8.6"
        const val auth = "com.google.android.gms:play-services-auth:18.1.0"
        const val volley = "com.android.volley:volley:1.1.1"
    }

    object Test {
        const val kotestVersion = "4.2.3"

        const val junit4 = "junit:junit:4.13"
        const val robolectric = "org.robolectric:robolectric:4.4"
        const val junit5Plugin = "de.mannodermaus.gradle.plugins:android-junit5:1.6.2.0"
        const val kotestMatchers = "io.kotest:kotest-assertions-core-jvm:${kotestVersion}"
        const val kotestProperties = "io.kotest:kotest-property-jvm:${kotestVersion}"
        const val kotlinTest = "org.jetbrains.kotlin:kotlin-test:1.4.0"
        const val kotlinCoroutineTest =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.8-1.4.0-rc"
        const val kotlinTestJunit = "org.jetbrains.kotlin:kotlin-test-junit5:1.4.0"
        const val mockk = "io.mockk:mockk:1.10.0"
        const val assertJ = "org.assertj:assertj-core:3.17.1"

        object Jupiter {
            private const val version = "5.6.1"

            // (Required) Writing and executing Unit Tests on the JUnit5 Platform
            const val api = "org.junit.jupiter:junit-jupiter-api:$version"
            const val engine = "org.junit.jupiter:junit-jupiter-engine:$version"

            // (Optional) If you need "Parameterized Tests"
            const val params = "org.junit.jupiter:junit-jupiter-params:$version"
        }
    }

    object Coroutines {
        private const val version = "1.3.8-1.4.0-rc"
        const val playservices = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$version"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object Square {
        object Retrofit {
            private const val version = "2.9.0"
            const val retrofit = "com.squareup.retrofit2:retrofit:$version"
            const val moshiConverter = "com.squareup.retrofit2:converter-moshi:$version"
        }

        object Moshi {
            private const val version = "1.10.0"
            const val core = "com.squareup.moshi:moshi:$version"
            const val codegen = "com.squareup.moshi:moshi-kotlin-codegen:$version"
        }
    }

    object Koin {
        const val viewModel = "org.koin:koin-androidx-viewmodel:2.1.6"
    }
}
