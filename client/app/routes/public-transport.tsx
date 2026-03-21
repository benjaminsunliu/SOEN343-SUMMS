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
    <main className="ml-56 p-4 container mx-auto bg-gray-900 min-h-screen">
      <h1 className="text-2xl font-semibold mb-2 text-white">Public Transportation</h1>
      <p className="text-gray-400">
        Navigation entry point for the public transportation feature (external service).
      </p>
    </main>
  );
}
