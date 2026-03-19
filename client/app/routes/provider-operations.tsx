import type { Route } from "./+types/provider-operations";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Provider Operations | SUMMS" },
    {
      name: "description",
      content: "Additional mobility provider-level operations.",
    },
  ];
}

export default function ProviderOperationsPage() {
  return (
    <main className="pt-16 p-4 container mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Mobility Provider Operations</h1>
      <p className="text-gray-600 dark:text-gray-300">
        This is the placeholder page for provider-level operations defined in your use cases.
      </p>
    </main>
  );
}
