import type { Route } from "./+types/city-dashboard";
import ProviderParkingPage from "./provider-parking";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "City Provider Dashboard | SUMMS" },
    {
      name: "description",
      content: "Manage city parking and transit-adjacent parking operations.",
    },
  ];
}

export default function CityDashboardPage() {
  return <ProviderParkingPage />;
}
