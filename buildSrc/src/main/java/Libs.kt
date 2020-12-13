object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildGradleVersion}"

    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"

    // AndroidX
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appCompatVersion}"
    const val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtxVersion}"
    const val legacySupport =
        "androidx.legacy:legacy-support-v4:${Versions.legacySupportVersion}"

    // Work
    const val workRuntime = "androidx.work:work-runtime:${Versions.workVersion}"
    const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${Versions.workVersion}"

    // Test
    const val testCore = "androidx.test:core:${Versions.testVersion}"
    const val testrunner = "androidx.test:runner:${Versions.testVersion}"
    const val testrules = "androidx.test:rules:${Versions.testVersion}"
    const val espressoCore =
        "androidx.test.espresso:espresso-core:${Versions.espressoVersion}"
    const val junitExt = "androidx.test.ext:junit:${Versions.junitExtVersion}"


    // Lifecycle
    const val viewModel =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifeCycleVersion}"
    const val extensions =
        "androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycleVersion}"
    const val liveData =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifeCycleVersion}"
    const val commonJava8 =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.lifeCycleVersion}"
    const val compiler =
        "androidx.lifecycle:lifecycle-compiler:${Versions.lifeCycleVersion}"
    const val viewModelSavedState =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifeCycleVersion}"

    // Room
    const val roomRuntime = "androidx.room:room-runtime:${Versions.roomVersion}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.roomVersion}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.roomVersion}"

    // Navigation
    const val navFragment =
        "androidx.navigation:navigation-ui:${Versions.navigationVersion}"
    const val navFragmentKtx =
        "androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}"
    const val navUi = "androidx.navigation:navigation-ui:${Versions.navigationVersion}"
    const val navUiKtx =
        "androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}"

    // App Startup
    const val startUp = "androidx.startup:startup-runtime:${Versions.startupVersion}"

    // Google
    const val material = "com.google.android.material:material:${Versions.materialVersion}"
    const val volley = "com.android.volley:volley:${Versions.volleyVersion}"

    // Junit
    const val junit4 = "junit:junit:${Versions.junitVersion}"

    // Coroutines
    const val couroutineAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.androidVersion}"


    // Retrofit
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    const val moshiConverter =
        "com.squareup.retrofit2:converter-moshi:${Versions.retrofitVersion}"

    // Moshi
    const val moshiCore = "com.squareup.moshi:moshi:${Versions.moshiVersion}"
    const val moshiCodegen =
        "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshiVersion}"

    // Okhttp
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp3Version}"
    const val loggingInterceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp3Version}"

    // Koin
    const val koinViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koinVersion}"

    // Debug
    const val timber = "com.jakewharton.timber:timber:${Versions.timberVersion}"
}
