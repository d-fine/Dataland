const sass = require('sass');
const fs = require('fs');

const ensureDirectoryExists = (path) => {
    if (!fs.existsSync(path)) {
        fs.mkdirSync(path, { recursive: true });
    }
}

console.log("Compiling SASS stylesheets")
const result = sass.compile(
    './resources/scss/main.scss',
    {
        loadPaths: ['./node_modules/']
    }
)
ensureDirectoryExists('./dist/resources/css')
fs.writeFileSync('./dist/resources/css/dist.css', result.css);
