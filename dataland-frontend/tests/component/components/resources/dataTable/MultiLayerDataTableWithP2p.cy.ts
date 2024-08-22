import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { DataTypeEnum, type PathwaysToParisData } from '@clients/backend';

import { convertDataModelToMLDTConfig } from '@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter';
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { p2pDataModel } from '@/components/resources/frameworkDataSearch/p2p/P2pDataModel';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { formatPercentageNumberAsString } from '@/utils/Formatter';
import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';

describe('Component test for P2pPanel', () => {
  let preparedFixtures: Array<FixtureData<PathwaysToParisData>>;
  const p2pDisplayConfiguration = convertDataModelToMLDTConfig(p2pDataModel) as MLDTConfig<PathwaysToParisData>;

  before(function () {
    cy.fixture('CompanyInformationWithP2pPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    });
  });

  it('Check P2p view page for company with one P2p data set', () => {
    const preparedFixture = getPreparedFixture('one-p2p-data-set-with-four-sectors', preparedFixtures);
    const p2pData = preparedFixture.t;
    const ccsTechnologyAdoptionInPercent = assertDefined(
      p2pData.ammonia?.decarbonisation?.ccsTechnologyAdoptionInPercent
    );
    const preCalcinedClayUsageInPercent = assertDefined(p2pData.cement?.material?.preCalcinedClayUsageInPercent);
    const driveMixPerFleetSegmentInPercentForSmallTrucks = assertDefined(
      p2pData.freightTransportByRoad?.technology?.driveMixPerFleetSegment?.SmallTrucks?.driveMixPerFleetSegmentInPercent
    );

    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.P2p, p2pDisplayConfiguration, [preparedFixture]);

    getCellValueContainer('Sectors').contains(`Show ${p2pData.general.general.sectors.length} values`).click();
    cy.get('.p-dialog').find('.p-dialog-title').should('have.text', 'Sectors');
    cy.get('td').contains('Ammonia').should('exist');
    cy.get('td').contains('Cement').should('exist');
    cy.get('td').contains('Livestock Farming').should('exist');
    cy.get('td').contains('Freight Transport by Road').should('exist');
    cy.get('.p-dialog').find('.p-dialog-header-icon').click();

    cy.get(`span.p-column-title`).should('contain.text', p2pData.general.general.dataDate.substring(0, 4));
    getCellValueContainer('Data Date').should('contain.text', p2pData.general.general.dataDate).should('be.visible');

    getSectionHead('General').eq(1).click();
    getCellValueContainer('Data Date', 0, false).should('not.be.visible');
    getSectionHead('General').eq(1).click();
    getCellValueContainer('Data Date').should('be.visible');

    getSectionHead('Ammonia').should('exist');
    getCellValueContainer('CCS technology adoption', 0, false).should('be.visible');
    getSectionHead('Decarbonisation').should('exist');
    getCellValueContainer('CCS technology adoption')
      .should('contain.text', formatPercentageNumberAsString(ccsTechnologyAdoptionInPercent))
      .should('be.visible');

    getSectionHead('Livestock farming').should('exist');
    getSectionHead('Animal feed').should('exist');
    cy.get('span[data-test=Report-Download-Policy]').next('i[data-test=download-icon]').should('be.visible');

    getSectionHead('Cement').should('exist');
    getSectionHead('Material').should('exist');
    getCellValueContainer('Pre-calcined clay usage').should(
      'contain.text',
      formatPercentageNumberAsString(preCalcinedClayUsageInPercent)
    );
    cy.get("em[title='Pre-calcined clay usage']").trigger('mouseenter', 'center');
    cy.get('.p-tooltip').should('be.visible').should('contain.text', 'Share of pre-calcined');
    getSectionHead('Cement').should('exist');

    getSectionHead('Freight transport by road').should('exist');
    getSectionHead('Technology').should('exist');
    getCellValueContainer('Drive mix per fleet segment').contains(`Show Drive mix per fleet segment`).click();
    cy.get('.p-dialog').contains(formatPercentageNumberAsString(driveMixPerFleetSegmentInPercentForSmallTrucks));
  });

  /**
   * This functions creates a list of six p2p-fixture-datasets from different reporting periods that range from
   * 2023 to 2028.
   * @param baseFixture the p2p fixture used as a basis for constructing the 6 mocked ones
   * @returns a list of six p2p-fixture-datasets with ascending years as reporting periods
   */
  function createListOfP2pFixturesWithSixDifferentReportingPeriods(
    baseFixture: FixtureData<PathwaysToParisData>
  ): FixtureData<PathwaysToParisData>[] {
    const fixtureDatasets: FixtureData<PathwaysToParisData>[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const reportingDate = `${reportingYear}-01-01`;
      const p2pData = structuredClone(baseFixture.t);
      p2pData.general.general.dataDate = reportingDate;
      fixtureDatasets.push({
        companyInformation: baseFixture.companyInformation,
        t: p2pData,
        reportingPeriod: reportingYear.toString(),
      });
    }
    return fixtureDatasets;
  }

  it('Check P2p view page for company with six P2p data sets reported in different years ', () => {
    const preparedFixture = getPreparedFixture('six-p2p-data-sets-in-different-years', preparedFixtures);
    const mockedData = createListOfP2pFixturesWithSixDifferentReportingPeriods(preparedFixture);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.P2p, p2pDisplayConfiguration, mockedData);

    cy.get('table').find(`tr:contains("Data Date")`).find('span').eq(6).get('span').contains('2023');

    for (let indexOfColumn = 1; indexOfColumn <= 6; indexOfColumn++) {
      cy.get(`span.p-column-title`)
        .eq(indexOfColumn)
        .should('contain.text', (2029 - indexOfColumn).toString());
    }
  });
});
