import { useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router";
import { hasAnyRole, type AuthRole, isAuthenticated } from "../utils/auth";
import { SiteNav } from "../root";

interface RouteRoleRule {
  pattern: RegExp;
  roles: AuthRole[];
}

const routeRoleRules: RouteRoleRule[] = [
  { pattern: /^\/provider(\/|$)/, roles: ["PROVIDER", "ADMIN"] },
  { pattern: /^\/analytics(\/|$)/, roles: ["PROVIDER", "ADMIN"] },
];

function isRouteAuthorized(pathname: string): boolean {
  const matchedRule = routeRoleRules.find((rule) => rule.pattern.test(pathname));
  if (!matchedRule) {
    return true;
  }

  return hasAnyRole(matchedRule.roles);
}

export default function ProtectedLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [canRenderProtectedRoute, setCanRenderProtectedRoute] = useState(false);

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/", { replace: true, state: { from: location.pathname } });
      return;
    }

    if (!isRouteAuthorized(location.pathname)) {
      navigate("/dashboard", { replace: true });
      return;
    }

    setCanRenderProtectedRoute(true);
  }, [location.pathname, navigate]);

  if (!canRenderProtectedRoute) {
    return null;
  }

  return (
    <div className="flex h-screen bg-gray-900 overflow-hidden">
      <SiteNav />
      <div className="flex-1 overflow-y-auto">
        <Outlet />
      </div>
    </div>
  );
}
