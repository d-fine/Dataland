<#macro dataFixtureSection sectionConfig>{
<#list sectionConfig.elements as element>
<@indent>${element.identifier}: <#if element.isSection()><@dataFixtureSection element/><#elseif element.isAtomicExpression()>${element.typescriptExpression}</#if></@indent>,
</#list>
}</#macro>
import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type ${frameworkIdentifier?cap_first}Data } from "@clients/backend";
import { ${frameworkIdentifier?cap_first}Generator } from "@e2e/fixtures/frameworks/${frameworkIdentifier}/${frameworkIdentifier?cap_first}Generator";
<#list imports as import>${import}
</#list>

/**
 * Generates a set number of ${frameworkIdentifier} fixtures
 * @param numFixtures the number of ${frameworkIdentifier} fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of ${frameworkIdentifier} fixtures
 */
export function generate${frameworkIdentifier?cap_first}Fixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<${frameworkIdentifier?cap_first}Data>[] {
  return generateFixtureDataset<${frameworkIdentifier?cap_first}Data>(
    () => generate${frameworkIdentifier?cap_first}Data(nullProbability),
    numFixtures,
    <#if reportingPeriodGetter??> (dataset) => ${reportingPeriodGetter},</#if>
  );
}

/**
 * Generates a random ${frameworkIdentifier} dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random ${frameworkIdentifier} dataset
 */
export function generate${frameworkIdentifier?cap_first}Data(nullProbability = DEFAULT_PROBABILITY): ${frameworkIdentifier?cap_first}Data {
  const dataGenerator = new ${frameworkIdentifier?cap_first}Generator(nullProbability);
  return <@dataFixtureSection rootSection/>
}
