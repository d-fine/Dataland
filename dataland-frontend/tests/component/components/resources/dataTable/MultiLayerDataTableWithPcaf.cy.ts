import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type PcafData, type SfdrData } from '@clients/backend';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { getCellValueContainer } from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { convertSfdrDataToPcafData } from '@sharedUtils/PcafFromSfdr';
import { pcafViewConfiguration } from '@/frameworks/pcaf/ViewConfig';

describe('Component tests for the PCAF view page', () => {
  // There are no PCAF fake fixtures, as PCAF datasets cannot be uploaded. PCAF data is derived from SFDR fixtures.
  let pcafFixture: FixtureData<PcafData>;

  before(function () {
    cy.fixture('CompanyInformationWithSfdrPreparedFixtures').then(function (jsonContent) {
      const sfdrFixture = getPreparedFixture(
        'Sfdr-dataset-with-no-null-fields',
        jsonContent as Array<FixtureData<SfdrData>>
      );
      pcafFixture = { ...sfdrFixture, t: convertSfdrDataToPcafData(sfdrFixture.t) };
    });
  });

  it('Displays the PCAF data points of an SFDR dataset on the PCAF view page', () => {
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Pcaf, pcafViewConfiguration, [pcafFixture]);

    getCellValueContainer('Fiscal year end').should(
      'contain.text',
      pcafFixture.t.general!.general!.fiscalYearEnd!.value
    );
    for (const label of [
      'Main PCAF sector',
      'Company exchange status',
      'Book value of debt',
      'Market capitalization',
      'Enterprise value',
      'Total equity',
      'Total equity and debt',
      'Scope 1 GHG emissions',
      'Scope 2 GHG emissions',
      'Scope 3 GHG emissions',
    ]) {
      getCellValueContainer(label).should('not.be.empty');
    }
  });

  it('Does not display the fiscal year deviation, as it is not part of PCAF', () => {
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Pcaf, pcafViewConfiguration, [pcafFixture]);

    getCellValueContainer('Fiscal year end').should('exist');
    cy.contains('Fiscal year deviation').should('not.exist');
  });
});
