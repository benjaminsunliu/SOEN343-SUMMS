import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router";
import type { Route } from "./+types/register";
import { apiUrl } from "../utils/api";
import { isAuthenticated, persistAuth } from "../utils/auth";

interface RegistrationFormData {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: "CITIZEN" | "PROVIDER" | "CITY_PROVIDER";
}

interface FormErrors {
  name?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
}

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
    { title: "Register | SUMMS" },
    {
      name: "description",
      content: "Create your SUMMS account and set mobility preferences.",
    },
  ];
}

export default function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<RegistrationFormData>({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: "CITIZEN",
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (isAuthenticated()) {
      navigate("/dashboard", { replace: true });
    }
  }, [navigate]);

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
    setErrorMessage(null);
    setSuccessMessage(null);
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Name is required.";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address.";
    }

    if (!formData.password) {
      newErrors.password = "Password is required.";
    } else if (formData.password.length < 6) {
      newErrors.password = "Password must be at least 6 characters.";
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = "Please confirm your password.";
    } else if (formData.confirmPassword !== formData.password) {
      newErrors.confirmPassword = "Passwords do not match.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await fetch(apiUrl("/api/auth/register"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: formData.name.trim(),
          email: formData.email.trim(),
          password: formData.password,
          role: formData.role,
        }),
      });

      if (!response.ok) {
        let message = "Registration failed. Please try again.";
        try {
          const data = await response.json();
          if (data && typeof data.message === "string") {
            message = data.message;
          }
        } catch (_) {
          // ignore JSON parse errors
        }
        setErrorMessage(message);
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
      setSuccessMessage(
        data.message || "Registration successful. Redirecting to your dashboard..."
      );

      // Optionally clear the form before navigating
      setFormData({
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
        role: "CITIZEN",
      });

      // Navigate to the dashboard after successful registration
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setErrorMessage("Network error. Please check your connection and try again.");
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-900 px-4">
      <div className="w-full max-w-lg rounded-2xl bg-gray-800 shadow-md border border-gray-700 p-8 space-y-6">
        <header className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight text-white">
            Create your account
          </h1>
          <p className="text-sm text-gray-400">
            Register to SUMMS and tell us about your mobility preferences.
          </p>
        </header>

        {successMessage && (
          <div className="rounded-md border border-green-600 bg-green-900 bg-opacity-20 px-4 py-2 text-sm text-green-300">
            {successMessage}
          </div>
        )}

        {errorMessage && (
          <div className="rounded-md border border-red-600 bg-red-900 bg-opacity-20 px-4 py-2 text-sm text-red-300">
            {errorMessage}
          </div>
        )}

        <form className="space-y-5" onSubmit={handleSubmit} noValidate>
          {/* Role Selection Buttons */}
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-gray-300">
              Account role
            </label>
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => handleChange({
                  target: { name: "role", value: "CITIZEN" },
                } as any)}
                className={`flex-1 flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition ${
                  formData.role === "CITIZEN"
                    ? "bg-cyan-600 text-white"
                    : "border border-gray-600 text-gray-400 hover:text-white hover:border-gray-500"
                }`}
              >
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                </svg>
                Citizen
              </button>
              <button
                type="button"
                onClick={() => handleChange({
                  target: { name: "role", value: "PROVIDER" },
                } as any)}
                className={`flex-1 flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition ${
                  formData.role === "PROVIDER"
                    ? "bg-cyan-600 text-white"
                    : "border border-gray-600 text-gray-400 hover:text-white hover:border-gray-500"
                }`}
              >
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.22.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm11 0c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z" />
                </svg>
                Provider
              </button>
              <button
                type="button"
                onClick={() => handleChange({
                  target: { name: "role", value: "CITY_PROVIDER" },
                } as any)}
                className={`flex-1 flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-medium transition ${
                  formData.role === "CITY_PROVIDER"
                    ? "bg-cyan-600 text-white"
                    : "border border-gray-600 text-gray-400 hover:text-white hover:border-gray-500"
                }`}
              >
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M3 5h18v3H3V5zm2 5h14v9H5v-9zm3 2v5h2v-5H8zm6 0v5h2v-5h-2z" />
                </svg>
                City Provider
              </button>
            </div>
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="name"
              className="block text-sm font-medium text-gray-300"
            >
              Full name
            </label>
            <input
              id="name"
              name="name"
              type="text"
              autoComplete="name"
              value={formData.name}
              onChange={handleChange}
              className="block w-full rounded-lg border border-gray-600 bg-gray-700 px-3 py-2 text-sm text-white placeholder-gray-500 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
            />
            {errors.name && (
              <p className="text-xs text-red-400">{errors.name}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="email"
              className="block text-sm font-medium text-gray-300"
            >
              Email
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              className="block w-full rounded-lg border border-gray-600 bg-gray-700 px-3 py-2 text-sm text-white placeholder-gray-500 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
            />
            {errors.email && (
              <p className="text-xs text-red-400">{errors.email}</p>
            )}
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <label
                htmlFor="password"
                className="block text-sm font-medium text-gray-300"
              >
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                className="block w-full rounded-lg border border-gray-600 bg-gray-700 px-3 py-2 text-sm text-white placeholder-gray-500 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
              />
              {errors.password && (
                <p className="text-xs text-red-400">{errors.password}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="confirmPassword"
                className="block text-sm font-medium text-gray-300"
              >
                Confirm password
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="block w-full rounded-lg border border-gray-600 bg-gray-700 px-3 py-2 text-sm text-white placeholder-gray-500 shadow-sm focus:border-cyan-500 focus:outline-none focus:ring-2 focus:ring-cyan-500/40"
              />
              {errors.confirmPassword && (
                <p className="text-xs text-red-400">{errors.confirmPassword}</p>
              )}
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="inline-flex w-full items-center justify-center rounded-lg bg-cyan-600 px-4 py-2.5 text-sm font-medium text-gray-900 shadow-sm transition hover:bg-cyan-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-cyan-500 focus-visible:ring-offset-2 focus-visible:ring-offset-gray-800 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {submitting ? "Registering..." : "Register"}
          </button>
        </form>

        <p className="text-sm text-gray-400">
          Already have an account?{" "}
          <Link
            to="/"
            className="font-medium text-cyan-400 hover:text-cyan-300"
          >
            Login
          </Link>
        </p>
      </div>
    </main>
  );
}
