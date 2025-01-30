import jsdoc from 'eslint-plugin-jsdoc';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default [
  {
    ignores: [
      '**/.eslintrc.js',
      '**/cypress.config.ts',
      '**/vite.config.mts',
      '**/build/',
      '**/coverage',
      '**/cypress',
      '**/dist',
      '**/plugins/index.js',
    ],
  },
  ...compat.extends(
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier/skip-formatting',
    'plugin:jsdoc/recommended'
  ),
  {
    plugins: {
      jsdoc,
    },

    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'script',
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

      'no-restricted-imports': [
        'error',
        {
          patterns: ['../*'],
        },
      ],

      'vue/no-unused-properties': [
        'error',
        {
          groups: ['props', 'data', 'computed', 'methods', 'setup'],
          deepData: true,
          ignorePublicMembers: false,
          unreferencedOptions: ['returnAsUnreferenced', 'unknownMemberAsUnreferenced'],
        },
      ],
    },
  },
  ...compat.extends('plugin:cypress/recommended').map((config) => ({
    ...config,
    files: ['tests/sharedUtils/**/*', 'tests/e2e/**/*'],
  })),
];
