import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type EutaxonomyFinancials202673Data } from '@clients/backend';
import { getCellValueContainer } from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { eutaxonomyFinancials202673ViewConfiguration } from '@/frameworks/eutaxonomy-financials-2026-73/ViewConfig';

describe('Component tests for EutaxonomyFinancials202673Panel', () => {
  let preparedFixtures: Array<FixtureData<EutaxonomyFinancials202673Data>>;

  before(function () {
    cy.fixture('CompanyInformationWithEutaxonomyFinancials202673PreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancials202673Data>>;
    });
  });

  it('Check EU Taxonomy Financials (2026/73) view page renders and displays fiscal year deviation correctly', () => {
    const preparedFixture = getPreparedFixture(
      'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company',
      preparedFixtures
    );
    const fiscalYearDeviationValue = preparedFixture.t.general?.general?.fiscalYearDeviation?.value;
    const expectedDisplay = fiscalYearDeviationValue === 'NoDeviation' ? 'No Deviation' : fiscalYearDeviationValue;

    mountMLDTFrameworkPanelFromFakeFixture(
      DataTypeEnum.EutaxonomyFinancials202673,
      eutaxonomyFinancials202673ViewConfiguration,
      [preparedFixture]
    );

    getCellValueContainer('Fiscal Year Deviation').should('contain.text', expectedDisplay);
  });
});
