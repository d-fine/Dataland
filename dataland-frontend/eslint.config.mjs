import pluginVue from 'eslint-plugin-vue';
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript';

// @ts-ignore
import pluginCypress from 'eslint-plugin-cypress';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';
import jsdoc from 'eslint-plugin-jsdoc';

const apiClientMessage =
  'API clients may only be used inside src/api-queries/. Reuse or add a TanStack Query hook there (see src/api-queries/AGENTS.md).';

const relativeImportPattern = { group: ['../*'] };

const apiClientRestrictedPatterns = [
  {
    group: ['@/utils/useApiClient', '@/utils/useApiClient.ts', '@/services/ApiClients', '@/services/ApiClients.ts'],
    message: apiClientMessage,
  },
];

// Legacy files that still call API clients directly. Only remove entries (after migrating to src/api-queries/); never add new ones.
const legacyDirectApiClientFiles = [
  'src/App.vue',
  'src/components/forms/CreateCompany.vue',
  'src/components/forms/CreateEuTaxonomyFinancials.vue',
  'src/components/forms/CreateEuTaxonomyNonFinancials.vue',
  'src/components/forms/CreateLksgDataset.vue',
  'src/components/forms/CreateNuclearAndGasDataset.vue',
  'src/components/forms/CreatePcafDataset.vue',
  'src/components/forms/CreateSfdrDataset.vue',
  'src/components/general/AddCompanyToPortfolios.vue',
  'src/components/general/DatasetsTabMenu.vue',
  'src/components/general/QaDatasetModal.vue',
  'src/components/generics/ViewFrameworkBase.vue',
  'src/components/generics/ViewMultipleDatasetsDisplayBase.vue',
  'src/components/pages/AdminAllRequestsOverview.vue',
  'src/components/pages/ApiKeysPage.vue',
  'src/components/pages/ChooseFrameworkForDataUpload.vue',
  'src/components/pages/CompanyCockpitPage.vue',
  'src/components/pages/CompanyInformation.vue',
  'src/components/pages/DocumentOverview.vue',
  'src/components/pages/MyDataRequestsOverview.vue',
  'src/components/pages/PlatformRedirect.vue',
  'src/components/pages/PortfolioOverview.vue',
  'src/components/pages/QualityAssurance.vue',
  'src/components/pages/SharedPortfolioOverview.vue',
  'src/components/pages/SingleDataRequest.vue',
  'src/components/pages/UnsubscribeFromMailsPage.vue',
  'src/components/pages/ViewDataRequestPage.vue',
  'src/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue',
  'src/components/resources/companyCockpit/AddUserDialog.vue',
  'src/components/resources/companyCockpit/ClaimOwnershipDialog.vue',
  'src/components/resources/companyCockpit/ClaimOwnershipPanel.vue',
  'src/components/resources/companyCockpit/CompanyDatasetsPane.vue',
  'src/components/resources/companyCockpit/CompanyRolesCard.vue',
  'src/components/resources/companyCockpit/ConflictingDocumentUploadDialog.vue',
  'src/components/resources/companyCockpit/CreditsCard.vue',
  'src/components/resources/companyCockpit/UploadDocumentDialog.vue',
  'src/components/resources/dataTable/modals/EditDataPointDialog.vue',
  'src/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue',
  'src/components/resources/datasetOverview/DatasetTableInfo.ts',
  'src/components/resources/documentPage/DocumentMetaDataDialog.vue',
  'src/components/resources/frameworkDataSearch/FileDownloadUtils.ts',
  'src/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue',
  'src/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableFrameworkPanel.vue',
  'src/components/resources/portfolio/GetHelpDialog.vue',
  'src/components/resources/portfolio/PortfolioDetails.vue',
  'src/components/resources/portfolio/PortfolioDetailsBase.vue',
  'src/components/resources/portfolio/PortfolioDialog.vue',
  'src/components/resources/portfolio/PortfolioMonitoring.vue',
  'src/components/resources/portfolio/PortfolioRemoveSharing.vue',
  'src/components/resources/portfolio/SharePortfolioDialog.vue',
  'src/components/resources/portfolio/SharedPortfolioDetails.vue',
  'src/frameworks/FrameworkApiUtils.ts',
  'src/utils/CompanyInformation.ts',
  'src/utils/CompanyRolesUtils.ts',
  'src/utils/FileUploadUtils.ts',
  'src/utils/SearchCompaniesForFrameworkDataPageDataRequester.ts',
];

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },
  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/build/**', '**/coverage/**', '**/public/**'],
  },
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommendedTypeChecked,
  {
    ...pluginCypress.configs.recommended,
    files: ['tests/e2e/**/*.{js,ts,jsx,tsx}', 'tests/sharedUtils/**/*.{js,ts,jsx,tsx}'],
  },
  {
    name: 'Rule overwrites for tests',
    files: ['cypress.config.ts', 'tests/**/*.{js,ts,jsx,tsx}'],
    rules: {
      '@typescript-eslint/no-require-imports': 'off',
      '@typescript-eslint/no-unused-expressions': 'off',
    },
  },
  {
    plugins: {
      jsdoc,
    },
    rules: {
      '@typescript-eslint/consistent-type-imports': [
        'error',
        {
          prefer: 'type-imports',
          fixStyle: 'inline-type-imports',
        },
      ],

      '@typescript-eslint/no-explicit-any': 'error',

      '@typescript-eslint/explicit-function-return-type': [
        'error',
        {
          allowTypedFunctionExpressions: true,
        },
      ],

      'jsdoc/require-jsdoc': [
        'error',
        {
          enableFixer: false,

          contexts: [
            'CallExpression[callee.name="defineComponent"] > ObjectExpression > Property[key.name="methods"] > ObjectExpression > Property > FunctionExpression',
          ],
        },
      ],

      'jsdoc/require-param-type': 'off',
      'jsdoc/require-returns-type': 'off',

      'vue/block-lang': [
        'error',
        {
          script: {
            lang: 'ts',
          },
        },
      ],

      'no-restricted-imports': ['error', { patterns: [relativeImportPattern, ...apiClientRestrictedPatterns] }],

      'vue/no-unused-properties': [
        'error',
        {
          groups: ['props', 'data', 'computed', 'methods', 'setup'],
          deepData: true,
          ignorePublicMembers: false,
          unreferencedOptions: ['returnAsUnreferenced', 'unknownMemberAsUnreferenced'],
        },
      ],

      '@typescript-eslint/ban-ts-comment': [
        'error',
        {
          minimumDescriptionLength: 3,
          'ts-check': false,
          'ts-expect-error': 'allow-with-description',
          'ts-ignore': false,
          'ts-nocheck': false,
        },
      ],
    },
  },
  {
    name: 'api-client-allowed-locations',
    files: ['src/api-queries/**', 'src/utils/useApiClient.ts', 'tests/**', ...legacyDirectApiClientFiles],
    rules: {
      'no-restricted-imports': ['error', { patterns: [relativeImportPattern] }],
    },
  },
  skipFormatting,
  {
    name: 'type-assertion-override',
    rules: {
      '@typescript-eslint/no-unnecessary-type-assertion': 'off',
    },
  }
);
