import { type YesNo, type YesNoNa } from '@clients/backend';

export const HumanizedYesNo: { [key in YesNo]: string } = {
  Yes: 'Yes',
  No: 'No',
};

export const HumanizedYesNoNa: { [key in YesNoNa]: string } = {
  Yes: HumanizedYesNo.Yes,
  No: HumanizedYesNo.No,
  NA: 'N/A',
};

/**
 * Array-shaped representation of HumanizedYesNo, as expected by components that render a list of
 * checkbox/radio options, which expect an array of {value, label} objects.
 */
export const YesNoFormOptions = Object.entries(HumanizedYesNo).map(([value, label]) => ({ value, label }));

/**
 * Array-shaped representation of HumanizedYesNoNa, as expected by components that render a list of
 * checkbox/radio options, which expect an array of {value, label} objects rather than a plain object map.
 */
export const YesNoNaFormOptions = Object.entries(HumanizedYesNoNa).map(([value, label]) => ({ value, label }));

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
