import TestSlideShow from "./TestSlideShow.vue";

const SLIDE_DELTA = 440;

describe("Component test for the SlideShow component", () => {
  it("Check if the buttons are working", () => {
    cy.mountWithPlugins(TestSlideShow, {}).then(() => {
      assertSlidesPosition(slidesSelector);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(slidesSelector, 1);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(slidesSelector, 2);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(slidesSelector, 2);
      cy.get(leftButtonSelector).click();
      assertSlidesPosition(slidesSelector, 1);
      cy.get(leftButtonSelector).click();
      assertSlidesPosition(slidesSelector, 0);
      cy.get(leftButtonSelector).click();
      assertSlidesPosition(slidesSelector, 0);
      cy.get(rightButtonSelector).click();
      assertSlidesPosition(slidesSelector, 1);
    });
  });

  it("Check if dragging is working", () => {
    cy.mountWithPlugins(TestSlideShow, {}).then(() => {
      assertSlidesPosition(slidesSelector);
      dragSlideTo(genericSlideSelector, 1, leftOffset);
      assertSlidesPosition(slidesSelector, 1);
      dragSlideTo(genericSlideSelector, 2, leftOffset);
      assertSlidesPosition(slidesSelector, 2);
      dragSlideTo(genericSlideSelector, 2, leftOffset + SLIDE_DELTA);
      assertSlidesPosition(slidesSelector, 1);
      dragSlideTo(genericSlideSelector, 1, leftOffset + SLIDE_DELTA);
      assertSlidesPosition(slidesSelector, 0);
    });
  });
});

const slidesSelector = ".test__slides";
const leftButtonSelector = "button[aria-label='Previous slide']";
const rightButtonSelector = "button[aria-label='Next slide']";
const genericSlideSelector = ".test__slide";
const leftOffset = 10;
const topOffset = 10;

/**
 * Checks that a slide show is centered at a given slide
 * @param slidesSelector the selector for the slides wrapper
 * @param centerSlide the index of the slide that is expected to be in the center
 * @param initialCenterSlide the index of the slide that was centered initially
 */
function assertSlidesPosition(slidesSelector: string, centerSlide?: number, initialCenterSlide = 0): void {
  const expectedTransformValue =
    centerSlide == undefined ? "none" : `matrix(1, 0, 0, 1, ${-SLIDE_DELTA * (centerSlide - initialCenterSlide)}, 0)`;
  cy.get(slidesSelector).should("have.css", "transform", expectedTransformValue);
}

/**
 * Drags the selected slide to the given position
 * @param genericSlideSelector a selector that applies for each of the slides in the slide show
 * @param slideIndex the slide to drag
 * @param targetX the targets position x value in viewport coordinates
 */
function dragSlideTo(genericSlideSelector: string, slideIndex: number, targetX: number): void {
  cy.get(genericSlideSelector).eq(slideIndex).click();
  cy.get(genericSlideSelector)
    .eq(slideIndex)
    .trigger("pointerdown", leftOffset, topOffset, { button: 0 })
    .trigger("pointermove", {
      eventConstructor: "MouseEvent",
      clientX: targetX,
      clientY: topOffset,
    })
    .trigger("pointerup", { button: 0 });
}
