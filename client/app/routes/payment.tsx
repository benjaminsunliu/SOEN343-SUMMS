import type { Route } from "./+types/payment";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Payment | SUMMS" },
    {
      name: "description",
      content: "Simulated payment processing for rentals.",
    },
  ];
}

export default function PaymentPage() {
  return (
    <main className="ml-56 p-4 bg-gray-900 min-h-screen">
      <h1 className="text-2xl font-semibold mb-2 text-white">Payment</h1>
      <p className="text-gray-400">
        Simulated payment processing for rentals.
      </p>
    </main>
  );
}
