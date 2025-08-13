// Shared interfaces for documentation components

export interface DataPointType {
  id: string;
  ref?: string;
}

export interface FrameworkRef {
  id: string;
  ref?: string;
}

export interface DataSource {
  page?: string;
  tagName?: string;
  fileName?: string;
  fileReference?: string;
  publicationDate?: string;
}

export interface Example {
  value?: any;
  quality?: string;
  comment?: string;
  dataSource?: DataSource;
}

export interface UsedByItem {
  id: string;
  ref?: string;
}

export interface DataPoint {
  dataPointType: DataPointType;
  name: string;
  businessDefinition?: string;
  dataPointBaseType: DataPointType;
  usedBy?: UsedByItem[];
  constraints?: string[];
}

export interface DataPointBaseType {
  dataPointBaseType: DataPointType;
  name: string;
  businessDefinition?: string;
  validatedBy?: string;
  example?: Example;
  usedBy?: UsedByItem[];
}

export interface Framework {
  framework: FrameworkRef;
  name: string;
  businessDefinition?: string;
  schema?: string;
  referencedReportJsonPath?: string;
}

// Color theme types for content boxes
export type ContentTheme = 'blue' | 'cyan' | 'green' | 'orange' | 'purple' | 'indigo' | 'teal';

export interface ContentBoxTheme {
  background: string;
  border: string;
  text: string;
  codeBackground: string;
  linkColor: string;
}
