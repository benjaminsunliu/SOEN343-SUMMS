import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { setActiveTrip } from "../utils/trips";
import type { Route } from "./+types/payment";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Payment | SUMMS" },
    {
      name: "description",
      content: "Checkout and simulated payment processing for rentals.",
    },
  ];
}

interface ReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
  endDate: string;
}

interface VehicleResponse {
  id: number;
  costPerMinute: number | null;
}

interface PaymentResponse {
  status: string;
  message: string;
  paymentToken: string | null;
}

interface StartTripResponse {
  tripId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
}

export default function PaymentPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const reservationId = useMemo(() => {
    const value = searchParams.get("reservationId");
    if (!value) {
      return null;
    }

    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  }, [searchParams]);

  const [reservation, setReservation] = useState<ReservationResponse | null>(null);
  const [vehicleCostPerMinute, setVehicleCostPerMinute] = useState<number>(0.5);

  const [cardHolderName, setCardHolderName] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");

  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const estimatedAmount = useMemo(() => {
    if (!reservation) {
      return 0;
    }

    const start = new Date(reservation.startDate);
    const end = new Date(reservation.endDate);
    const durationMinutes = Math.max(0, (end.getTime() - start.getTime()) / (1000 * 60));

    if (!Number.isFinite(durationMinutes)) {
      return 0;
    }

    return Number((durationMinutes * vehicleCostPerMinute).toFixed(2));
  }, [reservation, vehicleCostPerMinute]);

  useEffect(() => {
    async function loadCheckoutContext() {
      if (!reservationId) {
        setError("Missing reservation ID. Start from My Reservations.");
        setIsLoading(false);
        return;
      }

      try {
        const reservationResponse = await apiFetch(`/api/reservations/${reservationId}`);
        if (!reservationResponse.ok) {
          const reservationError = await readErrorMessage(
            reservationResponse,
            "Unable to load reservation details.",
          );
          throw new Error(reservationError);
        }

        const reservationData = (await reservationResponse.json()) as ReservationResponse;
        setReservation(reservationData);

        const vehicleResponse = await apiFetch(`/api/vehicles/${reservationData.vehicleId}`);
        if (vehicleResponse.ok) {
          const vehicleData = (await vehicleResponse.json()) as VehicleResponse;
          if (typeof vehicleData.costPerMinute === "number") {
            setVehicleCostPerMinute(vehicleData.costPerMinute);
          }
        }
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : "Unable to prepare checkout.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadCheckoutContext();
  }, [reservationId]);

  async function handleCheckoutSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setMessage(null);

    if (!reservation || reservationId === null) {
      setError("Reservation details are missing.");
      return;
    }

    if (cardHolderName.trim().length === 0 || cardNumber.trim().length === 0) {
      setError("Card holder name and card number are required.");
      return;
    }

    if (expiry.trim().length === 0 || cvv.trim().length === 0) {
      setError("Expiry date and CVV are required.");
      return;
    }

    setIsSubmitting(true);
    try {
      const paymentResponse = await apiFetch("/api/payments/process", {
        method: "POST",
        body: JSON.stringify({
          cardHolderName: cardHolderName.trim(),
          cardNumber: extractDigits(cardNumber),
          amount: Math.max(estimatedAmount, 0.01),
        }),
      });

      const paymentPayload = (await paymentResponse.json()) as PaymentResponse;
      if (!paymentResponse.ok) {
        setError(paymentPayload.message || "Payment failed.");
        return;
      }

      if (!paymentPayload.paymentToken) {
        setError("Payment token missing from checkout response.");
        return;
      }

      const startTripResponse = await apiFetch("/api/trips/start", {
        method: "POST",
        body: JSON.stringify({
          reservationId,
          paymentAuthorizationCode: paymentPayload.paymentToken,
        }),
      });

      if (!startTripResponse.ok) {
        const tripStartError = await readErrorMessage(startTripResponse, "Payment succeeded but trip could not be started.");
        throw new Error(tripStartError);
      }

      const tripPayload = (await startTripResponse.json()) as StartTripResponse;
      setActiveTrip({
        tripId: tripPayload.tripId,
        vehicleId: tripPayload.vehicleId,
        citizenId: tripPayload.citizenId,
        startTime: tripPayload.startTime,
      });

      setMessage("Payment approved. Your trip is now active.");
      navigate("/trips/active");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Unable to complete checkout.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <SiteNav />
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Checkout</h1>
          <p className="text-sm text-gray-300">Secure simulated payment for your reservation.</p>
        </header>

        {isLoading && (
          <div className="rounded-xl border border-gray-700 bg-gray-950 p-5 text-center">
            <p className="text-gray-300">Preparing checkout...</p>
          </div>
        )}

        {!isLoading && error && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {error}
          </div>
        )}

        {!isLoading && message && (
          <div className="mb-3 rounded-xl border border-green-500/70 bg-green-500/20 px-4 py-2 text-sm text-green-200">
            {message}
          </div>
        )}

        {!isLoading && reservation && (
          <section className="grid gap-5 lg:grid-cols-[1fr_1.05fr]">
            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-4 text-xl font-semibold">Trip Payment Summary</h2>
              <div className="space-y-3 text-sm">
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3">
                  <p className="text-gray-300">Reservation</p>
                  <p className="text-lg font-semibold">#{reservation.reservationId}</p>
                </div>
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3">
                  <p className="text-gray-300">Vehicle</p>
                  <p className="text-lg font-semibold">#{reservation.vehicleId}</p>
                </div>
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3">
                  <p className="text-gray-300">City</p>
                  <p className="text-lg font-semibold">{reservation.city}</p>
                </div>
                <div className="rounded-xl bg-[#1a2a45] px-4 py-3">
                  <p className="text-gray-300">Estimated Charge</p>
                  <p className="text-2xl font-bold text-cyan-400">${estimatedAmount.toFixed(2)}</p>
                </div>
              </div>
              <p className="mt-4 text-xs text-gray-400">
                Use test card 4000 0000 0000 0002 to simulate a declined transaction.
              </p>
            </article>

            <article className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-4 text-xl font-semibold">Payment Details</h2>
              <form className="space-y-4" onSubmit={(event) => void handleCheckoutSubmit(event)}>
                <div>
                  <label htmlFor="card-holder" className="mb-1.5 block text-sm uppercase text-gray-300">
                    Card Holder Name
                  </label>
                  <input
                    id="card-holder"
                    type="text"
                    value={cardHolderName}
                    onChange={(event) => setCardHolderName(event.target.value)}
                    className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    placeholder="Alex Citizen"
                    autoComplete="cc-name"
                  />
                </div>

                <div>
                  <label htmlFor="card-number" className="mb-1.5 block text-sm uppercase text-gray-300">
                    Card Number (Simulated)
                  </label>
                  <input
                    id="card-number"
                    type="text"
                    value={cardNumber}
                    onChange={(event) => setCardNumber(formatCardNumberInput(event.target.value))}
                    className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                    placeholder="4242 4242 4242 4242"
                    autoComplete="cc-number"
                    inputMode="numeric"
                    maxLength={19}
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label htmlFor="expiry" className="mb-1.5 block text-sm uppercase text-gray-300">
                      Expiry
                    </label>
                    <input
                      id="expiry"
                      type="text"
                      value={expiry}
                      onChange={(event) => setExpiry(formatExpiryInput(event.target.value))}
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                      placeholder="MM/YY"
                      autoComplete="cc-exp"
                      inputMode="numeric"
                      maxLength={5}
                    />
                  </div>

                  <div>
                    <label htmlFor="cvv" className="mb-1.5 block text-sm uppercase text-gray-300">
                      CVV
                    </label>
                    <input
                      id="cvv"
                      type="password"
                      value={cvv}
                      onChange={(event) => setCvv(event.target.value)}
                      className="w-full rounded-xl border border-[#50617c] bg-[#13233d] px-3 py-2.5 text-base outline-none"
                      placeholder="123"
                      autoComplete="cc-csc"
                      inputMode="numeric"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full rounded-xl bg-cyan-400 px-5 py-2.5 text-lg font-semibold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmitting ? "Processing Payment..." : `Pay $${estimatedAmount.toFixed(2)} & Start Trip`}
                </button>

                <Link
                  to="/my-reservations"
                  className="block text-center text-sm text-gray-300 underline underline-offset-4 hover:text-white"
                >
                  Back to My Reservations
                </Link>
              </form>
            </article>
          </section>
        )}
      </main>
    </>
  );
}

async function readErrorMessage(response: Response, fallbackMessage: string): Promise<string> {
  try {
    const data = (await response.json()) as { message?: string; error?: string };
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

function extractDigits(value: string): string {
  return value.replace(/\D/g, "");
}

function formatCardNumberInput(value: string): string {
  const digits = extractDigits(value).slice(0, 16);
  const groups: string[] = [];

  for (let index = 0; index < digits.length; index += 4) {
    groups.push(digits.slice(index, index + 4));
  }

  return groups.join(" ");
}

function formatExpiryInput(value: string): string {
  const digits = extractDigits(value).slice(0, 4);
  if (digits.length <= 2) {
    return digits;
  }

  return `${digits.slice(0, 2)}/${digits.slice(2)}`;
}
