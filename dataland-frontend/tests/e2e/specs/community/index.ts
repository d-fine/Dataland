/**
 * As a user I want to be able to use community pages
 */
describe('Community pages tests', () => {
  require('./BulkDataRequest');
  require('./SingleDataRequest');
  require('./MyPortfolios');
  require('../data-download/DownloadMyPortfolios.ts');
  require('./MonitorPortfolios.ts');
});
