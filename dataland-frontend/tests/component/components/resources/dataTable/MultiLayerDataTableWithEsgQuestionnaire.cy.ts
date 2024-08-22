// @ts-nocheck
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataEsgQuestionnaireData,
  type DataMetaInformation,
  DataTypeEnum,
  type EsgQuestionnaireData,
} from '@clients/backend';

import { formatNumberToReadableFormat } from '@/utils/Formatter';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import MultiLayerDataTableFrameworkPanel from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { esgQuestionnaireViewConfiguration } from '@/frameworks/esg-questionnaire/ViewConfig';
import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';

describe('Component Test for the ESG Questionnaire view Page with its componenets', () => {
  let preparedFixtureForTest: FixtureData<EsgQuestionnaireData>;
  const companyId = 'mock-company-id';
  before(function () {
    cy.fixture('CompanyInformationWithEsgQuestionnairePreparedFixtures').then(function (jsonContent) {
      const preparedFixtures = jsonContent as Array<FixtureData<EsgQuestionnaireData>>;
      preparedFixtureForTest = getPreparedFixture('EsgQuestionnaire-dataset-with-no-null-fields', preparedFixtures);
    });
  });

  it('Check that on the Esg questionnaire view Page the rolling window component works properly', () => {
    cy.intercept(`/api/data/${DataTypeEnum.EsgQuestionnaire}/mock-data-id`, {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgQuestionnaireData);
    mountEsgQuestionnaireFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead('Umwelt').should('exist');
    getSectionHead('Treibhausgasemissionen').should('exist');
    getCellValueContainer('Treibhausgas-Berichterstattung und Prognosen').click();

    cy.get('div').contains('Historische Daten');
    cy.get('div').contains('Aktuelles Jahr');
    cy.get('div').contains('Prognosen');
    const modalDatasets =
      preparedFixtureForTest.t.umwelt?.treibhausgasemissionen?.treibhausgasBerichterstattungUndPrognosen?.yearlyData;
    for (const dataSetOfOneYear in modalDatasets) {
      cy.get('div').contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope1));
      cy.get('div').contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope2));
      cy.get('div').contains(formatNumberToReadableFormat(modalDatasets[dataSetOfOneYear].scope3));
    }
    cy.get('body').type('{esc}');
    getSectionHead('Energieverbrauch').should('exist');
    getCellValueContainer('Berichterstattung Energieverbrauch').children().should('not.have.text');
  });

  it('Check that on the ESG Questionnaire view Page the string for datatable component works properly', () => {
    cy.intercept(`/api/data/${DataTypeEnum.EsgQuestionnaire}/mock-data-id`, {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgQuestionnaireData);
    mountEsgQuestionnaireFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead('Unternehmensführung/ Governance').should('exist');
    getSectionHead('Sonstige').should('exist');
    getCellValueContainer('Wirtschaftsprüfer').contains(
      assertDefined(preparedFixtureForTest.t.unternehmensfuehrungGovernance?.sonstige?.wirtschaftspruefer)
    );
  });

  it('Check that on the ESG Questionnaire view Page the list base data point component works properly', () => {
    cy.intercept(`/api/data/${DataTypeEnum.EsgQuestionnaire}/mock-data-id`, {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgQuestionnaireData);
    mountEsgQuestionnaireFrameworkFromFakeFixture([preparedFixtureForTest]);
    getSectionHead('ESG Berichte').should('exist');
    getCellValueContainer('Aktuelle Berichte').click();
    cy.get('span').contains('Beschreibung des Berichts');
    const aktuelleBerichte = assertDefined(preparedFixtureForTest.t.allgemein?.esgBerichte?.aktuelleBerichte);

    for (const singleEsgBericht of aktuelleBerichte) {
      cy.get('div').contains(assertDefined(singleEsgBericht.value));
      if (singleEsgBericht.dataSource) {
        cy.get('div').contains(assertDefined(singleEsgBericht.dataSource.fileName));
      }
    }
    cy.get('div.p-dialog-content i[data-test="download-icon"]').should('have.length', aktuelleBerichte.length - 1);
  });
});

/**
 *
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset for the ESG Questionnaire framework
 * @param fixtureDatasetsForDisplay the datasets from the fixtures to mount
 * @returns the component mounting chainable
 */
function mountEsgQuestionnaireFrameworkFromFakeFixture(
  fixtureDatasetsForDisplay: Array<FixtureData<EsgQuestionnaireData>>
): Cypress.Chainable {
  const dummyCompanyId = 'mock-company-id';
  const convertedDataAndMetaInformation: Array<DataAndMetaInformation<EsgQuestionnaireData>> =
    fixtureDatasetsForDisplay.map((it, idx) => {
      const metaInformation: DataMetaInformation = {
        dataId: `data-id-${idx}`,
        companyId: dummyCompanyId,
        dataType: DataTypeEnum.EsgQuestionnaire,
        uploadTime: 0,
        reportingPeriod: it.reportingPeriod,
        qaStatus: 'Accepted',
        currentlyActive: true,
      };
      return {
        data: it.t,
        metaInfo: metaInformation,
      };
    });

  return mountMLDTForEsgQuestionnairePanel(convertedDataAndMetaInformation, dummyCompanyId, false);
}

/**
 * Mounts the MultiLayerDataTableFrameworkPanel with the given dataset
 * @param datasetsToDisplay datasets to mount
 * @param companyId company ID of the mocked requests
 * @param reviewMode toggles the reviewer mode
 * @returns the component mounting chainable
 */
export function mountMLDTForEsgQuestionnairePanel(
  datasetsToDisplay: Array<DataAndMetaInformation<EsgQuestionnaireData>>,
  companyId: string,
  reviewMode: boolean
): Cypress.Chainable {
  cy.intercept(`/api/data/${DataTypeEnum.EsgQuestionnaire}/companies/${companyId}`, datasetsToDisplay);
  return cy.mountWithDialog(
    MultiLayerDataTableFrameworkPanel,
    {
      keycloak: minimalKeycloakMock({}),
    },
    {
      companyId: companyId,
      frameworkIdentifier: DataTypeEnum.EsgQuestionnaire,
      displayConfiguration: esgQuestionnaireViewConfiguration,
      inReviewMode: reviewMode,
    }
  );
}
