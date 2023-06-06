plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.sixtyninefourtwenty.persistentsearchview"
    compileSdk = 33

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.sixtyninefourtwenty"
            artifactId = "persistent-search-view-plus"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("persistent-search-view-plus")
                description.set("Fork of persistent-search-view with some cleanup")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("unbiaseduser")
                        name.set("Dang Quang Trung")
                        email.set("quangtrung02hn16@gmail.com")
                        url.set("https://github.com/unbiaseduser")
                    }
                }
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    coreLibraryDesugaring(libs.desuger.jdk.libs.minimal)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
