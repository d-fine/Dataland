import { FormKitNode } from "@formkit/core";
import { findAllValuesForKey, ObjectType } from "@/utils/updateObjectUtils";
import { ExtendedFile } from "@/components/forms/Types";

/**
 * Checks which inputs are not filled correctly
 *
 * @param node - single form field
 */
export function checkCustomInputs(node: FormKitNode): void {
  const invalidElements: HTMLElement[] = [];
  node.walk((child: FormKitNode) => {
    // Check if this child has errors
    if ((child.ledger.value("blocking") || child.ledger.value("errors")) && child.type !== "group") {
      // We found an input with validation errors
      if (typeof child.props.id === "string") {
        const invalidElement = document.getElementById(child.props.id);
        if (invalidElement) {
          invalidElements.push(invalidElement);
        }
      }
    }
  }, true);
  // TODO does this autoscroll work??
  invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: "smooth", block: "center" });
}

/**
 * checks if all reports that shall be uploaded are used as a data source at least once
 *
 * @param [dataModel] the data model that has a field for referenced reports, named 'report'
 * @param [uploadedReports] the list of reports that were uploaded via form
 * returns nothing but throws an error if not all reports are referenced
 */
export function checkIfAllUploadedReportsAreReferencedInDataModel(
  dataModel: ObjectType,
  uploadedReports: string[]
): void {
  const referencedReports = findAllValuesForKey(dataModel, "report");
  const unusedReports: string[] = [];
  uploadedReports.forEach((report) => {
    if (!referencedReports.some((refReport) => refReport === report)) {
      unusedReports.push(report);
    }
  });
  if (unusedReports.length >= 1) {
    throw new Error(
      `Not all uploaded reports are used as a data source. Please remove following reports, or use them as a data source: ${unusedReports.toString()}`
    );
  }
}

/**
 * checks if all reports that shall be uploaded do not have the same name as an already uploaded report
 *
 * @param filesToUpload the list of files that shall be checked
 */
export function checkIfThereAreNoDuplicateReportNames(filesToUpload: ExtendedFile[]): void {
  const duplicateFileNames = filesToUpload
    .filter((extendedFile) => extendedFile["nameAlreadyExists"] === "true")
    .map((extendedFile) => extendedFile.name);
  if (duplicateFileNames.length >= 1) {
    throw new Error(
      `Some of the reports cannot be uploaded because another report with the same name already exists: ${duplicateFileNames.toString()}`
    );
  }
}
