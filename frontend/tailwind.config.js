/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#00008F', // brand navy override
          custom: '#000051',
          container: '#00008f',
          fixed: '#e0e0ff',
          fixed_dim: '#bfc2ff',
        },
        secondary: {
          DEFAULT: '#F26522', // energetic orange
          alt: '#a63b00',
          container: '#fc6c29',
          fixed: '#ffdbce',
          fixed_dim: '#ffb599',
        },
        tertiary: {
          DEFAULT: '#009CDE', // sky blue
          alt: '#001421',
          container: '#002a40',
          fixed: '#c9e6ff',
          fixed_dim: '#89ceff',
        },
        surface: {
          DEFAULT: '#f9f9ff',
          bright: '#f9f9ff',
          dim: '#d2daec',
          variant: '#dbe3f5',
          tint: '#4950c5',
        },
        surface_container: {
          lowest: '#ffffff',
          low: '#f0f3ff',
          DEFAULT: '#e7eeff',
          high: '#e0e8fb',
          highest: '#dbe3f5',
        },
        on_surface: {
          DEFAULT: '#141c29',
          variant: '#454653',
        },
        on_primary: '#ffffff',
        on_primary_container: '#7a82f9',
        on_primary_fixed: '#00006e',
        on_primary_fixed_variant: '#2f36ac',
        inverse_surface: '#29313e',
        inverse_on_surface: '#ebf1ff',
        inverse_primary: '#bfc2ff',
        outline: {
          DEFAULT: '#767685',
          variant: '#c6c5d5',
        },
        error: {
          DEFAULT: '#ba1a1a',
          container: '#ffdad6',
        },
        on_error: '#ffffff',
        on_error_container: '#93000a',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        display: ['"Public Sans"', 'sans-serif'],
      },
      borderRadius: {
        sm: '0.25rem',
        md: '0.375rem',
        lg: '0.5rem',
        xl: '0.75rem',
        full: '9999px',
      },
      boxShadow: {
        ambient: '0 12px 40px rgba(20, 28, 41, 0.06)',
        dropdown: '0 4px 16px rgba(20, 28, 41, 0.08)',
      }
    },
  },
  plugins: [],
}
