import { SiteNav } from "../root";
import type { Route } from "./+types/vehicle-search";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Search | SUMMS" },
    { name: "description", content: "Search available vehicles for rental." },
  ];
}

export default function VehicleSearchPage() {
  return (
    <>
      <SiteNav />
      <main className="pt-16 p-4 container mx-auto">
        <h1 className="text-2xl font-semibold mb-2">Vehicle Search</h1>
        <p className="text-gray-600">
          This is the placeholder page for searching vehicles in the rental lifecycle.
        </p>
      </main>
    </>
  );
}
