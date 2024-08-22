import TestSlideShow from './TestSlideShow.vue';

const SLIDE_DELTA = 440;

describe('Component test for the SlideShow component', () => {
  it('Check if the buttons are working', () => {
    cy.mountWithPlugins(TestSlideShow, {}).then(() => {
      cy.get(leftButtonSelector).should('have.class', 'disabled');
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(1);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(2);
      cy.get(rightButtonSelector).should('have.class', 'disabled');
      assertSlidesPosition(2);
      cy.get(leftButtonSelector).click();
      assertSlidesPosition(1);
      cy.get(leftButtonSelector).click();
      assertSlidesPosition(0);
      cy.get(leftButtonSelector).should('have.class', 'disabled');
      assertSlidesPosition(0);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(1);
    });
  });

  it('Check if dragging is working', () => {
    cy.mountWithPlugins(TestSlideShow, {}).then(() => {
      assertSlidesPosition();
      dragSlideTo(1, leftOffset);
      assertSlidesPosition(1);
      dragSlideTo(2, leftOffset);
      assertSlidesPosition(2);
      dragSlideTo(2, leftOffset + SLIDE_DELTA);
      assertSlidesPosition(1);
      dragSlideTo(1, leftOffset + SLIDE_DELTA);
      assertSlidesPosition(0);
    });
  });

  it('Check if dragging is disabled on large screens', () => {
    cy.mountWithPlugins(TestSlideShow, {}).then(() => {
      cy.viewport(1900, 800);
      assertSlidesPosition();
      dragSlideTo(1, leftOffset);
      assertSlidesPosition();
    });
  });
});

const leftButtonSelector = "button[aria-label='Previous slide']";
const rightButtonSelector = "button[aria-label='Next slide']";
const leftOffset = 10;
const topOffset = 10;

/**
 * Checks that a slide show is centered at a given slide
 * @param centerSlide the index of the slide that is expected to be in the center
 */
function assertSlidesPosition(centerSlide?: number): void {
  const slidesSelector = '.test__slides';
  const expectedTransformValue =
    centerSlide == undefined ? 'none' : `matrix(1, 0, 0, 1, ${-SLIDE_DELTA * centerSlide}, 0)`;
  cy.get(slidesSelector).should('have.css', 'transform', expectedTransformValue);
}

/**
 * Drags the selected slide to the given position
 * @param slideIndex the slide to drag
 * @param targetX the targets position x value in viewport coordinates
 */
function dragSlideTo(slideIndex: number, targetX: number): void {
  cy.get('.test__slide')
    .eq(slideIndex)
    .trigger('pointerdown', leftOffset, topOffset, { button: 0 })
    .trigger('pointermove', {
      eventConstructor: 'MouseEvent',
      clientX: targetX,
      clientY: topOffset,
    })
    .trigger('pointerup', { button: 0 });
}
