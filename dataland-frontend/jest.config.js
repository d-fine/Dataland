module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
  collectCoverage: true,
  collectCoverageFrom: [`src/**/*.{js,vue,ts}`],
  coverageDirectory: "coverage/unit",
  coverageReporters: ["lcov", ["text", {"skipFull": false}]]
}
