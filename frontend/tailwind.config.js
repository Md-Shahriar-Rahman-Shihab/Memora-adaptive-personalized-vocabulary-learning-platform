/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        memora: {
          bg: '#FBFBF9',
          card: '#FFFFFF',
          'card-subtle': '#F8F8F5',
          green: {
            DEFAULT: '#6A8D2F',
            light: '#EBF2DF',
            hover: '#5B7C26',
            dark: '#455E1B',
            glow: '#92C443',
          },
          dark: {
            DEFAULT: '#141A14',
            surface: '#171F17',
            surfaceCard: '#212A21',
            muted: '#2D382D',
          },
          text: {
            DEFAULT: '#141A14',
            muted: '#6E766D',
            light: '#9AA199',
          },
          border: 'rgba(20, 26, 20, 0.08)',
        },
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
        serif: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'soft': '0 4px 20px -2px rgba(20, 26, 20, 0.05)',
        'card': '0 10px 30px -5px rgba(20, 26, 20, 0.06), 0 2px 6px -1px rgba(20, 26, 20, 0.02)',
        'glow': '0 0 25px rgba(106, 141, 47, 0.25)',
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
        '4xl': '2rem',
      },
    },
  },
  plugins: [],
}
