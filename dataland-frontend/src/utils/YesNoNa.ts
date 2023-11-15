import { type YesNo, type YesNoNa } from "@clients/backend";

export const HumanizedYesNo: { [key in YesNo]: string } = {
  Yes: "Yes",
  No: "No",
};

export const HumanizedYesNoNa: { [key in YesNoNa]: string } = {
  Yes: HumanizedYesNo.Yes,
  No: HumanizedYesNo.No,
  NA: "N/A",
};
