import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';

describe('Component tests for the BulkDataRequestSummary page', () => {
  it('Should display the BulkRequestSummary', () => {
    const bulkDataRequestBody = {
      companyIdentifiers: ['existsWithData', 'existsWithPartialData', 'existsWithoutData', 'notExists1', 'notExists2'],
      dataTypes: ['p2p', 'sfdr'],
      reportingPeriods: ['2022', '2023'],
    };

    const bulkDataResponseBody = {
      message:
        '2 of your 5 distinct ' +
        'company identifiers were rejected because they could not be uniquely matched with existing ' +
        'companies on Dataland.',
      acceptedDataRequests: [
        {
          userProvidedCompanyId: 'existsWithoutData',
          companyName: 'ExistsWithoutData',
          framework: 'p2p',
          reportingPeriod: '2022',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithoutData',
          companyName: 'ExistsWithoutData',
          framework: 'sfdr',
          reportingPeriod: '2022',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithoutData',
          companyName: 'ExistsWithoutData',
          framework: 'p2p',
          reportingPeriod: '2023',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithoutData',
          companyName: 'ExistsWithoutData',
          framework: 'sfdr',
          reportingPeriod: '2023',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithPartialData',
          companyName: 'ExistsWithPartialData',
          framework: 'p2p',
          reportingPeriod: '2022',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithPartialData',
          companyName: 'ExistsWithPartialData',
          framework: 'sfdr',
          reportingPeriod: '2023',
          requestId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          requestUrl: 'https://dataland.com/requests/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
      ],
      alreadyExistingDataRequest: [
        {
          userProvidedCompanyId: 'existsWithPartialData',
          companyName: 'ExistsWithPartialData',
          framework: 'sfdr',
          reportingPeriod: '2022',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithPartialData',
          companyName: 'ExistsWithPartialData',
          framework: 'p2p',
          reportingPeriod: '2023',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithData',
          companyName: 'ExistsWithData',
          framework: 'p2p',
          reportingPeriod: '2022',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithData',
          companyName: 'ExistsWithData',
          framework: 'p2p',
          reportingPeriod: '2023',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithData',
          companyName: 'ExistsWithData',
          framework: 'sfdr',
          reportingPeriod: '2022',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
        {
          userProvidedCompanyId: 'existsWithData',
          companyName: 'ExistsWithData',
          framework: 'sfdr',
          reportingPeriod: '2023',
          datasetId: '95a105a0-701a-45f0-a4b2-36b53761aedc',
          datasetUrl:
            'https://dataland.com/companies/95a105a0-701a-45f0-a4b2-36b53761aedc/frameworks/95a105a0-701a-45f0-a4b2-36b53761aedc',
        },
      ],
      rejectedCompanyIdentifiers: ['notExists1', 'notExists2'],
    };

    cy.intercept('POST', '**/community/bulk', (req) => {
      req.alias = 'postRequest';
      req.reply((res) => {
        res.send(200, bulkDataResponseBody);
      });
    }).as('mockPostRequest');

    cy.request({
      method: 'POST',
      url: '**/community/bulk',
      body: bulkDataRequestBody,
    }).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.deep.equal(bulkDataResponseBody);
    });
  });
});
