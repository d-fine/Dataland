import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataEsgDatenkatalogData,
  DataTypeEnum,
  type EsgDatenkatalogData,
} from '@clients/backend';

import { esgDatenkatalogViewConfiguration } from '@/frameworks/esg-datenkatalog/ViewConfig';
import {
  getCellValueContainer,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { mountMLDTFrameworkPanelFromFakeFixture } from '@ct/testUtils/MultiLayerDataTableComponentTestUtils';

describe('Component Test for the ESG Datenkatalog view Page with its components', () => {
  let preparedFixtureForTest: FixtureData<EsgDatenkatalogData>;
  const companyId = 'mock-company-id';
  before(function () {
    cy.fixture('CompanyInformationWithEsgDatenkatalogPreparedFixtures').then(function (jsonContent) {
      const preparedFixtures = jsonContent as Array<FixtureData<EsgDatenkatalogData>>;
      preparedFixtureForTest = getPreparedFixture('EsgDatenkatalog-dataset-with-no-null-fields', preparedFixtures);
    });
  });

  it('Check that on the ESG Datenkatalog view Page the list base data point component works properly', () => {
    cy.intercept(`/api/data/${DataTypeEnum.EsgDatenkatalog}/mock-data-id`, {
      companyId: companyId,
      reportingPeriod: preparedFixtureForTest.reportingPeriod,
      data: preparedFixtureForTest.t,
    } as CompanyAssociatedDataEsgDatenkatalogData);
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.EsgDatenkatalog, esgDatenkatalogViewConfiguration, [
      preparedFixtureForTest,
    ]);
    getSectionHead('Generelle ESG-Strategie').should('exist');
    getCellValueContainer('Dokumente zur Nachhaltigkeitsstrategie').click();
    cy.get('span').contains('Beschreibung des Dokuments zur Nachhaltigkeitsstrategie');
    const aktuelleBerichte = assertDefined(
      preparedFixtureForTest.t.allgemein?.generelleEsgStrategie?.dokumenteZurNachhaltigkeitsstrategie
    );

    for (const singleEsgBericht of aktuelleBerichte) {
      cy.get('div').contains(assertDefined(singleEsgBericht.value));
      if (singleEsgBericht.dataSource) {
        cy.get('div').contains(assertDefined(singleEsgBericht.dataSource.fileName));
      }
    }
    cy.get('div.p-dialog-content i[data-test="download-icon"]').should('have.length', aktuelleBerichte.length - 1);
  });
});
