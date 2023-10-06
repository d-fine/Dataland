import { type DocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  DataPointDisplayComponent = "DataPointDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: DocumentReference };
  [MLDTDisplayComponents.DataPointDisplayComponent]: { label: string; reference: DocumentReference; page?: number };
  [MLDTDisplayComponents.ModalLinkDisplayComponent]: {
    label: string;
    // Ignored as "any" type comes from DynamicDialog
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    modalComponent: any;
    modalOptions?: DynamicDialogOptions;
  };
};

export type AvailableDisplayValues = {
  [K in MLDTDisplayComponents]: MLDTDisplayValue<K>;
}[MLDTDisplayComponents];

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
