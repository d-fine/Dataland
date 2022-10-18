/**
 * As a User I want to be able to search for companies in dataland.
 * I also want to be able to create new companies.
 */
describe("Taxonomy Company Metadata tests", (): void => {
  require("./SearchPagination");
  require("./CompanyUpload");
  require("./SearchCompaniesForFrameworkData");
  require("./SearchCompaniesForFrameworkDataDropdownFilter");
  require("./CompaniesOnlySearch");
});
