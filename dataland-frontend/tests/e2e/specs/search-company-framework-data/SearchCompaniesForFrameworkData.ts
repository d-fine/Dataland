import { searchBasicCompanyInformationForDataType } from '@e2e//utils/GeneralApiUtils';
import { DataTypeEnum, type EutaxonomyFinancialsData, type BasicCompanyInformation } from '@clients/backend';
import { getUploaderToken } from '@e2e/utils/Auth';
import { validateCompanyCockpitPage, verifySearchResultTableExists } from '@sharedUtils/ElementChecks';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { describeIf, type ExecutionEnvironment } from '@e2e/support/TestUtility';
import { assertDefined } from '@/utils/TypeScriptUtils';

const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000);
const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);

let companiesWithEuTaxonomyFinancialsData: Array<FixtureData<EutaxonomyFinancialsData>>;
const executionEnvironments: ExecutionEnvironment[] = ['developmentLocal', 'ci', 'developmentCd'];

/**
 * Enters the given text in the search bar and hits enter verifying that the search result table matches the expected
 * format, and the url includes the search term
 * @param inputValue the text to enter into the search bar
 */
function executeCompanySearchWithStandardSearchBar(inputValue: string): void {
  const inputValueUntilFirstSpace = inputValue.substring(0, inputValue.indexOf(' '));
  cy.get('input[id=search-bar-input]').should('not.be.disabled').click({ force: true });
  cy.get('input[id=search-bar-input]').type(inputValue);
  cy.get('input[id=search-bar-input]').should('have.value', inputValue);
  cy.get('input[id=search-bar-input]').type('{enter}');
  cy.get('input[id=search-bar-input]').should('have.value', inputValue);
  cy.url({ decode: true }).should('include', '/companies?input=' + inputValueUntilFirstSpace);
}

/**
 * Verifies that the tooltip of the Lei in the search table header contains the expected text
 */
function checkPermIdToolTip(): void {
  const expectedTextInToolTip = 'The Legal Entity Identifier (LEI)';
  cy.get('[data-test="lei-tooltip-tag"]').trigger('mouseenter', 'center');
  cy.get('.p-tooltip').should('be.visible').contains(expectedTextInToolTip);
  cy.get('[data-test="lei-tooltip-tag"]').trigger('mouseleave');
  cy.get('.p-tooltip').should('not.exist');
}

/**
 * Verifies that the view button redirects to the view framework data page
 */
function clickFirstSearchResult(): void {
  cy.get('[data-test="viewButton"]').first().click();
}

/**
 * Returns the first company from the fake fixture that has at least one alternative name
 * @returns the matching company from the fake fixtures
 */
function getCompanyWithAlternativeName(): FixtureData<EutaxonomyFinancialsData> {
  return assertDefined(
    companiesWithEuTaxonomyFinancialsData.find((it) => {
      return (
        it.companyInformation.companyAlternativeNames != undefined &&
        it.companyInformation.companyAlternativeNames.length > 0
      );
    })
  );
}

/**
 * Generates all substrings of the given text that have at least `minimumLength` characters and consist only of
 * alphanumeric characters. Restricting to alphanumeric-only substrings keeps the result safe to type into the
 * search bar and to compare against unencoded page URLs, without having to worry about escaping or encoding.
 * @param text the text to generate substrings from
 * @param minimumLength the minimum length a candidate substring must have
 * @returns the candidate substrings, in ascending order of length
 */
function getAlphanumericSubstringCandidates(text: string, minimumLength: number): string[] {
  const lowerCaseText = text.toLowerCase();
  const candidates: string[] = [];
  for (let length = minimumLength; length <= lowerCaseText.length; length++) {
    for (let start = 0; start + length <= lowerCaseText.length; start++) {
      const candidate = lowerCaseText.substring(start, start + length);
      if (/^[a-z0-9]+$/.test(candidate)) {
        candidates.push(candidate);
      }
    }
  }
  return candidates;
}

/**
 * Counts how many companies in the loaded EU Taxonomy Financials fixtures have a company name or an
 * alternative name that contains the given search string (case-insensitively).
 * @param searchString the search string to look for
 * @returns the number of distinct matching companies
 */
function countCompaniesMatchingSearchString(searchString: string): number {
  return companiesWithEuTaxonomyFinancialsData.filter((it) => {
    const namesOfCompany = [
      it.companyInformation.companyName,
      ...(it.companyInformation.companyAlternativeNames ?? []),
    ];
    return namesOfCompany.some((name) => name.toLowerCase().includes(searchString));
  }).length;
}

/**
 * Scans the loaded EU Taxonomy Financials fixtures for an alphanumeric search string that is contained in the
 * company name or an alternative name of at least `minimumNumberOfMatches` distinct companies. This avoids
 * relying on a hardcoded substring (e.g. a specific surname) that may stop matching enough companies whenever
 * the number of generated fake fixtures changes.
 * @param minimumNumberOfMatches the minimum number of distinct companies the returned string must match
 * @returns a search string that is guaranteed to yield at least `minimumNumberOfMatches` autocomplete suggestions
 */
function findSearchStringWithMinimumAutocompleteMatches(minimumNumberOfMatches: number): string {
  const minimumSearchStringLength = 3; // The autocomplete component ignores queries shorter than this
  const allNamesInFixtures = companiesWithEuTaxonomyFinancialsData.flatMap((it) => [
    it.companyInformation.companyName,
    ...(it.companyInformation.companyAlternativeNames ?? []),
  ]);
  const candidates = allNamesInFixtures.flatMap((name) =>
    getAlphanumericSubstringCandidates(name, minimumSearchStringLength)
  );

  const matchingCandidate = candidates.find(
    (candidate) => countCompaniesMatchingSearchString(candidate) >= minimumNumberOfMatches
  );

  if (!matchingCandidate) {
    throw new Error(
      `Could not find a search string matching at least ${minimumNumberOfMatches} companies among the loaded ` +
        'EU Taxonomy Financials fake fixtures. Please check the fake fixture generation or adjust the test.'
    );
  }
  return matchingCandidate;
}

/**
 * Asserts that the company name is unique in the search results. If it is not unique, the test will fail.
 * @param testCompany the company that was searched for
 */
function assertSearchedCompanyNameIsUnique(testCompany: BasicCompanyInformation): void {
  cy.get('.p-autocomplete-list-container').should('exist');
  cy.get('.p-autocomplete-option').should('have.length.greaterThan', 0);
  cy.get(`.p-autocomplete-option:contains('${testCompany.companyName}')`, {
    timeout: mediumTimeoutInMs,
  }).then((items) => {
    if (items.length !== 1)
      throw new Error(
        `The company name ${testCompany.companyName} does not seem to be unique. Please change the fake fixture for this test.`
      );
  });
}

before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyFinancialsData').then(function (jsonContent) {
    companiesWithEuTaxonomyFinancialsData = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
  });
});

beforeEach(function () {
  cy.ensureLoggedInAsReader();
});
describeIf(
  'As a user, I expect the search functionality on the /companies page to show me the desired results',
  {
    executionEnvironments: executionEnvironments,
  },
  () => {
    describeIf(
      'Tests for LEI tooltip and that company can be found -- only executed on database reset',
      {
        executionEnvironments: executionEnvironments,
        onlyExecuteOnDatabaseReset: true,
      },
      () => {
        it(
          'Check Lei tooltip, execute company search by name, check result table and assure VIEW button works',
          { scrollBehavior: false },
          () => {
            cy.visitAndCheckAppMount('/companies');
            verifySearchResultTableExists();
            const testCompanyName = companiesWithEuTaxonomyFinancialsData[0].companyInformation.companyName;
            checkPermIdToolTip();
            executeCompanySearchWithStandardSearchBar(testCompanyName);
            clickFirstSearchResult();
            cy.get('h1[data-test="companyNameTitle"]').should('have.text', testCompanyName);
            cy.go('back');
            cy.get('input[id=search-bar-input]').should('contain.value', testCompanyName);
            clickFirstSearchResult();
            cy.get('h1[data-test="companyNameTitle"]').should('have.text', testCompanyName);
          }
        );

        it('Execute a company Search by identifier and assure that the company is found', () => {
          cy.visitAndCheckAppMount('/companies');
          const testCompanyInformation = companiesWithEuTaxonomyFinancialsData[0].companyInformation;
          const testCompanyIdentifiersObject = testCompanyInformation.identifiers;
          const testCompanyIdentifierTypeWithExistingValues = assertDefined(
            Object.keys(testCompanyIdentifiersObject).find((it) => testCompanyIdentifiersObject[it].length > 0)
          );
          const singleCompanyIdentifier = testCompanyIdentifiersObject[testCompanyIdentifierTypeWithExistingValues][0];
          const expectedCompanyName = testCompanyInformation.companyName;
          executeCompanySearchWithStandardSearchBar(singleCompanyIdentifier);
          cy.get("td[class='d-bg-white w-3 d-datatable-column-left']").contains(expectedCompanyName);
        });
      }
    );

    it('Search for company by its alternative name', () => {
      const testCompany = getCompanyWithAlternativeName();
      const searchValue = assertDefined(testCompany.companyInformation.companyAlternativeNames)[0];
      cy.visitAndCheckAppMount('/companies');
      executeCompanySearchWithStandardSearchBar(searchValue);
    });

    it('Visit framework data view page and assure that title is present and a Company Search Bar exists', () => {
      const placeholder = 'Search company by name or identifier (e.g. PermID, LEI, ...)';
      const inputValue = 'A company name';

      getUploaderToken().then((token) => {
        cy.browserThen(searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyFinancials)).then(
          (basicCompanyInformations: Array<BasicCompanyInformation>) => {
            cy.visitAndCheckAppMount(
              `/companies/${basicCompanyInformations[0].companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`
            );
            cy.get('input[id=company_search_bar_standard]').should('not.be.disabled').type(inputValue);
            cy.get('input[id=company_search_bar_standard]')
              .should('have.value', inputValue)
              .invoke('attr', 'placeholder')
              .should('contain', placeholder);
          }
        );
      });
    });

    it('Search with autocompletion for companies with a common name fragment in it, click and use arrow keys, find searched company in recommendation', () => {
      const primevueHighlightedSuggestionClass = 'p-focus';
      // 3 must match (or be below) FrameworkDataSearchBar's default `maxNumOfDisplayedAutocompleteEntries` prop
      // value, since that threshold controls when the "View all results" button appears further down.
      const minimumNumberOfAutocompleteMatchesForViewAllResultsButton = 3;
      const searchStringResultingInAtLeastThreeAutocompleteSuggestions = findSearchStringWithMinimumAutocompleteMatches(
        minimumNumberOfAutocompleteMatchesForViewAllResultsButton
      );

      getUploaderToken().then((token) => {
        cy.browserThen(searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyFinancials)).then(
          (basicCompanyInformation: Array<BasicCompanyInformation>) => {
            if (basicCompanyInformation.length < 2) {
              throw new Error('Expected at least two companies in framework data search results.');
            }
            const testCompany = basicCompanyInformation[1];
            cy.visitAndCheckAppMount('/companies');

            verifySearchResultTableExists();
            cy.intercept('GET', '**/api/companies/names*').as('companyNameAutocomplete');

            cy.get('input[id=search-bar-input]')
              .click()
              .type(searchStringResultingInAtLeastThreeAutocompleteSuggestions);

            cy.wait('@companyNameAutocomplete');

            cy.contains('[data-test="view-all-results-button"]', 'View all results')
              .should('be.visible')
              // The button is rendered inside the PrimeVue AutoComplete overlay.
              // A normal Cypress click can fail because the overlay is re-rendered/hidden
              // while Cypress performs actionability checks. Thats why the force: true is necessary
              .click({ force: true });

            verifySearchResultTableExists();
            cy.url().should(
              'include',
              `/companies?input=${searchStringResultingInAtLeastThreeAutocompleteSuggestions}`
            );
            cy.scrollTo('top');
            cy.get('input[id=search-bar-input]').click();
            cy.get('input[id=search-bar-input]').type(
              `{backspace}{backspace}{backspace}${searchStringResultingInAtLeastThreeAutocompleteSuggestions}`
            );
            cy.get('.p-autocomplete-list-container').should('exist');
            cy.get('.p-autocomplete-option').should('have.length.at.least', 2);
            cy.get('input[id=search-bar-input]').should('be.focused');
            cy.wait(shortTimeoutInMs);
            cy.get('input[id=search-bar-input]').type('{downArrow}', { scrollBehavior: false });
            cy.get('.p-autocomplete-option').eq(0).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-option').eq(1).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search-bar-input]').type('{downArrow}', { scrollBehavior: false });
            cy.get('.p-autocomplete-option').eq(0).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-option').eq(1).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search-bar-input]').type('{upArrow}', { scrollBehavior: false });
            cy.get('.p-autocomplete-option').eq(0).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-option').eq(1).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search-bar-input]').click({ scrollBehavior: false });
            cy.get('input[id=search-bar-input]').type(`{backspace}{backspace}{backspace}${testCompany.companyName}`);
            assertSearchedCompanyNameIsUnique(testCompany);
            const testCompanyName = testCompany.companyName;
            cy.get('.p-autocomplete-option').eq(0).should('contain.text', testCompanyName).click({ force: true });
            validateCompanyCockpitPage(testCompanyName, testCompany.companyId);
          }
        );
      });
    });
  }
);
