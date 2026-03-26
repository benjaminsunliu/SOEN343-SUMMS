import { useState, useEffect } from "react";
import { SiteNav } from "../root";
import type { Route } from "./+types/provider-operations";
import { apiFetch } from "../utils/api";
import { getAuthUser } from "../utils/auth";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Provider Operations | SUMMS" },
    {
      name: "description",
      content: "Mobility provider dashboard and operations.",
    },
  ];
}

interface VehicleResponse {
  id: number;
  type: string;
  status: string;
  location: { latitude: number; longitude: number };
  providerId: number;
  costPerMinute: number;
  maxRange?: number;
  licensePlate?: string;
  seatingCapacity?: number;
}

interface ProviderTransaction {
  id: number;
  reservationId: number;
  userId: number;
  paymentMethod: string;
  amount: number;
  success: boolean;
  processorTransactionId: string;
  createdAt: string;
}

export default function ProviderOperationsPage() {
  const [vehicles, setVehicles] = useState<VehicleResponse[]>([]);
  const [transactions, setTransactions] = useState<ProviderTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingTransactions, setLoadingTransactions] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [transactionError, setTransactionError] = useState<string | null>(null);
  const authUser = getAuthUser();
  const providerId = authUser?.id;

  useEffect(() => {
    fetchVehicles();
  }, [providerId]);

  useEffect(() => {
    fetchTransactions();
  }, [providerId]);

  const fetchVehicles = async () => {
    if (!providerId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await apiFetch(`/api/vehicles/provider/${providerId}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch vehicles (${response.status})`);
      }
      const data = await response.json();
      setVehicles(data || []);
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async () => {
    if (!providerId) {
      setLoadingTransactions(false);
      return;
    }

    setLoadingTransactions(true);
    setTransactionError(null);
    try {
      const response = await apiFetch("/api/payments/transactions/provider/me");
      if (!response.ok) {
        throw new Error(`Failed to fetch transactions (${response.status})`);
      }
      const data = (await response.json()) as ProviderTransaction[];
      setTransactions(data ?? []);
    } catch (err) {
      setTransactions([]);
      setTransactionError(err instanceof Error ? err.message : "Failed to load transactions");
    } finally {
      setLoadingTransactions(false);
    }
  };

  // Calculate stats from vehicles
  const stats = {
    total: vehicles.length,
    available: vehicles.filter(v => v.status.toUpperCase() === "AVAILABLE").length,
    inUse: vehicles.filter(v => v.status.toUpperCase() === "IN_USE").length,
    reserved: vehicles.filter(v => v.status.toUpperCase() === "RESERVED").length,
    unavailable: vehicles.filter(v => v.status.toUpperCase() === "UNAVAILABLE").length,
  };

  const activeRentals = vehicles.filter(v => v.status.toUpperCase() === "IN_USE");
  const statusBreakdown = [
    { label: "Available", value: stats.available, color: "bg-green-500" },
    { label: "In Use", value: stats.inUse, color: "bg-blue-500" },
    { label: "Reserved", value: stats.reserved, color: "bg-yellow-500" },
    { label: "Unavailable", value: stats.unavailable, color: "bg-gray-500" },
  ];
  const maxStatusCount = Math.max(1, ...statusBreakdown.map(item => item.value));

  const typeBreakdown = Object.entries(
    vehicles.reduce<Record<string, number>>((acc, vehicle) => {
      const normalizedType = vehicle.type.toUpperCase();
      acc[normalizedType] = (acc[normalizedType] ?? 0) + 1;
      return acc;
    }, {}),
  ).sort((a, b) => b[1] - a[1]);

  return (
    <>
      <SiteNav />
      <main className="ml-56 bg-gray-900 min-h-screen">
        <div className="max-w-7xl mx-auto px-4 py-8">
          {/* Header */}
          <div className="flex justify-between items-start mb-8">
            <div>
              <h1 className="text-3xl font-bold text-white">Provider Dashboard</h1>
              <p className="text-gray-400 text-sm mt-1">Manage your fleet and monitor performance</p>
            </div>
            <a href="/provider/vehicles" className="bg-cyan-600 text-gray-900 px-4 py-2 rounded-lg text-sm font-medium hover:bg-cyan-500">
              + Add Vehicle
            </a>
          </div>

          {error && (
            <div className="mb-6 rounded-lg border border-red-600 bg-red-900 bg-opacity-20 px-4 py-3 text-red-300">
              {error}
            </div>
          )}

          {/* Stats Cards - 4 columns */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard label="Total Vehicles" value={stats.total} icon="🚲" />
            <StatCard label="Available Now" value={stats.available} icon="✅" />
            <StatCard label="Active Rentals" value={stats.inUse} icon="👤" />
            <StatCard label="Reserved" value={stats.reserved} icon="📌" />
          </div>

          {/* Insights - 2 columns */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
              <h3 className="text-lg font-semibold text-white mb-1">Fleet Status</h3>
              <p className="text-sm text-gray-400 mb-6">Live availability across your vehicles</p>
              <div className="space-y-4">
                {statusBreakdown.map((item) => (
                  <div key={item.label}>
                    <div className="mb-1 flex items-center justify-between text-sm">
                      <span className="text-gray-300">{item.label}</span>
                      <span className="text-gray-400">{item.value}</span>
                    </div>
                    <div className="h-2 rounded bg-gray-700">
                      <div
                        className={`${item.color} h-2 rounded`}
                        style={{ width: `${(item.value / maxStatusCount) * 100}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
              <h3 className="text-lg font-semibold text-white mb-1">Vehicle Types</h3>
              <p className="text-sm text-gray-400 mb-6">Current fleet composition</p>
              {typeBreakdown.length === 0 ? (
                <div className="h-56 flex items-center justify-center text-gray-500">
                  <p>No vehicles added yet</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {typeBreakdown.map(([type, count]) => (
                    <div key={type} className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2">
                      <span className="text-gray-300">{type}</span>
                      <span className="font-semibold text-cyan-400">{count}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Active Rentals Table */}
          <div className="bg-gray-800 rounded-lg border border-gray-700 p-6 mb-8">
            <div className="flex items-center gap-2 mb-4">
              <span className="text-lg">📋</span>
              <h3 className="text-lg font-semibold text-white">Active Rentals</h3>
            </div>
            <p className="text-sm text-gray-400 mb-4">Vehicles currently in use</p>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Vehicle</th>
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Type</th>
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Location</th>
                    <th className="text-right font-semibold text-gray-300 py-3 px-4">Rate</th>
                  </tr>
                </thead>
                <tbody>
                  {activeRentals.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="py-8 text-center text-gray-500">
                        No active rentals
                      </td>
                    </tr>
                  ) : (
                    activeRentals.map((vehicle) => (
                      <tr key={vehicle.id} className="border-b border-gray-700 hover:bg-gray-700">
                        <td className="py-3 px-4 text-gray-300">
                          {vehicle.type.toUpperCase()} #{vehicle.id}
                        </td>
                        <td className="py-3 px-4 text-gray-400 capitalize">{vehicle.type}</td>
                        <td className="py-3 px-4 text-gray-400 text-xs">
                          {vehicle.location.latitude.toFixed(4)}, {vehicle.location.longitude.toFixed(4)}
                        </td>
                        <td className="py-3 px-4 text-right text-cyan-400 font-medium">
                          ${vehicle.costPerMinute.toFixed(2)}/min CAD
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Recent Payment Transactions */}
          <div className="bg-gray-800 rounded-lg border border-gray-700 p-6 mb-8">
            <div className="flex items-center gap-2 mb-4">
              <span className="text-lg">💳</span>
              <h3 className="text-lg font-semibold text-white">Recent Payment Transactions</h3>
            </div>
            <p className="text-sm text-gray-400 mb-4">Payments tied to reservations on your vehicles</p>

            {transactionError && (
              <div className="mb-4 rounded border border-red-500 bg-red-900/20 px-3 py-2 text-sm text-red-300">
                {transactionError}
              </div>
            )}

            {loadingTransactions ? (
              <div className="py-6 text-center text-gray-400">Loading transactions...</div>
            ) : transactions.length === 0 ? (
              <div className="py-6 text-center text-gray-500">No payment transactions found yet.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-700">
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Date</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Reservation</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">User</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Method</th>
                      <th className="text-right font-semibold text-gray-300 py-3 px-4">Amount</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Status</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Transaction ID</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.slice(0, 12).map((transaction) => (
                      <tr key={transaction.id} className="border-b border-gray-700 hover:bg-gray-700/40">
                        <td className="py-3 px-4 text-gray-300">{formatDateTime(transaction.createdAt)}</td>
                        <td className="py-3 px-4 text-gray-300">#{transaction.reservationId}</td>
                        <td className="py-3 px-4 text-gray-400">#{transaction.userId}</td>
                        <td className="py-3 px-4 text-gray-300">{transaction.paymentMethod}</td>
                        <td className="py-3 px-4 text-right text-cyan-400">
                          ${transaction.amount.toFixed(2)}
                        </td>
                        <td className="py-3 px-4">
                          <span
                            className={`rounded px-2 py-1 text-xs font-semibold ${
                              transaction.success
                                ? "bg-green-500/20 text-green-300 border border-green-500/50"
                                : "bg-amber-500/20 text-amber-300 border border-amber-500/50"
                            }`}
                          >
                            {transaction.success ? "SUCCESS" : "FAILED"}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-gray-400">{transaction.processorTransactionId}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Manage Vehicles Table */}
          <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h3 className="text-lg font-semibold text-white">Manage Vehicles</h3>
                <p className="text-sm text-gray-400">View and manage your fleet</p>
              </div>
              <a href="/provider/vehicles" className="bg-cyan-600 text-gray-900 px-4 py-2 rounded-lg text-sm font-medium hover:bg-cyan-500">
                Manage Vehicles
              </a>
            </div>
            {loading ? (
              <div className="py-8 text-center text-gray-400">Loading vehicles...</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-700">
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Vehicle</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Type</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Status</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Location</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Price</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vehicles.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="py-8 text-center text-gray-500">
                          No vehicles. <a href="/provider/vehicles" className="text-cyan-400 hover:text-cyan-300">Add one</a>
                        </td>
                      </tr>
                    ) : (
                      vehicles.map((vehicle, index) => {
                        // Count how many vehicles of this type appear before this one
                        const vehicleTypeCount = vehicles
                          .slice(0, index + 1)
                          .filter(v => v.type.toUpperCase() === vehicle.type.toUpperCase()).length;

                        return (
                          <tr key={vehicle.id} className="border-b border-gray-700 hover:bg-gray-700">
                            <td className="py-3 px-4">
                              <div className="flex items-center gap-2">
                                <span>{getVehicleIcon(vehicle.type)}</span>
                                <span className="font-medium text-gray-300">{vehicle.type.toUpperCase()} #{vehicleTypeCount}</span>
                              </div>
                            </td>
                            <td className="py-3 px-4 text-gray-400 capitalize">{vehicle.type}</td>
                            <td className="py-3 px-4">
                              <StatusBadge status={vehicle.status} />
                            </td>
                            <td className="py-3 px-4 text-gray-400 text-xs">
                               {vehicle.location.latitude.toFixed(2)}, {vehicle.location.longitude.toFixed(2)}
                            </td>
                            <td className="py-3 px-4 text-gray-300 font-medium">${vehicle.costPerMinute.toFixed(2)}/min CAD</td>
                            <td className="py-3 px-4 text-gray-400">{getVehicleDetail(vehicle)}</td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </main>
    </>
  );
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

interface StatCardProps {
  label: string;
  value: string | number;
  icon: string;
}

function StatCard({ label, value, icon }: StatCardProps) {
  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-400">{label}</p>
          <p className="text-2xl font-bold text-white mt-2">{value}</p>
        </div>
        <span className="text-2xl">{icon}</span>
      </div>
    </div>
  );
}

interface StatusBadgeProps {
  status: string;
}

function StatusBadge({ status }: StatusBadgeProps) {
  const getStatusColor = (s: string) => {
    switch (s.toUpperCase()) {
      case "AVAILABLE":
        return "bg-green-100 text-green-800 border border-green-300";
      case "IN_USE":
        return "bg-blue-100 text-blue-800 border border-blue-300";
      case "RESERVED":
        return "bg-yellow-100 text-yellow-800 border border-yellow-300";
      case "UNAVAILABLE":
        return "bg-gray-100 text-gray-800 border border-gray-300";
      default:
        return "bg-gray-100 text-gray-800 border border-gray-300";
    }
  };

  return (
    <span className={`px-2 py-1 text-xs font-medium rounded ${getStatusColor(status)}`}>
      {status}
    </span>
  );
}

function getVehicleDetail(vehicle: VehicleResponse): string {
  const normalizedType = vehicle.type.toUpperCase();
  if (normalizedType === "CAR") {
    if (vehicle.licensePlate && vehicle.seatingCapacity != null) {
      return `${vehicle.licensePlate} · ${vehicle.seatingCapacity} seats`;
    }
    if (vehicle.licensePlate) {
      return vehicle.licensePlate;
    }
    if (vehicle.seatingCapacity != null) {
      return `${vehicle.seatingCapacity} seats`;
    }
    return "No car details";
  }

  if (normalizedType === "SCOOTER" && vehicle.maxRange != null) {
    return `${vehicle.maxRange.toFixed(1)} km range`;
  }

  return "N/A";
}

function getVehicleIcon(type: string) {
  switch (type.toUpperCase()) {
    case "BICYCLE":
      return "🚲";
    case "SCOOTER":
      return "🛴";
    case "CAR":
      return "🚗";
    default:
      return "🚙";
  }
}
