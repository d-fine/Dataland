/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution');

module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier/skip-formatting',
    'plugin:jsdoc/recommended',
  ],
  plugins: ['jsdoc'],
  overrides: [
    {
      files: ['tests/sharedUtils/**/*', 'tests/e2e/**/*'],
      extends: ['plugin:cypress/recommended'],
    },
  ],
  parserOptions: {
    ecmaVersion: 'latest',
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
};
