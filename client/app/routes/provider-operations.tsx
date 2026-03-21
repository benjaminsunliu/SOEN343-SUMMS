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

export default function ProviderOperationsPage() {
  const [vehicles, setVehicles] = useState<VehicleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const authUser = getAuthUser();
  const providerId = authUser?.id;

  useEffect(() => {
    fetchVehicles();
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

  // Calculate stats from vehicles
  const stats = {
    total: vehicles.length,
    available: vehicles.filter(v => v.status.toUpperCase() === "AVAILABLE").length,
    inUse: vehicles.filter(v => v.status.toUpperCase() === "IN_USE").length,
    reserved: vehicles.filter(v => v.status.toUpperCase() === "RESERVED").length,
    unavailable: vehicles.filter(v => v.status.toUpperCase() === "UNAVAILABLE").length,
  };

  const todayRevenue = 0; // Revenue will be populated from real data

  // Weekly data - empty for now
  const weeklyRevenueData = [
    { day: "Mon", revenue: 0 },
    { day: "Tue", revenue: 0 },
    { day: "Wed", revenue: 0 },
    { day: "Thu", revenue: 0 },
    { day: "Fri", revenue: 0 },
    { day: "Sat", revenue: 0 },
    { day: "Sun", revenue: 0 },
  ];

  const rentalActivityData = [
    { day: "Mon", rentals: 0 },
    { day: "Tue", rentals: 0 },
    { day: "Wed", rentals: 0 },
    { day: "Thu", rentals: 0 },
    { day: "Fri", rentals: 0 },
    { day: "Sat", rentals: 0 },
    { day: "Sun", rentals: 0 },
  ];

  const activeRentals = vehicles
    .filter(v => v.status.toUpperCase() === "IN_USE")
    .slice(0, 3)
    .map((v, idx) => ({
      vehicle: `${v.type.charAt(0).toUpperCase() + v.type.slice(1)} ${v.id}`,
      user: ["Jean Tremblay", "Marie Leblanc", "Michel Gagné"][idx] || "User",
      startTime: ["14h15", "13h30", "15h00"][idx] || "00h00",
      duration: ["45 min", "1h 30m", "20 min"][idx] || "30 min",
      revenue: ["$2.25", "$18.00", "$0.67"][idx] || "$0.00",
    }));

  const maxRevenue = Math.max(...weeklyRevenueData.map(d => d.revenue));

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
            <StatCard label="Active Rentals" value={stats.inUse} icon="👤" />
            <StatCard label="Today's Revenue" value={`$${todayRevenue.toFixed(0)} CAD`} icon="💵" />
            <StatCard label="Weekly Rentals" value={rentalActivityData.reduce((s, d) => s + d.rentals, 0)} icon="📈" />
          </div>

          {/* Charts - 2 columns */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
            {/* Weekly Revenue Chart */}
            <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
              <h3 className="text-lg font-semibold text-white mb-1">Weekly Revenue</h3>
              <p className="text-sm text-gray-400 mb-6">Revenue generated over the past week</p>
              <div className="flex items-end justify-between h-56 gap-1.5">
                {weeklyRevenueData.map((data) => (
                  <div key={data.day} className="flex flex-col items-center flex-1">
                    <div className="relative w-full flex justify-center mb-2">
                      <div
                        className="bg-blue-500 rounded-t w-12"
                        style={{ height: `${Math.max(data.revenue / 523 * 160, 4)}px` }}
                      />
                    </div>
                    <p className="text-xs font-medium text-gray-600">{data.day}</p>
                  </div>
                ))}
              </div>
            </div>

            {/* Rental Activity Chart */}
            <div className="bg-gray-800 rounded-lg border border-gray-700 p-6">
              <h3 className="text-lg font-semibold text-white mb-1">Rental Activity</h3>
              <p className="text-sm text-gray-400 mb-6">Number of rentals per day</p>
              <div className="h-56 flex items-center justify-center text-gray-500">
                <p>No rental data available</p>
              </div>
            </div>
          </div>

          {/* Active Rentals Table */}
          <div className="bg-gray-800 rounded-lg border border-gray-700 p-6 mb-8">
            <div className="flex items-center gap-2 mb-4">
              <span className="text-lg">📋</span>
              <h3 className="text-lg font-semibold text-white">Active Rentals</h3>
            </div>
            <p className="text-sm text-gray-400 mb-4">Currently rented vehicles</p>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Vehicle</th>
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">User</th>
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Start Time</th>
                    <th className="text-left font-semibold text-gray-300 py-3 px-4">Duration</th>
                    <th className="text-right font-semibold text-gray-300 py-3 px-4">Revenue</th>
                  </tr>
                </thead>
                <tbody>
                  {activeRentals.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-8 text-center text-gray-500">
                        No active rentals
                      </td>
                    </tr>
                  ) : (
                    activeRentals.map((rental, idx) => (
                      <tr key={idx} className="border-b border-gray-700 hover:bg-gray-700">
                        <td className="py-3 px-4 text-gray-300">{rental.vehicle}</td>
                        <td className="py-3 px-4 text-gray-400">{rental.user}</td>
                        <td className="py-3 px-4 text-gray-400">{rental.startTime}</td>
                        <td className="py-3 px-4 text-gray-400">{rental.duration}</td>
                        <td className="py-3 px-4 text-right text-cyan-400 font-medium">{rental.revenue}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
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
                      <th className="text-center font-semibold text-gray-300 py-3 px-4">Battery</th>
                      <th className="text-left font-semibold text-gray-300 py-3 px-4">Price</th>
                      <th className="text-center font-semibold text-gray-300 py-3 px-4">Total Rentals</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vehicles.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-8 text-center text-gray-500">
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
                            <td className="py-3 px-4 text-center text-gray-400"> 85%</td>
                            <td className="py-3 px-4 text-gray-300 font-medium">${vehicle.costPerMinute.toFixed(2)}/hr CAD</td>
                            <td className="py-3 px-4 text-center text-gray-400">{Math.floor(Math.random() * 300)}</td>
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

