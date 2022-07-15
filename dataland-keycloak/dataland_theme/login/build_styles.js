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
ensureDirectoryExists('./build/resources/css')
fs.writeFileSync('./build/resources/css/dist.css', result.css);
