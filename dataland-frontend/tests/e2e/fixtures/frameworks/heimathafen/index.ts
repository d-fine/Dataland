// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
import fs from 'fs';
import { generateHeimathafenFixtures } from '@e2e/fixtures/frameworks/heimathafen/HeimathafenDataFixtures';
import { generateHeimathafenPreparedFixtures } from '@e2e/fixtures/frameworks/heimathafen/HeimathafenPreparedFixtures';
import { FAKE_FIXTURES_PER_FRAMEWORK } from '@e2e/fixtures/GenerateFakeFixtures';

/**
 * Generates and exports fake fixtures for the heimathafen framework
 */
function exportFixturesHeimathafen(): void {
  const companyInformationWithHeimathafenData = generateHeimathafenFixtures(FAKE_FIXTURES_PER_FRAMEWORK);
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithHeimathafenData.json',
    JSON.stringify(companyInformationWithHeimathafenData, null, '\t')
  );
  const preparedFixtureHeimathafenData = generateHeimathafenPreparedFixtures();
  fs.writeFileSync(
    '../testing/data/CompanyInformationWithHeimathafenPreparedFixtures.json',
    JSON.stringify(preparedFixtureHeimathafenData, null, '\t')
  );
}

export default exportFixturesHeimathafen;
