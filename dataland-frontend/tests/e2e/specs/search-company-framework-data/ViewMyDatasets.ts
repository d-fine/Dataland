before(function () {});

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn();
  });

  // TODO Stuff to test:
  /*      - Switch between Available Datasets and My Datasets and check for the page in general (is everything there?)
            - Assure that some dataset is not available, then upload it, the check that it's in "My Datasets"
            - Use the search to find a specific dataset from all of the users datasets
            - paginator?
            - CREATE button?
            - ...            open for more ideas

     */

  it("TODO", function () {
    cy.visitAndCheckAppMount("/companies");
  });
});
