import { useEffect, useMemo, useState } from "react";
import { useNavigate, useLocation } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { getAuthUser } from "../utils/auth";
import {
  mapVehiclesToCatalog,
  type VehicleApiResponse,
  type VehicleCatalogItem,
} from "../utils/vehicle-catalog";
import type { Route } from "./+types/reservation";

interface ReservationState {
  selectedVehicleId?: number;
}

interface AddressSuggestion {
  address: string;
  city: string;
  latitude: number | null;
  longitude: number | null;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservation | SUMMS" },
    { name: "description", content: "Create and manage vehicle reservations." },
  ];
}

interface TripStartResponse {
  tripId: number;
  reservationId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
  endTime: string | null;
  totalDurationMinutes: number | null;
  vehicleStatus: string;
}

interface VehicleOption {
  id: number;
  type: string;
  status: string;
  providerId: number;
  costPerMinute: number;
  location: {
    latitude: number;
    longitude: number;
  };
}

interface ReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
  endDate: string;
  startLocation: {
    latitude: number;
    longitude: number;
  };
  endLocation: {
    latitude: number;
    longitude: number;
  };
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString();
}

export default function ReservationPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const authUser = useMemo(() => getAuthUser(), []);
  const selectedState = (location.state as ReservationState | null) ?? {};
  const [availableVehicles, setAvailableVehicles] = useState<VehicleCatalogItem[]>([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(
    selectedState.selectedVehicleId ?? null,
  );

  const [city, setCity] = useState("");
  const [startDate, setStartDate] = useState(toDateTimeInputValue(addHours(new Date(), 1)));
  const [endDate, setEndDate] = useState(toDateTimeInputValue(addHours(new Date(), 2)));
  const [startAddress, setStartAddress] = useState("");
  const [endAddress, setEndAddress] = useState("");
  const [citySuggestions, setCitySuggestions] = useState<string[]>([]);
  const [endAddressSuggestions, setEndAddressSuggestions] = useState<AddressSuggestion[]>([]);

  const [isLoadingVehicles, setIsLoadingVehicles] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [vehicleError, setVehicleError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);

  const selectedVehicle = useMemo(() => {
    if (availableVehicles.length === 0) {
      return null;
    }

    if (selectedVehicleId === null) {
      return availableVehicles[0];
    }

    return availableVehicles.find((vehicle) => vehicle.id === selectedVehicleId) ?? null;
  }, [availableVehicles, selectedVehicleId]);

  useEffect(() => {
    if (!selectedVehicle) {
      setStartAddress("");
      return;
    }

    if (city.trim().length === 0 && selectedVehicle.locationCity) {
      setCity(selectedVehicle.locationCity);
    }

    setStartAddress(
      selectedVehicle.station !== "Location unavailable"
        ? selectedVehicle.station
        : "Vehicle location unavailable",
    );
  }, [city, selectedVehicle]);

  useEffect(() => {
    let isMounted = true;

    async function loadAvailableVehicles() {
      try {
        const response = await apiFetch("/api/vehicles/status/AVAILABLE");
        if (!response.ok) {
          throw new Error(`Failed to fetch vehicles (${response.status})`);
        }

        const data = (await response.json()) as VehicleApiResponse[];
        if (!isMounted) {
          return;
        }

        const mappedVehicles = mapVehiclesToCatalog(data).filter((vehicle) => vehicle.available);
        if (mappedVehicles.length === 0) {
          setAvailableVehicles([]);
          setVehicleError("No live available vehicles found.");
          return;
        }

        setAvailableVehicles(mappedVehicles);
        setVehicleError(null);
      } catch {
        if (isMounted) {
          setAvailableVehicles([]);
          setVehicleError("Unable to load live vehicles.");
        }
      } finally {
        if (isMounted) {
          setIsLoadingVehicles(false);
        }
      }
    }

    void loadAvailableVehicles();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    const query = city.trim();
    if (query.length < 2) {
      setCitySuggestions([]);
      return;
    }

    let isCancelled = false;
    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await apiFetch(
          `/api/locations/cities?query=${encodeURIComponent(query)}&limit=6`,
        );
        if (!response.ok) {
          throw new Error(`Failed to fetch city suggestions (${response.status})`);
        }

        const suggestions = (await response.json()) as string[];
        if (!isCancelled) {
          setCitySuggestions(suggestions);
        }
      } catch {
        if (!isCancelled) {
          setCitySuggestions([]);
        }
      }
    }, 250);

    return () => {
      isCancelled = true;
      window.clearTimeout(timeoutId);
    };
  }, [city]);

  useEffect(() => {
    const query = endAddress.trim();
    if (query.length < 3) {
      setEndAddressSuggestions([]);
      return;
    }

    const cityQuery = city.trim();
    let isCancelled = false;
    const timeoutId = window.setTimeout(async () => {
      try {
        const cityPart = cityQuery.length > 0
          ? `&city=${encodeURIComponent(cityQuery)}`
          : "";
        const response = await apiFetch(
          `/api/locations/suggestions?query=${encodeURIComponent(query)}${cityPart}&limit=6`,
        );
        if (!response.ok) {
          throw new Error(`Failed to fetch address suggestions (${response.status})`);
        }

        const suggestions = (await response.json()) as AddressSuggestion[];
        if (!isCancelled) {
          setEndAddressSuggestions(suggestions);
        }
      } catch {
        if (!isCancelled) {
          setEndAddressSuggestions([]);
        }
      }
    }, 250);

    return () => {
      isCancelled = true;
      window.clearTimeout(timeoutId);
    };
  }, [city, endAddress]);

  useEffect(() => {
    if (city.trim().length > 0) {
      return;
    }

    const matchingSuggestion = findExactAddressSuggestion(
      endAddress,
      endAddressSuggestions,
    );
    if (matchingSuggestion && matchingSuggestion.city.trim().length > 0) {
      setCity(matchingSuggestion.city);
    }
  }, [city, endAddress, endAddressSuggestions]);

  useEffect(() => {
    if (availableVehicles.length === 0) {
      setSelectedVehicleId(null);
      return;
    }

    setSelectedVehicleId((previousId) => {
      if (previousId !== null && availableVehicles.some((vehicle) => vehicle.id === previousId)) {
        return previousId;
      }

      if (
        typeof selectedState.selectedVehicleId === "number" &&
        availableVehicles.some((vehicle) => vehicle.id === selectedState.selectedVehicleId)
      ) {
        return selectedState.selectedVehicleId;
      }

      return availableVehicles[0].id;
    });
  }, [availableVehicles, selectedState.selectedVehicleId]);

  const selectedVehicleName = selectedVehicle?.name ?? "No vehicle selected";
  const selectedVehicleProvider = selectedVehicle?.provider ?? "Unknown Provider";
  const selectedVehicleCondition = selectedVehicle?.condition ?? "Unknown";

  const selectedVehiclePricePerMinute = selectedVehicle?.pricePerMinute ?? 0;
  const selectedVehiclePrice = `$${selectedVehiclePricePerMinute.toFixed(2)}`;

  const durationMinutes = useMemo(() => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const millis = end.getTime() - start.getTime();
    if (!Number.isFinite(millis) || millis <= 0) {
      return 0;
    }
    return millis / (1000 * 60);
  }, [startDate, endDate]);

  const estimatedTotal = selectedVehiclePricePerMinute * durationMinutes;

  const routeStops = [
    {
      label: "From",
      location: startAddress.trim().length > 0 ? startAddress : "--",
      dateTime: startDate.trim().length > 0 ? formatDateForDisplay(startDate) : "--",
    },
    {
      label: "To",
      location: endAddress.trim().length > 0 ? endAddress : "--",
      dateTime: endDate.trim().length > 0 ? formatDateForDisplay(endDate) : "--",
    },
  ];

  const handleCreateReservation = async (
    event: React.FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();
    setSubmitError(null);
    setSubmitSuccess(null);

    if (selectedVehicleId === null) {
      setSubmitError("Please select a vehicle first.");
      return;
    }

    if (startDate.trim().length === 0 || endDate.trim().length === 0) {
      setSubmitError("Start and end date/time are required.");
      return;
    }

    if (city.trim().length === 0) {
      setSubmitError("City is required.");
      return;
    }

    if (endAddress.trim().length === 0) {
      setSubmitError("End address is required.");
      return;
    }

    const normalizedStart = normalizeDateTimeValue(startDate);
    const normalizedEnd = normalizeDateTimeValue(endDate);

    if (new Date(normalizedStart).getTime() >= new Date(normalizedEnd).getTime()) {
      setSubmitError("Start date/time must be before end date/time.");
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await apiFetch(`/api/vehicles/${selectedVehicleId}/reservations`, {
        method: "POST",
        body: JSON.stringify({
          endAddress: endAddress.trim(),
          city: city.trim(),
          startDate: normalizedStart,
          endDate: normalizedEnd,
        }),
      });

      if (!response.ok) {
        const message = await readErrorMessage(
          response,
          "Could not create reservation.",
        );
        setSubmitError(message);
        return;
      }

      const createdReservation = (await response.json()) as { reservationId: number };
      setSubmitSuccess(
        `Reservation #${createdReservation.reservationId} created. Go to "My Reservations" to start your trip.`,
      );
    } catch {
      setSubmitError("Network error while creating reservation.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Reservation</h1>
        </header>
        {vehicleError && (
          <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2 text-sm text-amber-200">
            {vehicleError}
          </div>
        )}
        {submitError && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {submitError}
          </div>
        )}
        {submitSuccess && (
          <div className="mb-3 rounded-xl border border-green-500/70 bg-green-500/20 px-4 py-2 text-sm text-green-200">
            {submitSuccess}
          </div>
        )}

        <section className="grid gap-5 xl:grid-cols-[1.05fr_1fr]">
          <div className="space-y-5">
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <h2 className="border-b border-[#2a354a] px-5 py-3 text-xl font-semibold">Reservation Summary</h2>

              <div className="space-y-3 p-5">
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                  <p className="text-xl font-bold leading-tight">Selected: {selectedVehicleName}</p>
                  <p className="text-base text-gray-300">
                    {selectedVehicleProvider} - {selectedVehicleCondition} condition
                  </p>
                  <p className="text-right text-3xl font-bold leading-none text-cyan-400">{selectedVehiclePrice}/min</p>
                </div>

                {routeStops.map((stop) => (
                  <div key={stop.label} className="rounded-xl bg-[#1a2a45] px-4 py-3.5">
                    <p className="text-xs uppercase tracking-[0.2em] text-gray-400">{stop.label}</p>
                    <p className="text-xl font-medium leading-tight">{stop.location}</p>
                    <p className="text-base text-gray-300">{stop.dateTime}</p>
                  </div>
                ))}

                <div className="border-y border-[#2d3d57] py-3.5 text-lg">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-200">Duration</span>
                    <span className="font-semibold">{Math.round(durationMinutes)} min</span>
                  </div>
                </div>

                <div className="flex items-end justify-between text-2xl">
                  <p className="font-semibold">Estimated Total</p>
                  <p className="font-bold text-cyan-400">${estimatedTotal.toFixed(2)}</p>
                </div>
              </div>
            </article>

          </div>

          <div className="space-y-5">
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] px-5 py-4">
              <h2 className="mb-4 text-2xl font-semibold">Create Reservation</h2>

              <form className="space-y-4" onSubmit={(event) => void handleCreateReservation(event)}>
                <div>
                  <label htmlFor="vehicle" className="mb-1.5 block text-sm uppercase text-gray-300">
                    Vehicle
                  </label>
                  <select
                    id="vehicle"
                    value={selectedVehicleId ?? ""}
                    onChange={(event) => {
                      const parsedVehicleId = Number(event.target.value);
                      setSelectedVehicleId(Number.isFinite(parsedVehicleId) ? parsedVehicleId : null);
                    }}
                    className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    disabled={isLoadingVehicles}
                  >
                    {availableVehicles.length === 0 ? (
                      <option value="">No vehicles available</option>
                    ) : (
                      availableVehicles.map((vehicle) => (
                        <option key={vehicle.id} value={vehicle.id}>
                          {vehicle.name} ({vehicle.priceLabel})
                        </option>
                      ))
                    )}
                  </select>
                </div>

                <div>
                  <label htmlFor="city" className="mb-1.5 block text-sm uppercase text-gray-300">
                    City
                  </label>
                  <input
                    id="city"
                    type="text"
                    list="city-suggestion-list"
                    value={city}
                    onChange={(event) => setCity(event.target.value)}
                    className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none placeholder:text-gray-500"
                  />
                  <datalist id="city-suggestion-list">
                    {citySuggestions.map((suggestedCity) => (
                      <option key={suggestedCity} value={suggestedCity} />
                    ))}
                  </datalist>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label htmlFor="start-date" className="mb-1.5 block text-sm uppercase text-gray-300">
                      Start Date/Time
                    </label>
                    <input
                      id="start-date"
                      type="datetime-local"
                      value={startDate}
                      onChange={(event) => setStartDate(event.target.value)}
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    />
                  </div>

                  <div>
                    <label htmlFor="end-date" className="mb-1.5 block text-sm uppercase text-gray-300">
                      End Date/Time
                    </label>
                    <input
                      id="end-date"
                      type="datetime-local"
                      value={endDate}
                      onChange={(event) => setEndDate(event.target.value)}
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    />
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label htmlFor="start-address" className="mb-1.5 block text-sm uppercase text-gray-300">
                      Start Address
                    </label>
                    <input
                      id="start-address"
                      type="text"
                      value={startAddress}
                      readOnly
                      className="w-full cursor-not-allowed rounded-xl border border-[#50617c] bg-[#0f1c33] px-3 py-2.5 text-base text-gray-200 outline-none"
                    />
                    <p className="mt-1 text-xs text-gray-400">
                      Fixed to selected vehicle location.
                    </p>
                  </div>

                  <div>
                    <label htmlFor="end-address" className="mb-1.5 block text-sm uppercase text-gray-300">
                      End Address
                    </label>
                    <input
                      id="end-address"
                      type="text"
                      list="end-address-suggestion-list"
                      value={endAddress}
                      onChange={(event) => setEndAddress(event.target.value)}
                      placeholder="e.g. 800 Rue du Square-Victoria"
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    />
                    <datalist id="end-address-suggestion-list">
                      {endAddressSuggestions.map((suggestion) => (
                        <option
                          key={`${suggestion.address}-${suggestion.latitude}-${suggestion.longitude}`}
                          value={suggestion.address}
                          label={suggestion.city}
                        />
                      ))}
                    </datalist>
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting || selectedVehicleId === null}
                  className="w-full rounded-xl bg-cyan-400 px-5 py-2.5 text-xl font-semibold text-slate-950 transition hover:bg-cyan-300"
                >
                  {isSubmitting ? "Creating Reservation..." : `Reserve for ${selectedVehiclePrice}/min`}
                </button>
              </form>
            </article>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b]">
              <h3 className="border-b border-[#2a354a] px-5 py-3 text-2xl font-semibold">Cancellation Policy</h3>
              <p className="px-5 py-3 text-base leading-snug text-gray-200">
                Cancel up to <span className="font-semibold">1 hour before</span> start time for a full refund.
                Cancellations within 1 hour may incur a $2.00 fee. No-shows are charged the full amount.
              </p>
            </article>
          </div>
        </section>
      </main>
    </>
  );
}

function findExactAddressSuggestion(
  value: string,
  suggestions: AddressSuggestion[],
): AddressSuggestion | null {
  const normalizedValue = value.trim().toLowerCase();
  if (normalizedValue.length === 0) {
    return null;
  }

  return (
    suggestions.find(
      (suggestion) => suggestion.address.trim().toLowerCase() === normalizedValue,
    ) ?? null
  );
}

function addHours(date: Date, hours: number): Date {
  return new Date(date.getTime() + hours * 60 * 60 * 1000);
}

function toDateTimeInputValue(date: Date): string {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function normalizeDateTimeValue(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

function formatDateForDisplay(value: string): string {
  const parsedDate = new Date(value);
  if (!Number.isFinite(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString();
}

async function readErrorMessage(
  response: Response,
  fallbackMessage: string,
): Promise<string> {
  try {
    const data = (await response.json()) as {
      message?: string;
      error?: string;
    };

    if (typeof data.message === "string" && data.message.trim().length > 0) {
      return data.message;
    }
    if (typeof data.error === "string" && data.error.trim().length > 0) {
      return data.error;
    }
  } catch {
    // Ignore parse errors and use fallback.
  }

  return `${fallbackMessage} (${response.status})`;
}
