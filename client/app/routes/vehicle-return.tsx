import type { Route } from "./+types/vehicle-return";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Return | SUMMS" },
    { name: "description", content: "Handle vehicle returns after rentals." },
  ];
}

export default function VehicleReturnPage() {
  return (
    <main className="pt-16 p-4 container mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Vehicle Return</h1>
      <p className="text-gray-600 dark:text-gray-300">
        This is the placeholder page for returning vehicles at the end of the rental lifecycle.
      </p>
    </main>
  );
}
