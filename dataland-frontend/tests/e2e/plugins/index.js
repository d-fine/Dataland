module.exports = (on, config) => {
    require('@cypress/code-coverage/task')(on, config)
    config.fixturesFolder = '../testing/data'
    config.integrationFolder = 'tests/e2e/specs'
    config.supportFile = 'tests/e2e/support/index.ts'
    config.env.commit_id = require('git-commit-id')({cwd: ".."})
    on('before:browser:launch', (browser = {}, launchOptions) => {
        launchOptions.args.push(
            "--ignore-connections-limit=localhost:8090,proxy," +
            "localhost,preview-dataland.duckdns.org,dev-dataland.duckdns.org"
        )
    })
    return config
}