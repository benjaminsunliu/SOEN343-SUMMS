import type { Route } from "./+types/public-transport";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Public Transportation | SUMMS" },
    {
      name: "description",
      content: "Navigation entry point for the public transportation feature (external service).",
    },
  ];
}

export default function PublicTransportPage() {
  return (
    <main className="pt-16 p-4 container mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Public Transportation Feature</h1>
      <p className="text-gray-600 dark:text-gray-300">
        This is the placeholder page for navigating to the public transportation feature (can remain an abstracted external service).
      </p>
    </main>
  );
}
