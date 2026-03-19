import { useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router";
import { isAuthenticated } from "../utils/auth";

export default function ProtectedLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [canRenderProtectedRoute, setCanRenderProtectedRoute] = useState(false);

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate("/", { replace: true, state: { from: location.pathname } });
      return;
    }

    setCanRenderProtectedRoute(true);
  }, [location.pathname, navigate]);

  if (!canRenderProtectedRoute) {
    return null;
  }

  return <Outlet />;
}
