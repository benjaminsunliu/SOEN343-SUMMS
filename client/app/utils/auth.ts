export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: string;
}

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
      typeof parsed.role === "string"
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

