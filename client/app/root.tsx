import {
  isRouteErrorResponse,
  Links,
  Meta,
  NavLink,
  Outlet,
  Scripts,
  ScrollRestoration,
  useNavigate,
} from "react-router";

import type { Route } from "./+types/root";
import "./app.css";
import { clearAuth, hasAnyRole, type AuthRole } from "./utils/auth";

export const links: Route.LinksFunction = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  {
    rel: "preconnect",
    href: "https://fonts.gstatic.com",
    crossOrigin: "anonymous",
  },
  {
    rel: "stylesheet",
    href: "https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap",
  },
];

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

export default function App() {
  return <Outlet />;
}

interface NavItem {
  label: string;
  to: string;
  roles?: AuthRole[];
}

const navItems: NavItem[] = [
  { label: "Dashboard", to: "/dashboard" },
  { label: "Search", to: "/vehicles/search" },
  { label: "Reserve", to: "/reservation" },
  { label: "Analytics", to: "/analytics/rentals", roles: ["PROVIDER", "ADMIN"] },
  { label: "Provider", to: "/provider/operations", roles: ["PROVIDER", "ADMIN"] },
];

export function SiteNav() {
  const navigate = useNavigate();
  const visibleNavItems = navItems.filter(
    (item) => !item.roles || hasAnyRole(item.roles),
  );

  const handleLogout = () => {
    clearAuth();
    navigate("/", { replace: true });
  };

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-gray-950 border-r border-gray-800 flex flex-col z-50">
      {/* Logo */}
      <div className="p-4 border-b border-gray-800">
        <h1 className="text-xl font-bold text-white">SUMMS</h1>
        <p className="text-xs text-gray-500">Smart Urban Mobility</p>
      </div>

      {/* Navigation Items */}
      <nav className="flex-1 px-3 py-6 space-y-2 overflow-y-auto">
        {visibleNavItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `block rounded-lg px-4 py-2 text-sm font-medium transition-colors ${isActive
                ? "bg-cyan-600 text-white"
                : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* Logout Button */}
      <div className="p-3 border-t border-gray-800">
        <button
          type="button"
          onClick={handleLogout}
          className="w-full rounded-lg px-4 py-2 text-sm font-medium text-gray-400 hover:text-white hover:bg-gray-800 transition-colors"
        >
          Logout
        </button>
      </div>
    </aside>
  );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  let message = "Oops!";
  let details = "An unexpected error occurred.";
  let stack: string | undefined;

  if (isRouteErrorResponse(error)) {
    message = error.status === 404 ? "404" : "Error";
    details =
      error.status === 404
        ? "The requested page could not be found."
        : error.statusText || details;
  } else if (import.meta.env.DEV && error && error instanceof Error) {
    details = error.message;
    stack = error.stack;
  }

  return (
    <main className="pt-16 p-4 container mx-auto">
      <h1>{message}</h1>
      <p>{details}</p>
      {stack && (
        <pre className="w-full p-4 overflow-x-auto">
          <code>{stack}</code>
        </pre>
      )}
    </main>
  );
}
