import { type DocumentReference } from "@clients/backend";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  DataPointDisplayComponent = "DataPointDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: DocumentReference };
  [MLDTDisplayComponents.DataPointDisplayComponent]: { label: string; reference: DocumentReference, page?: number };
};

export type AvailableDisplayValues = {
  [K in MLDTDisplayComponents]: MLDTDisplayValue<K>;
}[MLDTDisplayComponents];

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
