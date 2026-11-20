/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bg: {
          primary: '#090D10',    // Deep obsidian charcoal
          secondary: '#11181D',  // Graphite slate
          card: '#161F26',       // Elevation layer
          surface: '#1C2730',    // Hover & modal surface
        },
        border: {
          subtle: '#22323D',     // Subtle geometric outline
          strong: '#334B5C',     // High-contrast border
        },
        accent: {
          emerald: '#10B981',    // Verified cryptographic integrity green
          forest: '#059669',
          cyan: '#06B6D4',       // Checkpoint anchor cyan
          amber: '#F59E0B',      // Warning / drift detection
          rose: '#F43F5E',       // Tamper alert crimson
        },
        text: {
          primary: '#F3F4F6',    // Warm stark off-white
          secondary: '#9CA3AF',  // Muted documentation body
          muted: '#6B7280',      // Metadata caption
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      animation: {
        'pulse-slow': 'pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.5s ease-out forwards',
        'shimmer': 'shimmer 2.5s infinite linear',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        }
      }
    },
  },
  plugins: [],
}
