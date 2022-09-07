const plugins = [];
if (process.env.NODE_ENV !== "test") {
  plugins.push([
    "babel-plugin-istanbul",
    {
      extends: "@istanbuljs/nyc-config-typescript",
      extension: [".ts", ".tsx", ".js", ".vue"],
    },
  ]);
}

module.exports = {
  presets: ["@vue/cli-plugin-babel/preset"],
  plugins,
};
