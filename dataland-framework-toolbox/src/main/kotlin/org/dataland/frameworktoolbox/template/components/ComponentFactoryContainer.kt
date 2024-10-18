package org.dataland.frameworktoolbox.template.components

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A surrogate component for retrieving an autowired ordered list of TemplateComponentFactories
 */
@Component
class ComponentFactoryContainer(
    @Autowired val factories: List<TemplateComponentFactory>,
)
