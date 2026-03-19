import { SiteNav } from "../root";
import type { Route } from "./+types/analytics-rentals";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Rental Analytics | SUMMS" },
    {
      name: "description",
      content: "Analytics related to rentals and usage patterns.",
    },
  ];
}

export default function RentalAnalyticsPage() {
  return (
    <>
      <SiteNav />
      <main className="ml-64 p-4 container mx-auto bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Rental Analytics</h1>
        <p className="text-gray-400">
          Analytics related to rentals and usage patterns.
        </p>
      </main>
    </>
  );
}
