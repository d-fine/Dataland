package org.dataland.frameworktoolbox.intermediate.logic

import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * A FrameworkConditional is a boolean formula that may take any data of the framework dataset
 * into account for it's calculation
 */
abstract class FrameworkConditional {
    object AlwaysTrue : FrameworkConditional() {
        override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda {
            return FrameworkBooleanLambda.TRUE
        }
    }

    object AlwaysFalse : FrameworkConditional() {
        override fun toFrameworkBooleanLambda(): FrameworkBooleanLambda {
            return FrameworkBooleanLambda.FALSE
        }
    }

    abstract fun toFrameworkBooleanLambda(): FrameworkBooleanLambda
}
