import { SiteNav } from "../root";
import type { Route } from "./+types/dashboard";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Dashboard | SUMMS" },
    {
      name: "description",
      content: "Dashboard placeholder (navigation only).",
    },
  ];
}

export default function DashboardPage() {
  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-gray-900" />
    </>
  );
}


