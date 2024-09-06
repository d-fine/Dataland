import CreateEuTaxonomyNonFinancials from '@/components/forms/CreateEuTaxonomyNonFinancials.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { TEST_PDF_FILE_BASEPATH, TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import { type CompanyAssociatedDataEutaxonomyNonFinancialsData } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import DataPointFormWithToggle from '@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { selectItemFromDropdownByIndex, selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';
import { getFilledKpis } from '@/utils/DataPoint';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { PAGE_NUMBER_VALIDATION_ERROR_MESSAGE } from '@/utils/ValidationUtils';

describe('Component tests for the Eu Taxonomy for non financials that test dependent fields', () => {
  const uploadReports = new UploadReports('referencedReports');

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
   * whose name equals the one of a file selected before
   */
  function checkFileWithExistingFilenameIsNotBeingAdded(): void {
    const reportThatCanBeUploaded = 'test-report';
    const reportThatAlreadyExists = TEST_PDF_FILE_NAME;
    uploadReports.selectFile(reportThatCanBeUploaded);
    uploadReports.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportThatCanBeUploaded);
    uploadReports.selectFile(reportThatAlreadyExists);
    uploadReports.validateReportIsListedAsAlreadyUploaded(reportThatAlreadyExists);
    uploadReports.validateReportIsNotInFileSelectorAndHasNoInfoForm(reportThatAlreadyExists);
    uploadReports.validateNumberOfReportsSelectedForUpload(1);
  }

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
   * whose name equals the one of a file selected before
   */
  function checkFileWithExistingFilenameOpensDialogWithWarning(): void {
    uploadReports.selectFile(TEST_PDF_FILE_NAME);
    cy.get(`button[data-test='upload-files-button-referencedReports']`).click();
    cy.get('input[type=file]').selectFile(
      `../${TEST_PDF_FILE_BASEPATH}/more-pdfs-in-seperate-directory/${TEST_PDF_FILE_NAME}.pdf`,
      { force: true }
    );
    cy.get('.p-dialog-content').should('contain.text', 'Files with duplicate names');
    cy.get('.p-dialog-header-close').click();
    cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should('have.length', 1);
  }

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
   * whose name contains an illegal character
   */
  function checkFileWithIllegalCharacterOpensDialogWithWarning(): void {
    uploadReports.selectDummyFile('Invalid:Filename', 400);
    cy.get('.p-dialog-content').should('contain.text', 'File names containing illegal characters');
    cy.get('.p-dialog-header-close').click();
    cy.get(`[data-test="Invalid:FilenameToUploadContainer"]`).should('not.exist');
  }

  /**
   * Adds a report to upload and removes it again afterwards checking that no dialog regarding a duplicate file name
   * is wrongly triggered and that the file is correctly removed.
   */
  function checkExistingFilenameDialogDidNotBreakSubsequentSelection(): void {
    const reportNameA = TEST_PDF_FILE_NAME;
    const reportNameB = `${TEST_PDF_FILE_NAME}2`;
    uploadReports.selectFile(reportNameB);

    cy.get('.p-dialog-content').should('not.exist');
    uploadReports.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameA);
    uploadReports.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);

    uploadReports.removeReportFromSelectionForUpload(TEST_PDF_FILE_NAME);

    uploadReports.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameA);
    uploadReports.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);
    uploadReports.validateNumberOfReportsSelectedForUpload(1);

    uploadReports.removeReportFromSelectionForUpload(reportNameB);

    uploadReports.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameB);
    uploadReports.validateNumberOfReportsSelectedForUpload(0);
  }

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that submission is denied
   * if a report is not referenced
   */
  function checkThatFilesMustBeReferenced(): void {
    uploadReports.fillAllFormsOfReportsSelectedForUpload();
    submitButton.clickButton();
    cy.get('[data-test="failedUploadMessage"]').should('exist').should('contain.text', 'test-report');
    cy.get('[data-test="failedUploadMessage"]')
      .should('exist')
      .should('satisfy', (element: JQuery<HTMLElement>) => {
        const expectedStrings = ['Not all uploaded reports are used', 'test-report'];
        const elementText = element.text();
        return expectedStrings.every((expectedString) => elementText.includes(expectedString));
      });
  }

  /**
   * this method fills and validates the assurance report page number in the general section
   */
  function fillAndValidateAssuranceReportPageNumber(): void {
    const invalidPageInputs = ['string', '0', '01', '0.5', '-13', '5-3', '5-5', '5-', '-5'];
    for (const invalidPageInput of invalidPageInputs) {
      cy.get('div[label="General"] input[name="page"]:not([type="hidden"])').last().clear().type(invalidPageInput);
      cy.get('div[label="General"] em[title="Page"]:not([type="hidden"])').last().click();
      cy.get('[data-message-type="validation"]')
        .should('contain', PAGE_NUMBER_VALIDATION_ERROR_MESSAGE)
        .should('exist');
    }
    const validPageInputs = ['3', '10-11'];
    for (const validPageInput of validPageInputs) {
      cy.get('div[label="General"] input[name="page"]:not([type="hidden"])').last().clear().type(validPageInput);
      cy.get('div[label="General"] em[title="Page"]:not([type="hidden"])').last().click();
    }
  }

  /**
   * this method fills and checks the general section
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateGeneralSection(reports: string[]): void {
    cy.get('[data-test="fiscalYearEnd"] button').should('have.class', 'p-datepicker-trigger').click();
    cy.get('div.p-datepicker').find('button[aria-label="Next Month"]').click();
    cy.get('div.p-datepicker').find('span:contains("11")').click();
    cy.get('div[data-test="fiscalYearEnd"] input[name="value"]').invoke('val').should('contain', '11');
    cy.get('div[data-test="fiscalYearDeviation"] input[type="checkbox"][value="Deviation"]').check();
    cy.get('div[data-test="submitSideBar"] li:last a').click();
    cy.get('div[data-test="scopeOfEntities"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="euTaxonomyActivityLevelReporting"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="numberOfEmployees"] input[name="value"]').clear().type('-13');
    cy.get('em[title="Number Of Employees"]').click();
    cy.get(`[data-message-type="validation"]`).should('contain', 'at least 0').should('exist');
    cy.get('div[data-test="numberOfEmployees"] input[name="value"]').clear().type('333');
    cy.get('div[data-test="nfrdMandatory"] input[type="checkbox"][value="Yes"]').click();
    selectItemFromDropdownByIndex(cy.get('div[name="value"'), 2);
    cy.get('input[name="provider"]').clear().type('Assurance Provider');
    cy.get('div[label="General"] div[name="fileName"]').each((reportField) =>
      selectItemFromDropdownByValue(cy.wrap(reportField), reports[0])
    );
    fillAndValidateAssuranceReportPageNumber();
  }

  /**
   * Types a value into a data point in the "Revenue" segment of the upload form
   * @param value to type
   * @param dataTestMarker marks the specific input field to type into
   */
  function insertValueIntoRevenueDataPoint(value: string, dataTestMarker: string): void {
    cy.get(`div[label="Revenue"] div[data-test="${dataTestMarker}"] input[name="value"]`).clear().type(value);
  }

  /**
   * Toggles a data point in the "Revenue" segment of the upload form
   * @param dataTestMarker identifies the specific data point to toggle
   */
  function toggleDataPoint(dataTestMarker: string): void {
    cy.get(
      `div[label="Revenue"] div[data-test="${dataTestMarker}"] 
        div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]`
    )
      .should('exist')
      .click();
  }

  /**
   * Toggles a data point inside a sub-segment of the segment "Revenue" of the upload form
   * @param dataTestMarkerOfSubSegment identifies the sub-segment
   * @param dataTestMarkerOfDataPoint identifies the data point itself
   */
  function toggleDataPointInSubSegmentOfRevenue(
    dataTestMarkerOfSubSegment: string,
    dataTestMarkerOfDataPoint: string
  ): void {
    cy.get(
      `div[label="Revenue"] div[data-test=${dataTestMarkerOfSubSegment}] div[data-test="${dataTestMarkerOfDataPoint}"] 
        div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]`
    )
      .should('exist')
      .click();
  }

  /**
   * this method fills and checks the Revenue section, excluding the activity component
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateOtherSections(reports: string[]): void {
    cy.get('div[label="Revenue"] div[data-test="totalAmount"] input[name="value"]').clear().type('130000');
    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[data-test="totalAmount"] div[name="currency"]'), 1);
    selectItemFromDropdownByValue(
      cy.get('div[label="Revenue"] div[data-test="totalAmount"] div[name="fileName"]'),
      reports[0]
    );

    cy.get('div[label="Revenue"] div[data-test="totalAmount"] input[name="page"]')
      .not('[type="hidden"]')
      .clear()
      .type('5');

    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[data-test="totalAmount"] div[name="quality"]'), 2);
    cy.get('div[label="Revenue"] div[data-test="totalAmount"] textarea[name="comment"]').clear().type('just a comment');

    toggleDataPointInSubSegmentOfRevenue('eligibleShare', 'relativeShareInPercent');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]').eq(0).clear().type('a');
    cy.get('div[label="Revenue"] em[title="Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`)
      .should('contain', 'must be a number')
      .should('exist');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]')
      .eq(0)
      .clear()
      .type('120');
    cy.get('div[label="Revenue"] em[title="Eligible Revenue"]').eq(0).click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`)
      .should('contain', 'must be between 0 and 100')
      .should('exist');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]').eq(0).clear().type('25');
    toggleDataPointInSubSegmentOfRevenue('eligibleShare', 'absoluteShare');
    cy.get('div[label="Revenue"] div[data-test="absoluteShare"] input[name="value"]').eq(0).clear().type('5000');
    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[name="currency"]').eq(0), 5);
    toggleDataPointInSubSegmentOfRevenue('alignedShare', 'relativeShareInPercent');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]').eq(1).clear().type('50');
    toggleDataPointInSubSegmentOfRevenue('alignedShare', 'absoluteShare');
    cy.get('div[label="Revenue"] div[data-test="absoluteShare"] input[name="value"]').eq(1).clear().type('4000');
    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[name="currency"]').eq(1), 51);
    toggleDataPoint('substantialContributionToClimateChangeMitigationInPercentEligible');
    insertValueIntoRevenueDataPoint('a', 'substantialContributionToClimateChangeMitigationInPercentEligible');
    cy.get('div[label="Revenue"] em[title="Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`)
      .should('contain', 'must be a number')
      .should('exist');
    insertValueIntoRevenueDataPoint('-12', 'substantialContributionToClimateChangeMitigationInPercentEligible');
    cy.get('div[label="Revenue"] em[title="Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`)
      .should('contain', 'must be between 0 and 100')
      .should('exist');
    insertValueIntoRevenueDataPoint('15', 'substantialContributionToClimateChangeMitigationInPercentEligible');
    toggleDataPoint('substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEligible');
    insertValueIntoRevenueDataPoint(
      '15',
      'substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEligible'
    );
    toggleDataPoint('substantialContributionToTransitionToACircularEconomyInPercentEligible');
    insertValueIntoRevenueDataPoint('15', 'substantialContributionToTransitionToACircularEconomyInPercentEligible');
    toggleDataPoint('substantialContributionToPollutionPreventionAndControlInPercentEligible');
    insertValueIntoRevenueDataPoint('15', 'substantialContributionToPollutionPreventionAndControlInPercentEligible');
    toggleDataPoint('substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEligible');
    insertValueIntoRevenueDataPoint(
      '15',
      'substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEligible'
    );
    toggleDataPointInSubSegmentOfRevenue('nonAlignedShare', 'relativeShareInPercent');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]').eq(2).clear().type('11');
    toggleDataPointInSubSegmentOfRevenue('nonAlignedShare', 'absoluteShare');
    cy.get('div[label="Revenue"] div[data-test="absoluteShare"] input[name="value"]').eq(2).clear().type('12000');
    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[name="currency"]').eq(2), 51);
    toggleDataPointInSubSegmentOfRevenue('nonEligibleShare', 'relativeShareInPercent');
    cy.get('div[label="Revenue"] div[data-test="relativeShareInPercent"] input[name="value"]').eq(3).clear().type('13');
    toggleDataPointInSubSegmentOfRevenue('nonEligibleShare', 'absoluteShare');
    cy.get('div[label="Revenue"] div[data-test="absoluteShare"] input[name="value"]').eq(3).clear().type('13000');
    selectItemFromDropdownByIndex(cy.get('div[label="Revenue"] div[name="currency"]').eq(3), 53);
    toggleDataPoint('enablingShareInPercent');
    insertValueIntoRevenueDataPoint('12', 'enablingShareInPercent');
    toggleDataPoint('transitionalShareInPercent');
    insertValueIntoRevenueDataPoint('12', 'transitionalShareInPercent');
  }

  /**
   * This method returns a mocked dataset for eu taxonomy for non financials with some fields filled.
   * @returns the dataset
   */
  function createMockCompanyAssociatedDataEutaxoNonFinancials(): CompanyAssociatedDataEutaxonomyNonFinancialsData {
    return {
      companyId: 'abc',
      reportingPeriod: '2020',
      data: {
        capex: {
          totalAmount: {
            quality: 'Estimated',
            dataSource: {
              fileName: `${TEST_PDF_FILE_NAME}FileCopy`,
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              page: '12',
            },
            comment: 'test',
            value: 12000000,
            currency: 'EUR',
          },
        },
        opex: {
          totalAmount: {
            quality: 'Estimated',
            dataSource: {
              fileName: 'None...',
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              page: '12',
            },
            comment: 'test',
            value: 10000000,
            currency: 'EUR',
          },
        },
        revenue: {
          totalAmount: {
            quality: 'Estimated',
            dataSource: {
              fileName: 'None...',
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              page: '12-14',
            },
            comment: 'test',
            value: 40000000,
            currency: 'EUR',
          },
        },
        general: {
          fiscalYearDeviation: {
            value: 'Deviation',
          },
          fiscalYearEnd: {
            value: '2023-09-11',
          },
          scopeOfEntities: {
            value: 'Yes',
          },
          nfrdMandatory: {
            value: 'Yes',
          },
          euTaxonomyActivityLevelReporting: {
            value: 'Yes',
          },
          assurance: {
            value: 'None',
            provider: 'Assurance Provider',
            dataSource: {
              fileName: TEST_PDF_FILE_NAME,
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              page: '1',
            },
          },
          numberOfEmployees: {
            value: 333,
          },
          referencedReports: {
            [`${TEST_PDF_FILE_NAME}FileCopy`]: {
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              publicationDate: '2023-07-12',
            },
            [TEST_PDF_FILE_NAME]: {
              fileReference: 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7',
              publicationDate: '2023-07-12',
            },
          },
        },
      },
    };
  }

  const companyAssociatedDataEutaxoNonFinancials = createMockCompanyAssociatedDataEutaxoNonFinancials();

  it('Check that warning appears if two pdf files with same name or illegal character are selected for upload', () => {
    cy.stub(DataPointFormWithToggle);
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          companyID: 'company-id-does-not-matter-in-this-test',
        },
      },
    })(CreateEuTaxonomyNonFinancials).then(() => {
      checkFileWithExistingFilenameOpensDialogWithWarning();
      checkFileWithIllegalCharacterOpensDialogWithWarning();
      checkExistingFilenameDialogDidNotBreakSubsequentSelection();
    });
  });

  it('Open upload page prefilled and assure that new file needs unique name and has to be referenced ', () => {
    cy.stub(DataPointFormWithToggle);

    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      // Ignored, as overwriting the data function is not safe anyway
      // @ts-ignore
    })(CreateEuTaxonomyNonFinancials, {
      props: {
        companyID: 'company-id-does-not-matter-in-this-test',
      },
      data() {
        return {
          referencedReportsForPrefill: companyAssociatedDataEutaxoNonFinancials?.data?.general?.referencedReports,
          companyAssociatedEutaxonomyNonFinancialsData: companyAssociatedDataEutaxoNonFinancials,
        };
      },
    }).then(() => {
      checkFileWithExistingFilenameIsNotBeingAdded();
      checkThatFilesMustBeReferenced();
    });
  });

  it('Open upload page, fill out and validate the upload form, except for new activities', () => {
    cy.stub(DataPointFormWithToggle);
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      // Ignored, as overwriting the data function is not safe anyway
      // @ts-ignore
    })(CreateEuTaxonomyNonFinancials, {
      data() {
        return {
          referencedReportsForPrefill: companyAssociatedDataEutaxoNonFinancials?.data?.general?.referencedReports,
          companyAssociatedEutaxonomyNonFinancialsData: companyAssociatedDataEutaxoNonFinancials,
          listOfFilledKpis: getFilledKpis(companyAssociatedDataEutaxoNonFinancials),
        };
      },
    }).then(() => {
      uploadReports.selectFile(TEST_PDF_FILE_NAME);
      fillAndValidateGeneralSection([TEST_PDF_FILE_NAME]);
      fillAndValidateOtherSections([TEST_PDF_FILE_NAME]);
    });
  });
});
