// https://docs.cypress.io/api/table-of-contents

describe('Root Setup', () => {
  it('Visits the app root url and check H1', () => {
    cy.visit('/')
    cy.contains('h1', 'Welcome to DataLand')
  })
})


