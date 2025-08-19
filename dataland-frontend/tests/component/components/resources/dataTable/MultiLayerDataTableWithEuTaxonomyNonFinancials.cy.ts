import ShowMultipleReportsBanner from '@/components/resources/frameworkDataSearch/ShowMultipleReportsBanner.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import {
  Activity,
  type CurrencyDataPoint,
  DataTypeEnum,
  type EuTaxonomyActivity,
  type EuTaxonomyAlignedActivity,
  type EutaxonomyNonFinancialsCapex,
  type EutaxonomyNonFinancialsData,
} from '@clients/backend';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { roundNumber } from '@/utils/NumberConversionUtils';
import { formatAmountWithCurrency } from '@/utils/Formatter';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { eutaxonomyNonFinancialsViewConfiguration } from '@/frameworks/eutaxonomy-non-financials/ViewConfig';
import { type FixtureData } from '@sharedUtils/Fixtures';
import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { runFunctionBlockWithinPrimeVueModal } from '@sharedUtils/ElementChecks';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component test for the Eu-Taxonomy-Non-Financials view page', () => {
  let fixturesForTests: FixtureData<EutaxonomyNonFinancialsData>[];

  let betaCapex: EutaxonomyNonFinancialsCapex;

  let gammaCapex: EutaxonomyNonFinancialsCapex;
  let gammaCapexFirstAlignedActivity: EuTaxonomyAlignedActivity;
  let gammaCapexFirstNonAlignedActivity: EuTaxonomyActivity;
  let gammaCapexTotalAmount: CurrencyDataPoint;

  before(function () {
    cy.fixture('CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json').then(
      (preparedFixtures: FixtureData<EutaxonomyNonFinancialsData>[]) => {
        fixturesForTests = preparedFixtures.filter((it) =>
          [
            'all-fields-defined-for-eu-taxo-non-financials-alpha',
            'all-fields-defined-for-eu-taxo-non-financials-beta',
            'all-fields-defined-for-eu-taxo-non-financials-gamma',
          ].includes(it.companyInformation.companyName)
        );

        const revenueOfDatasetAlphaTotalAmount = assertDefined(fixturesForTests[0].t.revenue?.totalAmount);
        revenueOfDatasetAlphaTotalAmount.value = 0;
        revenueOfDatasetAlphaTotalAmount.currency = 'EUR';

        betaCapex = assertDefined(fixturesForTests[1].t.capex);

        gammaCapex = assertDefined(fixturesForTests[2].t.capex);
        const gammaCapexAlignedActivities = assertDefined(gammaCapex.alignedActivities);
        if (!gammaCapexAlignedActivities.value || gammaCapexAlignedActivities.value.length < 1) {
          throw new Error(
            'Aligned activities list for capex of gamma dataset needs at least one element for this test to make sense.'
          );
        }
        gammaCapexFirstAlignedActivity = gammaCapexAlignedActivities.value[0];
        gammaCapexFirstAlignedActivity.activityName = Activity.Afforestation;
        gammaCapexFirstAlignedActivity.substantialContributionToClimateChangeAdaptationInPercent = 0;

        const gammaCapexNonAlignedActivities = assertDefined(gammaCapex.nonAlignedActivities);
        if (!gammaCapexNonAlignedActivities.value || gammaCapexNonAlignedActivities.value.length < 1) {
          throw new Error(
            'Non-Aligned activities list for capex of gamma dataset needs at least one element for this test to make sense.'
          );
        }
        gammaCapexFirstNonAlignedActivity = gammaCapexNonAlignedActivities.value[0];
        gammaCapexFirstNonAlignedActivity.activityName = Activity.Education;
        assertDefined(gammaCapexFirstNonAlignedActivity.share).relativeShareInPercent = 0;

        gammaCapexTotalAmount = assertDefined(gammaCapex.totalAmount);
      }
    );
  });

  it('Check if the view page for non-financials displays data correctly in its custom fields', () => {
    mountMLDTFrameworkPanelFromFakeFixture(
      DataTypeEnum.EutaxonomyNonFinancials,
      eutaxonomyNonFinancialsViewConfiguration,
      fixturesForTests
    ).then(() => {
      const betaTotalAlignedCapexPercentage = roundNumber(
        assertDefined(betaCapex.alignedShare?.relativeShareInPercent?.value),
        2
      );
      const gammaCapexTotalAmountFormattedString = formatAmountWithCurrency({
        amount: assertDefined(gammaCapexTotalAmount.value),
        currency: assertDefined(gammaCapexTotalAmount.currency),
      });
      const gammaTotalAlignedCapexAbsoluteShareString = formatAmountWithCurrency({
        amount: assertDefined(gammaCapex.alignedShare?.absoluteShare?.value),
        currency: assertDefined(gammaCapex.alignedShare?.absoluteShare?.currency),
      });
      const gammaContributionToClimateChangeMitigation = roundNumber(
        assertDefined(gammaCapex.substantialContributionToClimateChangeMitigationInPercentEligible?.value),
        2
      );
      getSectionHead('Revenue').should('exist');
      getCellValueContainer('Total Amount', 0)
        .invoke('text')
        .should('match', /^0\s*EUR\s.*/);
      getSectionHead('Revenue').click();

      getCellValueContainer('Relative Share in Percent', 1)
        .invoke('text')
        .should('contains', `${betaTotalAlignedCapexPercentage} %`);

      getCellValueContainer('Absolute Share', 2)
        .invoke('text')
        .should('contains', `${gammaTotalAlignedCapexAbsoluteShareString}`);
      getCellValueContainer('Substantial Contribution to Climate Change Mitigation In Percent - Eligible', 2)
        .invoke('text')
        .should('contains', `${gammaContributionToClimateChangeMitigation} %`);
      getCellValueContainer('Total Amount', 2)
        .invoke('text')
        .should('match', new RegExp(`^${gammaCapexTotalAmountFormattedString}\\s.*$`));
      getCellValueContainer('Total Amount', 2).first().click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.contains('td', gammaCapexTotalAmountFormattedString).should('exist');
        cy.contains('td', assertDefined(humanizeStringOrNumber(gammaCapexTotalAmount.quality))).should('exist');
        cy.contains('td', assertDefined(gammaCapexTotalAmount.comment).toString()).should('exist');
        cy.get(`[data-test="download-link-${assertDefined(gammaCapexTotalAmount.dataSource).fileName}"]`).should(
          'exist'
        );
      });
      cy.get('body').type('{esc}');

      getCellValueContainer('Aligned Activities', 2).find('a[data-test=activityLink]').first().click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.get('tr')
          .contains('td', assertDefined(gammaCapexFirstAlignedActivity.activityName))
          .nextAll()
          .eq(4)
          .invoke('text')
          .should('match', /^0 %$/);
      });
      cy.get('body').type('{esc}');

      getCellValueContainer('Non-Aligned Activities', 2).find('a[data-test=activityLink]').first().click();
      runFunctionBlockWithinPrimeVueModal(() => {
        cy.get('tr')
          .contains('td', assertDefined(gammaCapexFirstNonAlignedActivity.activityName))
          .nextAll()
          .eq(2)
          .invoke('text')
          .should(
            'match',
            new RegExp(`^${assertDefined(gammaCapexFirstNonAlignedActivity.share).relativeShareInPercent}\\s.*$`)
          );
      });
    });
  });

  it('Checks if the reports banner and the corresponding modal is properly displayed', () => {
    const allReportingPeriods = fixturesForTests.map((it) => it.reportingPeriod);
    const allReports = fixturesForTests.map((it) => assertDefined(it.t.general?.referencedReports));
    const expectedLatestReportingPeriod = allReportingPeriods[0];
    const nameOfFirstReportOfExpectedLatestReportingPeriod = Object.keys(allReports[0])[0];
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          reports: allReports,
          reportingPeriods: allReportingPeriods,
        },
      },
    })(ShowMultipleReportsBanner).then(() => {
      cy.get(`[data-test="frameworkNewDataTableTitle"`).contains(
        `Data extracted from the company report. Company Reports (${expectedLatestReportingPeriod})`
      );
      cy.get(`[data-test="report-link-${nameOfFirstReportOfExpectedLatestReportingPeriod}"]`);

      cy.get(`[data-test="previousReportsLinkToModal"]`).contains('Previous years reports').click();

      runFunctionBlockWithinPrimeVueModal(() => {
        for (let i = 0; i < fixturesForTests.length; i++) {
          const reportingPeriodOfDataset = allReportingPeriods[i];
          const reportsForDataset = allReports[i];

          if (reportingPeriodOfDataset != expectedLatestReportingPeriod) {
            cy.get(`[data-test="previousReportsList"]`).contains(`Company Reports (${reportingPeriodOfDataset})`);
            for (const reportKey in reportsForDataset) {
              cy.get(`[data-test='report-link-${reportKey}']`).first().click();
              cy.get(`[data-test='download-link-${reportKey}']`).contains(reportKey);
              cy.get('button.p-dialog-close-button').last().click();
            }
          }
        }
      });
    });
  });
});
