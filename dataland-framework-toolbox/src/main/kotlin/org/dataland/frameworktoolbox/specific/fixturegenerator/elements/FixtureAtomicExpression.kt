package org.dataland.frameworktoolbox.specific.fixturegenerator.elements

class FixtureAtomicExpression(
    override val identifier: String,
    val typescriptExpression: String,
    override val imports: Set<String> = emptySet(),
) : FixtureGeneratorElement
