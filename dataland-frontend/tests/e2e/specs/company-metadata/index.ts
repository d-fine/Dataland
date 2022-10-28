/**
 * As a User I want to be able to search for companies in dataland.
 * I also want to be able to create new companies.
 */
describe("Taxonomy Company Metadata tests", () => {
  /* Beware: this index.ts is NOT imported in the global specs index.ts.
     If you add tests, add them in the global index.ts in an appropriate runner as well.
   */
  require("./SearchPagination");
  require("./CompanyUpload");
  require("./SearchCompaniesForFrameworkData");
  require("./SearchCompaniesForFrameworkDataDropdownFilter");
  require("./CompaniesOnlySearch");
});
