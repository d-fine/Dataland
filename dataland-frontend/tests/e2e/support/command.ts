// add new command to the existing Cypress interface
declare global {
    namespace Cypress {
        type Greeting = {
            greeting: string,
            name: string
        }

        interface Chainable {
            /**
             * Yields "foo"
             *
             * @returns {typeof foo}
             * @memberof Chainable
             * @example
             *    cy.foo().then(f = ...) // f is "foo"
             */
            foo: typeof foo

            /**
             * Yields sum of the arguments.
             *
             * @memberof Cypress.Chainable
             *
             * @example
             ```
             cy.sum(2, 3).should('equal', 5)
             ```
             */
            /**
             * Example command that passes an object of arguments.
             * @memberof Cypress.Chainable
             * @example
             ```
             cy.greeting({ greeting: 'Hello', name: 'Friend' })
             // or use defaults
             cy.greeting()
             ```
             */
            greeting: (options?: Greeting) => void
        }
    }
}

export function foo() {
    cy.request('GET', `${Cypress.env("API")}/metadata`).then((response) => {
        return  response.body.map(function (e: any) {
            console.log(e)
            return e.dataId
        })
    })
}


const defaultGreeting: Cypress.Greeting = {
    greeting: 'hi',
    name: 'there'
}

/**
 * Prints a custom greeting.
 * @example printToConsole({ greeting: 'hello', name: 'world' })
 */
export const printToConsole = (options = defaultGreeting) => {

}

// add commands to Cypress like "cy.foo()" and "cy.foo2()"
Cypress.Commands.add('foo', foo)
Cypress.Commands.add('greeting', printToConsole)