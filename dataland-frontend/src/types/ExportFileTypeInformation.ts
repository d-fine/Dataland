import { type ExportFileType } from '@clients/backend';

export type FileTypeInformation = {
  fileExtension: string;
  description: string;
};

export const ExportFileTypeInformation: Record<ExportFileType, FileTypeInformation> = {
  CSV: {
    fileExtension: 'csv',
    description: 'Comma-separated Values',
  },
  EXCEL: {
    fileExtension: 'csv',
    description: 'Excel-compatible CSV File',
  },
  JSON: {
    fileExtension: 'json',
    description: 'JavaScript Object Notation',
  },
};
