// eslint.config.js

const parse = require("@typescript-eslint/parser");
const eslintPluginPrettier = require("eslint-plugin-prettier");

module.exports = [
  {
    ignores: ["vite.config.mts", "**/build/", "eslint.config.js"],
  },
  {
    files: [
      "**/*.vue",
      "**/*.js",
      "**/*.ts",
      "**/*.tsx",
      "**/*.cts",
      "**/*.mts",
    ],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      parser: parse,
      parserOptions: {
        project: "tsconfig.json",
        tsconfigRootDir: __dirname,
      },

    },
    plugins: {
      prettier: eslintPluginPrettier,
    },
    rules: {
      "prettier/prettier": ["error"],
    },
  },
];