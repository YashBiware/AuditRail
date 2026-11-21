/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#FAF9F6', // Warm organic parchment paper (oklch(98.1% .007 88))
        foreground: '#1F2421', // Deep graphite charcoal (oklch(21.5% .012 70))
        card: '#FFFFFF',       // Clean card surface (oklch(99.7% .003 90))
        'card-foreground': '#1F2421',
        primary: '#212529',    // Refined deep charcoal
        'primary-foreground': '#F8F9FA',
        secondary: '#F2EFE9',  // Subtle warm sand
        'secondary-foreground': '#2C302E',
        muted: '#EBE7DF',
        'muted-foreground': '#6C757D', // Neutral editorial secondary
        border: '#E2DDD3',     // Elegant tactile border (oklch(89.5% .009 84))
        signal: '#2A6F97',     // Slate blue-teal cryptographic signal accent (oklch(47.5% .058 205))
        'signal-foreground': '#FFFFFF',
        accent: '#DCE6E5',
        'accent-foreground': '#1B3B36',
        destructive: '#B02A37', // Crimson for tamper alerts
        'destructive-foreground': '#FFFFFF',
      },
      fontFamily: {
        sans: ['"Instrument Sans"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        serif: ['"Instrument Serif"', 'Georgia', 'serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
      borderRadius: {
        DEFAULT: '6px',
        sm: '4px',
        md: '6px',
        lg: '8px',
        xl: '12px',
        '2xl': '16px',
      },
      boxShadow: {
        subtle: '0 1px 3px 0 rgba(0, 0, 0, 0.04), 0 1px 2px -1px rgba(0, 0, 0, 0.04)',
        card: '0 0 0 1px #E2DDD3, 0 2px 4px -1px rgba(0, 0, 0, 0.02), 0 4px 6px -2px rgba(0, 0, 0, 0.04)',
        float: '0 12px 32px -8px rgba(0, 0, 0, 0.08), 0 4px 12px -2px rgba(0, 0, 0, 0.04)',
      }
    },
  },
  plugins: [],
}
