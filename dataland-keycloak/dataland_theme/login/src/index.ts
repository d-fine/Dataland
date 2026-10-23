// Styling
import "primeflex/primeflex.scss";
import "material-icons/iconfont/material-icons.scss";
import "../resources/scss/main.scss";

// Images
import.meta.glob("../resources/img/*", {
  eager: true,
  query: "?url",
  import: "default",
});
