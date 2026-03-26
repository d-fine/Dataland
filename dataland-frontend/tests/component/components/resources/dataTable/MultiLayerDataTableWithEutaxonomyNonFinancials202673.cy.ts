import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type EutaxonomyNonFinancials202673Data } from '@clients/backend';
import { getCellValueContainer } from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { eutaxonomyNonFinancials202673ViewConfiguration } from '@/frameworks/eutaxonomy-non-financials-2026-73/ViewConfig';

describe('Component tests for EutaxonomyNonFinancials202673Panel', () => {
  let preparedFixtures: Array<FixtureData<EutaxonomyNonFinancials202673Data>>;

  before(function () {
    cy.fixture('CompanyInformationWithEutaxonomyNonFinancials202673PreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyNonFinancials202673Data>>;
    });
  });

  it('Check EU Taxonomy Non-Financials (2026/73) view page renders and displays fiscal year deviation correctly', () => {
    const preparedFixture = getPreparedFixture(
      'All-fields-defined-for-EU-Taxonomy-Non-Financials-202673-Framework-Company',
      preparedFixtures
    );
    const fiscalYearDeviationValue = preparedFixture.t.general?.fiscalYearDeviation?.value;
    const expectedDisplay = fiscalYearDeviationValue === 'NoDeviation' ? 'No Deviation' : fiscalYearDeviationValue;

    mountMLDTFrameworkPanelFromFakeFixture(
      DataTypeEnum.EutaxonomyNonFinancials202673,
      eutaxonomyNonFinancials202673ViewConfiguration,
      [preparedFixture]
    );

    getCellValueContainer('Fiscal Year Deviation').should('contain.text', expectedDisplay);
  });
});
