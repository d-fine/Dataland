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
  // TODO does this autoscroll work??
  invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: "smooth", block: "center" });
}

/**
 * checks if all reports that were uploaded are used as a data source
 *
 * @param [dataModel] the data model that has a field for referenced reports, named 'report'
 * @param [uploadedReports] the list of reports that were uploaded via form
 * @returns a boolean stating if all uploaded reports are referenced in the data model
 */
export function areAllUploadedReportsReferencedInDataModel(dataModel: ObjectType, uploadedReports: string[]): boolean {
  const referencedReportsInDataModel = findAllValuesForKey(dataModel, "report");
  uploadedReports.forEach((uploadedReport) => {
    if (!referencedReportsInDataModel.some((referencedReport) => referencedReport === uploadedReport)) {
      return false;
    }
  });
  return true;
}
