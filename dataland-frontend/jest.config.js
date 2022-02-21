module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  collectCoverage: true,
  coverageDirectory: "coverage/unit",
  coverageReporters: ["clover", "json", "lcov", ["text", {"skipFull": true}]]
}
