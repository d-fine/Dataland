import { describeIf } from '@e2e/support/TestUtility';
import { getBaseUrl } from '@e2e/utils/Cypress';

describe('As a developer, I want to ensure that security relevant headers are set.', () => {
  /**
   * Verifies the presence of common security headers in the provided response
   * @param response the response to check
   */
  function checkCommonHeaders(response: Cypress.Response<unknown>): void {
    expect(response.headers).to.have.property('referrer-policy', 'no-referrer');
    expect(response.headers).to.have.property('strict-transport-security', 'max-age=31536000; includeSubDomains');
    expect(response.headers).to.have.property('x-content-type-options', 'nosniff');
  }

  /**
   * Verifies that the content-security-policy header of the Dataland webpage matches the provided expected header
   * @param expectedHeader the expected CSP header
   */
  function checkCommonCspHeaders(expectedHeader: string): void {
    const urlsToCheck = [`${getBaseUrl()}/`, `${getBaseUrl()}/keycloak/realms/datalandsecurity`];
    urlsToCheck.forEach((url): void => {
      it(`Check for local CSP headers in ${url}`, (): void => {
        cy.request('GET', url).then((response): void => {
          expect(response.headers).to.have.property('content-security-policy', expectedHeader);
        });
      });
    });
  }

  const cspHeaders =
    "frame-src 'self' data: https://www.youtube.com https://consentcdn.cookiebot.com; script-src-elem 'self' 'unsafe-eval' 'sha256-Ufh4gFF+3wijVQyJo86U1jiXhiwxTNfKBjPqBWLdvEY=' https://consent.cookiebot.com https://consentcdn.cookiebot.com https://www.youtube.com/; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'; form-action 'self'; font-src 'self' data:; img-src 'self' data: https://*.googleusercontent.com/ https://*.licdn.com/ https://consent.cookiebot.com https://i.ytimg.com/ https://img.youtube.com/";

  describeIf(
    'Check CSP headers in the local development environment',
    {
      executionEnvironments: ['developmentLocal'],
    },
    () => {
      checkCommonCspHeaders(cspHeaders);
    }
  );

  describeIf(
    'Check CSP headers in the CI/CD environment',
    {
      executionEnvironments: ['ci', 'developmentCd', 'previewCd'],
    },
    () => {
      checkCommonCspHeaders(cspHeaders);
    }
  );

  it('test for frontend response', () => {
    cy.request('GET', `${getBaseUrl()}/`).then((response): void => {
      checkCommonHeaders(response);
      expect(response.headers).to.have.property('x-frame-options', 'sameorigin');
    });
  });

  describeIf(
    'Check Cache headers in the CD environment',
    {
      executionEnvironments: ['developmentCd', 'previewCd'],
    },
    () => {
      it('test for frontend response', () => {
        cy.request('GET', `${getBaseUrl()}/`).then((response): void => {
          expect(response.headers).to.have.property('cache-control', 'no-cache, no-store, max-age=0, must-revalidate');
        });
        cy.request('GET', `${getBaseUrl()}/static/site.webmanifest`).then((response): void => {
          expect(response.headers).to.have.property('cache-control', 'max-age=31536000, public');
        });
      });
    }
  );

  it('test for backend response', () => {
    cy.request('GET', `${getBaseUrl()}/api/actuator/health`).then((response): void => {
      expect(response.headers).to.have.property('cache-control', 'no-cache, no-store, max-age=0, must-revalidate');
      expect(response.headers).to.have.property(
        'content-security-policy',
        "frame-ancestors 'none'; default-src 'none'"
      );
      checkCommonHeaders(response);
      expect(response.headers).to.have.property('x-frame-options', 'DENY');
    });
  });

  it('test for swagger ui response', () => {
    cy.request('GET', `${getBaseUrl()}/api/swagger-ui/index.html`).then((response): void => {
      expect(response.headers).to.have.property(
        'content-security-policy',
        "default-src 'self'; script-src 'self' 'sha256-4IiDsMH+GkJlxivIDNfi6qk0O5HPtzyvNwVT3Wt8TIw=';" +
          " style-src 'self'; frame-ancestors 'self'; form-action 'self'; font-src 'self' data:;" +
          " img-src 'self' data:"
      );
      checkCommonHeaders(response);
    });
  });

  it('test for keycloak response', () => {
    cy.request('GET', `${getBaseUrl()}/keycloak/realms/datalandsecurity`).then((response): void => {
      checkCommonHeaders(response);
      assert.equal(response.headers['x-frame-options'].toString().toLowerCase(), 'sameorigin');
    });
  });
});
