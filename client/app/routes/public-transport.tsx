import { SiteNav } from "../root";
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
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-6 py-5 text-white">
        <header className="mb-5 border-b border-[#253047] pb-3">
          <h1 className="text-3xl font-bold tracking-tight">Public Transit</h1>
        </header>
        <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-6 text-gray-300">
          <p className="text-lg">Empty page for now.</p>
        </section>
      </main>
    </>
  );
}
