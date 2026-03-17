import { Navigate, Outlet, useLocation } from "react-router";
import { isAuthenticated } from "../utils/auth";

export default function ProtectedLayout() {
  const location = useLocation();

  if (!isAuthenticated()) {
    return <Navigate to="/" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}

