import { DataTypeEnum } from "@clients/backend";

export interface SelectableItem {
  displayName: string;
  disabled: boolean;
}

export interface CountryCodeSelectableItem extends SelectableItem {
  countryCode: string;
}

export interface FrameworkSelectableItem extends SelectableItem {
  frameworkDataType: DataTypeEnum;
}
