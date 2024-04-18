package org.dataland.frameworktoolbox.specific.fixturegenerator.elements

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A FixtureAtomicExpression as a pure TS expression that generates
 * fixtures for the referenced element
 */
class FixtureAtomicExpression(
    override val identifier: String,
    val typescriptExpression: String,
    override val imports: Set<TypeScriptImport> = emptySet(),
) : FixtureGeneratorElement
