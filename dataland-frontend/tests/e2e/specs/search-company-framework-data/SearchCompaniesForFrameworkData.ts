import { searchBasicCompanyInformationForDataType } from '@e2e//utils/GeneralApiUtils';
import { DataTypeEnum, type EuTaxonomyDataForFinancials, type BasicCompanyInformation } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { validateCompanyCockpitPage, verifySearchResultTableExists } from '@sharedUtils/ElementChecks';
import { uploader_name, uploader_pw } from '@e2e/utils/Cypress';
import { type FixtureData } from '@sharedUtils/Fixtures';
import { describeIf, type ExecutionEnvironment } from '@e2e/support/TestUtility';
import { assertDefined } from '@/utils/TypeScriptUtils';

let companiesWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;
const executionEnvironments: ExecutionEnvironment[] = ['developmentLocal', 'ci', 'developmentCd'];

before(function () {
  cy.fixture('CompanyInformationWithEuTaxonomyDataForFinancials').then(function (jsonContent) {
    companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
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
    /**
     * Enters the given text in the search bar and hits enter verifying that the search result table matches the expected
     * format and the url includes the search term
     * @param inputValue the text to enter into the search bar
     */
    function executeCompanySearchWithStandardSearchBar(inputValue: string): void {
      const inputValueUntilFirstSpace = inputValue.substring(0, inputValue.indexOf(' '));
      cy.get('input[id=search_bar_top]').should('not.be.disabled').click({ force: true });
      cy.get('input[id=search_bar_top]').type(inputValue);
      cy.get('input[id=search_bar_top]').should('have.value', inputValue);
      cy.get('input[id=search_bar_top]').type('{enter}');
      cy.get('input[id=search_bar_top]').should('have.value', inputValue);
      cy.url({ decode: true }).should('include', '/companies?input=' + inputValueUntilFirstSpace);
    }

    describeIf(
      '',
      {
        executionEnvironments: executionEnvironments,
        onlyExecuteOnDatabaseReset: true,
      },
      () => {
        it(
          'Check Lei tooltip, execute company search by name, check result table and assure VIEW button works',
          { scrollBehavior: false },
          () => {
            /**
             * Verifies that the tooltip of the Lei in the search table header contains the expected text
             */
            function checkPermIdToolTip(): void {
              const expectedTextInToolTip = 'The Legal Entity Identifier (LEI)';
              cy.get('.material-icons[title="LEI"]').trigger('mouseenter', 'center');
              cy.get('.p-tooltip').should('be.visible').contains(expectedTextInToolTip);
              cy.get('.material-icons[title="LEI"]').trigger('mouseleave');
              cy.get('.p-tooltip').should('not.exist');
            }

            /**
             * Verifies that the view button redirects to the view framework data page
             */
            function clickFirstSearchResult(): void {
              cy.get('table.p-datatable-table').contains('td', 'VIEW').click();
            }

            cy.visitAndCheckAppMount('/companies');
            verifySearchResultTableExists();
            const testCompanyName = companiesWithEuTaxonomyDataForFinancials[0].companyInformation.companyName;
            checkPermIdToolTip();
            executeCompanySearchWithStandardSearchBar(testCompanyName);
            clickFirstSearchResult();
            cy.get('h1[data-test="companyNameTitle"]').should('have.text', testCompanyName);
            cy.get('[data-test="back-button"]').should('be.visible').click({ force: true });
            cy.get('input[id=search_bar_top]').should('contain.value', testCompanyName);
            clickFirstSearchResult();
            cy.get('h1[data-test="companyNameTitle"]').should('have.text', testCompanyName);
          }
        );

        it('Execute a company Search by identifier and assure that the company is found', () => {
          cy.visitAndCheckAppMount('/companies');
          const testCompanyInformation = companiesWithEuTaxonomyDataForFinancials[0].companyInformation;
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

    /**
     * Returns the first company from the fake fixture that has at least one alternative name
     * @returns the matching company from the fake fixtures
     */
    function getCompanyWithAlternativeName(): FixtureData<EuTaxonomyDataForFinancials> {
      return assertDefined(
        companiesWithEuTaxonomyDataForFinancials.find((it) => {
          return (
            it.companyInformation.companyAlternativeNames != undefined &&
            it.companyInformation.companyAlternativeNames.length > 0
          );
        })
      );
    }

    it('Search for company by its alternative name', () => {
      const testCompany = getCompanyWithAlternativeName();
      const searchValue = assertDefined(testCompany.companyInformation.companyAlternativeNames)[0];
      cy.visitAndCheckAppMount('/companies');
      executeCompanySearchWithStandardSearchBar(searchValue);
    });

    it('Visit framework data view page and assure that title is present and a Company Search Bar exists', () => {
      const placeholder = 'Search company by name or identifier (e.g. PermID, LEI, ...)';
      const inputValue = 'A company name';

      getKeycloakToken(uploader_name, uploader_pw).then((token) => {
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

    it("Search with autocompletion for companies with 'abs' in it, click and use arrow keys, find searched company in recommendation", () => {
      const primevueHighlightedSuggestionClass = 'p-focus';
      const searchStringResultingInAtLeastTwoAutocompleteSuggestions = 'abs';
      getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        cy.browserThen(searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyFinancials)).then(
          (basicCompanyInformations: Array<BasicCompanyInformation>) => {
            const testCompany = basicCompanyInformations[0];
            cy.visitAndCheckAppMount('/companies');

            verifySearchResultTableExists();
            cy.get('input[id=search_bar_top]').type('abs');
            cy.get('.p-autocomplete-item').contains('View all results').click();

            verifySearchResultTableExists();
            cy.url().should('include', '/companies?input=abs');
            cy.get('input[id=search_bar_top]').click({ force: true });
            cy.get('input[id=search_bar_top]').type(
              `{backspace}{backspace}{backspace}${searchStringResultingInAtLeastTwoAutocompleteSuggestions}`
            );
            cy.get('ul[class=p-autocomplete-items]').should('exist');
            cy.get('input[id=search_bar_top]').type('{downArrow}');
            cy.get('.p-autocomplete-item').eq(0).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-item').eq(1).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search_bar_top]').type('{downArrow}');
            cy.get('.p-autocomplete-item').eq(0).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-item').eq(1).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search_bar_top]').type('{upArrow}');
            cy.get('.p-autocomplete-item').eq(0).should('have.class', primevueHighlightedSuggestionClass);
            cy.get('.p-autocomplete-item').eq(1).should('not.have.class', primevueHighlightedSuggestionClass);
            cy.get('input[id=search_bar_top]').click({ force: true });
            cy.get('input[id=search_bar_top]').type(`{backspace}{backspace}{backspace}${testCompany.companyName}`);
            cy.get('.p-autocomplete-item').eq(0).should('contain.text', testCompany.companyName).click({ force: true });

            validateCompanyCockpitPage(testCompany.companyName, testCompany.companyId);
          }
        );
      });
    });
  }
);
