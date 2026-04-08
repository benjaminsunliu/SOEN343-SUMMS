export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: string;
  token: string;
}

export type AuthRole = "CITIZEN" | "PROVIDER" | "CITY_PROVIDER" | "ADMIN";

const AUTH_STORAGE_KEY = "summs.authUser";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

export function persistAuth(user: AuthUser): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
}

export function getAuthUser(): AuthUser | null {
  if (!isBrowser()) {
    return null;
  }

  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<AuthUser>;
    if (
      typeof parsed.id === "number" &&
      typeof parsed.name === "string" &&
      typeof parsed.email === "string" &&
      typeof parsed.role === "string" &&
      typeof parsed.token === "string"
    ) {
      return parsed as AuthUser;
    }
  } catch {
    // Ignore malformed auth payload and force sign-in again.
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY);
  return null;
}

export function isAuthenticated(): boolean {
  return getAuthUser() !== null;
}

export function clearAuth(): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY);
}

export function getAuthToken(): string | null {
  const user = getAuthUser();
  return user ? user.token : null;
}

export function getAuthRole(): AuthRole | null {
  const user = getAuthUser();
  if (!user) {
    return null;
  }

  const normalizedRole = user.role.trim().toUpperCase();
  if (
    normalizedRole === "CITIZEN" ||
    normalizedRole === "PROVIDER" ||
    normalizedRole === "CITY_PROVIDER" ||
    normalizedRole === "ADMIN"
  ) {
    return normalizedRole;
  }

  return null;
}

export function getDefaultDashboardPath(): string {
  const role = getAuthRole();
  if (role === "CITY_PROVIDER") {
    return "/city/dashboard";
  }

  return "/dashboard";
}

export function hasAnyRole(roles: AuthRole[]): boolean {
  const role = getAuthRole();
  return role !== null && roles.includes(role);
}
