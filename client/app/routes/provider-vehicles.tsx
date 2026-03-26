import { useState, useEffect } from "react";
import { SiteNav } from "../root";
import type { Route } from "./+types/provider-vehicles";
import { apiFetch } from "../utils/api";
import { getAuthUser } from "../utils/auth";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Provider Vehicles | SUMMS" },
    {
      name: "description",
      content: "Mobility provider vehicle management (add/update/remove).",
    },
  ];
}

interface LocationDto {
  latitude: number;
  longitude: number;
}

interface VehicleResponse {
  id: number;
  type: string;
  status: string;
  location: LocationDto;
  locationAddress: string | null;
  locationCity: string | null;
  providerId: number;
  costPerMinute: number;
  maxRange?: number;
  licensePlate?: string;
  seatingCapacity?: number;
}

export default function ProviderVehiclesPage() {
  const [vehicles, setVehicles] = useState<VehicleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [selectedVehicleType, setSelectedVehicleType] = useState<"bicycle" | "scooter" | "car">(
    "bicycle"
  );
  const authUser = getAuthUser();
  const providerId = authUser?.id;

  // Fetch vehicles on mount
  useEffect(() => {
    fetchVehicles();
  }, []);

  const fetchVehicles = async () => {
    if (!providerId) return;
    setLoading(true);
    setError(null);
    try {
      const response = await apiFetch(`/api/vehicles/provider/${providerId}`);
      if (!response.ok) {
        throw new Error("Failed to fetch vehicles");
      }
      const data = await response.json();
      setVehicles(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteVehicle = async (vehicleId: number) => {
    if (!confirm("Are you sure you want to delete this vehicle?")) {
      return;
    }

    try {
      const response = await apiFetch(`/api/vehicles/${vehicleId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error("Failed to delete vehicle");
      }

      setVehicles(vehicles.filter((v) => v.id !== vehicleId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete vehicle");
    }
  };

  return (
    <>
      <SiteNav />
      <main className="ml-56 p-4 bg-gray-900 min-h-screen">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-white">Vehicle Management</h1>
            <p className="text-gray-400 mt-1">
              Manage your fleet, add new vehicles, and track their status
            </p>
          </div>
          <button
            onClick={() => setShowAddForm(!showAddForm)}
            className="px-4 py-2 bg-cyan-500 text-gray-900 rounded-lg hover:bg-cyan-400 font-medium"
          >
            {showAddForm ? "Cancel" : "+ Add Vehicle"}
          </button>
        </div>

        {error && (
          <div className="mb-4 rounded-lg border border-red-500 bg-red-900 bg-opacity-20 px-4 py-3 text-red-300">
            {error}
          </div>
        )}

        {showAddForm && (
          <AddVehicleForm
            selectedType={selectedVehicleType}
            setSelectedType={setSelectedVehicleType}
            onVehicleAdded={() => {
              setShowAddForm(false);
              fetchVehicles();
            }}
            onError={setError}
            providerId={providerId!}
          />
        )}

        {loading ? (
          <div className="text-center py-12">
            <p className="text-gray-400">Loading vehicles...</p>
          </div>
        ) : vehicles.length === 0 ? (
          <div className="text-center py-12 bg-gray-800 rounded-lg border border-gray-700">
            <p className="text-gray-400 mb-4">No vehicles found.</p>
            <button
              onClick={() => setShowAddForm(true)}
              className="px-4 py-2 bg-cyan-500 text-gray-900 rounded-lg hover:bg-cyan-400"
            >
              Add your first vehicle
            </button>
          </div>
        ) : (
          <div className="grid gap-4">
            {vehicles.map((vehicle, index) => (
              <VehicleCard
                key={vehicle.id}
                vehicle={vehicle}
                onDelete={handleDeleteVehicle}
                vehicleNumber={index + 1}
              />
            ))}
          </div>
        )}
      </main>
    </>
  );
}

interface AddVehicleFormProps {
  selectedType: "bicycle" | "scooter" | "car";
  setSelectedType: (type: "bicycle" | "scooter" | "car") => void;
  onVehicleAdded: () => void;
  onError: (error: string) => void;
  providerId: number;
}

function AddVehicleForm({
  selectedType,
  setSelectedType,
  onVehicleAdded,
  onError,
  providerId,
}: AddVehicleFormProps) {
  const [formData, setFormData] = useState({
    latitude: "",
    longitude: "",
    costPerMinute: "",
    maxRange: "",
    licensePlate: "",
    seatingCapacity: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    onError("");

    try {
      // Validate required fields
      if (!formData.latitude || !formData.longitude || !formData.costPerMinute) {
        throw new Error("Please fill in all required fields");
      }

      const costPerMinute = parseFloat(formData.costPerMinute);
      const latitude = parseFloat(formData.latitude);
      const longitude = parseFloat(formData.longitude);

      if (isNaN(costPerMinute) || isNaN(latitude) || isNaN(longitude)) {
        throw new Error("Invalid number format");
      }

      let endpoint = "";
      let body: Record<string, unknown> = {
        location: {
          latitude,
          longitude,
        },
        providerId,
        costPerMinute,
      };

      if (selectedType === "scooter") {
        if (!formData.maxRange) {
          throw new Error("Max range is required for scooters");
        }
        const maxRange = parseFloat(formData.maxRange);
        if (isNaN(maxRange)) {
          throw new Error("Invalid max range");
        }
        endpoint = "/api/vehicles/scooters";
        body.maxRange = maxRange;
      } else if (selectedType === "car") {
        if (!formData.licensePlate || !formData.seatingCapacity) {
          throw new Error("License plate and seating capacity are required for cars");
        }
        const seatingCapacity = parseInt(formData.seatingCapacity, 10);
        if (isNaN(seatingCapacity)) {
          throw new Error("Invalid seating capacity");
        }
        endpoint = "/api/vehicles/cars";
        body.licensePlate = formData.licensePlate;
        body.seatingCapacity = seatingCapacity;
      } else {
        endpoint = "/api/vehicles/bicycles";
      }

      const response = await apiFetch(endpoint, {
        method: "POST",
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(extractErrorMessage(errorData, "Failed to create vehicle"));
      }

      setFormData({
        latitude: "",
        longitude: "",
        costPerMinute: "",
        maxRange: "",
        licensePlate: "",
        seatingCapacity: "",
      });
      onVehicleAdded();
    } catch (err) {
      onError(err instanceof Error ? err.message : "An error occurred");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mb-8 bg-gray-800 rounded-lg border border-gray-700 p-6 shadow-sm">
      <h2 className="text-xl font-semibold mb-4 text-white">Add New Vehicle</h2>

      <div className="mb-6 flex gap-4">
        <label className="flex items-center">
          <input
            type="radio"
            value="bicycle"
            checked={selectedType === "bicycle"}
            onChange={(e) => setSelectedType(e.target.value as "bicycle")}
            className="mr-2"
          />
          <span className="text-sm font-medium text-gray-300">Bicycle</span>
        </label>
        <label className="flex items-center">
          <input
            type="radio"
            value="scooter"
            checked={selectedType === "scooter"}
            onChange={(e) => setSelectedType(e.target.value as "scooter")}
            className="mr-2"
          />
          <span className="text-sm font-medium text-gray-300">Scooter</span>
        </label>
        <label className="flex items-center">
          <input
            type="radio"
            value="car"
            checked={selectedType === "car"}
            onChange={(e) => setSelectedType(e.target.value as "car")}
            className="mr-2"
          />
          <span className="text-sm font-medium text-gray-300">Car</span>
        </label>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Latitude *
            </label>
            <input
              type="number"
              step="0.000001"
              placeholder="e.g. 45.501700"
              value={formData.latitude}
              onChange={(e) => setFormData({ ...formData, latitude: e.target.value })}
              className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Longitude *
            </label>
            <input
              type="number"
              step="0.000001"
              placeholder="e.g. -73.567300"
              value={formData.longitude}
              onChange={(e) => setFormData({ ...formData, longitude: e.target.value })}
              className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-300 mb-1">
            Cost per Minute (CAD) *
          </label>
          <input
            type="number"
            step="0.01"
            placeholder="0.50"
            value={formData.costPerMinute}
            onChange={(e) => setFormData({ ...formData, costPerMinute: e.target.value })}
            className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            required
          />
        </div>

        {selectedType === "scooter" && (
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              Max Range (km) *
            </label>
            <input
              type="number"
              step="0.1"
              placeholder="25.5"
              value={formData.maxRange}
              onChange={(e) => setFormData({ ...formData, maxRange: e.target.value })}
              className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              required
            />
          </div>
        )}

        {selectedType === "car" && (
          <>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">
                License Plate *
              </label>
              <input
                type="text"
                placeholder="ABC123"
                value={formData.licensePlate}
                onChange={(e) => setFormData({ ...formData, licensePlate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">
                Seating Capacity *
              </label>
              <input
                type="number"
                min="1"
                placeholder="4"
                value={formData.seatingCapacity}
                onChange={(e) => setFormData({ ...formData, seatingCapacity: e.target.value })}
                className="w-full px-3 py-2 border border-gray-600 rounded-lg bg-gray-700 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cyan-500"
                required
              />
            </div>
          </>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="w-full px-4 py-2 bg-cyan-500 text-gray-900 rounded-lg hover:bg-cyan-400 disabled:bg-gray-600 font-medium"
        >
          {submitting ? "Adding..." : "Add Vehicle"}
        </button>
      </form>
    </div>
  );
}

interface VehicleCardProps {
  vehicle: VehicleResponse;
  onDelete: (id: number) => void;
  vehicleNumber: number;
}

function VehicleCard({ vehicle, onDelete, vehicleNumber }: VehicleCardProps) {
  const getVehicleIcon = (type: string) => {
    switch (type.toUpperCase()) {
      case "BICYCLE":
        return "🚲";
      case "SCOOTER":
        return "🛴";
      case "CAR":
        return "🚗";
      default:
        return "🚗";
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case "AVAILABLE":
        return "bg-green-100 text-green-800 border-green-300";
      case "IN_USE":
        return "bg-blue-100 text-blue-800 border-blue-300";
      case "RESERVED":
        return "bg-yellow-100 text-yellow-800 border-yellow-300";
      case "UNAVAILABLE":
        return "bg-gray-100 text-gray-800 border-gray-300";
      default:
        return "bg-gray-100 text-gray-800 border-gray-300";
    }
  };

  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-4">
          <div className="text-3xl">{getVehicleIcon(vehicle.type)}</div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="font-semibold text-white capitalize">{vehicle.type} #{vehicleNumber}</h3>
              <span
                className={`px-2 py-1 text-xs font-medium rounded-full border ${getStatusColor(
                  vehicle.status
                )}`}
              >
                {vehicle.status}
              </span>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3 text-sm text-gray-400">
              <div>
                <p className="text-gray-500 text-xs">Location</p>
                <p className="font-medium text-gray-300">
                  {formatVehicleLocation(vehicle)}
                </p>
              </div>
              <div>
                <p className="text-gray-500 text-xs">Cost/Min</p>
                <p className="font-medium text-gray-300">${vehicle.costPerMinute.toFixed(2)} CAD</p>
              </div>
              {vehicle.type.toUpperCase() === "SCOOTER" && (
                <div>
                  <p className="text-gray-500 text-xs">Max Range</p>
                  <p className="font-medium text-gray-300">{vehicle.maxRange} km</p>
                </div>
              )}
              {vehicle.type.toUpperCase() === "CAR" && (
                <>
                  <div>
                    <p className="text-gray-500 text-xs">License Plate</p>
                    <p className="font-medium text-gray-300">{vehicle.licensePlate}</p>
                  </div>
                  <div>
                    <p className="text-gray-500 text-xs">Seating</p>
                    <p className="font-medium text-gray-300">{vehicle.seatingCapacity} seats</p>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
        <button
          onClick={() => onDelete(vehicle.id)}
          className="px-3 py-2 text-red-400 hover:bg-red-900 hover:bg-opacity-30 rounded-lg text-sm font-medium"
        >
          Delete
        </button>
      </div>
    </div>
  );
}

function extractErrorMessage(errorData: unknown, fallbackMessage: string): string {
  if (!errorData || typeof errorData !== "object") {
    return fallbackMessage;
  }

  const maybeMessage = (errorData as { message?: unknown }).message;
  if (typeof maybeMessage === "string" && maybeMessage.trim().length > 0) {
    return maybeMessage;
  }

  const maybeError = (errorData as { error?: unknown }).error;
  if (typeof maybeError === "string" && maybeError.trim().length > 0) {
    return maybeError;
  }

  return fallbackMessage;
}

function formatVehicleLocation(vehicle: VehicleResponse): string {
  const normalizedAddress = vehicle.locationAddress?.trim();
  if (normalizedAddress) {
    return normalizedAddress;
  }

  const normalizedCity = vehicle.locationCity?.trim();
  if (normalizedCity) {
    return normalizedCity;
  }

  return `${vehicle.location.latitude.toFixed(4)}, ${vehicle.location.longitude.toFixed(4)}`;
}
