import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum } from '@clients/backend';
import { getCellValueContainer } from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { runFunctionBlockWithinPrimeVueModal } from '@sharedUtils/ElementChecks';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { eutaxonomyFinancialsViewConfiguration } from '@/frameworks/eutaxonomy-financials/ViewConfig';
import { eutaxonomyFinancials202673ViewConfiguration } from '@/frameworks/eutaxonomy-financials-2026-73/ViewConfig';
import { eutaxonomyNonFinancials202673ViewConfiguration } from '@/frameworks/eutaxonomy-non-financials-2026-73/ViewConfig';
import { eutaxonomyNonFinancialsViewConfiguration } from '@/frameworks/custom/EuTaxoNonFinancialsStaticViewConfig.ts';

type GeneralSection = { general?: Record<string, unknown> };
type AssuranceFixture = FixtureData<GeneralSection>;

const scenarios = [
  {
    name: 'EU Taxonomy Financials',
    fixtureFile: 'CompanyInformationWithEutaxonomyFinancialsPreparedFixtures',
    fixtureName: 'eutaxonomy-financials-dataset-with-no-null-fields',
    dataType: DataTypeEnum.EutaxonomyFinancials,
    viewConfig: eutaxonomyFinancialsViewConfiguration,
    nested: true,
  },
  {
    name: 'EU Taxonomy Financials (2026/73)',
    fixtureFile: 'CompanyInformationWithEutaxonomyFinancials202673PreparedFixtures',
    fixtureName: 'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company',
    dataType: DataTypeEnum.EutaxonomyFinancials202673,
    viewConfig: eutaxonomyFinancials202673ViewConfiguration,
    nested: true,
  },
  {
    name: 'EU Taxonomy Non-Financials (2026/73)',
    fixtureFile: 'CompanyInformationWithEutaxonomyNonFinancials202673PreparedFixtures',
    fixtureName: 'All-fields-defined-for-EU-Taxonomy-Non-Financials-202673-Framework-Company',
    dataType: DataTypeEnum.EutaxonomyNonFinancials202673,
    viewConfig: eutaxonomyNonFinancials202673ViewConfiguration,
    nested: false,
  },
  {
    name: 'EU Taxonomy Non-Financials',
    fixtureFile: 'CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures',
    fixtureName: 'all-fields-defined-for-eu-taxo-non-financials-alpha',
    dataType: DataTypeEnum.EutaxonomyNonFinancials,
    viewConfig: eutaxonomyNonFinancialsViewConfiguration,
    nested: false,
  },
];

scenarios.forEach(({ name, fixtureFile, fixtureName, dataType, viewConfig, nested }) => {
  describe(`Assurance display of the ${name} view page`, () => {
    let preparedFixtures: AssuranceFixture[];

    before(() => {
      cy.fixture(fixtureFile).then((json) => {
        preparedFixtures = json as AssuranceFixture[];
      });
    });

    /**
     * Mounts the view page with the assurance data point replaced by the given one
     * @param assurance the assurance data point to display
     */
    function mountWithAssurance(assurance: Record<string, unknown>): void {
      const fixture = structuredClone(getPreparedFixture(fixtureName, preparedFixtures));
      const general = (fixture.t.general ?? {}) as Record<string, unknown>;
      fixture.t.general = nested
        ? { ...general, general: { ...(general.general as object), assurance } }
        : { ...general, assurance };
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      mountMLDTFrameworkPanelFromFakeFixture(dataType, viewConfig as any, [fixture as any]);
    }

    it('shows no Assurance Provider row and only the assurance value in the cell', () => {
      mountWithAssurance({ value: 'LimitedAssurance', provider: 'Test Provider', comment: 'Test comment' });
      cy.contains('Assurance Provider').should('not.exist');
      getCellValueContainer('Assurance')
        .should('contain.text', 'Limited Assurance')
        .and('not.contain.text', 'Test Provider')
        .and('not.contain.text', 'Assurance provided by');
    });

    it('shows the provider and the real comment but no quality in the modal', () => {
      mountWithAssurance({ value: 'ReasonableAssurance', provider: 'Test Provider', comment: 'Test comment' });
      getCellValueContainer('Assurance').find('a').click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.contains('tr', 'Provider').should('contain.text', 'Test Provider');
        cy.contains('tr', 'Comment').should('contain.text', 'Test comment');
        cy.contains('Quality').should('not.exist');
      });
    });

    it('shows a plain value without modal link if neither provider nor comment exist', () => {
      mountWithAssurance({ value: 'LimitedAssurance' });
      getCellValueContainer('Assurance').should('contain.text', 'Limited Assurance').find('a').should('not.exist');
    });

    it('offers the modal for a provider-only assurance', () => {
      mountWithAssurance({ value: 'None', provider: 'Only Provider' });
      getCellValueContainer('Assurance').find('a').click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.contains('tr', 'Provider').should('contain.text', 'Only Provider');
        cy.contains('tr', 'Comment').should('not.exist');
      });
    });
  });
});
