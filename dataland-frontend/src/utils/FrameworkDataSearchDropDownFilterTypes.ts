import { type DataTypeEnum } from '@clients/backend';
import { type SearchForDocumentMetaInformationDocumentCategoriesEnum } from '@clients/documentmanager';

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

export interface DocumentCategorySelectableItem extends SelectableItem {
  documentCategoryDataType: SearchForDocumentMetaInformationDocumentCategoriesEnum;
}
