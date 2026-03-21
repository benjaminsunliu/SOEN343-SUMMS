import { SiteNav } from "../root";
import type { Route } from "./+types/analytics-gateway";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Gateway Analytics | SUMMS" },
    {
      name: "description",
      content: "Gateway/service-level analytics for the mobility platform.",
    },
  ];
}

export default function GatewayAnalyticsPage() {
  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <h1 className="text-2xl font-semibold mb-2 text-white">Gateway Analytics</h1>
        <p className="text-gray-400">
          Gateway/service-level analytics for the mobility platform.
        </p>
      </main>
    </>
  );
}
