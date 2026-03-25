import { SiteNav } from "../root";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { apiFetch } from "../utils/api";
import type { Route } from "./+types/dashboard";

interface RentalMetric {
  metricName: string;
  dimension: string;
  count: number;
}

interface RentalAnalyticsResponse {
  totalActiveRentals: number;
  rentalsByVehicleType: RentalMetric[];
}

interface ApiAccessMetric {
  endpoint: string;
  timeWindow: string;
  accessCount: number;
}

interface GatewayAnalyticsResponse {
  metrics: {
    [key: string]: ApiAccessMetric[];
  };
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Admin Analytics Dashboard | SUMMS" },
    {
      name: "description",
      content: "Admin dashboard displaying rental and gateway analytics.",
    },
  ];
}

export default function AdminAnalyticsDashboard() {
  const navigate = useNavigate();
  
  // Rental analytics state
  const [rentalAnalytics, setRentalAnalytics] = useState<RentalAnalyticsResponse | null>(null);
  const [rentalLoading, setRentalLoading] = useState(true);
  const [rentalError, setRentalError] = useState<string | null>(null);

  // Gateway analytics state
  const [gatewayAnalytics, setGatewayAnalytics] = useState<GatewayAnalyticsResponse | null>(null);
  const [gatewayLoading, setGatewayLoading] = useState(true);
  const [gatewayError, setGatewayError] = useState<string | null>(null);

  // Fetch rental analytics
  useEffect(() => {
    const fetchRentalAnalytics = async () => {
      try {
        setRentalLoading(true);
        const response = await apiFetch("/api/admin/analytics/rentals");

        if (!response.ok) {
          if (response.status === 403) {
            setRentalError("Unauthorized: Admin access required");
            return;
          }
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        setRentalAnalytics(data);
        setRentalError(null);
      } catch (err) {
        setRentalError(err instanceof Error ? err.message : "Failed to fetch rental analytics");
      } finally {
        setRentalLoading(false);
      }
    };

    fetchRentalAnalytics();
  }, []);

  // Fetch gateway analytics
  useEffect(() => {
    const fetchGatewayAnalytics = async () => {
      try {
        setGatewayLoading(true);
        const response = await apiFetch("/api/admin/analytics/gateway");

        if (!response.ok) {
          if (response.status === 403) {
            setGatewayError("Unauthorized: Admin access required");
            return;
          }
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        setGatewayAnalytics(data);
        setGatewayError(null);
      } catch (err) {
        setGatewayError(err instanceof Error ? err.message : "Failed to fetch gateway analytics");
      } finally {
        setGatewayLoading(false);
      }
    };

    fetchGatewayAnalytics();
  }, []);

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-8 bg-gray-900 min-h-screen">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">Admin Analytics Dashboard</h1>
          <p className="text-gray-400">Real-time system performance and usage metrics</p>
        </div>

        {/* Two-column layout: Rentals (left) and Gateway (right) */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* LEFT: RENTAL ANALYTICS */}
          <div className="bg-gray-800 rounded-lg shadow-lg p-6 border border-gray-700">
            <h2 className="text-xl font-semibold text-white mb-6 flex items-center">
              <span className="w-3 h-3 bg-blue-500 rounded-full mr-3"></span>
              Rental Analytics
            </h2>

            {rentalLoading && (
              <div className="flex justify-center py-8">
                <div className="text-gray-400">Loading rental data...</div>
              </div>
            )}

            {rentalError && (
              <div className="bg-red-900 border border-red-500 text-red-100 px-4 py-3 rounded mb-4">
                <p className="font-semibold">Error</p>
                <p className="text-sm">{rentalError}</p>
              </div>
            )}

            {rentalAnalytics && !rentalLoading && (
              <>
                {/* Total Active Rentals Card */}
                <div className="bg-gray-700 rounded-lg p-4 mb-6 border border-blue-500/30">
                  <div className="text-gray-400 text-sm font-semibold uppercase tracking-wide mb-2">
                    Total Active Rentals
                  </div>
                  <div className="text-4xl font-bold text-blue-400">
                    {rentalAnalytics.totalActiveRentals}
                  </div>
                </div>

                {/* Rentals by Vehicle Type */}
                <div>
                  <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wide mb-3">
                    Breakdown by Vehicle Type
                  </h3>

                  {rentalAnalytics.rentalsByVehicleType.length === 0 ? (
                    <p className="text-gray-500 text-sm py-4">No active rentals by vehicle type</p>
                  ) : (
                    <div className="space-y-2">
                      {rentalAnalytics.rentalsByVehicleType.map((metric, idx) => (
                        <div key={idx} className="bg-gray-700 rounded p-3 flex justify-between items-center">
                          <span className="text-gray-300 font-medium">{metric.dimension}</span>
                          <span className="text-blue-400 font-semibold text-lg">{metric.count}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </>
            )}
          </div>

          {/* RIGHT: GATEWAY ANALYTICS */}
          <div className="bg-gray-800 rounded-lg shadow-lg p-6 border border-gray-700">
            <h2 className="text-xl font-semibold text-white mb-6 flex items-center">
              <span className="w-3 h-3 bg-green-500 rounded-full mr-3"></span>
              Gateway Analytics
            </h2>

            {gatewayLoading && (
              <div className="flex justify-center py-8">
                <div className="text-gray-400">Loading gateway data...</div>
              </div>
            )}

            {gatewayError && (
              <div className="bg-red-900 border border-red-500 text-red-100 px-4 py-3 rounded mb-4">
                <p className="font-semibold">Error</p>
                <p className="text-sm">{gatewayError}</p>
              </div>
            )}

            {gatewayAnalytics && !gatewayLoading && (
              <div className="space-y-6">
                {Object.entries(gatewayAnalytics.metrics).map(([timeWindow, metrics]) => (
                  <div key={timeWindow}>
                    <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wide mb-3">
                      {formatTimeWindow(timeWindow)}
                    </h3>

                    {metrics.length === 0 ? (
                      <p className="text-gray-500 text-sm py-2">No activity</p>
                    ) : (
                      <div className="space-y-2">
                        {metrics.map((metric, idx) => (
                          <div key={idx} className="bg-gray-700 rounded p-3 flex justify-between items-center">
                            <span className="text-gray-300 font-medium text-sm">
                              {formatEndpointName(metric.endpoint)}
                            </span>
                            <span className="text-green-400 font-semibold text-lg">
                              {metric.accessCount}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Refresh Button */}
        <div className="mt-8 flex justify-end">
          <button
            onClick={() => window.location.reload()}
            className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg transition-colors"
          >
            Refresh Data
          </button>
        </div>
      </main>
    </>
  );
}

/**
 * Format time window strings for display
 * e.g., "TWENTY_FOUR_HOURS" → "Last 24 Hours"
 */
function formatTimeWindow(timeWindow: string): string {
  const mapping: Record<string, string> = {
    TWENTY_FOUR_HOURS: "Last 24 Hours",
    WEEK: "Last 7 Days",
    MONTH: "Last 30 Days",
  };
  return mapping[timeWindow] || timeWindow;
}

/**
 * Format endpoint names for display
 * e.g., "VEHICLE_SEARCH" → "Vehicle Search"
 */
function formatEndpointName(endpoint: string): string {
  return endpoint
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(" ");
}
