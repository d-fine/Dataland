import { type DocumentReference } from "@clients/backend";

export enum MLDTDisplayComponents {
  StringDisplayComponent = "StringDisplayComponent",
}

export type MLDTDisplayComponentTypes = {
  [MLDTDisplayComponents.StringDisplayComponent]: string | undefined | null;
};

export type AvailableDisplayValues = MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>;

export interface MLDTDisplayValue<DisplayComponent extends MLDTDisplayComponents> {
  displayComponent: DisplayComponent;
  displayValue: MLDTDisplayComponentTypes[DisplayComponent];
}
