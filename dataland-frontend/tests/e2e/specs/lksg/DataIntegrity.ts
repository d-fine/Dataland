import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { getOneCompanyThatHasDataForDataType } from "../../utils/ApiUtils";
import { FixtureData } from "../../fixtures/FixtureUtils";
import { uploadOneLksgDatasetViaApi } from "../../utils/LksgUpload";
import { generateLksgData } from "../../fixtures/lksg/LksgDataFixtures";
import {
  CompanyInformation,
  LksgData,
  CompanyDataControllerApi,
  Configuration,
  DataTypeEnum,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "../../utils/CompanyUpload";
import {
  generateEuTaxonomyDataForFinancials
} from "../../fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import {uploadOneEuTaxonomyFinancialsDatasetViaApi} from "../../utils/EuTaxonomyFinancialsUpload";

// TODO use shortcuts in imports above

const timeout = 120 * 1000;
describeIf(
  "As a user, I expect Lksg data that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });
    });

    function getPreparedFixture(name: string): FixtureData<LksgData> {
      const preparedFixture = preparedFixtures.find((it): boolean => it.companyInformation.companyName == name)!;
      if (!preparedFixture) {
        throw new ReferenceError(
          "Variable preparedFixture is undefined because the provided company name could not be found in the prepared fixtures."
        );
      } else {
        return preparedFixture;
      }
    } // TODO delete at the end if not needed

    function uploadCompanyAndEuTaxonomyDataForFinancialsViaApi(
      companyInformation: CompanyInformation,
      testData: LksgData
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany) => {
            return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, testData);
          }
        );
      });
    }

    function uploadSecondLksgDataSetToExistingCompany() {
      // TODO could set a flag if we want the second lksg data set to be reported in another year or same year
      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        const existingCompanyId = getCompanyIdByName(token, "two-lksg-data-sets")
        const dataSet = generateLksgData();
        await uploadOneLksgDatasetViaApi(token, existingCompanyId, dataSet);
      });
    }

    function uploadEuTaxonomyFinancialsDataSetToExistingCompany() {
      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        const existingCompanyId = getCompanyIdByName(token, "two-different-data-sets")
        const dataSet = generateEuTaxonomyDataForFinancials();
        await uploadOneEuTaxonomyFinancialsDatasetViaApi(token, existingCompanyId, dataSet);
      });
    }

    async function getCompanyIdByName(token: string, companyName: string): string {
      return (await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
              companyName
          )
      ).data[0].companyId;
    }

    function pickOneUploadedLksgDataSetAndVerifyLksgPageForIt(): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return getOneCompanyThatHasDataForDataType(token, DataTypeEnum.Lksg).then((storedCompany) => {
          // TODO get one lksg data set for this company and put it into a variable
          // TODO then start to do the actual test:
          cy.intercept("**/api/data/lksg/*").as("retrieveLksgData");
          cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg`);
          cy.wait("@retrieveLksgData", { timeout: timeout }).then(() => {
            // TODO verify code by comparing the displayed data to the actual lksg dataset and by general
            // TODO checks
          });
        });
      });
    }

    it("Check Lksg view page for company with one Lksg data set", () => {
      pickOneUploadedLksgDataSetAndVerifyLksgPageForIt();
    });

    it("Check Lksg view page for company with two Lksg data sets reported for the same year", () => {
      // uploadSecondLksgDataSetToExistingCompany( companyName: "two-lksg-data-sets-same-year", sameYear: true)
      // checks
    });

    it("Check Lksg view page for company with two Lksg data sets reported in different years", () => {
      // uploadSecondLksgDataSetToExistingCompany( companyName: "two-lksg-data-sets-different-years", sameYear: false)
      // checks
    });

    // TODO find the right place for this test
    it("Check the dropdown menu works as expected", () => {
      uploadEuTaxonomyFinancialsDataSetToExistingCompany()
      // TODO finish test after dropdown is there.
    });
  }
);
