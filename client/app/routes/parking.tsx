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
    <main className="pt-16 p-4 container mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Parking Feature</h1>
      <p className="text-gray-600 dark:text-gray-300">
        This is the placeholder page for navigating to the parking feature (can remain an abstracted external service).
      </p>
    </main>
  );
}
