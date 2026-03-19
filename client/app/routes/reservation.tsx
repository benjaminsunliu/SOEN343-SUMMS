import { SiteNav } from "../root";
import type { Route } from "./+types/reservation";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservation | SUMMS" },
    { name: "description", content: "Create and manage vehicle reservations." },
  ];
}

export default function ReservationPage() {
  return (
    <>
      <SiteNav />
      <main className="pt-16 p-4 container mx-auto">
        <h1 className="text-2xl font-semibold mb-2">Reservation</h1>
        <p className="text-gray-600">
          This is the placeholder page for reservation steps in the rental lifecycle.
        </p>
      </main>
    </>
  );
}
