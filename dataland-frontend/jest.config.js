module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  collectCoverage: true,
  coverageDirectory: "reports",
  coverageReporters: ["clover", "json", "lcov", ["text", {"skipFull": true}]]
}
