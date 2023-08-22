// TODO adapt to new framework
// import CreateEuTaxonomyForNonFinancials from "@/components/forms/CreateEuTaxonomyForNonFinancials.vue";
// import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
// import { TEST_PDF_FILE_BASEPATH, TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
// import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
// import { type CompanyAssociatedDataEuTaxonomyDataForNonFinancials } from "@clients/backend";
// import { submitButton } from "@sharedUtils/components/SubmitButton";
// import DataPointFormWithToggle from "@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue";
//
// describe("Component tests for the CreateP2pDataset that test dependent fields", () => {
//   /**
//    * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
//    * whose name equals the one of a file selected before
//    */
//   function checkFileWithExistingFilenameIsNotBeingAdded(): void {
//     const reportThatCanBeUploaded = "test-report";
//     const reportThatAlreadyExists = TEST_PDF_FILE_NAME;
//     uploadDocuments.selectFile(reportThatCanBeUploaded);
//     uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportThatCanBeUploaded);
//     uploadDocuments.selectFile(reportThatAlreadyExists);
//     uploadDocuments.validateReportIsListedAsAlreadyUploaded(reportThatAlreadyExists);
//     uploadDocuments.validateReportIsNotInFileSelectorAndHasNoInfoForm(reportThatAlreadyExists);
//     uploadDocuments.validateNumberOfReportsSelectedForUpload(1);
//   }
//
//   /**
//    * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
//    * whose name equals the one of a file selected before
//    */
//   function checkFileWithExistingFilenameOpensDialogWithWarning(): void {
//     uploadDocuments.selectFile(TEST_PDF_FILE_NAME);
//     cy.get(`button[data-test='upload-files-button-UploadReports']`).click();
//     cy.get("input[type=file]").selectFile(
//       `../${TEST_PDF_FILE_BASEPATH}/more-pdfs-in-seperate-directory/${TEST_PDF_FILE_NAME}.pdf`,
//       { force: true },
//     );
//     cy.get(".p-dialog-content").should("contain.text", "already uploaded");
//     cy.get(".p-dialog-header-close").click();
//     cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("have.length", 1);
//   }
//
//   /**
//    * Adds a report to upload and removes it again afterwards checking that no dialog regarding a duplicate file name
//    * is wrongly triggered and that the file is correctly removed.
//    */
//   function checkExistingFilenameDialogDidNotBreakSubsequentSelection(): void {
//     const reportNameA = TEST_PDF_FILE_NAME;
//     const reportNameB = `${TEST_PDF_FILE_NAME}2`;
//     uploadDocuments.selectFile(reportNameB);
//
//     cy.get(".p-dialog-content").should("not.exist");
//     uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameA);
//     uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);
//
//     uploadDocuments.removeReportFromSelectionForUpload(TEST_PDF_FILE_NAME);
//
//     uploadDocuments.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameA);
//     uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);
//     uploadDocuments.validateNumberOfReportsSelectedForUpload(1);
//
//     uploadDocuments.removeReportFromSelectionForUpload(reportNameB);
//
//     uploadDocuments.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameB);
//     uploadDocuments.validateNumberOfReportsSelectedForUpload(0);
//   }
//
//   /**
//    * On the eu taxonomy for non-financial services edit page, this method checks that submission is denied
//    * if a report is not referenced
//    */
//   function checkThatFilesMustBeReferenced(): void {
//     uploadDocuments.fillAllFormsOfReportsSelectedForUpload();
//     submitButton.clickButton();
//     cy.get('[data-test="failedUploadMessage"]').should("exist").should("contain.text", "test-report");
//     cy.get('[data-test="failedUploadMessage"]')
//       .should("exist")
//       .should("satisfy", (element: JQuery<HTMLElement>) => {
//         const expectedStrings = ["Not all uploaded reports are used", "test-report"];
//         const elementText = element.text();
//         return expectedStrings.every((expectedString) => elementText.includes(expectedString));
//       });
//   }
//
//   /**
//    * This method returns a mocked dataset for eu taxo non financials with all fields filled.
//    * @returns the dataset
//    */
//   function createMockCompanyAssociatedDataEuTaxoNonFinancials(): CompanyAssociatedDataEuTaxonomyDataForNonFinancials {
//     return {
//       companyId: "abc",
//       reportingPeriod: "2020",
//       data: {
//         capex: {
//           totalAmount: {
//             quality: "Estimated",
//             dataSource: {
//               report: `${TEST_PDF_FILE_NAME}FileCopy`,
//               page: 12,
//             },
//             comment: "test",
//             value: 12000000,
//           },
//           alignedData: {
//             valueAsPercentage: 0.07,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 7,
//           },
//           eligibleData: {
//             valueAsPercentage: 0.17,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 17,
//           },
//         },
//         opex: {
//           totalAmount: {
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             value: 10000000,
//           },
//           alignedData: {
//             valueAsPercentage: 0.07,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 7,
//           },
//           eligibleData: {
//             valueAsPercentage: 0.17,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 17,
//           },
//         },
//         revenue: {
//           totalAmount: {
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             value: 40000000,
//           },
//           alignedData: {
//             valueAsPercentage: 0.07,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 7,
//           },
//           eligibleData: {
//             valueAsPercentage: 0.3,
//             quality: "Estimated",
//             dataSource: {
//               report: "None...",
//               page: 12,
//             },
//             comment: "test",
//             valueAsAbsolute: 30,
//           },
//         },
//         fiscalYearDeviation: "Deviation",
//         fiscalYearEnd: "2023-09-11",
//         scopeOfEntities: "Yes",
//         nfrdMandatory: "Yes",
//         euTaxonomyActivityLevelReporting: "Yes",
//         assurance: {
//           assurance: "None",
//           provider: "Assurance Provider",
//           dataSource: {
//             report: TEST_PDF_FILE_NAME,
//             page: 1,
//           },
//         },
//         numberOfEmployees: 333,
//         referencedReports: {
//           [`${TEST_PDF_FILE_NAME}FileCopy`]: {
//             reference: "bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7",
//             isGroupLevel: "No",
//             reportDate: "2023-07-12",
//             currency: "EUR",
//           },
//           [TEST_PDF_FILE_NAME]: {
//             reference: "bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7",
//             isGroupLevel: "No",
//             reportDate: "2023-07-12",
//             currency: "EUR",
//           },
//         },
//       },
//     };
//   }
//
//   const companyAssociatedEuTaxoFinancialsData = createMockCompanyAssociatedDataEuTaxoNonFinancials();
//
//   it("Check that warning appears if two pdf files with same name are selected for upload", () => {
//     cy.stub(DataPointFormWithToggle);
//     cy.mountWithDialog(
//       CreateEuTaxonomyForNonFinancials,
//       {
//         keycloak: minimalKeycloakMock({}),
//       },
//       { companyID: "company-id-does-not-matter-in-this-test" },
//     ).then(() => {
//       checkFileWithExistingFilenameOpensDialogWithWarning();
//       checkExistingFilenameDialogDidNotBreakSubsequentSelection();
//     });
//   });
//
//   it("Open upload page prefilled and assure that only the sections that the dataset holds are displayed", () => {
//     cy.stub(DataPointFormWithToggle);
//     cy.mountWithPlugins(CreateEuTaxonomyForNonFinancials, {
//       keycloak: minimalKeycloakMock({}),
//       data() {
//         return {
//           formInputsModel: companyAssociatedEuTaxoFinancialsData,
//           templateDataset: companyAssociatedEuTaxoFinancialsData.data,
//         };
//       },
//     }).then(() => {
//       checkFileWithExistingFilenameIsNotBeingAdded();
//       checkThatFilesMustBeReferenced();
//     });
//   });
// });
