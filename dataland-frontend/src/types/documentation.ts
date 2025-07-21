// Base specification interface
export interface BaseSpecification {
  id: string;
  name: string;
  businessDefinition: string;
}

// Extended currency specification
export interface ExtendedCurrencySpecification extends BaseSpecification {
  validatedBy: string;
  example: {
    value: number;
    currency: string;
    quality: string;
    comment: string;
    dataSource: {
      page: string;
      tagName: string;
      fileName: string;
      fileReference: string;
    };
  };
}

// Reference object for IDs and URLs
export interface SpecificationReference {
  id: string;
  ref: string;
}

// Data point type specification from specification service
export interface DataPointTypeSpecification {
  dataPointType: SpecificationReference;
  name: string;
  businessDefinition: string;
  dataPointBaseType: SpecificationReference;
  usedBy: SpecificationReference[];
  constraints: any;
}

// Data point base type specification from specification service
export interface DataPointBaseTypeSpecification {
  dataPointBaseType: SpecificationReference;
  name: string;
  businessDefinition: string;
  validatedBy: string;
  example: {
    value: number;
    currency: string;
    quality: string;
    comment: string;
    dataSource: {
      page: string;
      tagName: string;
      fileName: string;
      fileReference: string;
    };
  };
  usedBy: SpecificationReference[];
}

// Extended decimal specification (legacy structure for backward compatibility)
export interface ExtendedDecimalSpecification extends BaseSpecification {
  validatedBy: string;
  example: {
    value: number;
    quality: string;
    comment: string;
    dataSource: {
      page: string;
      tagName: string;
      fileName: string;
      fileReference: string;
    };
  };
}

// Framework reference
export interface FrameworkReference {
  id: string;
  ref: string;
}

// SFDR framework specification
export interface SfdrFrameworkSpecification extends BaseSpecification {
  framework: FrameworkReference;
  schema: string;
  referencedReportJsonPath: string;
}

// Data point reference for schema rendering
export interface DataPointRef {
  id: string;
  ref: string;
  aliasExport?: string;
}

// Generic specification type union
export type SpecificationType = 
  | ExtendedCurrencySpecification 
  | ExtendedDecimalSpecification 
  | DataPointTypeSpecification
  | SfdrFrameworkSpecification;