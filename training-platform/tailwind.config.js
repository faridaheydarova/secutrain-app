/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'sans-serif'],
        display: ['Syne', 'sans-serif'],
      },
      colors: {
        primary: { DEFAULT: '#2563EB', dark: '#1D4ED8', light: '#DBEAFE' },
        accent: '#F59E0B',
        surface: '#F8FAFC',
      }
    },
  },
  plugins: [],
}
