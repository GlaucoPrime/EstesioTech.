plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.code.EstesioTech"
    // Usando SDK 34 (estável)
    compileSdk = 34

    defaultConfig {
        applicationId = "com.code.EstesioTech"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Adiciona o buildFeatures para ViewBinding (boa prática, embora não estejamos usando ainda)
    buildFeatures {
        viewBinding = true
    }
}

// ✅ ESTA É A VERSÃO CORRIGIDA QUE FUNCIONA
dependencies {

    // --- Dependências Padrão do Android ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- Dependências de UI (Interface do Usuário) ---

    // Fornece todos os componentes de Material Design (Botões, Caixas de Texto)
    implementation("com.google.android.material:material:1.11.0")

    // Adiciona o RecyclerView (para a lista de chat)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // --- Outras Dependências ---
    implementation("androidx.activity:activity-ktx:1.8.0") // Dependência 'activity'

    // --- Dependências de Teste (Não afetam o app) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}