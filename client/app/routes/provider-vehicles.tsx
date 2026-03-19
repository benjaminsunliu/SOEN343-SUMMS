import { SiteNav } from "../root";
import type { Route } from "./+types/provider-vehicles";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Provider Vehicles | SUMMS" },
    {
      name: "description",
      content: "Mobility provider vehicle management (add/update/remove).",
    },
  ];
}

export default function ProviderVehiclesPage() {
  return (
    <>
      <SiteNav />
      <main className="pt-16 p-4 container mx-auto">
        <h1 className="text-2xl font-semibold mb-2">Vehicle Management</h1>
        <p className="text-gray-600">
          This is the placeholder page for mobility providers to add, update, and remove vehicles.
        </p>
      </main>
    </>
  );
}
