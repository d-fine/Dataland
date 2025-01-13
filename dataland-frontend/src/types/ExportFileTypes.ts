export type FileType = {
  fileExtension: string;
  description: string;
};

export const ExportFileTypes: Record<'CsvFile' | 'ExcelFile' | 'JsonFile', FileType> = {
  CsvFile: {
    fileExtension: 'csv',
    description: 'Comma-separated Values',
  },
  ExcelFile: {
    fileExtension: 'xlsx',
    description: 'Excel File',
  },
  JsonFile: {
    fileExtension: 'json',
    description: 'JavaScript Object Notation',
  },
};
