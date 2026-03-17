const configuredApiBase = import.meta.env.VITE_API_BASE_URL?.trim();
const defaultDevApiBase = "http://localhost:8080";

const apiBase = configuredApiBase || (import.meta.env.DEV ? defaultDevApiBase : "");

export function apiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${apiBase}${normalizedPath}`;
}

