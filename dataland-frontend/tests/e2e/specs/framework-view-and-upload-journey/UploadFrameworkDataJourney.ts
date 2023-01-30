before(function () {});

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn();
  });

  // TODO Stuff to test:
  /*      - Before: Create an fresh company with a specific name, but do not upload any framework data for it
          - Click on CREATE DATASET on search page and assure that you get redirected to the ChooseCompanyPage
          - Verify the search bar there for option 1, and assure that the created company appears there
          - Don't use that company. Instead, click on "add it" and add a new company via the form, assure that you get automatically redirected
          - Click on CREATE DATASET for some framwork and assure that you are redirected to the form

          - Before: Create Company with lots of framework data for it
          - Click on CREATE DATASET on search page and assure that you get redirected to the ChooseCompanyPage
          - Choose that company via keys on the autocomplete dropdown and assure that you are redirected to the ChooseFrameworkPage
          - Now verify the ChooseCompanyPage (are the existing datasets displayed?)
          - Click on one existing non-lksg dataset and check if you get redirected to the exakt framework-view-page for it
          - Click on one existing lksg dataset and check if you get redirected to the general lksg view page for that company

     */

  it("TODO", function () {
    cy.visitAndCheckAppMount("/companies");
  });
});
