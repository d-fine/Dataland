import QualityAssurance from '@/components/pages/QualityAssurance.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataPathwaysToParisData,
  type DataMetaInformation,
  DataTypeEnum,
  type PathwaysToParisData,
} from '@clients/backend';
import { type QaReviewResponse, QaStatus } from '@clients/qaservice';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { buildDataAndMetaInformationMock } from '@sharedUtils/components/ApiResponseMocks.ts';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';

describe('Component tests for the Quality Assurance page', () => {
  let p2pFixture: FixtureData<PathwaysToParisData>;
  let mockDataMetaInfoForActiveDataset: DataMetaInformation;

  before(function () {
    cy.fixture('CompanyInformationWithP2pPreparedFixtures').then(function (jsonContent) {
      const preparedP2pFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
      p2pFixture = getPreparedFixture('P2p-date-2023-04-18', preparedP2pFixtures);
      cy.fixture('MetaInfoDataMocksForOneCompany.json').then((metaInfos: Array<DataMetaInformation>) => {
        mockDataMetaInfoForActiveDataset = metaInfos[0];
      });
    });
  });

  const keycloakMockWithUploaderAndReviewerRoles = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_REVIEWER],
  });

  const dataIdAlpha = crypto.randomUUID();
  const companyNameAlpha = 'Alpha Company AG';
  const companyIdAlpha = crypto.randomUUID();
  const reviewQueueElementAlpha = buildReviewQueueElement(
    dataIdAlpha,
    companyNameAlpha,
    companyIdAlpha,
    DataTypeEnum.P2p,
    '2022'
  );

  const dataIdBeta = crypto.randomUUID();
  const companyNameBeta = 'Beta Corporate Ltd.';
  const companyIdBeta = crypto.randomUUID();
  const reviewQueueElementBeta = buildReviewQueueElement(
    dataIdBeta,
    companyNameBeta,
    companyIdBeta,
    DataTypeEnum.Sfdr,
    '2023'
  );

  /**
   * Builds a review queue element.
   * @param dataId to include
   * @param companyName to include
   * @param companyId to include
   * @param framework to include
   * @param reportingPeriod to include
   * @param timestamp to include
   * @returns the element
   */
  function buildReviewQueueElement(
    dataId: string,
    companyName: string,
    companyId: string,
    framework: string,
    reportingPeriod: string,
    timestamp: number = Date.now()
  ): QaReviewResponse {
    return {
      dataId: dataId,
      timestamp: timestamp,
      companyName: companyName,
      companyId: companyId,
      framework: framework,
      reportingPeriod: reportingPeriod,
      qaStatus: QaStatus.Pending,
    };
  }

  /**
   * Picks a reporting period to filter for in the date-picker.
   * @param reportingPeriod to click on in the date-picker
   */
  function clickOnReportingPeriod(reportingPeriod: string): void {
    cy.get('span[data-test="reportingPeriod"]').should('exist').click();
    cy.contains('span', reportingPeriod).should('exist').click();
    cy.get('span[data-test="reportingPeriod"]').should('exist').click();
  }

  /**
   * Waits for the requests that occurs if all filters are reset and checks that both expected rows in the table
   * are there.
   */
  function assertUnfilteredDatatableState(): void {
    cy.wait('@nonFilteredFetch');
    cy.wait('@nonFilteredNumberFetch');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`);
  }

  /**
   * Mounts the qa assurance page with two mock elements in the review queue and asserts that they are shown.
   */
  function mountQaAssurancePageWithMocks(): void {
    const mockReviewQueue = [reviewQueueElementAlpha, reviewQueueElementBeta];
    cy.intercept(`**/qa/datasets?chunkSize=10&chunkIndex=0`, mockReviewQueue).as('nonFilteredFetch');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets`, mockReviewQueue.length.toString()).as('nonFilteredNumberFetch');

    getMountingFunction({ keycloak: keycloakMockWithUploaderAndReviewerRoles })(QualityAssurance);
    assertUnfilteredDatatableState();
    cy.contains('span', 'Showing results 1-2 of 2.');
  }

  /**
   * Validates that no search is triggered if the company search term is too short and that a warning is show to users.
   */
  function validateNoSearchIfNotEnoughChars(): void {
    /**
     * Types the input into the search bar
     * @param input to type
     */
    function typeIntoSearchBar(input: string): void {
      cy.get(`input[data-test="companyNameSearchbar"]`).type(input);
    }

    /**
     * Checks if the warning is there or is not there, based on the boolean passed to the function.
     * @param isWarningExpectedToExist decides whether the warning is expected to be displayed or not
     */
    function validateSearchStringWarning(isWarningExpectedToExist: boolean): void {
      cy.contains('span', 'Please type at least 3 characters').should(isWarningExpectedToExist ? 'exist' : 'not.exist');
    }

    /**
     * Checks if the search results for an empty company name search string are currently displayed or not,
     * based on the boolean passed to the function.
     * @param searchResultsExpectedToBeDisplayed decides whether the search results are expected to be displayed or not
     */
    function validateAllMockSearchResults(searchResultsExpectedToBeDisplayed: boolean): void {
      cy.contains('td', `${dataIdAlpha}`).should(searchResultsExpectedToBeDisplayed ? 'exist' : 'not.exist');
      cy.contains('td', `${dataIdBeta}`).should(searchResultsExpectedToBeDisplayed ? 'exist' : 'not.exist');
    }

    validateSearchStringWarning(false);
    validateAllMockSearchResults(true);

    typeIntoSearchBar('a');
    validateSearchStringWarning(true);
    validateAllMockSearchResults(true);

    typeIntoSearchBar('b');
    validateSearchStringWarning(true);
    validateAllMockSearchResults(true);

    cy.intercept(`**/qa/datasets?companyName=abc&chunkSize=10&chunkIndex=0`, []).as('searchForAbc');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?companyName=abc`, '0').as('searchForAbcNumber');
    typeIntoSearchBar('c');
    validateSearchStringWarning(false);

    cy.wait('@searchForAbc');
    cy.wait('@searchForAbcNumber');
    validateAllMockSearchResults(false);

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();
    validateSearchStringWarning(false);
    validateAllMockSearchResults(true);
  }

  it('Check QA-overview-page for filtering on company name', () => {
    mountQaAssurancePageWithMocks();

    validateNoSearchIfNotEnoughChars();

    const companySearchTerm = 'Alpha';
    cy.intercept(`**/qa/datasets?companyName=${companySearchTerm}&chunkSize=10&chunkIndex=0`, [
      reviewQueueElementAlpha,
    ]).as('companyNameFilteredFetch');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?companyName=${companySearchTerm}`, '1').as(
      'companyNameFilteredNumberFetch'
    );

    cy.get(`input[data-test="companyNameSearchbar"]`).type(companySearchTerm);

    cy.wait('@companyNameFilteredFetch');
    cy.wait('@companyNameFilteredNumberFetch');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');
    cy.contains('span', 'Showing results 1-1 of 1.');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();

    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for filtering on framework', () => {
    mountQaAssurancePageWithMocks();

    const frameworkToFilterFor = DataTypeEnum.P2p;
    const frameworkHumanReadableName = humanizeStringOrNumber(frameworkToFilterFor);
    cy.intercept(`**/qa/datasets?dataTypes=${DataTypeEnum.P2p}&chunkSize=10&chunkIndex=0`, [
      reviewQueueElementAlpha,
    ]).as('frameworkFilteredFetch');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?dataTypes=${DataTypeEnum.P2p}`, '1').as(
      'frameworkFilteredNumberFetch'
    );

    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();

    cy.wait('@frameworkFilteredFetch');
    cy.wait('@frameworkFilteredNumberFetch');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();

    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for filtering on reporting period', () => {
    mountQaAssurancePageWithMocks();
    const reportingPeriodToFilterFor = '2022';
    cy.intercept(`**/qa/datasets?reportingPeriods=${reportingPeriodToFilterFor}&chunkSize=10&chunkIndex=0`, [
      reviewQueueElementAlpha,
    ]).as('repPeriodFilteredFetch');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?reportingPeriods=${reportingPeriodToFilterFor}`, '1').as(
      'repPeriodFilteredNumberFetch'
    );

    clickOnReportingPeriod(reportingPeriodToFilterFor);

    cy.wait('@repPeriodFilteredFetch');
    cy.wait('@repPeriodFilteredNumberFetch');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    clickOnReportingPeriod(reportingPeriodToFilterFor);

    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for combined filtering', () => {
    mountQaAssurancePageWithMocks();

    const reportingPeriodToFilterFor = '2022';
    cy.intercept(`**/qa/datasets?reportingPeriods=${reportingPeriodToFilterFor}&chunkSize=10&chunkIndex=0`, [
      reviewQueueElementAlpha,
    ]).as('repPeriodFilteredFetch');
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?reportingPeriods=${reportingPeriodToFilterFor}`, '1').as(
      'repPeriodFilteredNumberFetch'
    );

    clickOnReportingPeriod(reportingPeriodToFilterFor);

    const companyNameSearchStringAlpha = 'Alpha';
    cy.intercept(
      `**/qa/datasets?reportingPeriods=${reportingPeriodToFilterFor}&companyName=${companyNameSearchStringAlpha}&chunkSize=10&chunkIndex=0`,
      [reviewQueueElementAlpha]
    ).as('combinedFilterFetchAlpha');
    cy.intercept(
      `**/qa/numberOfUnreviewedDatasets?reportingPeriods=${reportingPeriodToFilterFor}&companyName=${companyNameSearchStringAlpha}`,
      '1'
    ).as('combinedFilterNumberFetchAlpha');

    cy.get(`input[data-test="companyNameSearchbar"]`).type(companyNameSearchStringAlpha);

    cy.wait('@combinedFilterFetchAlpha');
    cy.wait('@combinedFilterNumberFetchAlpha');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    const companyNameSearchStringBeta = 'Beta';
    cy.intercept(
      `**/qa/datasets?reportingPeriods=${reportingPeriodToFilterFor}&companyName=${companyNameSearchStringBeta}&chunkSize=10&chunkIndex=0`,
      []
    ).as('combinedFilterFetchBeta');
    cy.intercept(
      `**/qa/numberOfUnreviewedDatasets?reportingPeriods=${reportingPeriodToFilterFor}&companyName=${companyNameSearchStringBeta}`,
      '0'
    ).as('combinedFilterNumberFetchBeta');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear().type(companyNameSearchStringBeta);

    cy.wait('@combinedFilterFetchBeta');
    cy.wait('@combinedFilterNumberFetchBeta');
    cy.contains('td', `${dataIdAlpha}`).should('not.exist');
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.contains('p', 'There are no unreviewed datasets on Dataland matching your filters');
    cy.contains('span', 'No results for this search.');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();

    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    clickOnReportingPeriod(reportingPeriodToFilterFor);

    assertUnfilteredDatatableState();
  });

  it('Check if dataset can be reviewed on the view page', () => {
    const mockDataMetaInfo: DataMetaInformation = {
      dataId: 'p2pTestDataId',
      companyId: 'testCompanyId',
      dataType: DataTypeEnum.P2p,
      uploadTime: 1672531200,
      reportingPeriod: '2023',
      currentlyActive: false,
      qaStatus: QaStatus.Pending,
      ref: 'https://example.com',
    };
    const mockCompanyAssociatedP2pData: CompanyAssociatedDataPathwaysToParisData = {
      companyId: mockDataMetaInfo.companyId,
      reportingPeriod: mockDataMetaInfo.reportingPeriod,
      data: p2pFixture.t,
    };
    const mockP2pDataAndMetaInfo: DataAndMetaInformation<PathwaysToParisData> = buildDataAndMetaInformationMock(
      mockDataMetaInfo,
      p2pFixture.t
    );

    cy.intercept(`**/community/requests/user`, {});
    cy.intercept(`**/api/metadata?companyId=${mockDataMetaInfo.companyId}`, [mockDataMetaInfoForActiveDataset]);
    cy.intercept(`**/api/companies/${mockDataMetaInfo.companyId}/info`, p2pFixture.companyInformation);
    cy.intercept(`**/api/metadata/${mockDataMetaInfo.dataId}`, mockDataMetaInfo);
    cy.intercept(`**/api/data/${DataTypeEnum.P2p}/${mockDataMetaInfo.dataId}`, mockCompanyAssociatedP2pData).as(
      'fetchP2pData'
    );
    cy.intercept(`**/api/data/${DataTypeEnum.P2p}/companies/${mockDataMetaInfo.companyId}*`, [mockP2pDataAndMetaInfo]);

    getMountingFunction({
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          companyId: mockDataMetaInfo.companyId,
          dataType: DataTypeEnum.P2p,
          dataId: mockDataMetaInfo.dataId,
        },
      },
    })(ViewFrameworkData);
    cy.get('h1').contains(p2pFixture.companyInformation.companyName).should('be.visible');

    cy.get('#framework_data_search_bar_standard').should('not.exist');
    cy.get('#chooseFrameworkDropdown').should('not.exist');
    cy.get('a[data-test="gotoNewDatasetButton"]').should('not.exist');

    cy.get('div[data-test="datasetDisplayStatusContainer"] span').contains('This dataset is currently pending review');

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Accepted}`, (request) => {
      request.reply(200, {});
    }).as('approveDataset');
    cy.get('button[data-test="qaApproveButton"]').should('exist').click();
    cy.wait('@approveDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-header-close').click();

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Rejected}`, (request) => {
      request.reply(200, {});
    }).as('rejectDataset');
    cy.get('button[data-test="qaRejectButton"]').should('exist').click();
    cy.wait('@rejectDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-header-close').click();
  });
});
