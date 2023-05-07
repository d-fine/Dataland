import { getStringCypressEnv } from "@e2e/utils/Cypress";

const queues = [
  "dataQualityAssuredBackendDataManager",
  "dataReceivedInternalStorageDatabaseDataStore",
  "dataStoredBackendDataManager",
  "dataStoredDocumentManager",
  "dataStoredQaService",
  "deadLetterQueue",
  "documentQualityAssuredDocumentManager",
  "documentReceivedDatabaseDataStore",
  "documentStoredQaService",
];

describe("As a developer, I expect the RabbitMQ GUI console to be available to me. Also check if all expected channels exist.", () => {
  it("Checks if the RabbitMQ Management GUI is available and the login page is shown. Then check that all expected queues exist.", () => {
    cy.visit("http://dataland-admin:6789/rabbitmq")
      .get("input[name=username]")
      .should("exist")
      .type(getStringCypressEnv("RABBITMQ_USER"))
      .get("input[name=password]")
      .should("exist")
      .type(getStringCypressEnv("RABBITMQ_PASS"))
      .get("input[type=submit]")
      .should("contain.value", "Login")
      .click()
      .get("#logout")
      .contains("Log out")
      .should("contain.value", "Log out")
      .get("ul[id='tabs'")
      .find("a[href='#/queues']")
      .click()
      .get("table[class=list]")
      .should("exist")
      .then(() => {
        queues.forEach((queue) => {
          cy.get("table[class=list]").contains(queue).should("contain.text", queue);
        });
      });
  });
});
