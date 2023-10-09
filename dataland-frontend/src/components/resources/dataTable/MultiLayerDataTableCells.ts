import { type BaseDocumentReference } from "@clients/backend";
import { type DynamicDialogOptions } from "primevue/dynamicdialogoptions";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
  DocumentLinkDisplayComponent = "DocumentLinkDisplayComponent",
  ModalLinkDisplayComponent = "ModalLinkDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
  [MLDTDisplayComponents.DocumentLinkDisplayComponent]: { label: string; reference: BaseDocumentReference };
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

export const EmptyDisplayValue: AvailableDisplayValues = {
  displayComponent: MLDTDisplayComponents.StringDisplayComponent,
  displayValue: "",
};

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
