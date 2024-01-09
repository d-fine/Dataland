/**
 * Checks whether the Assurance data source has appropriate values
 * @param isMounted if the component hasn't been mounted yet, return true
 * @param fileName the of the report to be checked
 * @param invalidFileNames an array of file names that the fileName cannot be equal ( default ["None..."] )
 * @returns if no file selected or 'None...' selected it returns undefined. Else it returns the data source
 */
export function isValidFileName(
  isMounted: boolean,
  fileName: string | undefined,
  invalidFileNames = [noReportLabel],
): boolean {
  if (!isMounted) {
    return true;
  }
  return !!fileName && fileName?.length > 0 && !invalidFileNames.includes(fileName);
}

export const noReportLabel = "None...";
