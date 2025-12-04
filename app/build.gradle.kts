// Arquivo de Configuração do MÓDULO APP
// Aqui ficam as dependências e configurações específicas do aplicativo.

plugins {
    // Aplica o plugin Android
    id("com.android.application")

    // Aplica o plugin Kotlin
    id("org.jetbrains.kotlin.android")

    // Aplica o plugin do Google Services (Necessário para o Firebase funcionar)
    // Note que não colocamos versão aqui, pois ela foi definida no arquivo raiz (pai)
    id("com.google.gms.google-services")
}

android {
    // Namespace: Identificador único para geração de recursos (R.java)
    namespace = "com.code.EstesioTech"

    // SDK de Compilação: Usamos o 34 (Android 14)
    compileSdk = 34

    defaultConfig {
        // ID do Aplicativo na Loja (Deve ser único no mundo e tudo minúsculo)
        applicationId = "com.code.EstesioTech"

        minSdk = 24     // Android 7.0 (Abrange a maioria dos dispositivos)
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Suporte para vetores em versões antigas
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Minificação desativada para facilitar debug inicial (ative se for publicar)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configurações de compatibilidade Java (Kotlin 1.9 usa Java 1.8 por padrão)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Habilita recursos de Build
    buildFeatures {
        compose = true      // Habilita Jetpack Compose
        viewBinding = true  // Habilita ViewBinding (para códigos legados/XML)
    }

    // Configuração do Compilador Compose
    // IMPORTANTE: A versão 1.5.1 é a compatível com Kotlin 1.9.0
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // Evita conflitos de arquivos duplicados em bibliotecas
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ==========================================================
    // NÚCLEO ANDROID (Core)
    // ==========================================================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // ==========================================================
    // JETPACK COMPOSE (Interface Moderna)
    // ==========================================================
    // BOM (Bill of Materials): Controla as versões do Compose para não haver conflito
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Ícones Estendidos (Necessário para ícones como Bluetooth, Setas, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // ==========================================================
    // UI LEGADA (XML / Views)
    // Necessário se você ainda tiver layouts em XML ou usar AppCompatActivity
    // ==========================================================
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ==========================================================
    // FIREBASE (Nuvem / Backend)
    // ==========================================================
    // BOM do Firebase: Garante que Auth e Firestore usem versões compatíveis
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Dependências (sem versão, pois o BOM gerencia)
    implementation("com.google.firebase:firebase-auth")      // Login/Cadastro
    implementation("com.google.firebase:firebase-firestore") // Banco de Dados
    implementation("com.google.firebase:firebase-analytics") // Analytics (Padrão)

    // ==========================================================
    // TESTES (Unitários e Instrumentados)
    // ==========================================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Ferramentas de Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}