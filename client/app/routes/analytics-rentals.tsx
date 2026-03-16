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
      <main className="pt-16 p-4 container mx-auto">
        <h1 className="text-2xl font-semibold mb-2">Rental Analytics</h1>
        <p className="text-gray-600">
          This is the placeholder page for rental-related analytics.
        </p>
      </main>
    </>
  );
}
