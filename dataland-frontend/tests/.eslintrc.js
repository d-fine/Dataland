module.exports = {
  plugins: ['cypress'],
  env: {
    mocha: true,
    'cypress/globals': true,
  },
  rules: {
    strict: 'off',
    '@typescript-eslint/no-non-null-assertion': 'off',
  },
};
