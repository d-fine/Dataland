module.exports = {
  env: {
    browser: true,
    commonjs: true,
    es2021: true
  },
  extends: [
      'eslint:recommended',
      'plugin:@typescript-eslint/recommended-requiring-type-checking',
      'prettier'
  ],
  plugins: ['prettier'],
  overrides: [
  ],
  parserOptions: {
    project: 'tsconfig.json',
    tsconfigRootDir: __dirname,
    ecmaVersion: 'latest'
  },
  rules: {
    "prettier/prettier": ["error"]
  }
}
