module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
  collectCoverage: true,
  collectCoverageFrom: [`src/**/*.{jDALA-16_taxanomy_endpointss,vue}`],
  coverageDirectory: "coverage/unit",
  coverageReporters: ["lcov", ["text", {"skipFull": false}]]
}
