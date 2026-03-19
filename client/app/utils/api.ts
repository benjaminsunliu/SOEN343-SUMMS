export function apiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  // Use explicit API origin when provided, otherwise rely on relative paths.
  const env = import.meta.env as Record<string, string | boolean | undefined>;
  const apiOrigin = typeof env.VITE_API_ORIGIN === "string" ? env.VITE_API_ORIGIN : undefined;
  if (apiOrigin && apiOrigin.length > 0) {
    return `${apiOrigin.replace(/\/$/, "")}${normalizedPath}`;
  }

  return normalizedPath;
}

export async function apiFetch(
  url: string,
  options: RequestInit & { headers?: Record<string, string> } = {}
): Promise<Response> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  return fetch(apiUrl(url), {
    ...options,
    headers,
  });
}


