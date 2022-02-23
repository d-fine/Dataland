module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  collectCoverage: true,
  collectCoverageFrom: [`src/**/*.{js,vue}`],
  coverageDirectory: "coverage/unit",
  coverageReporters: ["lcov", ["text", {"skipFull": true}]]
}
