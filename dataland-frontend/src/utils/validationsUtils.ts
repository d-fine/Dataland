import { FormKitNode } from "@formkit/core";
import { findAllValuesForKey, ObjectType } from "@/utils/updateObjectUtils";

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
  invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: "smooth", block: "center" });
}

/**
 * checks if all reports that were uploaded are used as a data source
 *
 * @param [dataModel] the data model that has a field for referenced reports, named 'report'
 * @param [uploadedReports] the list of reports that were uploaded via form
 * returns nothing but throws an error if not all reports are referenced
 */
export function checkThatAllReportsAreReferenced(dataModel: ObjectType, uploadedReports: string[]): void {
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
