plugins {
    alias(libs.plugins.android.application)
//    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.zephyrevents"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.zephyrevents"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.bumptech.glide:glide:4.16.0")


    testImplementation("org.mockito:mockito-core:5.23.0")  // Mockito framework
    testImplementation("org.mockito:mockito-inline:5.2.0")  // Allows mocking final classes
    implementation("com.google.firebase:firebase-messaging")  // Cloud messaging

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

//    // Use Navigation Graph?
//    implementation("androidx.navigation:navigation-fragment:2.9.7")
//    implementation("androidx.navigation:navigation-ui:2.9.7")

    // Splash screen api
    implementation("androidx.core:core-splashscreen:1.2.0")

    // QR Codes
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}

// Gemini 3.1 Pro Preview, Google AiStudio, "What is the best way to fix this issue? Adding:
//implementation(files("/Users/jasonhu/Library/Android/sdk/platforms/android-36/android.jar"))
//does work, though with a bunch of warnings. Is that recommended?"

val generateJavadoc by tasks.registering(Javadoc::class) {
    description = "Generates standard Javadoc for the app."
    group = "documentation"

    // 1. Set the source files
    source = fileTree("src/main/java")
    exclude("**/R.java", "**/BuildConfig.java")

    // 2. Output directory
    //destinationDir = file("$buildDir/reports/javadoc")
    destinationDir = file("${rootProject.projectDir}/../javadoc")

    // 3. Wait for the app to compile so dependencies are downloaded and generated
    dependsOn("assembleDebug")

    doFirst {
        // Dynamically grab the correct android.jar from your installed SDK
        val androidBootClasspath = android.bootClasspath

        // Grab all dependencies (AndroidX, Firebase, etc.) from the debug build
        val dependencyClasspath = project.files()
        android.applicationVariants.all {
            if (name == "debug") {
                dependencyClasspath.from(javaCompileProvider.get().classpath)
            }
        }

        // Tell the Javadoc tool where everything is
        classpath = project.files(androidBootClasspath, dependencyClasspath)
    }

    // Ignore missing tags or minor doc errors so the build doesn't fail
    (options as StandardJavadocDocletOptions).apply {
        isFailOnError = false
        addStringOption("Xdoclint:none", "-quiet")
    }
}
// Run ./gradlew generateJavadoc
