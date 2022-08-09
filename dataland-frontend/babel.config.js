const plugins = [];
if (process.env.NODE_ENV !== "test") {
  plugins.push([
    "babel-plugin-istanbul",
    {
      extension: [".ts", ".js", ".vue"],
    },
  ]);
}

module.exports = {
  presets: ["@vue/cli-plugin-babel/preset"],
  plugins,
};
