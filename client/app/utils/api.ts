import { getAuthToken } from "./auth";

export function apiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  // Use explicit API origin when provided, otherwise rely on relative paths.
  const env = import.meta.env as Record<string, string | boolean | undefined>;
  const apiOrigin =
    typeof env.VITE_API_ORIGIN === "string" ? env.VITE_API_ORIGIN : undefined;
  if (apiOrigin && apiOrigin.length > 0) {
    return `${apiOrigin.replace(/\/$/, "")}${normalizedPath}`;
  }

  return normalizedPath;
}

export async function apiFetch(
  url: string,
  options: RequestInit = {},
): Promise<Response> {
  const headers = new Headers(authHeaders(options.headers));

  // Default to JSON for API payloads while preserving caller overrides.
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(apiUrl(url), {
    ...options,
    headers,
  });
}

export function authHeaders(extraHeaders?: HeadersInit): HeadersInit {
  const token = getAuthToken();
  const headers = new Headers(extraHeaders);

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  return headers;
}
