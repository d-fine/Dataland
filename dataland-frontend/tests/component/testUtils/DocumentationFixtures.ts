import type {
  ExtendedCurrencySpecification,
  ExtendedDecimalSpecification,
  DataPointTypeSpecification,
  DataPointBaseTypeSpecification,
  SfdrFrameworkSpecification,
} from '@/types/documentation';

/**
 * Test fixtures for documentation components
 */

export const mockExtendedCurrencySpecification: ExtendedCurrencySpecification = {
  id: 'extendedCurrency',
  name: 'General currency data point with an extended source',
  businessDefinition: 'Data point with an extended document reference as source, a currency represented by a string and a number as value. The currency has to conform to ISO 4217. Currency and value have to be both set or both null.',
  validatedBy: 'org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint',
  example: {
    value: 100.5,
    currency: 'USD',
    quality: 'Reported',
    comment: 'The value is reported by the company.',
    dataSource: {
      page: '5-7',
      tagName: 'monetaryAmount',
      fileName: 'AnnualReport2020.pdf',
      fileReference: '207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47',
    },
  },
};

export const mockDataPointBaseTypeSpecification: DataPointBaseTypeSpecification = {
  dataPointBaseType: {
    id: 'extendedCurrency',
    ref: 'https://local-dev.dataland.com/specifications/data-point-base-types/extendedCurrency'
  },
  name: 'General currency data point with an extended source',
  businessDefinition: 'Data point with an extended document reference as source, a currency represented by a string and a number as value. The currency has to conform to ISO 4217. Currency and value have to be both set or both null.',
  validatedBy: 'org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint',
  example: {
    value: 100.5,
    currency: 'USD',
    quality: 'Reported',
    comment: 'The value is reported by the company.',
    dataSource: {
      page: '5-7',
      tagName: 'monetaryAmount',
      fileName: 'AnnualReport2020.pdf',
      fileReference: '207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47',
    },
  },
  usedBy: [
    {
      id: 'extendedCurrencyOpexEligibleShareAbsoluteShare',
      ref: 'https://local-dev.dataland.com/specifications/data-point-types/extendedCurrencyOpexEligibleShareAbsoluteShare'
    },
    {
      id: 'extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption',
      ref: 'https://local-dev.dataland.com/specifications/data-point-types/extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption'
    }
  ],
};

export const mockDataPointTypeSpecification: DataPointTypeSpecification = {
  dataPointType: {
    id: 'extendedDecimalScope1GhgEmissionsInTonnes',
    ref: 'https://local-dev.dataland.com/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes'
  },
  name: 'Scope 1 GHG emissions',
  businessDefinition: 'Scope 1 greenhouse gas emissions, namely emissions generated from sources that are controlled by the company that issues the underlying assets (equity share approach preferably used).',
  dataPointBaseType: {
    id: 'extendedDecimal',
    ref: 'https://local-dev.dataland.com/specifications/data-point-base-types/extendedDecimal'
  },
  usedBy: [
    {
      id: 'sfdr',
      ref: 'https://local-dev.dataland.com/specifications/frameworks/sfdr'
    }
  ],
  constraints: null,
};

export const mockExtendedDecimalSpecification: ExtendedDecimalSpecification = {
  id: 'extendedDecimalScope1GhgEmissionsInTonnes',
  name: 'Scope 1 GHG emissions data point with extended source',
  businessDefinition: 'Data point representing Scope 1 greenhouse gas emissions measured in tonnes CO2 equivalent, with an extended document reference as source. This includes direct emissions from owned or controlled sources.',
  validatedBy: 'org.dataland.datalandbackend.model.datapoints.extended.ExtendedDecimalDataPoint',
  example: {
    value: 1250.75,
    quality: 'Reported',
    comment: 'Direct emissions from company-owned facilities and vehicles.',
    dataSource: {
      page: '12-15',
      tagName: 'scope1Emissions',
      fileName: 'SustainabilityReport2023.pdf',
      fileReference: 'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456',
    },
  },
};

export const mockSfdrFrameworkSpecification: SfdrFrameworkSpecification = {
  id: 'sfdr',
  framework: {
    id: 'sfdr',
    ref: 'https://local-dev.dataland.com/specifications/frameworks/sfdr',
  },
  name: 'SFDR',
  businessDefinition: 'Sustainability Finance Disclosure Regulation',
  schema: JSON.stringify({
    general: {
      general: {
        dataDate: {
          id: 'plainDateSfdrDataDate',
          ref: 'https://local-dev.dataland.com/specifications/data-point-types/plainDateSfdrDataDate',
          aliasExport: 'DATA_DATE',
        },
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1GhgEmissionsInTonnes: {
          id: 'extendedDecimalScope1GhgEmissionsInTonnes',
          ref: 'https://local-dev.dataland.com/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes',
          aliasExport: 'SCOPE_1_GHG_EMISSIONS_IN_T',
        },
        scope2GhgEmissionsInTonnes: {
          id: 'extendedDecimalScope2GhgEmissionsInTonnes',
          ref: 'https://local-dev.dataland.com/specifications/data-point-types/extendedDecimalScope2GhgEmissionsInTonnes',
          aliasExport: 'SCOPE_2_GHG_EMISSIONS_IN_T',
        },
      },
    },
  }),
  referencedReportJsonPath: 'general.general.referencedReports',
};

export const mockApiResponses = {
  extendedCurrency: {
    url: '/specifications/data-point-base-types/extendedCurrency',
    fixture: mockDataPointBaseTypeSpecification,
  },
  extendedDecimal: {
    url: '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes',
    fixture: mockDataPointTypeSpecification,
  },
  sfdrFramework: {
    url: '/specifications/frameworks/sfdr',
    fixture: mockSfdrFrameworkSpecification,
  },
};

/**
 * Helper function to mock fetch responses for tests
 */
export const mockFetchResponses = () => {
  cy.intercept('GET', mockApiResponses.extendedCurrency.url, {
    statusCode: 200,
    body: mockApiResponses.extendedCurrency.fixture,
  }).as('getExtendedCurrencySpec');

  cy.intercept('GET', mockApiResponses.extendedDecimal.url, {
    statusCode: 200,
    body: mockApiResponses.extendedDecimal.fixture,
  }).as('getExtendedDecimalSpec');

  cy.intercept('GET', mockApiResponses.sfdrFramework.url, {
    statusCode: 200,
    body: mockApiResponses.sfdrFramework.fixture,
  }).as('getSfdrFrameworkSpec');
};

/**
 * Helper function to mock error responses for tests
 */
export const mockErrorResponses = () => {
  cy.intercept('GET', mockApiResponses.extendedCurrency.url, {
    statusCode: 404,
    body: { error: 'Not Found' },
  }).as('getExtendedCurrencySpecError');

  cy.intercept('GET', mockApiResponses.extendedDecimal.url, {
    statusCode: 500,
    body: { error: 'Internal Server Error' },
  }).as('getExtendedDecimalSpecError');

  cy.intercept('GET', mockApiResponses.sfdrFramework.url, {
    statusCode: 503,
    body: { error: 'Service Unavailable' },
  }).as('getSfdrFrameworkSpecError');
};