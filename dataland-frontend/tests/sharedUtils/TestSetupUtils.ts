/**
 * Sets the viewport of cypress to values suitable for a mobile device
 */
export function setMobileDeviceViewport(): void {
  const mobileDeviceViewportHeight = Number(Cypress.expose('mobile_device_viewport_height') ?? 667);
  const mobileDeviceViewportWidth = Number(Cypress.expose('mobile_device_viewport_width') ?? 300);
  cy.viewport(mobileDeviceViewportWidth, mobileDeviceViewportHeight);
}
