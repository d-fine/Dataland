import { type DataTypeEnum } from '@clients/backend';
import { type RequestPriority} from "@clients/communitymanager";

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

export interface PrioritySelectableItem extends SelectableItem {
  priorityDataType: RequestPriority;
}
