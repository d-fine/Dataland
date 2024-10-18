package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.DecimalComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFails

class ComponentGroupApiImplTests {
    @Test
    fun `it should be possible to create new components dynamically`() {
        val componentGroup = DemoComponentGroupApiImpl()
        val dateComponent = componentGroup.create<DateComponent>("testDateComponent") {}
        assertContains(componentGroup.children, dateComponent)
    }

    @Test
    fun `component creation should fail if the identifier is in use`() {
        val componentGroup = DemoComponentGroupApiImpl()
        componentGroup.create<DateComponent>("testDateComponent") {}
        val exception =
            assertFails {
                componentGroup.create<DateComponent>("testDateComponent") {}
            }
        assertEquals("The identifier testDateComponent already exists.", exception.message)
    }

    @Test
    fun `component creation should fail if no suitable constructor is available`() {
        @Suppress("UnusedPrivateProperty")
        class ComponentBaseWithInvalidConstructor(
            identifier: String,
            parent: FieldNodeParent,
            additionalRequiredVariable: Boolean,
        ) : ComponentBase(identifier, parent)

        val componentGroup = DemoComponentGroupApiImpl()

        assertFails {
            componentGroup.create<ComponentBaseWithInvalidConstructor>("testDateComponent") {}
        }
    }

    @Test
    fun `it should be possible to edit components dynamically`() {
        val componentGroup = DemoComponentGroupApiImpl()
        val component =
            componentGroup.create<DecimalComponent>("testNumber") {
                constantUnitSuffix = "unit A"
            }
        componentGroup.edit<DecimalComponent>("testNumber") {
            constantUnitSuffix = "Unit B"
        }
        assertEquals("Unit B", component.constantUnitSuffix)
    }

    @Test
    fun `component editing should fail if the component does not exist`() {
        val componentGroup = DemoComponentGroupApiImpl()
        val exception =
            assertFails {
                componentGroup.edit<DecimalComponent>("testNumber") {}
            }
        assertEquals("Could not find the component with identifier testNumber.", exception.message)
    }

    @Test
    fun `component editing should fail if the component is of a different type`() {
        val componentGroup = DemoComponentGroupApiImpl()
        componentGroup.create<DecimalComponent>("testNumber") {}
        val exception =
            assertFails {
                componentGroup.edit<DateComponent>("testNumber") {}
            }
        assertEquals(
            "The component with identifier testNumber is of type class" +
                " org.dataland.frameworktoolbox.intermediate.components.DecimalComponent." +
                " Expected class org.dataland.frameworktoolbox.intermediate.components.DateComponent.",
            exception.message,
        )
    }

    @Test
    fun `it should be possible to delete components dynamically`() {
        val componentGroup = DemoComponentGroupApiImpl()
        componentGroup.create<DecimalComponent>("testNumber") {}
        componentGroup.delete<DecimalComponent>("testNumber")
        assert(componentGroup.children.toList().isEmpty())
    }

    @Test
    fun `deleting components should fail if the component does not exist`() {
        val componentGroup = DemoComponentGroupApiImpl()
        val exception =
            assertFails {
                componentGroup.delete<DecimalComponent>("testNumber")
            }
        assertEquals("Could not find the component with identifier testNumber.", exception.message)
    }

    @Test
    fun `deleting components should fail if the component has a different type`() {
        val componentGroup = DemoComponentGroupApiImpl()
        componentGroup.create<DecimalComponent>("testNumber") {}
        val exception =
            assertFails {
                componentGroup.delete<DateComponent>("testNumber")
            }
        assertEquals(
            "The component with identifier testNumber is of type class" +
                " org.dataland.frameworktoolbox.intermediate.components.DecimalComponent." +
                " Expected class org.dataland.frameworktoolbox.intermediate.components.DateComponent.",
            exception.message,
        )
    }
}
