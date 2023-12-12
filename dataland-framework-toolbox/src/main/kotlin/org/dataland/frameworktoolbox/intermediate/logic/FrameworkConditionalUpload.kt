package org.dataland.frameworktoolbox.intermediate.logic

import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkBooleanLambda

/**
 * A FrameworkConditional is a boolean formula that may take any data of the framework dataset
 * into account for it's calculation
 */
// TODO Emanuel: Do we really need an extra Conditional class for uploads?
// TODO: Why not use the same one for upload and view?
abstract class FrameworkConditionalUpload {

    /**
     * Always TRUE. Forever.
     */
    object AlwaysTrue : FrameworkConditionalUpload() {
        override fun toFrameworkBooleanLambdaUpload(): FrameworkBooleanLambda {
            return FrameworkBooleanLambda.TRUE
        }
    }

    /**
     * Always FALSE. Forever.
     */
    object AlwaysFalse : FrameworkConditionalUpload() {
        override fun toFrameworkBooleanLambdaUpload(): FrameworkBooleanLambda {
            return FrameworkBooleanLambda.FALSE
        }
    }

    /**
     * Convert this conditional expression to a TS-Lambda
     */
    abstract fun toFrameworkBooleanLambdaUpload(): FrameworkBooleanLambda
}
