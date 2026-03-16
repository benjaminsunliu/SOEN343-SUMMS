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
      <main className="pt-16 p-4 container mx-auto">
        <h1 className="text-2xl font-semibold mb-2">Gateway / Service Analytics</h1>
        <p className="text-gray-600">
          This is the placeholder page for gateway or service-level analytics.
        </p>
      </main>
    </>
  );
}
