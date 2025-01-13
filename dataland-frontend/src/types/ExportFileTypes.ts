export type FileType = {
  identifier: string;
  fileExtension: string;
  description: string;
};

export const ExportFileTypes: Record<'CsvFile' | 'ExcelFile' | 'JsonFile', FileType> = {
  CsvFile: {
    identifier: 'csv',
    fileExtension: 'csv',
    description: 'Comma-separated Values',
  },
  ExcelFile: {
    identifier: 'excel',
    fileExtension: 'csv',
    description: 'Excel-compatible CSV File',
  },
  JsonFile: {
    identifier: 'json',
    fileExtension: 'json',
    description: 'JavaScript Object Notation',
  },
};
