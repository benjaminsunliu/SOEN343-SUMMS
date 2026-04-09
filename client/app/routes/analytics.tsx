import { useEffect, useMemo, useState } from "react";
import type { Route } from "./+types/analytics";
import {
  fetchTransitAnalytics,
  fetchTransitLineStatuses,
  searchTransitRoutes,
  type TransitAnalyticsResponse,
  type TransitLineStatus,
  type TransitRoute,
} from "../utils/api";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Analytics | SUMMS" },
    {
      name: "description",
      content: "City provider transit analytics dashboard.",
    },
  ];
}

export default function CityAnalyticsPage() {
  const [routes, setRoutes] = useState<TransitRoute[]>([]);
  const [lineStatuses, setLineStatuses] = useState<TransitLineStatus[]>([]);
  const [analytics, setAnalytics] = useState<TransitAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadTransitAnalyticsData() {
      setLoading(true);
      setError(null);

      const now = new Date();
      const date = now.toISOString().slice(0, 10);
      const time = `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;

      const [routesResult, statusesResult, analyticsResult] = await Promise.allSettled([
        searchTransitRoutes({
          origin: "",
          destination: "",
          date,
          time,
          type: "ALL",
        }),
        fetchTransitLineStatuses(),
        fetchTransitAnalytics(),
      ]);

      if (!isMounted) {
        return;
      }

      if (routesResult.status === "fulfilled") {
        setRoutes(routesResult.value);
      } else {
        setRoutes([]);
      }

      if (statusesResult.status === "fulfilled") {
        setLineStatuses(statusesResult.value);
      } else {
        setLineStatuses([]);
      }

      if (analyticsResult.status === "fulfilled") {
        setAnalytics(analyticsResult.value);
      } else {
        setAnalytics(null);
      }

      if (
        routesResult.status === "rejected" &&
        statusesResult.status === "rejected" &&
        analyticsResult.status === "rejected"
      ) {
        setError("Unable to load transit analytics data.");
      }

      setLoading(false);
    }

    void loadTransitAnalyticsData();

    return () => {
      isMounted = false;
    };
  }, []);

  const totalTrips = routes.length;
  const totalSearches = analytics?.totalSearches ?? 0;

  const topDestinations = useMemo(() => {
    if (analytics?.topDestinations?.length) {
      return analytics.topDestinations.slice(0, 5).map((item) => ({
        label: item.destination,
        count: item.count,
      }));
    }

    return Object.entries(
      routes.reduce<Record<string, number>>((acc, route) => {
        const key = route.destination?.trim() || "Unknown";
        acc[key] = (acc[key] ?? 0) + 1;
        return acc;
      }, {}),
    )
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
      .map(([label, count]) => ({ label, count }));
  }, [analytics?.topDestinations, routes]);

  const mostUsedTransitType = useMemo(() => {
    const fromResultAnalytics = analytics?.topResultTransitTypes?.[0];
    if (fromResultAnalytics) {
      return { label: fromResultAnalytics.type, count: fromResultAnalytics.count };
    }

    const fromRoutes = Object.entries(
      routes.reduce<Record<string, number>>((acc, route) => {
        const key = route.type?.trim() || "UNKNOWN";
        acc[key] = (acc[key] ?? 0) + 1;
        return acc;
      }, {}),
    ).sort((a, b) => b[1] - a[1])[0];

    if (!fromRoutes) {
      return { label: "N/A", count: 0 };
    }

    return { label: fromRoutes[0], count: fromRoutes[1] };
  }, [analytics?.topResultTransitTypes, routes]);

  const mostUsedLine = useMemo(() => {
    const fromResultAnalytics = analytics?.topResultLines?.[0];
    if (fromResultAnalytics && fromResultAnalytics.lineNumber) {
      const lineLabel = fromResultAnalytics.lineName
        ? `${fromResultAnalytics.lineNumber} - ${fromResultAnalytics.lineName}`
        : fromResultAnalytics.lineNumber;
      return { label: lineLabel, count: fromResultAnalytics.count };
    }

    const grouped = Object.entries(
      routes.reduce<Record<string, number>>((acc, route) => {
        const label = `${route.lineNumber} - ${route.lineName}`;
        acc[label] = (acc[label] ?? 0) + 1;
        return acc;
      }, {}),
    ).sort((a, b) => b[1] - a[1])[0];

    if (!grouped) {
      return { label: "N/A", count: 0 };
    }

    return { label: grouped[0], count: grouped[1] };
  }, [analytics?.topResultLines, routes]);

  const delayedOrDisruptedCount = useMemo(
    () =>
      lineStatuses.filter(
        (status) => status.status === "DELAYED" || status.status === "DISRUPTED",
      ).length,
    [lineStatuses],
  );

  return (
    <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <header className="mb-6">
          <h1 className="text-3xl font-bold text-white">Analytics</h1>
          <p className="text-sm text-gray-400 mt-1">
            City provider transit statistics from live API data.
          </p>
        </header>

        {error && (
          <div className="mb-6 rounded-lg border border-red-600 bg-red-900/20 px-4 py-3 text-red-300">
            {error}
          </div>
        )}

        {loading ? (
          <div className="rounded-lg border border-gray-700 bg-gray-800 px-4 py-6 text-gray-300">
            Loading transit analytics...
          </div>
        ) : (
          <>
            <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
              <MetricCard label="Transit Trips" value={String(totalTrips)} />
              <MetricCard label="Transit Searches" value={String(totalSearches)} />
              <MetricCard
                label="Most Used Transit Type"
                value={`${mostUsedTransitType.label} (${mostUsedTransitType.count})`}
              />
              <MetricCard
                label="Most Used Line / Bus"
                value={`${mostUsedLine.label} (${mostUsedLine.count})`}
              />
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="bg-gray-800 rounded-lg border border-gray-700 p-5">
                <h2 className="text-lg font-semibold text-white mb-3">Most Common Destinations</h2>
                {topDestinations.length === 0 ? (
                  <p className="text-sm text-gray-400">No destination data available.</p>
                ) : (
                  <div className="space-y-2">
                    {topDestinations.map((item) => (
                      <div
                        key={item.label}
                        className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2"
                      >
                        <span className="text-gray-300">{item.label}</span>
                        <span className="font-semibold text-cyan-300">{item.count}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="bg-gray-800 rounded-lg border border-gray-700 p-5">
                <h2 className="text-lg font-semibold text-white mb-3">Transit Network Health</h2>
                <div className="space-y-2">
                  <InsightRow label="Lines Monitored" value={String(lineStatuses.length)} />
                  <InsightRow
                    label="Delayed or Disrupted Lines"
                    value={String(delayedOrDisruptedCount)}
                  />
                  <InsightRow
                    label="On-Time Lines"
                    value={String(Math.max(0, lineStatuses.length - delayedOrDisruptedCount))}
                  />
                </div>
              </div>
            </section>
          </>
        )}
      </div>
    </main>
  );
}

interface MetricCardProps {
  label: string;
  value: string;
}

function MetricCard({ label, value }: MetricCardProps) {
  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4">
      <p className="text-sm text-gray-400">{label}</p>
      <p className="text-xl font-bold text-cyan-300 mt-2">{value}</p>
    </div>
  );
}

function InsightRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2">
      <span className="text-sm text-gray-400">{label}</span>
      <span className="text-sm font-semibold text-gray-200">{value}</span>
    </div>
  );
}