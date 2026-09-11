import type { DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import ViewMultipleDatasetsDisplayBase from '@/components/generics/ViewMultipleDatasetsDisplayBase.vue';
import {
  type CompanyInformation,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  QaStatus,
} from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { buildDataAndMetaInformationMock } from '@sharedUtils/components/ApiResponseMocks.ts';

describe('Component test for the view multiple dataset display base component', () => {
  const reportingYear = 2023;
  const lksgMetaInfo: DataMetaInformation = {
    dataId: `dataset-a`,
    reportingPeriod: reportingYear.toString(),
    qaStatus: QaStatus.Accepted,
    currentlyActive: true,
    dataType: DataTypeEnum.Lksg,
    companyId: 'mock-company-id',
    uploadTime: 0,
    uploaderUserId: 'mock-uploader-id',
  };

  let preparedFixtureLksgData: LksgData;
  let companyInformation: CompanyInformation;

  before(function () {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      const preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
      const preparedFixture = getPreparedFixture('lksg-with-nulls-and-no-child-labor-under-18', preparedFixtures);
      preparedFixtureLksgData = preparedFixture.t;
      companyInformation = preparedFixture.companyInformation;
    });
  });

  /**
   * Mounts the component with the standard set of intercepts (company info, LkSG data, viewable dimensions),
   * plus a configurable mocked response for the non-sourceable dimensions search.
   * @param nonSourceableSearchResponse the mocked response for POST /api/non-sourceable/search
   */
  function mountComponentWithMocks(nonSourceableSearchResponse: object[]): void {
    const mockDataAndMetaInfo: DataAndMetaInformation<LksgData> = buildDataAndMetaInformationMock(
      lksgMetaInfo,
      preparedFixtureLksgData
    );

    cy.intercept('/community/requests/user', {});
    cy.intercept('/api/companies/mock-company-id/info', companyInformation);
    cy.intercept('/api/data/lksg/companies/mock-company-id*', [mockDataAndMetaInfo]);
    cy.intercept('POST', '/api/data-availability/viewable-dimensions/search', [
      { companyId: 'mock-company-id', dataType: DataTypeEnum.Lksg, reportingPeriod: reportingYear.toString() },
    ]).as('postViewableDimensionsSearch');
    cy.intercept(
      {
        method: 'GET',
        pathname: '/api/data/lksg/',
        query: { reportingPeriod: reportingYear.toString(), companyId: 'mock-company-id' },
      },
      {
        companyId: mockDataAndMetaInfo.metaInfo.companyId,
        reportingPeriod: mockDataAndMetaInfo.metaInfo.reportingPeriod,
        data: mockDataAndMetaInfo.data,
      }
    ).as('getLkSGData');
    cy.intercept('POST', '/api/non-sourceable/search', nonSourceableSearchResponse).as('postNonSourceableSearch');

    //@ts-ignore
    cy.mountWithPlugins(ViewMultipleDatasetsDisplayBase, {
      keycloak: minimalKeycloakMock({}),
      props: {
        companyId: mockDataAndMetaInfo.metaInfo.companyId,
        dataType: DataTypeEnum.Lksg,
        reportingPeriod: mockDataAndMetaInfo.metaInfo.reportingPeriod,
      },
    });

    cy.wait('@postViewableDimensionsSearch');
    cy.wait('@getLkSGData');
    cy.wait('@postNonSourceableSearch');
  }

  it('Check if the toggle of hidden fields works for empty and conditional fields', () => {
    mountComponentWithMocks([]);

    checkToggleEmptyFieldsSwitch('Number of Employees');
    cy.get('tr[data-section-label="Social"]');
    cy.get('tr[data-section-label="Child labor"]');
    cy.get('td[data-cell-label="Employee(s) Under 15"]').should('not.exist');
  });

  it('Shows the non-sourceability info text when non-sourceable periods are returned', () => {
    mountComponentWithMocks([
      { companyId: 'mock-company-id', dataType: DataTypeEnum.Lksg, reportingPeriod: '2023' },
      { companyId: 'mock-company-id', dataType: DataTypeEnum.Lksg, reportingPeriod: '2022' },
    ]);

    cy.get('[data-test="nonSourceabilityReportingYearInfo"]').should(
      'have.text',
      'For the following periods the data is non-sourceable: 2022, 2023'
    );
  });

  it('Does not show a non-sourceability info text when no periods are non-sourceable', () => {
    mountComponentWithMocks([]);

    cy.get('[data-test="nonSourceabilityReportingYearInfo"]').should('not.exist');
  });
});

/**
 * This function toggles the hide data button and checks whether a specific field is hidden or displayed.
 * @param toggledFieldName Name of a field which is toggled by the input switch
 */
function checkToggleEmptyFieldsSwitch(toggledFieldName: string): void {
  cy.get('span').contains(toggledFieldName).should('not.exist');
  cy.get('span[data-test="hideEmptyDataToggleCaption"]').should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('have.class', 'p-toggleswitch-checked').click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('not.have.class', 'p-toggleswitch-checked');
  cy.get('span').contains(toggledFieldName).should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('have.class', 'p-toggleswitch-checked');
  cy.get('span').contains(toggledFieldName).should('not.exist');
}
