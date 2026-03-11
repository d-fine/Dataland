import QualityAssurance from '@/components/pages/QualityAssurance.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataLksgData,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
} from '@clients/backend';
import { type QaReviewResponse, QaStatus } from '@clients/qaservice';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles';
import { buildDataAndMetaInformationMock } from '@sharedUtils/components/ApiResponseMocks.ts';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import router from '@/router';

/**
 * Picks a reporting period to filter for in the column filter of the datatable.
 * @param reportingPeriod
 * @param closeFilterMenu
 */
function chooseReportingPeriodFilter(reportingPeriod: string, closeFilterMenu = false): void {
  cy.contains('#qa-data-result th', 'REPORTING PERIOD')
    .should('be.visible')
    .within(() => {
      cy.get('button.p-datatable-column-filter-button').click();
    });
  cy.get('[data-test="reporting-period-filter"]').click();
  cy.get('.p-datepicker-year').contains(reportingPeriod).click();
  if (closeFilterMenu) {
    cy.contains('#qa-data-result th', 'REPORTING PERIOD').within(() => {
      cy.get('button.p-datatable-column-filter-button').click();
    });
  }
}

/**
 * Picks a framework to filter for in the column filter of the datatable.
 * @param framework
 * @param closeFilterMenu
 */
function chooseFrameworkFilter(framework: DataTypeEnum, closeFilterMenu = false): void {
  const frameworkHumanReadableName = humanizeStringOrNumber(framework);
  cy.contains('#qa-data-result th', 'FRAMEWORK')
    .should('be.visible')
    .within(() => {
      cy.get('button.p-datatable-column-filter-button').click();
    });
  cy.get('div[data-test="framework-picker"]').click().click();
  cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
  if (closeFilterMenu) {
    cy.contains('#qa-data-result th', 'FRAMEWORK').within(() => {
      cy.get('button.p-datatable-column-filter-button').click();
    });
  }
}

/**
 * Gets the first element in a column based on the column name.
 * This is used to check the order of the elements in the column after sorting.
 * @param columnName
 */
function getFirstElementInColumn(columnName: string): Cypress.Chainable<JQuery> {
  return cy
    .get('#qa-data-result thead tr')
    .first()
    .contains('th', columnName)
    .then(($th) => {
      const colIndex = $th.index();
      return cy.get('#qa-data-result tbody tr').first().find('td').eq(colIndex);
    });
}

/**
 * Moves the slider handle in the priority filter by a certain value.
 * This is used to set the priority filter to a certain value, since there is no input field to type the value into.
 * @param side
 * @param value
 */
function moveSliderHandleByValue(side: 'left' | 'right', value: number): void {
  if (side === 'left') {
    cy.get('[data-test="priority-slider"]').find('.p-slider-handle').first().as('leftHandle');
    for (let i = 0; i < value - 1; i++) {
      cy.get('@leftHandle').focus().type('{rightarrow}');
    }
  } else {
    cy.get('[data-test="priority-slider"]').find('.p-slider-handle').last().as('rightHandle');
    for (let i = 0; i < value - 1; i++) {
      cy.get('@rightHandle').focus().type('{leftarrow}');
    }
  }
}
type ReviewQueueElementOptions = {
  dataId: string;
  companyName: string;
  companyId: string;
  framework: string;
  reportingPeriod: string;
  datasetReviewId?: string;
  reviewerUserName?: string;
  reviewerUserId?: string;
  timestamp?: number;
  priorityOfAssociatedDataSourcing?: number;
};

/**
 * Builds a review queue element.
 * @param options to include in the element
 * @returns the element
 */
function buildReviewQueueElement(options: ReviewQueueElementOptions): QaReviewResponse {
  return {
    dataId: options.dataId,
    timestamp: options.timestamp ?? Date.now(),
    companyName: options.companyName,
    companyId: options.companyId,
    framework: options.framework,
    reportingPeriod: options.reportingPeriod,
    qaStatus: QaStatus.Pending,
    datasetReviewId: options.datasetReviewId,
    reviewerUserName: options.reviewerUserName,
    reviewerUserId: options.reviewerUserId,
    numberQaReports: 0,
    priorityOfAssociatedDataSourcing: options.priorityOfAssociatedDataSourcing ?? undefined,
  };
}

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
  cy.get('[data-test="companySearchBarWithMessage"]')
    .contains('Please type at least 3 characters')
    .should(isWarningExpectedToExist ? 'exist' : 'not.exist');
}

describe('Component tests for the Quality Assurance page', () => {
  let LksgFixture: FixtureData<LksgData>;
  let mockDataMetaInfoForActiveDataset: DataMetaInformation;

  before(function () {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      const preparedLksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      LksgFixture = getPreparedFixture('LkSG-date-2023-04-18', preparedLksgFixtures);
      cy.fixture('MetaInfoDataMocksForOneCompany.json').then((metaInfos: Array<DataMetaInformation>) => {
        mockDataMetaInfoForActiveDataset = metaInfos[0]!;
      });
    });
  });

  const keycloakMockWithUploaderAndReviewerRoles = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_REVIEWER],
  });

  const dataIdAlpha = crypto.randomUUID();
  const companyNameAlpha = 'Alpha Company AG';
  const companyIdAlpha = crypto.randomUUID();
  const timestampAlpha = 1700000000000;
  const reviewQueueElementAlpha = buildReviewQueueElement({
    dataId: dataIdAlpha,
    companyName: companyNameAlpha,
    companyId: companyIdAlpha,
    framework: DataTypeEnum.Lksg,
    reportingPeriod: '2022',
    priorityOfAssociatedDataSourcing: 3,
    timestamp: timestampAlpha,
  });

  const dataIdBeta = crypto.randomUUID();
  const companyNameBeta = 'Beta Corporate Ltd.';
  const companyIdBeta = crypto.randomUUID();
  const datasetReviewId = crypto.randomUUID();
  const reviewerUserName = 'Reviewer user name';
  const reviewerUserId = 'Revieweruserid';
  const timestampBeta = 1711110000000;
  const reviewQueueElementBeta = buildReviewQueueElement({
    dataId: dataIdBeta,
    companyName: companyNameBeta,
    companyId: companyIdBeta,
    framework: DataTypeEnum.Sfdr,
    reportingPeriod: '2023',
    datasetReviewId: datasetReviewId,
    reviewerUserName: reviewerUserName,
    reviewerUserId: reviewerUserId,
    timestamp: timestampBeta,
  });

  /**
   * Waits for the requests that occurs if all filters are reset and checks that both expected rows in the table
   * are there.
   */
  function assertUnfilteredDatatableState(): void {
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`);
  }

  /**
   * Mounts the qa assurance page with two mock elements in the review queue and asserts that they are shown.
   * @param createQueueWithThreeElements
   */
  function mountQaAssurancePageWithMocks(createQueueWithThreeElements = false): void {
    const mockReviewQueue: Array<QaReviewResponse> = [reviewQueueElementAlpha, reviewQueueElementBeta];
    if (createQueueWithThreeElements) {
      const reviewQueueElementGamma = buildReviewQueueElement({
        dataId: crypto.randomUUID(),
        companyName: 'Gamma Company GmbH',
        companyId: crypto.randomUUID(),
        framework: DataTypeEnum.Sfdr,
        reportingPeriod: '2024',
        priorityOfAssociatedDataSourcing: 10,
      });
      mockReviewQueue.push(reviewQueueElementGamma);
    }
    cy.intercept(`**/qa/datasets/queue`, mockReviewQueue).as('nonFilteredFetch');

    getMountingFunction({ keycloak: keycloakMockWithUploaderAndReviewerRoles })(QualityAssurance);
    assertUnfilteredDatatableState();
    cy.get('[data-test="qa-review-section"]').should('exist');
    cy.get('#qa-data-result tbody tr').should('have.length', mockReviewQueue.length);
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

  /**
   * Validates that no search is triggered if the company search term is too short and that a warning is show to users.
   */
  function validateNoSearchIfNotEnoughChars(): void {
    validateSearchStringWarning(false);
    validateAllMockSearchResults(true);

    typeIntoSearchBar('a');
    validateSearchStringWarning(true);
    validateAllMockSearchResults(true);

    typeIntoSearchBar('b');
    validateSearchStringWarning(true);
    validateAllMockSearchResults(true);

    cy.intercept(`**/qa/datasets/queue?companyName=abc`, []).as('searchForAbc');
    typeIntoSearchBar('c');
    validateSearchStringWarning(false);

    cy.wait('@searchForAbc');
    validateAllMockSearchResults(false);

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();
    validateSearchStringWarning(false);
    validateAllMockSearchResults(true);
  }

  it('Check QA-overview-page for filtering on company name', () => {
    mountQaAssurancePageWithMocks();

    validateNoSearchIfNotEnoughChars();

    const companySearchTerm = 'Alpha';
    cy.intercept(`**/qa/datasets/queue?companyName=${companySearchTerm}`, [reviewQueueElementAlpha]).as(
      'companyNameFilteredFetch'
    );

    cy.get(`input[data-test="companyNameSearchbar"]`).type(companySearchTerm);

    cy.wait('@companyNameFilteredFetch');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();

    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for filtering on framework', () => {
    mountQaAssurancePageWithMocks();
    chooseFrameworkFilter(DataTypeEnum.Lksg);

    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get('button.p-datatable-filter-clear-button').click();

    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for filtering on reporting period', () => {
    mountQaAssurancePageWithMocks();
    chooseReportingPeriodFilter('2022');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get('button.p-datatable-filter-clear-button').click();
    assertUnfilteredDatatableState();
  });

  it('Check QA-overview-page for sorting by reporting period', () => {
    mountQaAssurancePageWithMocks();
    cy.get('#qa-data-result thead tr').first().contains('th', 'REPORTING PERIOD').as('reportingPeriodHeader');

    cy.get('@reportingPeriodHeader').click();
    cy.get('@reportingPeriodHeader').should('have.attr', 'aria-sort', 'ascending');
    getFirstElementInColumn('REPORTING PERIOD').should('contain', '2022');

    cy.get('@reportingPeriodHeader').click();
    cy.get('@reportingPeriodHeader').should('have.attr', 'aria-sort', 'descending');
    getFirstElementInColumn('REPORTING PERIOD').should('contain', '2023');
  });

  it('Check QA-overview-page for sorting by priority', () => {
    mountQaAssurancePageWithMocks(true);
    cy.get('#qa-data-result thead tr').first().contains('th', 'PRIORITY').as('priorityHeader');

    cy.get('@priorityHeader').click();
    cy.get('@priorityHeader').should('have.attr', 'aria-sort', 'ascending');
    getFirstElementInColumn('PRIORITY').should('contain', '3');

    cy.get('@priorityHeader').click();
    cy.get('@priorityHeader').should('have.attr', 'aria-sort', 'descending');
    getFirstElementInColumn('PRIORITY').should('have.text', '');
  });

  it('Check QA-overview-page for sorting by submission date', () => {
    mountQaAssurancePageWithMocks();
    cy.get('#qa-data-result thead tr').first().contains('th', 'SUBMISSION DATE').as('submissionDateHeader');

    cy.get('@submissionDateHeader').click();
    cy.get('@submissionDateHeader').should('have.attr', 'aria-sort', 'ascending');
    getFirstElementInColumn('SUBMISSION DATE')
      .parent('tr')
      .within(() => {
        cy.contains('td', `${dataIdAlpha}`);
      });

    cy.get('@submissionDateHeader').click();
    cy.get('@submissionDateHeader').should('have.attr', 'aria-sort', 'descending');
    getFirstElementInColumn('SUBMISSION DATE')
      .parent('tr')
      .within(() => {
        cy.contains('td', `${dataIdBeta}`);
      });
  });

  it('Check QA-overview-page for filtering by priority', () => {
    mountQaAssurancePageWithMocks(true);
    cy.contains('#qa-data-result th', 'PRIORITY')
      .should('be.visible')
      .within(() => {
        cy.get('button.p-datatable-column-filter-button').click();
      });
    moveSliderHandleByValue('left', 4);
    cy.contains('td', `Gamma Company GmbH`);
    cy.contains('td', `Alpha Company AG`).should('not.exist');
    cy.contains('td', `Beta Corporate Ltd.`).should('not.exist');
    moveSliderHandleByValue('right', 2);
    cy.contains('td', `Gamma Company GmbH`).should('not.exist');
    cy.contains('td', `Alpha Company AG`).should('not.exist');
    cy.contains('td', `Beta Corporate Ltd.`).should('not.exist');
    cy.get('button.p-datatable-filter-clear-button').click();
    cy.get('#qa-data-result tbody tr').should('have.length', 3);
  });

  it('Check QA-overview-page for combined filtering', () => {
    mountQaAssurancePageWithMocks();

    const reportingPeriodToFilterFor = '2022';
    chooseReportingPeriodFilter(reportingPeriodToFilterFor);

    const companyNameSearchStringAlpha = 'Alpha';
    cy.intercept(`**/qa/datasets/queue?companyName=${companyNameSearchStringAlpha}`, [reviewQueueElementAlpha]).as(
      'combinedFilterFetchAlpha'
    );

    cy.get(`input[data-test="companyNameSearchbar"]`).type(companyNameSearchStringAlpha);

    cy.wait('@combinedFilterFetchAlpha');
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    const companyNameSearchStringBeta = 'Beta';
    cy.intercept(`**/qa/datasets/queue?companyName=${companyNameSearchStringBeta}`, []).as('combinedFilterFetchBeta');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear().type(companyNameSearchStringBeta);

    cy.wait('@combinedFilterFetchBeta');
    cy.contains('td', `${dataIdAlpha}`).should('not.exist');
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.contains('p', 'There are no unreviewed datasets on Dataland matching your filters');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();

    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get('button.p-datatable-filter-clear-button').click();
    assertUnfilteredDatatableState();
  });

  it('Check that priority tags are displayed as expected', () => {
    mountQaAssurancePageWithMocks();
    getMountingFunction({ keycloak: keycloakMockWithUploaderAndReviewerRoles })(QualityAssurance);
    assertUnfilteredDatatableState();

    cy.contains('td', `${dataIdAlpha}`)
      .parent('tr')
      .within(() => {
        cy.contains('[data-test="priority-tag"]', '3').should('exist');
      });

    cy.contains('td', `${dataIdBeta}`)
      .parent('tr')
      .within(() => {
        cy.contains('[data-test="priority-tag"]').should('not.exist');
      });
  });

  it('Check if dataset can be reviewed on the view page', () => {
    const mockDataMetaInfo: DataMetaInformation = {
      dataId: 'lksgTestDataId',
      companyId: 'testCompanyId',
      dataType: DataTypeEnum.Lksg,
      uploadTime: 1672531200,
      reportingPeriod: '2023',
      currentlyActive: false,
      qaStatus: QaStatus.Pending,
      ref: 'https://example.com',
    };
    const mockCompanyAssociatedLksgData: CompanyAssociatedDataLksgData = {
      companyId: mockDataMetaInfo.companyId,
      reportingPeriod: mockDataMetaInfo.reportingPeriod,
      data: LksgFixture.t,
    };
    const mockLksgDataAndMetaInfo: DataAndMetaInformation<LksgData> = buildDataAndMetaInformationMock(
      mockDataMetaInfo,
      LksgFixture.t
    );

    cy.intercept(`**/community/requests/user`, {});
    cy.intercept(`**/api/metadata?companyId=${mockDataMetaInfo.companyId}`, [mockDataMetaInfoForActiveDataset]);
    cy.intercept(`**/api/companies/${mockDataMetaInfo.companyId}/info`, LksgFixture.companyInformation);
    cy.intercept(`**/api/metadata/${mockDataMetaInfo.dataId}`, mockDataMetaInfo);
    cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/${mockDataMetaInfo.dataId}`, mockCompanyAssociatedLksgData).as(
      'fetchLksgData'
    );
    cy.intercept(`**/api/data/${DataTypeEnum.Lksg}/companies/${mockDataMetaInfo.companyId}*`, [
      mockLksgDataAndMetaInfo,
    ]);

    getMountingFunction({
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          companyId: mockDataMetaInfo.companyId,
          dataType: DataTypeEnum.Lksg,
          dataId: mockDataMetaInfo.dataId,
        },
      },
    })(ViewFrameworkData);
    cy.get('h1').contains(LksgFixture.companyInformation.companyName).should('be.visible');

    cy.get('#framework_data_search_bar_standard').should('not.exist');
    cy.get('[data-test="chooseFrameworkDropdown"]').should('not.exist');
    cy.get('a[data-test="goToNewDatasetButton"]').should('not.exist');

    cy.get('div[data-test="datasetDisplayStatusContainer"] span').contains('This dataset is currently pending review');

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Accepted}`, (request) => {
      request.reply(200, {});
    }).as('approveDataset');
    cy.get('button[data-test="qaApproveButton"]').should('exist').click();
    cy.wait('@approveDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-close-button').click();

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Rejected}`, (request) => {
      request.reply(200, {});
    }).as('rejectDataset');
    cy.get('button[data-test="qaRejectButton"]').should('exist').click();
    cy.wait('@rejectDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-close-button').click();
  });

  it('Check routing of Start Review button.', () => {
    cy.spy(router, 'push').as('routerPush');
    mountQaAssurancePageWithMocks();
    cy.intercept('POST', `**/qa/dataset-reviews/${dataIdAlpha}`, (request) => {
      request.reply(201, {
        companyId: companyIdAlpha,
        dataType: DataTypeEnum.Lksg,
      });
    }).as('createDatasetReview');
    cy.get('button[data-test="goToReviewButton"]').not(`:contains(${reviewerUserName})`).click();
    cy.get('[data-test="ok-confirmation-modal-button"]').should('be.visible').click();
    cy.wait('@createDatasetReview');
    cy.get('@routerPush').should('have.been.calledWith', `/companies/${companyIdAlpha}/frameworks/lksg/${dataIdAlpha}`);
  });

  it('Check routing of row click.', () => {
    cy.spy(router, 'push').as('routerPush');
    mountQaAssurancePageWithMocks();
    cy.contains('td', `${dataIdBeta}`).click();
    cy.get('@routerPush').should('have.been.calledWith', `/companies/${companyIdBeta}/frameworks/sfdr/${dataIdBeta}`);
  });

  it('Check display of error message.', () => {
    mountQaAssurancePageWithMocks();
    cy.intercept('POST', `**/qa/dataset-reviews/${dataIdAlpha}`, (request) => {
      request.reply(403, {
        errors: [
          {
            errorType: 'access-denied',
            summary: 'Access Denied',
            message:
              'Access to this resource has been denied. Please contact support if you believe this to be an error',
            httpStatus: 403,
          },
        ],
      });
    }).as('createDatasetReviewForbidden');
    cy.get('button[data-test="goToReviewButton"]').not(`:contains(${reviewerUserName})`).click();
    cy.get('[data-test="ok-confirmation-modal-button"]').should('be.visible').click();
    cy.wait('@createDatasetReviewForbidden');
    cy.get('[data-test="confirmation-modal-error-message"]')
      .should('be.visible')
      .and('contain', 'Access Denied: Access to this resource has been denied.');
  });

  it('Check QA-overview-page for RESET FILTERS button behaviour', () => {
    mountQaAssurancePageWithMocks();
    assertUnfilteredDatatableState();

    chooseReportingPeriodFilter('2022', true);
    chooseFrameworkFilter(DataTypeEnum.Lksg, true);
    cy.contains('#qa-data-result th', 'PRIORITY')
      .should('be.visible')
      .within(() => {
        cy.get('button.p-datatable-column-filter-button').click();
      });
    moveSliderHandleByValue('right', 2);
    const companySearchTerm = 'Alpha';
    cy.intercept(`**/qa/datasets/queue?companyName=${companySearchTerm}`, [reviewQueueElementAlpha]).as(
      'companyNameFilteredFetch'
    );
    cy.get(`input[data-test="companyNameSearchbar"]`).type(companySearchTerm);
    cy.wait('@companyNameFilteredFetch');

    cy.get('[data-test="reset-filters-button"]').click();
    cy.wait('@nonFilteredFetch');
    cy.get(`input[data-test="companyNameSearchbar"]`).should('have.value', '');
    validateSearchStringWarning(false);
    assertUnfilteredDatatableState();
  });
});
