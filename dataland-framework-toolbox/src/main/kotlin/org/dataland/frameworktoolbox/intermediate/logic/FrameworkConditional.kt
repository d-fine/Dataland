package org.dataland.frameworktoolbox.intermediate.logic

import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A FrameworkConditional is a boolean formula that may take any data of the framework dataset
 * into account for it's calculation
 */
abstract class FrameworkConditional {
    /**
     * Always TRUE. Forever.
     */
    object AlwaysTrue : FrameworkConditional() {
        override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda = FrameworkBooleanLambda.TRUE
    }

    /**
     * Always FALSE. Forever.
     */
    object AlwaysFalse : FrameworkConditional() {
        override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda = FrameworkBooleanLambda.FALSE
    }

    /**
     * Convert this conditional expression to a TS-Lambda
     */
    abstract fun toFrameworkBooleanLambda(): FrameworkBooleanLambda
}
