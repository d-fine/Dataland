import { searchBasicCompanyInformationForDataType } from '@e2e//utils/GeneralApiUtils';
import { DataTypeEnum, type EutaxonomyFinancialsData, type BasicCompanyInformation } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { validateCompanyCockpitPage, verifySearchResultTableExists } from '@sharedUtils/ElementChecks';
import { uploader_name, uploader_pw } from '@e2e/utils/Cypress';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { describeIf, type ExecutionEnvironment } from '@e2e/support/TestUtility';
import { assertDefined } from '@/utils/TypeScriptUtils';

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
  const company = companiesWithEuTaxonomyFinancialsData.find((it) => {
    const hasAlternativeNames = it.companyInformation.companyAlternativeNames != undefined;
    return hasAlternativeNames && it.companyInformation.companyAlternativeNames!.length > 0;
  });
  return assertDefined(company);
}

/**
 * Asserts that the company name is unique in the search results. If it is not unique, the test will fail.
 * @param testCompany the company that was searched for
 */
function assertSearchedCompanyNameIsUnique(testCompany: BasicCompanyInformation): void {
  cy.get(`.p-autocomplete-option:contains('${testCompany.companyName}')`).then((items) => {
    if (items.length !== 1) {
      throw new Error(
        `The company name ${testCompany.companyName} does not seem to be unique. Please change the fake fixture for this test.`
      );
    }
  });
}

/**
 * Clears and types a value into the search bar
 * @param value the value to type
 */
function typeInSearchBar(value: string): void {
  cy.get('input[id=search-bar-input]').clear();
  cy.get('input[id=search-bar-input]').type(value);
}

/**
 * Validates arrow key navigation in autocomplete
 * @param highlightClass the class name for highlighted items
 */
function validateArrowKeyNavigation(highlightClass: string): void {
  cy.get('input[id=search-bar-input]').type('{downArrow}');
  cy.get('.p-autocomplete-option').eq(0).should('have.class', highlightClass);
  cy.get('.p-autocomplete-option').eq(1).should('not.have.class', highlightClass);

  cy.get('input[id=search-bar-input]').type('{downArrow}');
  cy.get('.p-autocomplete-option').eq(0).should('not.have.class', highlightClass);
  cy.get('.p-autocomplete-option').eq(1).should('have.class', highlightClass);

  cy.get('input[id=search-bar-input]').type('{upArrow}');
  cy.get('.p-autocomplete-option').eq(0).should('have.class', highlightClass);
  cy.get('.p-autocomplete-option').eq(1).should('not.have.class', highlightClass);
}

/**
 * Performs initial autocomplete search and validates results
 * @param searchString the string to search for
 */
function performInitialAutocompleteSearch(searchString: string): void {
  typeInSearchBar(searchString);
  cy.get('[data-test="view-all-results-button"]').contains('View all results').click();
  verifySearchResultTableExists();
  cy.url().should('include', `/companies?input=${searchString}`);
  cy.scrollTo('top');
}

/**
 * Searches for a specific company and validates selection
 * @param testCompany the company to search for
 */
function searchAndSelectCompany(testCompany: BasicCompanyInformation): void {
  const testCompanyName = testCompany.companyName;

  cy.get('input[id=search-bar-input]').click();
  typeInSearchBar(testCompanyName);
  assertSearchedCompanyNameIsUnique(testCompany);
  cy.get('.p-autocomplete-list-container').should('be.visible');

  cy.get('.p-autocomplete-option')
    .contains(testCompanyName)
    .should('be.visible')
    .then(($el) => {
      cy.wrap($el).click({ force: true });
    });

  validateCompanyCockpitPage(testCompanyName, testCompany.companyId);
}

/**
 * Tests arrow key navigation in autocomplete dropdown
 * @param searchString the string to search for
 * @param highlightClass the class name for highlighted items
 */
function testArrowKeyNavigation(searchString: string, highlightClass: string): void {
  cy.get('input[id=search-bar-input]').click();
  typeInSearchBar(searchString);
  cy.get('.p-autocomplete-list-container').should('be.visible');
  validateArrowKeyNavigation(highlightClass);
}

before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyFinancialsData').then(function (jsonContent) {
    companiesWithEuTaxonomyFinancialsData = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
  });
});

beforeEach(function () {
  cy.ensureLoggedIn();
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
            const testCompanyName = companiesWithEuTaxonomyFinancialsData[0]!.companyInformation.companyName;
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
          const testCompanyInformation = companiesWithEuTaxonomyFinancialsData[0]!.companyInformation;
          const testCompanyIdentifiersObject = testCompanyInformation.identifiers;
          const testCompanyIdentifierTypeWithExistingValues = assertDefined(
            Object.keys(testCompanyIdentifiersObject).find((it) => testCompanyIdentifiersObject[it]!.length > 0)
          );
          const singleCompanyIdentifier =
            testCompanyIdentifiersObject[testCompanyIdentifierTypeWithExistingValues]![0]!;
          const expectedCompanyName = testCompanyInformation.companyName;
          executeCompanySearchWithStandardSearchBar(singleCompanyIdentifier);
          cy.get("td[class='d-bg-white w-3 d-datatable-column-left']").contains(expectedCompanyName);
        });
      }
    );

    it('Search for company by its alternative name', () => {
      const testCompany = getCompanyWithAlternativeName();
      const searchValue = assertDefined(testCompany.companyInformation.companyAlternativeNames)[0]!;
      cy.visitAndCheckAppMount('/companies');
      executeCompanySearchWithStandardSearchBar(searchValue);
    });

    it('Visit framework data view page and assure that title is present and a Company Search Bar exists', () => {
      const placeholder = 'Search company by name or identifier (e.g. PermID, LEI, ...)';
      const inputValue = 'A company name';

      getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        cy.browserThen(searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyFinancials)).then(
          (basicCompanyInformations: Array<BasicCompanyInformation>) => {
            const companyId = basicCompanyInformations[0]!.companyId;
            cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
            cy.get('input[id=company_search_bar_standard]').should('not.be.disabled').type(inputValue);
            cy.get('input[id=company_search_bar_standard]')
              .should('have.value', inputValue)
              .invoke('attr', 'placeholder')
              .should('contain', placeholder);
          }
        );
      });
    });

    it('Search with autocompletion for companies with "abs" in it, click and use arrow keys, find searched company in recommendation', () => {
      const primevueHighlightedSuggestionClass = 'p-focus';
      const searchStringResultingInAtLeastTwoAutocompleteSuggestions = 'abs';

      getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        const apiCall = searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyFinancials);
        cy.browserThen(apiCall).then((basicCompanyInformation: Array<BasicCompanyInformation>) => {
          const testCompany = basicCompanyInformation[1]!;

          cy.visitAndCheckAppMount('/companies');
          verifySearchResultTableExists();

          performInitialAutocompleteSearch(searchStringResultingInAtLeastTwoAutocompleteSuggestions);
          testArrowKeyNavigation(
            searchStringResultingInAtLeastTwoAutocompleteSuggestions,
            primevueHighlightedSuggestionClass
          );
          searchAndSelectCompany(testCompany);
        });
      });
    });
  }
);
