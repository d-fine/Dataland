/**
 * As a user I want to be able to search for companies with framework data on the central Dataland search page.
 * I want to use the provided filters on that page and navigate through the search results.
 */
describe("Taxonomy Company Metadata tests", () => {
  /* Beware: this index.ts is NOT imported in the global specs index.ts.
     If you add tests, add them in the global index.ts in an appropriate runner as well.
   */
  require("./SearchPagination");
  require("./SearchCompaniesForFrameworkData");
  require("./SearchCompaniesForFrameworkDataDropdownFilter");
});
