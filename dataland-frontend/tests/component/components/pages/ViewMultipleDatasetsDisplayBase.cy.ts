// @ts-nocheck
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import ViewMultipleDatasetsDisplayBase from '@/components/generics/ViewMultipleDatasetsDisplayBase.vue';
import { type DataMetaInformation, DataTypeEnum, type LksgData, QaStatus } from '@clients/backend';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import router from '@/router';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';

describe('Component test for the view multiple dataset display base component', () => {
  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it('Check if the toggle of hidden fields works for empty and conditional fields', () => {
    const preparedFixture = getPreparedFixture('lksg-with-nulls-and-no-child-labor-under-18', preparedFixtures);
    const mockDataAndMetaInfo: DataAndMetaInformation<LksgData> = constructCompanyApiResponse(preparedFixture.t);
    const companyInformation = preparedFixture.companyInformation;

    cy.intercept('/community/requests/user', {});
    cy.intercept('/api/companies/mock-company-id/info', companyInformation);
    cy.intercept('/api/data/lksg/companies/mock-company-id', [mockDataAndMetaInfo]);
    cy.intercept(`/api/data/lksg/dataset-a`, {
      companyId: mockDataAndMetaInfo.metaInfo.companyId,
      reportingPeriod: mockDataAndMetaInfo.metaInfo.reportingPeriod,
      data: mockDataAndMetaInfo.data,
    });
    cy.intercept(`/api/metadata?companyId=mock-company-id`, [mockDataAndMetaInfo.metaInfo]);

    cy.mountWithPlugins(ViewMultipleDatasetsDisplayBase, {
      keycloak: minimalKeycloakMock({}),
      props: {
        companyId: mockDataAndMetaInfo.metaInfo.companyId,
        dataType: DataTypeEnum.Lksg,
        reportingPeriod: mockDataAndMetaInfo.metaInfo.reportingPeriod,
        viewInPreviewMode: false,
      },
    });

    checkToggleEmptyFieldsSwitch('Number of Employees');
    cy.get('tr[data-section-label="Social"]');
    cy.get('tr[data-section-label="Child labor"]');
    cy.get('td[data-cell-label="Employee(s) Under 15"]').should('not.exist');
  });

  it('Check whether Edit Data button has dropdown with two different Reporting Periods', () => {
    const preparedFixture = getPreparedFixture('lksg-with-nulls-and-no-child-labor-under-18', preparedFixtures);
    const mockedData2024 = constructCompanyApiResponse(preparedFixture.t);
    mockedData2024.metaInfo.dataId = 'id-2024';
    mockedData2024.metaInfo.reportingPeriod = '2024';
    const mockedData2023 = constructCompanyApiResponse(preparedFixture.t);
    mockedData2023.metaInfo.dataId = 'id-2023';
    mockedData2023.metaInfo.reportingPeriod = '2023';
    cy.intercept('/community/requests/user', {});
    cy.intercept(`/api/companies/*/info`, preparedFixture.companyInformation);
    cy.intercept(`/api/data/lksg/companies/mock-company-id`, [mockedData2024, mockedData2023]);

    cy.spy(router, 'push').as('routerPush');
    cy.mountWithPlugins(ViewMultipleDatasetsDisplayBase, {
      keycloak: minimalKeycloakMock({ roles: [KEYCLOAK_ROLE_UPLOADER] }),
      router: router,
      props: {
        companyId: mockedData2023.metaInfo.companyId,
        dataType: DataTypeEnum.Lksg,
        viewInPreviewMode: false,
      },
    });
    cy.get('[data-test="editDatasetButton"').find('.material-icons-outlined').should('exist').click();
    cy.get('[data-test="select-reporting-period-dialog"')
      .should('exist')
      .get('[data-test="reporting-periods"')
      .last()
      .should('contain', '2024')
      .should('contain', '2023')
      .within(() => {
        cy.contains('2023').click();
      });

    cy.get('@routerPush').should(
      'have.been.calledWith',
      `/companies/mock-company-id/frameworks/lksg/upload?reportingPeriod=${mockedData2023.metaInfo.reportingPeriod}`
    );
  });
});

/**
 * Create a DataAndMetaInformation object for LksgData and a defined DataMetaInformation mock
 * @param baseDataset the lksg dataset
 * @returns a mocked DataAndMetaInformation object
 */
function constructCompanyApiResponse<T extends FrameworkData>(baseDataset: T): DataAndMetaInformation<T> {
  const reportingYear = 2023;
  const lksgData: T = structuredClone(baseDataset);
  lksgData.general.masterData.dataDate = `${reportingYear}-01-01`;
  const metaData: DataMetaInformation = {
    dataId: `dataset-a`,
    reportingPeriod: reportingYear.toString(),
    qaStatus: QaStatus.Accepted,
    currentlyActive: true,
    dataType: DataTypeEnum.Lksg,
    companyId: 'mock-company-id',
    uploadTime: 0,
    uploaderUserId: 'mock-uploader-id',
  };
  return { metaInfo: metaData, data: lksgData };
}

/**
 * This function toggles the hide data button and checks whether a specific field is hidden or displayed.
 * @param toggledFieldName Name of a field which is toggled by the input switch
 */
export function checkToggleEmptyFieldsSwitch(toggledFieldName: string): void {
  cy.wait(100);
  cy.get('span').contains(toggledFieldName).should('not.exist');
  cy.get('span[data-test="hideEmptyDataToggleCaption"]').should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('have.class', 'p-inputswitch-checked').click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('not.have.class', 'p-inputswitch-checked');
  cy.get('span').contains(toggledFieldName).should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').click();
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('have.class', 'p-inputswitch-checked');
  cy.get('span').contains(toggledFieldName).should('not.exist');
}
