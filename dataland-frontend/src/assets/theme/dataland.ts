import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

export const DatalandPreset = definePreset(Aura, {
  primitive: {
    borderRadius: {
      none: '0',
      xs: '0.5rem',
      sm: '1rem',
      md: '1.5rem',
      lg: '2rem',
      xl: '3rem',
    },
    orange: {
      50: '#fae7db',
      100: '#f8cdb2',
      200: '#f4b38a',
      300: '#ee9963',
      400: '#e67f3f', // orange-prime
      500: '#cc7139', // orange-prime-dark
      600: '#a45b2f',
      700: '#7d4626',
      800: '#58321d',
      900: '#361f13',
      950: '#170b03',
    },
    blue: {
      50: '#f1f6f8',
      100: '#ddeaef',
      200: '#c9dde5',
      300: '#b5d1dc',
      400: '#a1c5d3', //blue-light
      500: '#87adba',
      600: '#6d95a2',
      700: '#547e8b',
      800: '#3b6774',
      900: '#22525d',
      950: '#013d48', // aquamarine-dark
    },
  },
  semantic: {
    transitionDuration: '',
    focusRing: {

    },
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
          contrastColor: '{neutral.50}',
          hoverColor: '{orange.500}',
          activeColor: '{orange.500}',
        },
        highlight: {
          background: '{neutral.900}',
          focusBackground: '{neutral.900}',
          color: '{neutral.900}',
          focusColor: '{neutral.900}',
        },
      },
    },
  },
  components: {
    tabs: {
      tab: {
        fontWeight: '700',
        padding: '1.25rem 0',
        margin: '0 1.25rem',
      }
    }
  }
});
