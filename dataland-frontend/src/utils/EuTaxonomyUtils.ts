import { ExtendedCompanyReport, ExtendedFile } from "@/components/forms/Types";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

/**
 * Update property in uploaded files
 *
 * @param indexFileToBeEdited Index number of the report to be edited
 * @param property Property which is to be edited
 * @param value Value to which it is to be changed
 * @param setOfFilesToBeEdited Set of files will be edited
 * @returns Edited set of files
 */
export function updatePropertyFilesUploaded(
  indexFileToBeEdited: number,
  property: string,
  value: string | Date,
  setOfFilesToBeEdited: ExtendedFile[] | ExtendedCompanyReport[]
): ExtendedFile[] | ExtendedCompanyReport[] {
  if (
    setOfFilesToBeEdited &&
    Object.prototype.hasOwnProperty.call(setOfFilesToBeEdited[indexFileToBeEdited], property)
  ) {
    if (property === "reportDateAsDate") {
      setOfFilesToBeEdited[indexFileToBeEdited].reportDate = getHyphenatedDate(value as Date);
    }
    setOfFilesToBeEdited[indexFileToBeEdited][property] = value;
  }
  return setOfFilesToBeEdited;
}

/**
 * Complete information about selected file with additional fields
 *
 * @param filesThatShouldBeCompleted Files that should be completed
 * @param listOfFilesThatAlreadyExistInReportsInfo List Of Files That Already Exist In Reports Info
 * @returns List of files with additional fields
 */
export function completeInformationAboutSelectedFileWithAdditionalFields(
  filesThatShouldBeCompleted: Record<string, string>[],
  listOfFilesThatAlreadyExistInReportsInfo: ExtendedCompanyReport[]
): ExtendedFile[] {
  return filesThatShouldBeCompleted.map((file) => {
    if (listOfFilesThatAlreadyExistInReportsInfo.some((it) => it.name === file.name.split(".")[0])) {
      file["nameAlreadyExists"] = "true";
    } else {
      file["nameAlreadyExists"] = "false";
      file["reportDate"] = file["reportDate"] ?? "";
      file["reportDateAsDate"] = file["reportDateAsDate"] ?? "";
      file["documentId"] = file["documentId"] ?? "";
    }
    return file as ExtendedFile;
  });
}
