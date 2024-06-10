import {type YesNo, type YesNoNa, YesNoNoEvidenceFound} from "@clients/backend";

export const HumanizedYesNo: { [key in YesNo]: string } = {
  Yes: "Yes",
  No: "No",
};

export const HumanizedYesNoNa: { [key in YesNoNa]: string } = {
  Yes: HumanizedYesNo.Yes,
  No: HumanizedYesNo.No,
  NA: "N/A",
};

export const HumanizedYesNoNoEvidenceFound: { [key in YesNoNoEvidenceFound]: string } = {
  Yes: HumanizedYesNo.Yes,
  No: HumanizedYesNo.No,
  NoEvidenceFound: "No Evidence Found",
};

/**
 * Util function to convert yes/no to true/false/undefined
 * @param yesNoValue a string
 * @returns boolean or undefined
 */
export function convertYesNoUndefinedToBoolean(yesNoValue: string | undefined): boolean | undefined {
  if (yesNoValue == HumanizedYesNo.Yes) {
    return true;
  } else if (yesNoValue == HumanizedYesNo.No) {
    return false;
  } else {
    return undefined;
  }
}
