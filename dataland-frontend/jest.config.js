module.exports = {
  preset: "@vue/cli-plugin-unit-jest/presets/typescript-and-babel",
  collectCoverage: true,
  coveragePathIgnorePatterns: ["src/main.ts", "src/components/helper/*"],
  collectCoverageFrom: [`src/**/*.{js,vue,ts}`],
  coverageDirectory: "coverage/unit",
  coverageReporters: ["lcov", ["text", { skipFull: false }]],
};
