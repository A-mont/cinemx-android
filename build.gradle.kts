plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

/**
 * Gating de calidad.
 *
 * `true` = ktlint y detekt reportan pero no rompen el build. Está así a propósito
 * para el primer build en Codemagic: con el gating estricto, un import fuera de
 * orden mata el pipeline antes de compilar y el log no llega a mostrar los errores
 * de compilación, que es lo que realmente interesa ver la primera vez.
 *
 * En cuanto el pipeline esté verde, ponerlo en `false` (o pasar
 * `-PqualityAdvisory=false`) para que la calidad vuelva a ser bloqueante.
 */
val qualityAdvisory: Boolean =
    (findProperty("qualityAdvisory") as String?)?.toBoolean() ?: true

subprojects {
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        ignoreFailures.set(qualityAdvisory)
        // Reporte legible en consola y XML para que el CI lo publique como artefacto.
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude { it.file.path.contains("/build/") }
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        ignoreFailures = qualityAdvisory
    }
}
