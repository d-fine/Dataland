module.exports = (on, config) => {
    require('@cypress/code-coverage/task')(on, config)
    config.fixturesFolder = '../testing/data'
    config.integrationFolder = 'tests/e2e/specs'
    config.supportFile = 'tests/e2e/support/index.js'
    config.env.commit_id = require('git-commit-id')({cwd: ".."})
    return config
}