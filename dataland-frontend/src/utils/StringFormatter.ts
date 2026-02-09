/**
 * Module to convert string to a human-readable text
 */

import { HumanizedYesNoNa } from '@/utils/YesNoNa';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { DataTypeEnum } from '@clients/backend';
import { getBasePrivateFrameworkDefinition } from '@/frameworks/BasePrivateFrameworkRegistry';
import { DocumentMetaInfoDocumentCategoryEnum, type DocumentMetaInfoResponse } from '@clients/documentmanager';

/**
 * Convert kebab case string to camelCase
 * @param rawText is the string to be converted
 * @returns the converted string in camel case
 */
export function convertKebabCaseToCamelCase(rawText: string): string {
  return rawText.replaceAll(/-([a-z])/g, (_, char: string) => char.toUpperCase());
}

/**
 * convert kebab case string to pascal case string using regex
 * @param rawText is the string to be converted
 * @returns the converted string in pascal case
 */
export function convertKebabCaseToPascalCase(rawText: string): string {
  const camelCase = convertKebabCaseToCamelCase(rawText);
  return camelCase.charAt(0).toUpperCase() + camelCase.slice(1);
}

/**
 * convert camel case string to sentence case string using regex
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string in "sentence-case"
 */
function convertCamelCaseToSentenceCase(rawText: string): string {
  // Split the sting to words
  const processedText = rawText.replaceAll(/((?!^)[A-Z])/g, ' $1');
  // uppercase the first letter of the first word
  return processedText.charAt(0).toUpperCase() + processedText.slice(1);
}

/**
 * convert camel case string to a string with spaces
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string with "Words With Spaces"
 */
export function convertCamelCaseToWordsWithSpaces(rawText: string): string {
  return rawText
    .toString()
    .replaceAll(/([A-Z])/g, ' $1')
    .trim();
}

/**
 * get the representable text from the mapping object
 * @param rawText is the string to be converted to a human-readable string
 * @returns the converted string
 */
function humanizeViaMapping(rawText: string): string {
  const mappingObject: { [key: string]: string } = {
    isin: 'ISIN',
    permid: 'PermID',
    lei: 'LEI',
    ticker: 'Ticker',
    duns: 'DUNS',
    eligiblecapex: 'Eligible CapEx',
    eligibleopex: 'Eligible OpEx',
    alignedcapex: 'Aligned CapEx',
    alignedopex: 'Aligned OpEx',
    insuranceorreinsurance: 'Insurance or Reinsurance',
    annualreport: 'Annual Report',
    sustainabilityreport: 'Sustainability Report',
    integratedreport: 'Integrated Report',
    esefreport: 'ESEF Report',
    yes: HumanizedYesNoNa.Yes,
    no: HumanizedYesNoNa.No,
    na: HumanizedYesNoNa.NA,
    'eutaxonomy-financials': 'EU Taxonomy for financial companies',
    'eutaxonomy-non-financials': 'EU Taxonomy for non-financial companies',
    lksg: 'LkSG',
    sfdr: 'SFDR',
    sme: 'SME',
    inhouseproduction: 'In-house Production',
    contractprocessing: 'Contract Processing',
    hvcplastics: 'HVC Plastics',
    contaminationofsoilwaterairornoiseemissionsorexcessivewaterconsumption:
      'Contamination of soil/water/air, noise emissions, excessive water consumption',
    useofmercuryormercurywaste: 'Use of mercury, mercury waste (Minamata Convention)',
    productionanduseofpersistentorganicpollutants:
      'Production and use of persistent organic pollutants (POPs Convention)',
    exportimportofhazardouswaste: 'Export/import of hazardous waste (Basel Convention)',
    policy: 'Policy',
    other: 'Other Document',
  };

  const lowerCaseText = rawText.toLowerCase();
  return mappingObject[lowerCaseText] ?? '';
}

/**
 * convert string or number to a human-readable string
 * @param rawInput is the string or number to be converted to a human-readable string
 * @returns the converted string
 */
export function humanizeStringOrNumber(rawInput: string | number | null | undefined): string {
  if (typeof rawInput === 'number') {
    return rawInput.toString();
  }
  if (!rawInput) {
    return '';
  }

  const frameworkLabel =
    getBasePublicFrameworkDefinition(rawInput)?.label ?? getBasePrivateFrameworkDefinition(rawInput)?.label;
  if (frameworkLabel) return frameworkLabel;
  const resultOfCustomMappingHumanisation = humanizeViaMapping(rawInput);
  return resultOfCustomMappingHumanisation === ''
    ? convertCamelCaseToSentenceCase(rawInput)
    : resultOfCustomMappingHumanisation;
}
/**
 * Return the title of a framework
 * @param framework dataland framework
 * @returns title of framework
 */
export function getFrameworkTitle(framework: string): string {
  switch (framework) {
    case DataTypeEnum.EutaxonomyFinancials:
      return 'EU Taxonomy';
    case DataTypeEnum.EutaxonomyNonFinancials:
      return 'EU Taxonomy';
    default:
      return humanizeStringOrNumber(framework);
  }
}
/**
 * Checks the existence of subtitle for framework
 * @param framework dataland framework
 * @returns boolean if framework has subtitle
 */
export function frameworkHasSubTitle(framework: string): boolean {
  return framework == DataTypeEnum.EutaxonomyFinancials || framework == DataTypeEnum.EutaxonomyNonFinancials;
}
/**
 * Return the subtitle of a framework
 * @param framework dataland framework
 * @returns subtitle of framework
 */
export function getFrameworkSubtitle(framework: string): string {
  switch (framework) {
    case DataTypeEnum.EutaxonomyFinancials:
      return 'for financial companies';
    case DataTypeEnum.EutaxonomyNonFinancials:
      return 'for non-financial companies';
    default:
      return '';
  }
}

/**
 * Return the human readable plural of a report category
 * @param category document category
 * @returns title of category
 */
export function getPluralCategory(category: string): string {
  switch (category) {
    case DocumentMetaInfoDocumentCategoryEnum.Policy:
      return 'Policies';
    case DocumentMetaInfoDocumentCategoryEnum.AnnualReport:
      return 'Annual Reports';
    case DocumentMetaInfoDocumentCategoryEnum.SustainabilityReport:
      return 'Sustainability Reports';
    case DocumentMetaInfoDocumentCategoryEnum.Other:
      return 'Other Reports';
    default:
      return humanizeStringOrNumber(category);
  }
}

/**
 * Returns the filename to a given document, i.e., its documentName unless it is undefined or null, in which case
 * the documentId is returned.
 * @param document The document of interest
 * @return The filename as a string
 */
export function documentNameOrId(document: DocumentMetaInfoResponse): string {
  return document.documentName ?? document.documentId;
}

/**
 * If document has a publication date that is not undefined, returns its documentNameOrId with the
 * publication date appended in parentheses. Otherwise, simply returns the documentNameOrId.
 * @param document The document of interest
 * @return A string consisting of the documentNameOrId and the publication date if existent
 */
export function documentPublicationDateOrEmpty(document: DocumentMetaInfoResponse): string {
  const documentPublicationDate = document.publicationDate;
  if (documentPublicationDate == undefined) {
    return '';
  } else {
    return '(' + document.publicationDate + ')';
  }
}

/**
 * Generates initials from a name string.
 * @param name - The name to generate initials from
 * @returns The initials derived from the name
 */
export function generateInitials(name: string): string {
  return name
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase())
    .join('');
}

/**
 * Truncates text to a maximum length at word boundaries.
 * If the text is longer than maxLength, it will be truncated and "..." will be appended.
 * @param text - The text to truncate
 * @param maxLength - The maximum length of the truncated text (including "...")
 * @returns An object containing the truncated text and a flag indicating if truncation occurred
 */
export function truncateText(text: string, maxLength: number): { truncated: string; needsTruncation: boolean } {
  if (text.length <= maxLength) {
    return { truncated: text, needsTruncation: false };
  }

  // Find the last space within the maxLength (accounting for "...")
  const ellipsis = '...';
  const targetLength = maxLength - ellipsis.length;
  const lastSpaceIndex = text.lastIndexOf(' ', targetLength);

  // If no space found, just cut at target length
  const cutIndex = lastSpaceIndex > 0 ? lastSpaceIndex : targetLength;
  const truncated = text.substring(0, cutIndex) + ellipsis;

  return { truncated, needsTruncation: true };
}

/**
 * Converts a data point base type ID to a human-readable label.
 * Used to display user-friendly type names alongside technical IDs in the specification viewer.
 * 
 * @param baseTypeId - The technical base type ID (e.g., "plainString", "extendedDecimal")
 * @returns A human-readable label for the type
 * 
 * @example
 * humanizeDataPointBaseType("plainString") // Returns "Text"
 * humanizeDataPointBaseType("plainDate") // Returns "Date"
 * humanizeDataPointBaseType("extendedDecimal") // Returns "Number"
 * humanizeDataPointBaseType("extendedEnumPcafMainSector") // Returns "Selection"
 */
export function humanizeDataPointBaseType(baseTypeId: string): string {
  // Map of common base types to user-friendly labels
  const typeMapping: { [key: string]: string } = {
    plainString: 'Text',
    plainDate: 'Date',
    plainInteger: 'Number',
    plainDecimal: 'Number',
    extendedDecimal: 'Number',
    plainBoolean: 'Yes/No',
    extendedEnum: 'Selection',
    plainEnum: 'Selection',
    extendedArray: 'List',
  };

  // Check if we have a direct mapping
  if (typeMapping[baseTypeId]) {
    return typeMapping[baseTypeId];
  }

  // Detect common patterns in the type ID for better fallback labels
  const lowerCaseId = baseTypeId.toLowerCase();
  
  // Pattern-based detection for partial matches
  if (lowerCaseId.includes('enum')) return 'Selection';
  if (lowerCaseId.includes('date')) return 'Date';
  if (lowerCaseId.includes('decimal') || lowerCaseId.includes('integer') || lowerCaseId.includes('number')) return 'Number';
  if (lowerCaseId.includes('boolean')) return 'Yes/No';
  if (lowerCaseId.includes('array') || lowerCaseId.includes('list')) return 'List';
  if (lowerCaseId.includes('string') || lowerCaseId.includes('text')) return 'Text';
  if (lowerCaseId.includes('document') || lowerCaseId.includes('file')) return 'Document';
  
  // If no pattern matches, return a simple generic label
  return 'Data';
}
