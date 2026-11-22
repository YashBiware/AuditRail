import http.server
import socketserver
import webbrowser
import os
import sys

PORT = 5050
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

def main():
    os.chdir(DIRECTORY)
    print("=" * 70)
    print("  AuditRail Simulated Client Banking Application (Investor Showcase)")
    print("=" * 70)
    print(f"  [+] Serving simulated banking application at: http://localhost:{PORT}")
    print(f"  [+] Demonstrates dual-tenant isolation (Apex Bank vs Horizon Health)")
    print(f"  [+] Automatically forwards SQL mutations to AuditRail Core Engine")
    print("=" * 70)
    
    url = f"http://localhost:{PORT}/index.html"
    try:
        webbrowser.open(url)
    except Exception:
        pass

    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print("  Press Ctrl+C to terminate the client application server.\n")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n  Shutting down demo server cleanly.")
            httpd.server_close()

if __name__ == "__main__":
    main()
