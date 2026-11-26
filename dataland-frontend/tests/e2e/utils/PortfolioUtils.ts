import { getAdminToken, getReviewerToken } from '@e2e/utils/Auth.ts';
import { NotificationFrequency, PortfolioControllerApi, Configuration } from '@clients/userservice';
import { generateDummyCompanyInformation, getOrUploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';

/**
 * Creates a new portfolio for the reviewer user by utilizing an API. The reviewer is used on purpose: no other
 * test works with the portfolios of this user, so tests using these helpers cannot interfere with portfolio tests
 * of the admin user running in parallel against the same server.
 */
export function createPortfolio(): void {
  getAdminToken()
    .then((adminToken) => getOrUploadCompanyViaApi(adminToken, generateDummyCompanyInformation('Dummy Company')))
    .then((company) => getReviewerToken().then((token) => ({ token, companyId: company.companyId })))
    .then(async ({ token, companyId }) => {
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
 * Deletes all portfolios of the reviewer user (see createPortfolio for why the reviewer is used).
 */
export function deleteAllPortfolios(): void {
  getReviewerToken().then(async (token) => {
    const allUserPortfoliosAxiosResponse = await new PortfolioControllerApi(
      new Configuration({ accessToken: token })
    ).getAllPortfolioNamesForCurrentUser();
    const allUserPortfolios = allUserPortfoliosAxiosResponse.data;
    for (const portfolio of allUserPortfolios) {
      // prettier-ignore
      await new PortfolioControllerApi(new Configuration({ accessToken: token })).deletePortfolio( // NOSONAR: sequential, test helper
        portfolio.portfolioId
      );
    }
  });
}
