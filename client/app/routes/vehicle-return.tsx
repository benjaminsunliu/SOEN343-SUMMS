import type { Route } from "./+types/vehicle-return";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Vehicle Return | SUMMS" },
    { name: "description", content: "Handle vehicle returns after rentals." },
  ];
}

export default function VehicleReturnPage() {
  return (
    <main className="ml-64 p-4 container mx-auto bg-gray-900 min-h-screen">
      <h1 className="text-2xl font-semibold mb-2 text-white">Vehicle Return</h1>
      <p className="text-gray-400">
        This is the placeholder page for returning vehicles at the end of the rental lifecycle.
      </p>
    </main>
  );
}
