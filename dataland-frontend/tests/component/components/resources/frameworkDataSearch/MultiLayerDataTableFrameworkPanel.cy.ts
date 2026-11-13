import { DataTypeEnum, type DataMetaInformation } from '@clients/backend';
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import MultiLayerDataTableFrameworkPanel from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

type TestData = { value: string };

const COMPANY_ID = 'mock-company-id';
const DATA_ID = 'data-id-1';
const FRAMEWORK = DataTypeEnum.Sfdr;
const LOADING_TEXT = /Loading .* Data\.\.\./;
const ERROR_TEXT = 'We are having issues loading the data.';
const INSUFFICIENT_RIGHTS_TEXT = 'Sorry! You have insufficient rights to view the available datasets.';

const displayConfiguration: MLDTConfig<TestData> = [
  {
    type: 'cell',
    label: 'Test Value',
    shouldDisplay: (): boolean => true,
    valueGetter: (dataset: TestData) => ({
      displayComponentName: 'StringDisplayComponent',
      displayValue: dataset.value,
    }),
  },
] as MLDTConfig<TestData>;

const metaInfo = (dataId: string, reportingPeriod: string): DataMetaInformation => ({
  dataId,
  companyId: COMPANY_ID,
  dataType: FRAMEWORK,
  uploadTime: 0,
  reportingPeriod,
  qaStatus: 'Accepted',
  currentlyActive: true,
  ref: 'https://example.com',
});

/**
 * Mounts the panel with the given props.
 * @param singleDataMetaInfoToDisplay if set, the panel runs in single dataset mode
 */
function mountPanel(singleDataMetaInfoToDisplay?: DataMetaInformation): void {
  getMountingFunction({
    keycloak: minimalKeycloakMock(),
    dialogOptions: {
      mountWithDialog: true,
      propsToPassToTheMountedComponent: {
        companyId: COMPANY_ID,
        frameworkIdentifier: FRAMEWORK,
        displayConfiguration,
        inReviewMode: false,
        singleDataMetaInfoToDisplay,
      },
    },
  })(MultiLayerDataTableFrameworkPanel);
}

describe('MultiLayerDataTableFrameworkPanel', () => {
  describe('all datasets of a company', () => {
    it('shows a loading indicator until the datasets arrived and then displays them', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/companies/${COMPANY_ID}`, {
        delay: 500,
        body: [{ metaInfo: metaInfo(DATA_ID, '2023'), data: { value: 'first-value' } }],
      });
      mountPanel();
      cy.contains(LOADING_TEXT).should('be.visible');
      cy.contains('first-value').should('be.visible');
      cy.contains(LOADING_TEXT).should('not.exist');
    });

    it('shows an insufficient rights message on a 403 response', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/companies/${COMPANY_ID}`, { statusCode: 403, body: {} });
      mountPanel();
      cy.contains(INSUFFICIENT_RIGHTS_TEXT).should('be.visible');
      cy.contains(ERROR_TEXT).should('not.exist');
    });

    it('shows a generic error message on any other failure', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/companies/${COMPANY_ID}`, { statusCode: 500, body: {} });
      mountPanel();
      cy.contains(ERROR_TEXT).should('be.visible');
      cy.contains(INSUFFICIENT_RIGHTS_TEXT).should('not.exist');
    });
  });

  describe('single dataset', () => {
    it('displays the dataset fetched by its id without requesting all company datasets', () => {
      let allDatasetsRequests = 0;
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/companies/${COMPANY_ID}`, (req) => {
        allDatasetsRequests++;
        req.reply([]);
      });
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/${DATA_ID}`, {
        body: { companyId: COMPANY_ID, reportingPeriod: '2023', data: { value: 'value-by-id' } },
      });
      mountPanel(metaInfo(DATA_ID, '2023'));
      cy.contains('value-by-id').should('be.visible');
      cy.then(() => expect(allDatasetsRequests).to.equal(0));
    });

    it('falls back to the reporting period if the dataset cannot be fetched by its id', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/${DATA_ID}`, { statusCode: 404, body: {} }).as('byId');
      cy.intercept({ method: 'GET', pathname: `/api/data/${FRAMEWORK}/` }, (req) => {
        expect(req.query.reportingPeriod).to.equal('2023');
        expect(req.query.companyId).to.equal(COMPANY_ID);
        req.reply({ companyId: COMPANY_ID, reportingPeriod: '2023', data: { value: 'value-by-dimensions' } });
      }).as('byDimensions');
      mountPanel(metaInfo(DATA_ID, '2023'));
      cy.wait('@byId');
      cy.wait('@byDimensions');
      cy.contains('value-by-dimensions').should('be.visible');
      cy.contains(ERROR_TEXT).should('not.exist');
    });

    it('does not show an error while the fetch by id is failing and the fallback is still loading', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/${DATA_ID}`, { statusCode: 404, body: {} });
      cy.intercept({ method: 'GET', pathname: `/api/data/${FRAMEWORK}/` }, (req) => {
        req.reply({
          delay: 1000,
          body: { companyId: COMPANY_ID, reportingPeriod: '2023', data: { value: 'late-value' } },
        });
      });
      mountPanel(metaInfo(DATA_ID, '2023'));
      cy.contains(LOADING_TEXT).should('be.visible');
      cy.contains(ERROR_TEXT).should('not.exist');
      cy.contains('late-value').should('be.visible');
    });

    it('shows an error if both the fetch by id and the fallback fail', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/${DATA_ID}`, { statusCode: 404, body: {} });
      cy.intercept({ method: 'GET', pathname: `/api/data/${FRAMEWORK}/` }, { statusCode: 500, body: {} });
      mountPanel(metaInfo(DATA_ID, '2023'));
      cy.contains(ERROR_TEXT).should('be.visible');
    });

    it('shows an insufficient rights message if the fallback is forbidden', () => {
      cy.intercept('GET', `**/api/data/${FRAMEWORK}/${DATA_ID}`, { statusCode: 404, body: {} });
      cy.intercept({ method: 'GET', pathname: `/api/data/${FRAMEWORK}/` }, { statusCode: 403, body: {} });
      mountPanel(metaInfo(DATA_ID, '2023'));
      cy.contains(INSUFFICIENT_RIGHTS_TEXT).should('be.visible');
    });
  });
});
