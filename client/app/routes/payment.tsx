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
    <main className="pt-16 p-4 container mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Payment Processing</h1>
      <p className="text-gray-600">
        This is the placeholder page for payment processing (simulation acceptable).
      </p>
    </main>
  );
}
