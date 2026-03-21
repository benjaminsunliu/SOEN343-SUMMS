import type { Route } from "./+types/parking";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Parking | SUMMS" },
    {
      name: "description",
      content: "Navigation entry point for the parking feature (external service).",
    },
  ];
}

export default function ParkingPage() {
  return (
    <main className="ml-56 p-4 bg-gray-900 min-h-screen">
      <h1 className="text-2xl font-semibold mb-2 text-white">Parking</h1>
      <p className="text-gray-400">
        Navigation entry point for the parking feature (external service).
      </p>
    </main>
  );
}
