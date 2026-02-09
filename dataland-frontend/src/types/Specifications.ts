import type { FrameworkSpecification, DataPointTypeSpecification } from '@clients/specificationservice';

/**
 * Schema structure returned by the specification service API.
 * 
 * The schema JSON from the API has a recursive structure where:
 * - Nested objects represent sections (containers for other sections or data points)
 * - Leaf nodes are objects with data point type references
 * 
 * Example structure from API:
 * ```json
 * {
 *   "general": {
 *     "general": {
 *       "dataDate": {
 *         "id": "plainDateSfdrDataDate",
 *         "ref": "https://host/specifications/dataPointTypes/plainDateSfdrDataDate",
 *         "aliasExport": "DATA_DATE"
 *       },
 *       "fiscalYearDeviation": {
 *         "id": "extendedEnumFiscalYearDeviation",
 *         "ref": "https://host/specifications/dataPointTypes/extendedEnumFiscalYearDeviation",
 *         "aliasExport": "FISCAL_YEAR_DEVIATION"
 *       }
 *     }
 *   },
 *   "environmental": {
 *     "greenhouseGasEmissions": {
 *       "scope1GhgEmissionsInTonnes": {
 *         "id": "extendedDecimalScope1GhgEmissionsInTonnes",
 *         "ref": "https://...",
 *         "aliasExport": "SCOPE_1_GHG_EMISSIONS_IN_T"
 *       }
 *     }
 *   }
 * }
 * ```
 */

/**
 * Reference to a data point type as returned by the API.
 */
export interface DataPointTypeReference {
  id: string;
  ref: string;
  aliasExport?: string;
}

/**
 * Parsed schema node representing either a section or a data point.
 * 
 * Sections group related data points hierarchically (e.g., "General Information", "Environmental").
 * Data points are the actual fields that hold data (e.g., "Company Name", "Fiscal Year End").
 */
export type ParsedSchemaNode = ParsedSection | ParsedDataPoint;

/**
 * A section in the schema tree (a group of nested sections and/or data points).
 */
export interface ParsedSection {
  type: 'section';
  /** Unique identifier for this section within the tree (based on path) */
  id: string;
  /** Property key from the schema (e.g., "general", "environmental") */
  key: string;
  /** Human-readable label for display (derived from key if not available) */
  label: string;
  /** Nested child nodes (sections and data points) */
  children: ParsedSchemaNode[];
}

/**
 * A data point in the schema tree (a leaf node representing an actual data field).
 */
export interface ParsedDataPoint {
  type: 'dataPoint';
  /** Unique identifier for this data point within the tree (based on path) */
  id: string;
  /** Property key from the schema (e.g., "dataDate", "companyName") */
  key: string;
  /** Data point type ID (e.g., "plainDateSfdrDataDate") */
  dataPointTypeId: string;
  /** Reference URL to the data point type specification */
  ref: string;
  /** Human-readable name for display (preferred over key) */
  aliasExport?: string;
}

/**
 * Framework specification with parsed schema tree.
 * Extends the generated FrameworkSpecification type with an additional parsed schema property
 * for easier consumption in Vue components.
 */
export interface FrameworkSpecificationWithParsedSchema extends Omit<FrameworkSpecification, 'schema'> {
  /** Original schema JSON string from the API */
  schemaJson: string;
  /** Parsed and hierarchical schema tree for rendering */
  parsedSchema: ParsedSchemaNode[];
}

/**
 * Extended data point type specification with additional UI state.
 */
export interface DataPointTypeDetails extends DataPointTypeSpecification {
  // Currently no extensions, but this structure allows for future additions
}
