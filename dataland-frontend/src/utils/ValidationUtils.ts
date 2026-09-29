import { type FormKitNode } from '@formkit/core';
import { findAllValuesForKey, type ObjectType } from '@/utils/UpdateObjectUtils';

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
 * @param [uploadedReports] the names of the reports that were uploaded via form
 * @param [namesAndReferencesOfAllCompanyReports] a map from report name to the report's file reference,
 * needed because data points only store the "fileReference" of the report they reference (not its name)
 * returns nothing but throws an error if not all reports are referenced
 */
export function checkIfAllUploadedReportsAreReferencedInDataModel(
  dataModel: ObjectType,
  uploadedReports: string[],
  namesAndReferencesOfAllCompanyReports: ObjectType
): void {
  // "referencedReports" itself is excluded from the search: it is a registry listing every report that was
  // selected/uploaded (each carrying its own fileReference), not evidence that a report is actually used as a
  // data source. Without this exclusion, every uploaded report would trivially "reference" itself and this check
  // would never flag genuinely unused reports.
  const referencedReportFileReferences = findAllValuesForKey(dataModel, 'fileReference', ['referencedReports']);
  const unusedReports: string[] = [];
  for (const report of uploadedReports) {
    const fileReference = namesAndReferencesOfAllCompanyReports[report];
    if (typeof fileReference !== 'string' || !referencedReportFileReferences.includes(fileReference)) {
      unusedReports.push(report);
    }
  }
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
 * Checks if a company ID is valid
 * @param companyId id as string
 * @returns boolean if the company is valid
 */
export function isCompanyIdValid(companyId: string): boolean {
  const uuidRegexExp = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegexExp.test(companyId);
}

/**
 * Matches a single page-reference list entry, i.e. either a single page number ("4") or an ascending page
 * range ("4-7").
 */
export const regexPageNumber = /^([1-9]\d*)(?:-([1-9]\d*))?$/;

export const PAGE_NUMBER_VALIDATION_ERROR_MESSAGE =
  'Page(s) must be a positive integer, a range of ascending positive integers, or a comma-separated, ' +
  'strictly ascending list of these, e.g. 2, 13-15, or 4, 112.';

/**
 * A single parsed page-reference list entry, e.g. "4" becomes { start: 4, end: 4 } and "4-7" becomes
 * { start: 4, end: 7 }.
 */
export interface PageEntry {
  start: number;
  end: number;
}

/**
 * Parses a single, already-trimmed page-reference list entry into a PageEntry.
 * @param entry the trimmed single entry to parse, e.g. "4" or "4-7"
 * @returns the parsed PageEntry, or undefined if the entry is not a valid single page number or an
 * ascending page range
 */
export function parsePageEntry(entry: string): PageEntry | undefined {
  const match = regexPageNumber.exec(entry);
  if (!match) return undefined;

  const start = Number(match[1]);
  const end = match[2] === undefined ? start : Number(match[2]);
  if (match[2] !== undefined && start >= end) return undefined;

  return { start, end };
}

/**
 * Checks if a page reference is valid. A valid page reference is a comma-separated list of single page
 * numbers and/or ascending page ranges (e.g. "4", "4-5" or "4, 112"). Whitespace around list entries is
 * allowed and ignored. List entries must be listed in strictly ascending, non-overlapping order. This
 * logic must be kept consistent with the backend's PageRangeValidator (dataland-backend PageRange.kt).
 * @param value the page reference string to validate
 * @returns true if the page reference is valid, false otherwise
 */
export function isPageReferenceValid(value: string): boolean {
  const parsedEntries = value.split(',').map((entry) => parsePageEntry(entry.trim()));

  return parsedEntries.reduce<{ isValidSoFar: boolean; previousEnd: number }>(
    (result, entry) => ({
      isValidSoFar: result.isValidSoFar && entry !== undefined && entry.start > result.previousEnd,
      previousEnd: entry?.end ?? result.previousEnd,
    }),
    { isValidSoFar: true, previousEnd: 0 }
  ).isValidSoFar;
}

/**
 * Checks if a page number is valid
 * @param node FormKitNode
 * @returns boolean that expresses if the page number is valid
 */
export function validatePageNumber(node: FormKitNode): boolean {
  const pageNumber = node.value;
  if (typeof pageNumber !== 'string') return false;

  return isPageReferenceValid(pageNumber);
}

// This RegEx should be kept consistent with the regex used in the backend and defined by EmailUtils.kt
const emailAddressRegex = /^[a-zA-Z0-9_.!+-]+@([a-zA-Z0-9-]+\.){1,2}[a-zA-Z]{2,}$/;

/**
 * Checks if an email address is valid using a regex
 * @param emailAddress the email string to check
 * @returns true if the email is valid, false otherwise
 */
export function isEmailAddressValid(emailAddress: string): boolean {
  return emailAddressRegex.test(emailAddress.toLowerCase());
}
