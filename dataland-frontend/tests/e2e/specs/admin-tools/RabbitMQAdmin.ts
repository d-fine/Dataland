import { getStringCypressEnv } from "@e2e/utils/Cypress";

describe("As a developer, I expect the RabbitMQ GUI console to be available to me. Also check if all expected channels exist.", () => {
  it("Checks if the RabbitMQ Management GUI is available and the login page is shown. Also check if all expected channels exist.", () => {
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
      .should("contain.value", "Log out");
    cy.visit("http://dataland-admin:6789/rabbitmq/#/queues")
      .get("table[class=list]")
      .contains("qa_queue")
      .should("contain.text", "qa_queue")
      .get("table[class=list]")
      .contains("storage_queue")
      .should("contain.text", "storage_queue")
      .get("table[class=list]")
      .contains("qa_queue")
      .should("contain.text", "qa_queue")
      .get("table[class=list]")
      .contains("upload_queue")
      .should("contain.text", "upload_queue")
      .get("table[class=list]");
  });
});
