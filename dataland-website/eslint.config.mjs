import eslintPluginAstro from 'eslint-plugin-astro';
import pluginVue from 'eslint-plugin-vue';
import tseslint from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import vueParser from 'vue-eslint-parser';
// @ts-ignore
import pluginCypress from 'eslint-plugin-cypress';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';

export default [
  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/build/**', '**/.astro/**'],
  },
  ...eslintPluginAstro.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  {
    name: 'app/vue-rules',
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tsParser,
      },
    },
  },
  {
    name: 'app/ts-rules',
    files: ['**/*.{ts,mts}'],
    plugins: {
      '@typescript-eslint': tseslint,
    },
    languageOptions: {
      parser: tsParser,
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
      'no-restricted-imports': [
        'error',
        {
          patterns: ['../*'],
        },
      ],
    },
  },
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
    name: 'app/astro-script-overrides',
    files: ['**/*.astro', '**/*.astro/*.ts'],
    rules: {
      '@typescript-eslint/explicit-function-return-type': 'off',
    },
  },
  skipFormatting,
];
