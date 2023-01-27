require("@rushstack/eslint-patch/modern-module-resolution")

module.exports = {
  root: true,
  parser: 'vue-eslint-parser',
  extends: [
    'plugin:vue/base',
    'eslint:recommended',
    'plugin:vue/vue3-essential',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier',
    'plugin:@typescript-eslint/recommended-requiring-type-checking',
    'plugin:jsdoc/recommended'
  ],
  parserOptions: {
    parser: '@typescript-eslint/parser',
    project: ["tsconfig.app.json", "tsconfig.cypress-ct.json", "tsconfig.cypress-e2e.json"],
    tsconfigRootDir: __dirname,
    extraFileExtensions: ['.vue'],
    sourceType: "module"
  },
  plugins: ['@typescript-eslint', 'jsdoc'],
  rules: {
    '@typescript-eslint/no-explicit-any': 'error',
    '@typescript-eslint/no-unnecessary-type-assertion': 'error',
    'semi-spacing': 'error',
    "@typescript-eslint/explicit-function-return-type": ["error", {
      "allowTypedFunctionExpressions": true,
    }],
    "jsdoc/require-jsdoc": ["error", {
      "enableFixer": false,
      "contexts": ['CallExpression[callee.name="defineComponent"] > ObjectExpression > Property[key.name="methods"] > ObjectExpression > Property > FunctionExpression']
    }],
    "jsdoc/require-param-type": "off",
    "jsdoc/require-returns-type": "off",
    "vue/block-lang": ["error",
      {
        "script": {
          "lang": "ts"
        }
      }
    ]
  },
}