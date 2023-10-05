import { type DocumentReference } from "@clients/backend";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  DataPointDisplayComponent = "DataPointDisplayComponent",
  DataTableModalDisplayComponent = "DataTableModalDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: DocumentReference };
  [MLDTDisplayComponents.DataPointDisplayComponent]: { label: string; reference: DocumentReference; page?: number };
  [MLDTDisplayComponents.DataTableModalDisplayComponent]: { label: string; data: unknown };
};

export type AvailableDisplayValues = {
  [K in MLDTDisplayComponents]: MLDTDisplayValue<K>;
}[MLDTDisplayComponents];

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
