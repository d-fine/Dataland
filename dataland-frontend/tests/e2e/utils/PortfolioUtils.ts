import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { PortfolioControllerApi } from '@clients/userservice';
import { Configuration } from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';

/**
 * Creates a new portfolio by utilizing an API. The method obtains an access token through Keycloak authentication
 * and uses it to authenticate the API request for creating the portfolio.
 */
export function createPortfolio(): void {
  getKeycloakToken(admin_name, admin_pw).then(async (token) => {
    const companyToUpload = generateDummyCompanyInformation('Dummy Company');
    const companyId = (await uploadCompanyViaApi(token, companyToUpload)).companyId;

    const dummyPortfolioUpload = {
      portfolioName: 'Dummy Portfolio ' + Date.now(),
      companyIds: [companyId],
      isMonitored: false,
      startingMonitoringPeriod: '',
      monitoredFrameworks: [],
    };
    await new PortfolioControllerApi(new Configuration({ accessToken: token })).createPortfolio(dummyPortfolioUpload);
  });
}

/**
 * Deletes all portfolios associated with the current user.
 */
export function deleteAllPortfolios(): void {
  getKeycloakToken(admin_name, admin_pw).then(async (token) => {
    const allUserPortfoliosAxiosResponse = await new PortfolioControllerApi(
      new Configuration({ accessToken: token })
    ).getAllPortfolioNamesForCurrentUser();
    const allUserPortfolios = allUserPortfoliosAxiosResponse.data;
    for (const portfolio of allUserPortfolios) {
      await new PortfolioControllerApi(new Configuration({ accessToken: token })).deletePortfolio(
        portfolio.portfolioId
      );
    }
  });
}
