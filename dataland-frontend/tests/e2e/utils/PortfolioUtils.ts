import { getAdminToken } from '@e2e/utils/Auth.ts';
import { NotificationFrequency, PortfolioControllerApi, Configuration } from '@clients/userservice';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';

/**
 * Creates a new portfolio by utilizing an API. The method obtains an access token through Keycloak authentication
 * and uses it to authenticate the API request for creating the portfolio.
 */
export function createPortfolio(): void {
  getAdminToken().then(async (token) => {
    const companyToUpload = generateDummyCompanyInformation('Dummy Company');
    const companyId = (await getOrUploadCompanyViaApi(token, companyToUpload)).companyId;

    const dummyPortfolioUpload = {
      portfolioName: 'Dummy Portfolio ' + Date.now(),
      identifiers: [companyId] as unknown as Set<string>,
      isMonitored: false,
      monitoredFrameworks: [] as unknown as Set<string>,
      notificationFrequency: NotificationFrequency.Weekly,
      timeWindowThreshold: undefined,
      sharedUserIds: [] as unknown as Set<string>,
    };
    await new PortfolioControllerApi(new Configuration({ accessToken: token })).createPortfolio(dummyPortfolioUpload);
  });
}

/**
 * Deletes all portfolios associated with the current user.
 */
export function deleteAllPortfolios(): void {
  getAdminToken().then(async (token) => {
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
