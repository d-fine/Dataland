import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type EutaxonomyFinancials202673Data } from '@clients/backend';
import { getCellValueContainer } from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { runFunctionBlockWithinPrimeVueModal } from '@sharedUtils/ElementChecks';
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

    getCellValueContainer('Fiscal year deviation').should('contain.text', expectedDisplay);
  });
  describe('Assurance display', () => {
    /**
     * Mounts the panel with a fixture whose assurance data point is replaced by the given one
     * @param assurance the assurance data point to display
     */
    function mountWithAssurance(assurance: Record<string, unknown> | undefined): void {
      const fixture = structuredClone(
        getPreparedFixture('All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company', preparedFixtures)
      );
      fixture.t.general = {
        ...fixture.t.general,
        general: { ...fixture.t.general?.general, assurance: assurance as never },
      };
      mountMLDTFrameworkPanelFromFakeFixture(
        DataTypeEnum.EutaxonomyFinancials202673,
        eutaxonomyFinancials202673ViewConfiguration,
        [fixture]
      );
    }

    it('does not show an Assurance Provider row and shows only the assurance value in the cell', () => {
      mountWithAssurance({ value: 'LimitedAssurance', provider: 'Test Provider', comment: 'Test comment' });
      cy.contains('Assurance Provider').should('not.exist');
      getCellValueContainer('Assurance')
        .should('contain.text', 'Limited Assurance')
        .and('not.contain.text', 'Test Provider')
        .and('not.contain.text', 'Assurance provided by');
    });

    it('shows the provider and the real comment but no quality in the data point modal', () => {
      mountWithAssurance({ value: 'ReasonableAssurance', provider: 'Test Provider', comment: 'Test comment' });
      getCellValueContainer('Assurance').find('a').click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.contains('tr', 'Provider').should('contain.text', 'Test Provider');
        cy.contains('tr', 'Comment').should('contain.text', 'Test comment');
        cy.contains('Quality').should('not.exist');
        cy.contains('Assurance provided by').should('not.exist');
      });
    });

    it('offers the modal when only a provider exists and displays assurance without comment correctly', () => {
      mountWithAssurance({ value: 'None', provider: 'Only Provider' });
      getCellValueContainer('Assurance').should('contain.text', 'None').find('a').click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.contains('tr', 'Provider').should('contain.text', 'Only Provider');
        cy.contains('tr', 'Comment').should('not.exist');
      });
    });

    it('displays assurance without provider and comment as plain value without a modal link', () => {
      mountWithAssurance({ value: 'LimitedAssurance' });
      getCellValueContainer('Assurance').should('contain.text', 'Limited Assurance').find('a').should('not.exist');
    });
  });
});
