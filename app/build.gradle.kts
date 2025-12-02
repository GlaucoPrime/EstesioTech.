plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // O plugin do Compose para Kotlin 2.0+// A versão foi removida aqui para usar a 2.0.21 que já está no classpath do projeto raiz
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // Defina o namespace aqui. Se seus arquivos Kotlin usam "package com.code.EstesioTech",
    // o namespace deve refletir onde o R será gerado.
    // Recomendo fortemente usar tudo minúsculo para evitar dores de cabeça com o R.
    // Mas se você já tem os arquivos com Maiúsculas, tente manter assim OU mudar para minúsculo.
    // O padrão e o recomendado é MINÚSCULO:
    namespace = "com.code.EstesioTech"

    compileSdk = 34

    defaultConfig {
        // O applicationId é o ID único do app na loja
        applicationId = "com.code.estesiotech"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        viewBinding = true // Mantendo caso ainda use algum XML legado
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // --- Dependências Padrão do Android ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- COMPOSE (UI Moderna) ---
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Ícones Estendidos (Setas, Send, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // --- UI Legada (XML/Views) - Necessário se ainda usar XML ou AppCompatActivity ---
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // O RecyclerView foi substituído por LazyColumn no Compose, mas se ainda usar algum XML com ele:
    // Use a versão 1.3.2 para compatibilidade com compileSdk 34
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // --- Testes ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
