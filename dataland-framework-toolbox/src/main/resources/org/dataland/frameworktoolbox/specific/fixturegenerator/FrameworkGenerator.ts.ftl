<#include "DataFixtureSectionMacro.ftl">
import { Generator } from "@e2e/utils/FakeFixtureUtils";
import {<#list generators as generator>${generator.className}<#sep>, </#sep></#list>} from "@clients/backend";

export class ${frameworkBaseName}Generator extends Generator {

<#list generators as generator>
    /**
    * Generates random ${generator.className} data
    * @returns random ${generator.className} data
    */
    ${generator.prefix}${generator.className}(): ${generator.className} {
        const dataGenerator = this;
        return <@dataFixtureSection generator.rootSection/>
    }

</#list>
}