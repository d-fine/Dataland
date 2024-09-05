import { type FormKitNode } from '@formkit/core';
import { findAllValuesForKey, type ObjectType } from '@/utils/UpdateObjectUtils';
import { ref } from 'vue';

/**
 * Checks which inputs are not filled correctly
 * @param node - single form field
 */
export function checkCustomInputs(node: FormKitNode): void {
  const invalidElements: HTMLElement[] = [];
  node.walk((child: FormKitNode) => {
    // Check if this child has errors
    if ((child.ledger.value('blocking') || child.ledger.value('errors')) && child.type !== 'group') {
      // We found an input with validation errors
      if (typeof child.props.id === 'string') {
        const invalidElement = document.getElementById(child.props.id);
        if (invalidElement) {
          invalidElements.push(invalidElement);
        }
      }
    }
  }, true);
  invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/**
 * checks if all reports that shall be uploaded are used as a data source at least once
 * @param [dataModel] the data model that has a field for referenced reports, named 'report'
 * @param [uploadedReports] the list of reports that were uploaded via form
 * returns nothing but throws an error if not all reports are referenced
 */
export function checkIfAllUploadedReportsAreReferencedInDataModel(
  dataModel: ObjectType,
  uploadedReports: string[]
): void {
  const referencedReports = findAllValuesForKey(dataModel, 'fileName');
  const unusedReports: string[] = [];
  uploadedReports.forEach((report) => {
    if (!referencedReports.some((refReport) => refReport === report)) {
      unusedReports.push(report);
    }
  });
  if (unusedReports.length >= 1) {
    const uploadReportComponent = document.getElementById('uploadReports');
    if (uploadReportComponent) {
      uploadReportComponent.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    throw new Error(
      `Not all uploaded reports are used as a data source. Please remove the following reports, or use them as a data source: ${unusedReports.toString()}`
    );
  }
}

/**
 * Checks if for a given validation the corresponding formkit field requires some input
 * @param validation the formkit validation string
 * @returns true if the validation string contains required else false
 */
export function isInputRequired(validation?: string): boolean {
  return validation?.includes('required') ?? false;
}

/**
 * Checks if a company ID is valid
 * @param companyId id as string
 * @returns boolean if the company is valid
 */
export function isCompanyIdValid(companyId: string): boolean {
  const uuidRegexExp = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegexExp.test(companyId);
}

export const PAGE_NUMBER_VALIDATION_MESSAGE = ref('');
/**
 * Checks if a page number is valid
 * @param node FormKit node
 * @returns boolean that expresses if the page number is valid
 */
export function validatePageNumber(node: FormKitNode): boolean {
  const pageNumberErrorMessage =
    'Page number must be a non-zero number or a range of ascending non-zero numbers,' + ' e.g. 2, 13-15 etc.';
  const pageNumberInput = node.value;
  const regexSinglePage = /^[1-9]\d*$/;
  const regexRange = /^[1-9]\d*-[1-9]\d*$/;
  let result;

  if (typeof pageNumberInput == 'string' && regexSinglePage.test(pageNumberInput)) {
    PAGE_NUMBER_VALIDATION_MESSAGE.value = '';
    result = true;
  } else if (typeof pageNumberInput == 'string' && regexRange.test(pageNumberInput)) {
    const hyphenIndex = pageNumberInput.indexOf('-');
    const firstNumber = Number(pageNumberInput.substring(0, hyphenIndex));
    const secondNumber = Number(pageNumberInput.substring(hyphenIndex + 1));
    if (firstNumber < secondNumber) {
      PAGE_NUMBER_VALIDATION_MESSAGE.value = '';
      result = true;
    } else {
      PAGE_NUMBER_VALIDATION_MESSAGE.value = pageNumberErrorMessage;
      result = false;
    }
  } else {
    PAGE_NUMBER_VALIDATION_MESSAGE.value = pageNumberErrorMessage;
    result = false;
  }
  return result;
}
