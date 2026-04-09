import { useEffect, useMemo, useState } from "react";
import type { Route } from "./+types/analytics";
import {
  fetchCityParkingAnalytics,
  fetchTransitAnalytics,
  fetchTransitLineStatuses,
  type CityParkingAnalytics,
  type TransitAnalyticsResponse,
  type TransitLineStatus,
} from "../utils/api";

type AnalyticsView = "transit" | "parking";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "City Analytics | SUMMS" },
    {
      name: "description",
      content: "City provider parking and transit analytics dashboard.",
    },
  ];
}

export default function CityAnalyticsPage() {
  const [activeView, setActiveView] = useState<AnalyticsView>("transit");
  const [lineStatuses, setLineStatuses] = useState<TransitLineStatus[]>([]);
  const [analytics, setAnalytics] = useState<TransitAnalyticsResponse | null>(null);
  const [parkingAnalytics, setParkingAnalytics] = useState<CityParkingAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadTransitAnalyticsData() {
      setLoading(true);
      setError(null);

      const [statusesResult, analyticsResult, parkingAnalyticsResult] = await Promise.allSettled([
        fetchTransitLineStatuses(),
        fetchTransitAnalytics(),
        fetchCityParkingAnalytics(),
      ]);

      if (!isMounted) {
        return;
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

      if (parkingAnalyticsResult.status === "fulfilled") {
        setParkingAnalytics(parkingAnalyticsResult.value);
      } else {
        setParkingAnalytics(null);
      }

      if (
        statusesResult.status === "rejected" &&
        analyticsResult.status === "rejected" &&
        parkingAnalyticsResult.status === "rejected"
      ) {
        setError("Unable to load city analytics data.");
      }

      setLoading(false);
    }

    void loadTransitAnalyticsData();

    return () => {
      isMounted = false;
    };
  }, []);

  const totalTrips = analytics?.totalTrips ?? 0;
  const totalSearches = analytics?.totalSearches ?? 0;

  const topDestinations = useMemo(() => {
    return (
      analytics?.topDestinations
        ?.map((item) => ({
          label: item.destination?.trim() ?? "",
          value: item.count,
        }))
        .filter((item) => item.label.length > 0)
        .slice(0, 5) ?? []
    );
  }, [analytics?.topDestinations]);

  const topTransitTypes = useMemo(() => {
    return (
      analytics?.topResultTransitTypes
        ?.map((item) => ({
          label: item.type?.trim() ?? "",
          value: item.count,
        }))
        .filter((item) => item.label.length > 0)
        .slice(0, 5) ?? []
    );
  }, [analytics?.topResultTransitTypes]);

  const topLines = useMemo(() => {
    return (
      analytics?.topResultLines
        ?.map((item) => {
          const lineNumber = item.lineNumber?.trim() ?? "";
          const lineName = item.lineName?.trim() ?? "";
          return {
            label: lineName ? `${lineNumber} - ${lineName}` : lineNumber,
            value: item.count,
          };
        })
        .filter((item) => item.label.length > 0)
        .slice(0, 5) ?? []
    );
  }, [analytics?.topResultLines]);

  const mostUsedTransitType = topTransitTypes[0] ?? { label: "N/A", value: 0 };
  const mostUsedLine = topLines[0] ?? { label: "N/A", value: 0 };

  const delayedOrDisruptedCount = useMemo(
    () =>
      lineStatuses.filter(
        (status) => status.status === "DELAYED" || status.status === "DISRUPTED",
      ).length,
    [lineStatuses],
  );

  const parkingFacilities = parkingAnalytics?.facilities ?? [];
  const activeParkingReservations = parkingAnalytics?.activeReservations ?? [];

  const mostReservedParkingSpaces = useMemo(() => {
    return [...parkingFacilities]
      .filter((facility) => facility.reservedSpaces > 0)
      .sort((a, b) => {
        if (b.reservedSpaces !== a.reservedSpaces) {
          return b.reservedSpaces - a.reservedSpaces;
        }
        if (b.occupiedSpaces !== a.occupiedSpaces) {
          return b.occupiedSpaces - a.occupiedSpaces;
        }
        return a.name.localeCompare(b.name);
      })
      .slice(0, 5)
      .map((facility) => ({
        label: facility.name,
        value: facility.reservedSpaces,
      }));
  }, [parkingFacilities]);

  const highestRevenueParkingSpaces = useMemo(() => {
    return [...parkingFacilities]
      .filter((facility) => facility.totalRevenue > 0)
      .sort((a, b) => {
        if (b.totalRevenue !== a.totalRevenue) {
          return b.totalRevenue - a.totalRevenue;
        }
        return a.name.localeCompare(b.name);
      })
      .slice(0, 5)
      .map((facility) => ({
        label: facility.name,
        value: facility.totalRevenue,
      }));
  }, [parkingFacilities]);

  return (
    <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <header className="mb-6">
          <h1 className="text-3xl font-bold text-white">City Analytics</h1>
          <p className="text-sm text-gray-400 mt-1">
            Parking and transit performance for city-managed mobility services.
          </p>
        </header>

        <div className="mb-6 flex flex-wrap gap-3">
          <AnalyticsToggleButton
            label="Transit Analytics"
            selected={activeView === "transit"}
            onClick={() => setActiveView("transit")}
          />
          <AnalyticsToggleButton
            label="Parking Analytics"
            selected={activeView === "parking"}
            onClick={() => setActiveView("parking")}
          />
        </div>

        {error && (
          <div className="mb-6 rounded-lg border border-red-600 bg-red-900/20 px-4 py-3 text-red-300">
            {error}
          </div>
        )}

        {loading ? (
          <div className="rounded-lg border border-gray-700 bg-gray-800 px-4 py-6 text-gray-300">
            Loading city analytics...
          </div>
        ) : activeView === "transit" ? (
          <>
            <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
              <MetricCard label="Transit Trips" value={String(totalTrips)} />
              <MetricCard label="Transit Searches" value={String(totalSearches)} />
              <MetricCard
                label="Most Used Transit Type"
                value={`${mostUsedTransitType.label} (${mostUsedTransitType.value})`}
              />
              <MetricCard
                label="Most Used Line / Bus"
                value={`${mostUsedLine.label} (${mostUsedLine.value})`}
              />
            </section>

            <section className="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
              <RankedListCard
                title="Most Common Destinations"
                items={topDestinations}
                emptyMessage="No destination data available."
              />
              <RankedListCard
                title="Most Common Transit Types"
                items={topTransitTypes}
                emptyMessage="No transit type data available."
              />
              <RankedListCard
                title="Most Common Lines / Buses"
                items={topLines}
                emptyMessage="No line or bus data available."
              />
            </section>

            <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
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
        ) : (
          <>
            <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-5 gap-4 mb-6">
              <MetricCard
                label="Managed Facilities"
                value={String(parkingAnalytics?.totalFacilities ?? 0)}
              />
              <MetricCard label="Total Parking Spots" value={String(parkingAnalytics?.totalSpots ?? 0)} />
              <MetricCard
                label="Reserved Spots"
                value={String(parkingAnalytics?.reservedSpaces ?? 0)}
              />
              <MetricCard
                label="Occupied Now"
                value={String(parkingAnalytics?.occupiedSpaces ?? 0)}
              />
              <MetricCard
                label="Parking Revenue"
                value={formatCurrency(parkingAnalytics?.totalRevenue ?? 0)}
              />
            </section>

            <section className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
              <RankedListCard
                title="Most Reserved Parking Spaces"
                items={mostReservedParkingSpaces}
                emptyMessage="No reserved parking spaces yet."
              />
              <RankedListCard
                title="Highest Revenue Parking Spaces"
                items={highestRevenueParkingSpaces}
                emptyMessage="No parking revenue yet."
                valueFormatter={(value) => formatCurrency(value)}
              />
            </section>

            <section className="bg-gray-800 rounded-lg border border-gray-700 p-5 mb-6">
              <h2 className="text-lg font-semibold text-white mb-3">Parking Space Performance</h2>
              {parkingFacilities.length === 0 ? (
                <p className="text-sm text-gray-400">No parking spaces added yet.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-gray-700">
                        <th className="px-3 py-2 text-left text-gray-300">Parking Space</th>
                        <th className="px-3 py-2 text-right text-gray-300">Total Spots</th>
                        <th className="px-3 py-2 text-right text-gray-300">Reserved</th>
                        <th className="px-3 py-2 text-right text-gray-300">Occupied Now</th>
                        <th className="px-3 py-2 text-right text-gray-300">Available</th>
                        <th className="px-3 py-2 text-right text-gray-300">Revenue</th>
                      </tr>
                    </thead>
                    <tbody>
                      {parkingFacilities.map((facility) => (
                        <tr
                          key={facility.facilityId}
                          className="border-b border-gray-800"
                        >
                          <td className="px-3 py-3">
                            <p className="font-medium text-white">{facility.name}</p>
                            <p className="text-xs text-gray-400">{facility.city}</p>
                          </td>
                          <td className="px-3 py-3 text-right text-gray-300">{facility.totalSpots}</td>
                          <td className="px-3 py-3 text-right text-cyan-300">{facility.reservedSpaces}</td>
                          <td className="px-3 py-3 text-right text-amber-300">{facility.occupiedSpaces}</td>
                          <td className="px-3 py-3 text-right text-green-300">{facility.availableSpots}</td>
                          <td className="px-3 py-3 text-right text-cyan-300">
                            {formatCurrency(facility.totalRevenue)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            <section className="bg-gray-800 rounded-lg border border-gray-700 p-5">
              <h2 className="text-lg font-semibold text-white mb-3">Active Parking Occupancy</h2>
              {activeParkingReservations.length === 0 ? (
                <p className="text-sm text-gray-400">No parking spaces are currently occupied.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-gray-700">
                        <th className="px-3 py-2 text-left text-gray-300">Parking Space</th>
                        <th className="px-3 py-2 text-left text-gray-300">Arrival</th>
                        <th className="px-3 py-2 text-right text-gray-300">Duration</th>
                        <th className="px-3 py-2 text-right text-gray-300">Revenue</th>
                        <th className="px-3 py-2 text-left text-gray-300">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {activeParkingReservations.map((reservation) => (
                        <tr
                          key={reservation.reservationId}
                          className="border-b border-gray-800"
                        >
                          <td className="px-3 py-3">
                            <p className="font-medium text-white">{reservation.facilityName}</p>
                            <p className="text-xs text-gray-400">{reservation.city}</p>
                          </td>
                          <td className="px-3 py-3 text-gray-300">
                            {reservation.arrivalDate} {reservation.arrivalTime}
                          </td>
                          <td className="px-3 py-3 text-right text-gray-300">
                            {reservation.durationHours}h
                          </td>
                          <td className="px-3 py-3 text-right text-cyan-300">
                            {formatCurrency(reservation.totalCost)}
                          </td>
                          <td className="px-3 py-3 text-gray-300">{reservation.status}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
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

interface RankedListItem {
  label: string;
  value: number;
}

function AnalyticsToggleButton({
  label,
  selected,
  onClick,
}: {
  label: string;
  selected: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-lg border px-4 py-2 text-sm font-medium transition-colors ${
        selected
          ? "border-cyan-500 bg-cyan-500/15 text-cyan-300"
          : "border-gray-700 bg-gray-800 text-gray-300 hover:border-gray-600 hover:text-white"
      }`}
    >
      {label}
    </button>
  );
}

function MetricCard({ label, value }: MetricCardProps) {
  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4">
      <p className="text-sm text-gray-400">{label}</p>
      <p className="text-xl font-bold text-cyan-300 mt-2">{value}</p>
    </div>
  );
}

function RankedListCard({
  title,
  items,
  emptyMessage,
  valueFormatter,
}: {
  title: string;
  items: RankedListItem[];
  emptyMessage: string;
  valueFormatter?: (value: number) => string;
}) {
  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-5">
      <h2 className="text-lg font-semibold text-white mb-3">{title}</h2>
      {items.length === 0 ? (
        <p className="text-sm text-gray-400">{emptyMessage}</p>
      ) : (
        <div className="space-y-2">
          {items.map((item) => (
            <div
              key={item.label}
              className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2"
            >
              <span className="text-gray-300">{item.label}</span>
              <span className="font-semibold text-cyan-300">
                {valueFormatter ? valueFormatter(item.value) : item.value}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function formatCurrency(amount: number) {
  return `$${amount.toFixed(2)}`;
}

function InsightRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2">
      <span className="text-sm text-gray-400">{label}</span>
      <span className="text-sm font-semibold text-gray-200">{value}</span>
    </div>
  );
}
