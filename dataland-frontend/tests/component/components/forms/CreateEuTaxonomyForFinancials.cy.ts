import CreateEuTaxonomyForFinancials from "@/components/forms/CreateEuTaxonomyForFinancials.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import {
  type CompanyAssociatedDataEuTaxonomyDataForFinancials,
  type ExtendedDataPointBigDecimal,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
} from "@clients/backend";

describe("Component tests for the Eu Taxonomy Financials", () => {
  /**
   * Enters a single decimal inputs field value in the upload eutaxonomy-financials form
   * @param divTag value of the parent div data-test attribute to fill in
   * @param inputsTag value of the parent div data-test attribute to fill in
   * @param value the value to fill in
   */
  function fillField(divTag: string, inputsTag: string, value?: ExtendedDataPointBigDecimal | null): void {
    if (value?.value) {
      const valueAsString = value.value.toString();
      if (divTag === "") {
        cy.get(`[data-test="${inputsTag}"]`).find('input[name="value"]').type(valueAsString);
        cy.get(`[data-test="${inputsTag}"]`).find('input[name="page"]').type("13");
        cy.get(`[data-test="${inputsTag}"]`).find('select[name="fileName"]').select(1);
        cy.get(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
        cy.get(`[data-test="${inputsTag}"]`)
          .find('textarea[name="comment"]')
          .type(`${value.comment ?? "comment"}`);
      } else {
        cy.get(`[data-test="${divTag}"]`)
          .find(`[data-test="${inputsTag}"]`)
          .find('input[name="value"]')
          .type(valueAsString);
        cy.get(`[data-test="${divTag}"]`)
          .find(`[data-test="${inputsTag}"]`)
          .find('input[name="page"]')
          .type(`${value.dataSource?.page ?? "13"}`);
        cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="fileName"]').select(1);
        cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
        cy.get(`[data-test="${divTag}"]`)
          .find(`[data-test="${inputsTag}"]`)
          .find('textarea[name="comment"]')
          .type(`${value.comment ?? "comment"}`);
      }
    }
  }

  /**
   * Fills a set with eligibility-kpis for different company types
   * @param divTag value of the parent div data-test attribute to fill in
   * @param data the kpi data to use to fill the form
   */
  function fillEligibilityKpis(divTag: string, data: EligibilityKpis | undefined): void {
    fillField(divTag, "taxonomyEligibleActivityInPercent", data?.taxonomyEligibleActivityInPercent);
    fillField(divTag, "taxonomyNonEligibleActivityInPercent", data?.taxonomyNonEligibleActivityInPercent);
    fillField(divTag, "derivativesInPercent", data?.derivativesInPercent);
    fillField(divTag, "banksAndIssuersInPercent", data?.banksAndIssuersInPercent);
    fillField(divTag, "investmentNonNfrdInPercent", data?.investmentNonNfrdInPercent);
  }

  /**
   * this method fills company services type KPIs
   * @param data the data to fill the form with
   */
  function fillCompanyServicesTypeKPIs(data: EuTaxonomyDataForFinancials): void {
    fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
    fillEligibilityKpis("insuranceKpis", data.eligibilityKpis?.InsuranceOrReinsurance);
    fillEligibilityKpis("investmentFirmKpis", data.eligibilityKpis?.InvestmentFirm);
    fillEligibilityKpis("assetManagementKpis", data.eligibilityKpis?.AssetManagement);
    fillField(
      "insuranceKpis",
      "taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
      data.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
    );
    fillField("investmentFirmKpis", "greenAssetRatioInPercent", data.investmentFirmKpis?.greenAssetRatioInPercent);
    fillField(
      "creditInstitutionKpis",
      "tradingPortfolioAndInterbankLoansInPercent",
      data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent,
    );
    fillField(
      "creditInstitutionKpis",
      "tradingPortfolioInPercent",
      data.creditInstitutionKpis?.tradingPortfolioInPercent,
    );
    fillField("creditInstitutionKpis", "interbankLoansInPercent", data.creditInstitutionKpis?.interbankLoansInPercent);
    fillField(
      "creditInstitutionKpis",
      "greenAssetRatioInPercent",
      data.creditInstitutionKpis?.greenAssetRatioInPercent,
    );
  }

  /**
   * this method fills general section
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateGeneralSectionEuTaxonomyFinancial(reports: string[]): void {
    cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
    cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find('span:contains("10")').click();
    cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "10");
    cy.get('div[data-test="submitSideBar"] li:last a').click({ force: true });
    cy.get('input[name="scopeOfEntities"][value="No"]').check();
    cy.get('input[name="euTaxonomyActivityLevelReporting"][value="No"]').check();
    cy.get('input[name="numberOfEmployees"]').clear().type("133");
    cy.get('input[name="nfrdMandatory"][value="No"]').check();
    cy.get('div[data-test="assuranceSection"] select[name="value"]').select(2);
    cy.get('div[data-test="assuranceSection"] input[name="provider"]').clear().type("Provider of assurance");
    cy.get('div[data-test="assuranceSection"] select[name="fileName"]').select(reports);
    cy.get('div[data-test="assuranceSection"] input[name="page"]').clear().type("4");
  }

  /**
   * This method returns a mocked dataset for eu taxonomy for non financials with some fields filled.
   * @returns the dataset
   */
  function createMockCompanyAssociatedDataEuTaxoFinancials(): CompanyAssociatedDataEuTaxonomyDataForFinancials {
    return {
      companyId: "cba",
      reportingPeriod: "2022",

      data: {
        financialServicesTypes: ["CreditInstitution", "InsuranceOrReinsurance", "AssetManagement", "InvestmentFirm"],
        eligibilityKpis: {
          InvestmentFirm: {
            taxonomyEligibleActivityInPercent: {
              value: 57.6343,
              quality: "Audited",
              comment: "connect auxiliary alarm",
              dataSource: {
                page: 173,
                tagName: "experiences",
                fileName: "IntegratedReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            taxonomyNonEligibleActivityInPercent: {
              value: 79.3501,
              quality: "Estimated",
              comment: "navigate virtual hard drive",
              dataSource: {
                page: 233,
                tagName: "technologies",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            derivativesInPercent: {
              value: 88.4483,
              quality: "Estimated",
              comment: null,
              dataSource: null,
            },
            banksAndIssuersInPercent: {
              value: 78.8514,
              quality: "Incomplete",
              comment: "transmit virtual interface",
              dataSource: {
                page: 252,
                tagName: "applications",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            investmentNonNfrdInPercent: {
              value: 7.8117,
              quality: "Estimated",
              comment: null,
              dataSource: null,
            },
          },
          AssetManagement: {
            taxonomyEligibleActivityInPercent: {
              value: 79.936,
              quality: "Incomplete",
              comment: "input 1080p firewall",
              dataSource: {
                page: 740,
                tagName: "infrastructures",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            taxonomyNonEligibleActivityInPercent: {
              value: 90.8148,
              quality: "Reported",
              comment: "bypass wireless pixel",
              dataSource: {
                page: 1150,
                tagName: "action-items",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            derivativesInPercent: {
              value: 93.517,
              quality: "Estimated",
              comment: "connect open-source driver",
              dataSource: {
                page: 41,
                tagName: "mindshare",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            banksAndIssuersInPercent: {
              value: 59.4758,
              quality: "Estimated",
              comment: null,
              dataSource: null,
            },
            investmentNonNfrdInPercent: {
              value: 93.5808,
              quality: "Incomplete",
              comment: null,
              dataSource: null,
            },
          },
          InsuranceOrReinsurance: {
            taxonomyEligibleActivityInPercent: {
              value: 0.924,
              quality: "Audited",
              comment: "program redundant capacitor",
              dataSource: {
                page: 604,
                tagName: "supply-chains",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            taxonomyNonEligibleActivityInPercent: {
              value: 17.2237,
              quality: "Reported",
              comment: "index wireless sensor",
              dataSource: {
                page: 642,
                tagName: "relationships",
                fileName: "SustainabilityReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            derivativesInPercent: {
              value: 43.2518,
              quality: "Estimated",
              comment: "calculate online microchip",
              dataSource: {
                page: 1154,
                tagName: "experiences",
                fileName: "IntegratedReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            banksAndIssuersInPercent: {
              value: 54.1805,
              quality: "Incomplete",
              comment: null,
              dataSource: null,
            },
            investmentNonNfrdInPercent: {
              value: 83.4182,
              quality: "Audited",
              comment: "program optical sensor",
              dataSource: {
                page: 205,
                tagName: "bandwidth",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
          },
          CreditInstitution: {
            taxonomyEligibleActivityInPercent: {
              value: 82.4142,
              quality: "Estimated",
              comment: "synthesize back-end microchip",
              dataSource: {
                page: 1152,
                tagName: "networks",
                fileName: "IntegratedReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            taxonomyNonEligibleActivityInPercent: {
              value: 65.1291,
              quality: "Incomplete",
              comment: "bypass back-end program",
              dataSource: {
                page: 483,
                tagName: "blockchains",
                fileName: "SustainabilityReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            derivativesInPercent: {
              value: 57.4296,
              quality: "Estimated",
              comment: null,
              dataSource: null,
            },
            banksAndIssuersInPercent: {
              value: 41.0106,
              quality: "Reported",
              comment: "program solid state program",
              dataSource: {
                page: 48,
                tagName: "portals",
                fileName: "ESEFReport",
                fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
              },
            },
            investmentNonNfrdInPercent: {
              value: 88.9066,
              quality: "Estimated",
              comment: null,
              dataSource: null,
            },
          },
        },
        creditInstitutionKpis: {
          tradingPortfolioInPercent: {
            value: 10.3479,
            quality: "Audited",
            comment: "program auxiliary array",
            dataSource: {
              page: 552,
              tagName: "methodologies",
              fileName: "SustainabilityReport",
              fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            },
          },
          interbankLoansInPercent: {
            value: 47.5173,
            quality: "Incomplete",
            comment: null,
            dataSource: null,
          },
          tradingPortfolioAndInterbankLoansInPercent: {
            value: 33.1361,
            quality: "Audited",
            comment: "synthesize cross-platform pixel",
            dataSource: {
              page: 844,
              tagName: "methodologies",
              fileName: "SustainabilityReport",
              fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            },
          },
          greenAssetRatioInPercent: {
            value: 52.4804,
            quality: "Audited",
            comment: "program virtual transmitter",
            dataSource: {
              page: 840,
              tagName: "e-commerce",
              fileName: "SustainabilityReport",
              fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            },
          },
        },
        investmentFirmKpis: {
          greenAssetRatioInPercent: {
            value: 7.904,
            quality: "Audited",
            comment: "reboot auxiliary alarm",
            dataSource: {
              page: 1025,
              tagName: "users",
              fileName: "ESEFReport",
              fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            },
          },
        },
        insuranceKpis: {
          taxonomyEligibleNonLifeInsuranceActivitiesInPercent: {
            value: 47.3072,
            quality: "Incomplete",
            comment: "parse back-end firewall",
            dataSource: {
              page: 1084,
              tagName: "bandwidth",
              fileName: "ESEFReport",
              fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            },
          },
        },
        fiscalYearDeviation: "NoDeviation",
        fiscalYearEnd: "2023-07-08",
        scopeOfEntities: "NA",
        nfrdMandatory: "Yes",
        euTaxonomyActivityLevelReporting: "No",
        assurance: {
          value: "ReasonableAssurance",
          dataSource: null,
          provider: null,
        },
        numberOfEmployees: 40590,
        referencedReports: {
          [TEST_PDF_FILE_NAME]: {
            fileReference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
            isGroupLevel: "No",
            reportDate: "2023-07-12",
            currency: "EUR",
          },
        },
      },
    };
  }

  const companyAssociatedEuTaxoFinancialsData = createMockCompanyAssociatedDataEuTaxoFinancials();

  it("Open upload page, fill out and validate the upload form", () => {
    cy.mountWithPlugins(CreateEuTaxonomyForFinancials, {
      keycloak: minimalKeycloakMock({}),
      props: {
        companyID: companyAssociatedEuTaxoFinancialsData.companyId,
      },
      data() {
        return {
          templateDataset: companyAssociatedEuTaxoFinancialsData?.data,
          selectedFinancialServiceOptions: [
            { label: "Credit Institution", value: "creditInstitutionKpis" },
            { label: "Insurance & Re-insurance", value: "insuranceKpis" },
            { label: "Asset Management", value: "assetManagementKpis" },
            { label: "Investment Firm", value: "investmentFirmKpis" },
          ],
          confirmedSelectedFinancialServiceOptions: [
            { label: "Credit Institution", value: "creditInstitutionKpis" },
            { label: "Insurance & Re-insurance", value: "insuranceKpis" },
            { label: "Asset Management", value: "assetManagementKpis" },
            { label: "Investment Firm", value: "investmentFirmKpis" },
          ],
          confirmedSelectedFinancialServiceTypes: [
            "CreditInstitution",
            "InsuranceOrReinsurance",
            "AssetManagement",
            "InvestmentFirm",
          ],
        };
      },
    }).then(() => {
      uploadDocuments.selectFile(TEST_PDF_FILE_NAME);
      fillAndValidateGeneralSectionEuTaxonomyFinancial([TEST_PDF_FILE_NAME]);
      fillCompanyServicesTypeKPIs(companyAssociatedEuTaxoFinancialsData.data);

      cy.get(
        'div[data-test="investmentFirmKpis"] div[data-test="greenAssetRatioInPercent"] div[data-test="dataPointToggle"] div[data-test="dataPointToggleButton"]',
      ).click();
      cy.get('div[data-test="insuranceKpis"] button[data-test="removeSectionButton"]').click();
      cy.get('div[data-test="insuranceKpis"]').should("not.exist");

      cy.intercept("POST", "**/api/data/eutaxonomy-financials", (request) => {
        request.reply(200, {});
      }).as("postEuTaxonomyFinancial");
      cy.get('button[data-test="submitButton"]').should("not.have.class", "button-disabled").click();
      cy.wait("@postEuTaxonomyFinancial").then((interception) => {
        const postedObject = interception.request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials;
        const postedEuTaxonomyFinancialDataset = postedObject.data;
        const interbankLoansInPercentForCreditInstitutionKpis =
          postedEuTaxonomyFinancialDataset?.creditInstitutionKpis?.interbankLoansInPercent?.value;
        expect(interbankLoansInPercentForCreditInstitutionKpis).to.equal("47.5173");
        const banksAndIssuersInPercentForCreditInstitutionKpis =
          postedEuTaxonomyFinancialDataset?.eligibilityKpis?.AssetManagement?.banksAndIssuersInPercent?.value;
        expect(banksAndIssuersInPercentForCreditInstitutionKpis).to.equal("59.4758");
      });
    });
  });
});
