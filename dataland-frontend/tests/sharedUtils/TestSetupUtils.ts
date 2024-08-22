/**
 * Sets the viewport of cypress to values suitable for a mobile device
 */
export function setMobileDeviceViewport(): void {
  const mobileDeviceViewportHeight = Cypress.env('mobile_device_viewport_height') as number;
  const mobileDeviceViewportWidth = Cypress.env('mobile_device_viewport_width') as number;
  cy.viewport(mobileDeviceViewportWidth, mobileDeviceViewportHeight);
}
