import { useState } from "react";
import { useNavigate, Link } from "react-router";
import type { Route } from "./+types/register";

interface RegistrationFormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  mobilityPreferences: string;
}

interface FormErrors {
  firstName?: string;
  lastName?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
}

export function meta({}: Route.MetaArgs) {
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
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    mobilityPreferences: "",
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
    setErrorMessage(null);
    setSuccessMessage(null);
  };

  const validate = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = "First name is required.";
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = "Last name is required.";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email is required.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address.";
    }

    if (!formData.password) {
      newErrors.password = "Password is required.";
    } else if (formData.password.length < 8) {
      newErrors.password = "Password must be at least 8 characters.";
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
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          firstName: formData.firstName.trim(),
          lastName: formData.lastName.trim(),
          email: formData.email.trim(),
          password: formData.password,
          mobilityPreferences: formData.mobilityPreferences.trim(),
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

      setSuccessMessage("Registration successful. Redirecting to your dashboard...");

      // Optionally clear the form before navigating
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
        mobilityPreferences: "",
      });

      // Navigate to the dashboard after successful registration
      navigate("/dashboard");
    } catch (error) {
      setErrorMessage("Network error. Please check your connection and try again.");
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <main className="flex min-h-[calc(100vh-56px)] items-center justify-center bg-white px-4">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-md border border-gray-200 p-8 space-y-6">
        <header className="space-y-1">
          <h1 className="text-2xl font-semibold tracking-tight text-gray-900">
            Create your account
          </h1>
          <p className="text-sm text-gray-600">
            Register to SUMMS and tell us about your mobility preferences.
          </p>
        </header>

        {successMessage && (
          <div className="rounded-md border border-green-200 bg-green-50 px-4 py-2 text-sm text-green-800">
            {successMessage}
          </div>
        )}

        {errorMessage && (
          <div className="rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-800">
            {errorMessage}
          </div>
        )}

        <form className="space-y-5" onSubmit={handleSubmit} noValidate>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <label
                htmlFor="firstName"
                className="block text-sm font-medium text-gray-700"
              >
                First name
              </label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                autoComplete="given-name"
                value={formData.firstName}
                onChange={handleChange}
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
              />
              {errors.firstName && (
                <p className="text-xs text-red-500">{errors.firstName}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="lastName"
                className="block text-sm font-medium text-gray-700"
              >
                Last name
              </label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                autoComplete="family-name"
                value={formData.lastName}
                onChange={handleChange}
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
              />
              {errors.lastName && (
                <p className="text-xs text-red-500">{errors.lastName}</p>
              )}
            </div>
          </div>

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
              value={formData.email}
              onChange={handleChange}
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
            />
            {errors.email && (
              <p className="text-xs text-red-500">{errors.email}</p>
            )}
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
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
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
              />
              {errors.password && (
                <p className="text-xs text-red-500">{errors.password}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="confirmPassword"
                className="block text-sm font-medium text-gray-700"
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
                className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
              />
              {errors.confirmPassword && (
                <p className="text-xs text-red-500">{errors.confirmPassword}</p>
              )}
            </div>
          </div>

          <div className="space-y-1.5">
            <label
              htmlFor="mobilityPreferences"
              className="block text-sm font-medium text-gray-700"
            >
              Mobility preferences
            </label>
            <textarea
              id="mobilityPreferences"
              name="mobilityPreferences"
              rows={3}
              value={formData.mobilityPreferences}
              onChange={handleChange}
              placeholder="Describe any mobility aids, accessibility needs, or preferences (e.g., wheelchair access, reduced walking distance)."
              className="block w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/40"
            />
            <p className="text-xs text-gray-500">
              This helps us better tailor services to your mobility needs.
            </p>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="inline-flex w-full items-center justify-center rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-medium text-white shadow-sm transition hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 focus-visible:ring-offset-gray-50 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {submitting ? "Registering..." : "Register"}
          </button>
        </form>
      </div>
    </main>
  );
}
