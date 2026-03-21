import { useEffect, useState } from "react";
import { useNavigate, Link, useLocation } from "react-router";
import type { Route } from "./+types/login";
import { apiUrl } from "../utils/api";
import { isAuthenticated, persistAuth } from "../utils/auth";

interface AuthResponsePayload {
  id: number;
  name: string;
  email: string;
  role: string;
  message: string;
  token: string;
}

export function meta({ }: Route.MetaArgs) {
  return [
    { title: "Login | SUMMS" },
    { name: "description", content: "User authentication and access." },
  ];
}

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [authResolved, setAuthResolved] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (isAuthenticated()) {
      navigate("/dashboard", { replace: true });
      return;
    }

    setAuthResolved(true);
  }, [navigate]);

  const redirectPath =
    location.state &&
      typeof location.state === "object" &&
      "from" in location.state &&
      typeof location.state.from === "string"
      ? location.state.from
      : "/dashboard";

  const validate = () => {
    let message: string | null = null;
    if (!email.trim() || !password.trim()) {
      message = "Email and password are required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      message = "Please enter a valid email address.";
    }
    setError(message);
    return !message;
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await fetch(apiUrl("/api/auth/login"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.trim(), password }),
      });

      if (!response.ok) {
        let message = "Login failed. Please try again.";
        try {
          const data = await response.json();
          if (data && typeof data.message === "string") {
            message = data.message;
          }
        } catch (_) {
          // Ignore JSON parse errors
        }
        setError(message);
        return;
      }

      const data = (await response.json()) as AuthResponsePayload;
      persistAuth({
        id: data.id,
        name: data.name,
        email: data.email,
        role: data.role,
        token: data.token,
      });
      navigate(redirectPath, { replace: true });
    } catch {
      setError("Network error. Please check your connection and try again.");
    }
  };

  if (!authResolved) {
    return null;
  }

  return (
    <main className="flex min-h-[calc(100vh-56px)] items-center justify-center bg-white px-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-md border border-gray-200 p-8 space-y-6">
        <header className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight text-gray-900">
            Welcome back
          </h1>
          <p className="text-sm text-gray-600">
            Sign in to access your SUMMS dashboard.
          </p>
        </header>

        {error && (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
            {error}
          </div>
        )}

        <form className="space-y-4" onSubmit={handleSubmit}>
          <div className="space-y-1.5">
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-700"
            >
              Email
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
            />
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700"
            >
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button
            type="submit"
            className="inline-flex w-full items-center justify-center rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 focus-visible:ring-offset-gray-50"
          >
            Login
          </button>
        </form>

        <p className="text-sm text-gray-600">
          Don&apos;t have an account?{" "}
          <Link
            to="/register"
            className="font-medium text-blue-600 hover:underline"
          >
            Register
          </Link>
        </p>
      </div>
    </main>
  );
}
