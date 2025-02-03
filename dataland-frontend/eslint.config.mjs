import pluginVue from 'eslint-plugin-vue';
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import pluginCypress from 'eslint-plugin-cypress/flat';
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting';
import jsdoc from 'eslint-plugin-jsdoc';

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },
  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/buildr/**', '**/coverage/**'],
  },
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommendedTypeChecked,
  {
    ...pluginCypress.configs.recommended,
    files: ['tests/e2e/**/*.{js,ts,jsx,tsx}', 'tests/sharedUtils/**/*.{js,ts,jsx,tsx}'],
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
  skipFormatting
);
