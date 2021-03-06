package org.catafratta.strukt.processor

import java.io.File
import javax.annotation.processing.AbstractProcessor


/**
 * Extends [AbstractProcessor] providing easier access to kapt-specific options.
 */
internal abstract class KaptProcessor : AbstractProcessor() {
    /**
     * The base path for files generated by this processor.
     */
    protected val kaptOutputPath: File by lazy {
        processingEnv.options[OPT_KAPT_GEN_PATH]?.let(::File)
            ?: throw IllegalArgumentException("Required option $OPT_KAPT_GEN_PATH not found")
    }

    override fun getSupportedOptions(): Set<String> =
        super.getSupportedOptions().toMutableSet().also { it.add(OPT_KAPT_GEN_PATH) }

    companion object {
        /**
         * Option name used by kapt to specify the path for generated files.
         */
        const val OPT_KAPT_GEN_PATH = "kapt.kotlin.generated"
    }
}
