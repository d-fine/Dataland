import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

/**
 * In this file, we define the basic primeVue preset for Dataland.
 * The Dataland design is derived from the Aura preset, and particular design tokens are overwritten. In particular, we
 * overwrite the primitive colors 'orange' and 'blue'.
 */
export const DatalandPreset = definePreset(Aura, {
  primitive: {
    borderRadius: {
      none: '0',
      xxs: '0.25rem',
      xs: '0.5rem',
      sm: '1rem',
      md: '1.5rem',
      lg: '2rem',
      xl: '3rem',
    },
    orange: {
      50: '#ffefe5',
      100: '#ffd0b1',
      200: '#ffb07f',
      300: '#ff8f4d',
      400: '#ff6813', // orange-prime
      500: '#d55818', // orange-prime-dark
      600: '#ac4718',
      700: '#843716',
      800: '#5e2813',
      900: '#5e2813',
      950: '#1a0900',
    },
    blue: {
      50: '#f1f6f8',
      100: '#ddeaef',
      200: '#c9dde5',
      300: '#b5d1dc',
      400: '#a1c5d3',
      500: '#87adba',
      600: '#6d95a2',
      700: '#547e8b',
      800: '#3b6774',
      900: '#22525d',
      950: '#013d48', // aquamarine-dark
    },
  },
  semantic: {
    primary: {
      50: '{orange.50}',
      100: '{orange.100}',
      200: '{orange.200}',
      300: '{orange.300}',
      400: '{orange.400}',
      500: '{orange.500}',
      600: '{orange.600}',
      700: '{orange.700}',
      800: '{orange.800}',
      900: '{orange.900}',
      950: '{orange.950}',
    },
    overlay: {
      select: {
        borderRadius: '{border.radius.none}',
      },
    },
    list: {
      option: {
        borderRadius: '{border.radius.none}',
      },
    },
    colorScheme: {
      light: {
        surface: {
          0: '#ffffff',
          50: '{neutral.50}',
          100: '{neutral.100}',
          200: '{neutral.200}',
          300: '{neutral.300}',
          400: '{neutral.400}',
          500: '{neutral.500}',
          600: '{neutral.600}',
          700: '{neutral.700}',
          800: '{neutral.800}',
          900: '{neutral.900}',
          950: '{neutral.950}',
        },
        primary: {
          color: '{orange.400}',
          contrastColor: '{neutral.900}',
          hoverColor: '{orange.500}',
          activeColor: '{orange.500}',
        },
        highlight: {
          background: '{slate.900}',
          focusBackground: '{slate.900}',
          color: '{blue.300}',
          focusColor: '{neutral.900}',
        },
        content: {
          borderColor: '{surface.200}',
          extend: {
            hoverBorderColor: '{surface.800}',
          },
        },
        formField: {
          filledHoverBackground: '{surface.100}',
          filledFocusBackground: '{surface.100}',
          borderColor: '{surface.100}',
          hoverBorderColor: '{surface.0}',
          focusBorderColor: '{blue.300}',
          invalidBorderColor: '{surface.0}',
          borderRadius: '{border.radius.none}',
          shadow: 'none',
          focusRing: {
            width: '3px',
            style: 'solid',
            color: '{blue.200}',
          },
        },
      },
    },
  },
  components: {
    tabs: {
      tablist: {
        borderWidth: '0',
      },
      tabpanel: {
        padding: '0',
      },
      tab: {
        padding: '1.25rem 0',
        margin: '0 1.25rem',
        borderWidth: '0 0 2px 0',
        borderColor: '{surface.0}',
        hoverBorderColor: '{content.hoverBorderColor}',
      },
      activeBar: {
        height: '0',
        bottom: '0',
      },
      css: () => `
      .p-tab.p-disabled {
        display: none;
}`,
    },
    button: {
      root: {
        borderRadius: '{border.radius.none}',
      },
      colorScheme: {
        light: {
          root: {
            primary: {
              color: '{neutral.50}',
              hoverColor: '{neutral.50}',
            },
          },
        },
      },
    },
  },
});
