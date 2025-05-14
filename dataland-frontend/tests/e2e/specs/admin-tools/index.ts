/**
 * Aa a developer or dataland employee I want to have access to meaningful admin tools
 */
Cypress._.times(20, () => {
  describe('Admin tool test', () => {
    require('./AdminConsoleSecurity');
    require('./Grafana.ts');
    require('./PGAdmin');
    require('./RabbitMQAdmin');
  });
});
