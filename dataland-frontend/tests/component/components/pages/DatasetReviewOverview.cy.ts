import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';

describe('DatasetReviewOverview page', () => {
  const keycloakMockWithReviewer = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_REVIEWER],
  });

  const dataId = 'test-data-id';
  const companyID = '9af067dc-8280-4172-8974-1ae363c56260';
});
