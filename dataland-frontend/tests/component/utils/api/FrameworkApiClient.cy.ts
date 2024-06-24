import { Configuration, DataTypeEnum } from '@clients/backend';
import { getUnifiedFrameworkDataControllerFromConfiguration } from '@/utils/api/FrameworkApiClient';

describe('It should be possible to construct Unified API Clients for every framework using the old paradigm', () => {
  const frameworks = [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.P2p];

  for (const frameworkIdentifier of frameworks) {
    it(`Should be possible to construct Unified API Clients for the ${frameworkIdentifier} framework and request data using them`, () => {
      const frameworkApiClient = getUnifiedFrameworkDataControllerFromConfiguration(
        frameworkIdentifier,
        new Configuration()
      );

      cy.intercept(
        {
          method: 'GET',
          url: `/api/data/${frameworkIdentifier}/test-data-id`,
        },
        {}
      ).as('api-client-test-request');

      void frameworkApiClient.getFrameworkData('test-data-id');

      cy.wait('@api-client-test-request');
    });
  }
});
