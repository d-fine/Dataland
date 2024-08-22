import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the SingleSelectFormElement', () => {
  const placeholderText = 'Empty';
  const nonExistantOption = 'This option does not exist';

  it('Selecting an unknown option via API should be allowed and displayed in the UI when enabled', () => {
    getMountingFunction()(SingleSelectFormElement, {
      props: {
        allowUnknownOption: true,
        modelValue: nonExistantOption,
      },
    }).then(() => {
      cy.get('span.p-dropdown-label').should('have.text', nonExistantOption);
    });
  });

  it('An unknown option should be auto-deselected if unknown options are not allowed', () => {
    getMountingFunction()(SingleSelectFormElement, {
      props: {
        placeholder: placeholderText,
        allowUnknownOption: false,
        modelValue: nonExistantOption,
      },
    }).then(() => {
      cy.get('span.p-dropdown-label').should('have.text', placeholderText);
    });
  });

  it('An unknown option should be auto-deselected if the auto-deselection feature is enabled mid-run', () => {
    getMountingFunction()(SingleSelectFormElement, {
      props: {
        placeholder: placeholderText,
        allowUnknownOption: true,
        modelValue: nonExistantOption,
      },
    }).then((mounted) => {
      cy.get('span.p-dropdown-label').should('have.text', nonExistantOption);

      cy.wrap(mounted.wrapper).then((wrapper) => {
        void wrapper.setProps({
          allowUnknownOption: false,
        });
        cy.get('span.p-dropdown-label').should('have.text', placeholderText);
      });
    });
  });

  for (const allowUnknownOption of [true, false]) {
    it(
      'Removing the currently selected option from the list of allowed option should result in a deselection ' +
        `if allowUnknownOption is set to '${allowUnknownOption}'`,
      () => {
        getMountingFunction()(SingleSelectFormElement, {
          props: {
            placeholder: placeholderText,
            allowUnknownOption: allowUnknownOption,
            deselectRemovedOptionsOnShrinkage: true,
            options: [
              {
                label: 'A',
                value: 'A',
              },
              {
                label: 'B',
                value: 'B',
              },
            ],
            modelValue: 'A',
          },
        }).then((mounted) => {
          cy.wrap(mounted.wrapper).then(() => {
            cy.get('span.p-dropdown-label').should('have.text', 'A');
          });

          cy.wrap(mounted.wrapper).then((wrapper) => {
            void wrapper.setProps({
              options: [
                {
                  label: 'B',
                  value: 'B',
                },
              ],
            });

            cy.get('span.p-dropdown-label').should('have.text', placeholderText);
          });
        });
      }
    );
  }
});
