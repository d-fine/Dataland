export const dateFormElement = {
  selectDayOfNextMonth(fieldName: string, day: number): void {
    cy.get(`[data-test="${fieldName}"] button`).should("have.class", "p-datepicker-trigger").click();
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find(`span:contains("${day}")`).click();
  },
  validateDay(fieldName: string, day: number): void {
    cy.get(`input[name="${fieldName}"]`).invoke("val").should("contain", day.toString());
    cy.get(`input[name="${fieldName}"]`).should("not.be.visible");
  },
};
