import { useState } from "react";
import { useLocation, useNavigate } from "react-router";
import type { Route } from "./+types/parking-confirmation";
import {
  createParkingReservation,
  type ParkingFacility,
  type ParkingReservationResponse,
} from "../utils/api";

export function meta({}: Route.MetaArgs) {
  return [{ title: "Confirm Parking | SUMMS" }];
}

type PaymentMethod = "CREDIT" | "DEBIT" | "CREDITS";

interface LocationState {
  facility: ParkingFacility;
  durationHours: number;
}

function SummaryRow({
  label,
  value,
  accent = false,
}: {
  label: string;
  value: string;
  accent?: boolean;
}) {
  return (
    <div className="flex justify-between items-center py-1.5">
      <span className="text-gray-400 text-sm">{label}</span>
      <span
        className={
          accent
            ? "text-cyan-400 font-bold text-base"
            : "text-white text-sm font-medium"
        }
      >
        {value}
      </span>
    </div>
  );
}

function PaymentMethodBtn({
  label,
  selected,
  onSelect,
}: {
  method: PaymentMethod;
  label: string;
  selected: boolean;
  onSelect: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onSelect}
      className={[
        "flex-1 py-2.5 rounded-lg border text-sm font-semibold transition-colors",
        selected
          ? "bg-cyan-500/15 border-cyan-500 text-cyan-400"
          : "bg-gray-900 border-gray-700 text-gray-400 hover:border-gray-500",
      ].join(" ")}
    >
      {label}
    </button>
  );
}

function SuccessScreen({
  reservation,
  onBack,
  onViewReservations,
}: {
  reservation: ParkingReservationResponse;
  onBack: () => void;
  onViewReservations: () => void;
}) {
  return (
    <div className="flex flex-col items-center justify-center min-h-full py-16 text-center px-6">
      <div
        className="w-16 h-16 rounded-full bg-cyan-500/15 border-2 border-cyan-500
                    flex items-center justify-center text-3xl mb-5 animate-bounce"
      >
        ✓
      </div>
      <h2 className="text-white text-2xl font-bold mb-2">Booking Confirmed!</h2>
      <p className="text-gray-400 text-sm mb-6">
        Your spot at{" "}
        <span className="text-white font-medium">{reservation.facilityName}</span>{" "}
        has been reserved.
      </p>

      <div
        className="w-full max-w-sm bg-gray-800 border border-gray-700
                    rounded-xl p-5 text-left mb-6"
      >
        <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-3">
          Booking Summary
        </p>
        <SummaryRow label="Reservation #" value={`#${reservation.reservationId}`} />
        <SummaryRow label="Location" value={reservation.facilityAddress} />
        <SummaryRow
          label="Arrival"
          value={`${reservation.arrivalDate} at ${reservation.arrivalTime}`}
        />
        <SummaryRow label="Duration" value={`${reservation.durationHours}h`} />
        <div className="border-t border-gray-700 mt-2 pt-2">
          <SummaryRow
            label="Total Paid"
            value={`$${reservation.totalCost?.toFixed(2)}`}
            accent
          />
        </div>
        <p className="text-gray-500 text-xs mt-3">
          Confirmed at {reservation.confirmedAt}
        </p>
      </div>

      <div className="flex flex-wrap items-center justify-center gap-3">
        <button
          onClick={onViewReservations}
          className="bg-cyan-500 hover:bg-cyan-400 text-black font-bold
                     px-8 py-3 rounded-xl transition-colors"
        >
          View My Reservations
        </button>
        <button
          onClick={onBack}
          className="border border-gray-600 bg-transparent text-gray-200 font-bold
                     px-8 py-3 rounded-xl transition-colors hover:border-gray-400"
        >
          Back to Parking
        </button>
      </div>
    </div>
  );
}

export default function ParkingConfirmationPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const state = location.state as LocationState | null;

  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("CREDIT");
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirmed, setConfirmed] = useState<ParkingReservationResponse | null>(null);

  const formatCardNumber = (val: string) =>
    val
      .replace(/\D/g, "")
      .slice(0, 16)
      .replace(/(.{4})/g, "$1 ")
      .trim();

  const formatExpiry = (val: string) => {
    const clean = val.replace(/\D/g, "").slice(0, 4);
    return clean.length > 2 ? `${clean.slice(0, 2)}/${clean.slice(2)}` : clean;
  };

  const handleConfirm = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (paymentMethod !== "CREDITS") {
      if (cardNumber.replace(/\s/g, "").length < 16)
        return setError("Please enter a valid 16-digit card number.");
      if (expiry.length < 5)
        return setError("Please enter a valid expiry date (MM/YY).");
      if (cvv.length < 3)
        return setError("Please enter a valid CVV.");
    }

    if (!state?.facility) return;

    const { facility, durationHours } = state;
    const estimatedTotal = facility.pricePerHour * durationHours;

    setSubmitting(true);
    try {
      const response = await createParkingReservation({
        facilityId: facility.facilityId,
        facilityName: facility.name,
        facilityAddress: facility.address,
        city: facility.city,
        arrivalDate: new Date().toISOString().split("T")[0],
        arrivalTime: "14:00",
        durationHours,
        totalCost: estimatedTotal,
        paymentMethod,
      });
      setConfirmed(response);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Payment failed. Please try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  // Guard — no state means user navigated here directly
  if (!state?.facility) {
    return (
      <div className="flex flex-col items-center justify-center h-full py-20">
        <p className="text-gray-400 mb-4">No parking facility selected.</p>
        <button
          onClick={() => navigate("/services/parking")}
          className="text-cyan-400 underline text-sm"
        >
          Go back to Parking
        </button>
      </div>
    );
  }

  const { facility, durationHours } = state;
  const estimatedTotal = facility.pricePerHour * durationHours;

  // Success screen
  if (confirmed) {
    return (
      <div className="flex flex-col h-full bg-gray-900 overflow-y-auto">
        <SuccessScreen
          reservation={confirmed}
          onViewReservations={() => navigate("/my-reservations")}
          onBack={() => navigate("/services/parking")}
        />
      </div>
    );
  }

  // Confirmation + payment form
  return (
    <div className="flex flex-col h-full bg-gray-900 overflow-hidden ml-150">

      {/* Header */}
      <div className="px-7 py-5 border-b border-gray-800 flex items-center gap-3 shrink-0">
        <button
          onClick={() => navigate("/services/parking")}
          className="text-gray-400 hover:text-white transition-colors text-lg"
          aria-label="Back"
        >
          ←
        </button>
        <h1 className="text-xl font-bold text-white">Confirm Parking Reservation</h1>
      </div>

      {/* Body */}
      <div className="flex flex-1 overflow-hidden items-center justify-center p-6">

        {/* LEFT — Summary */}
        <aside className="w-80 shrink-0 p-6 border-r border-gray-800 overflow-y-auto">
          <div className="bg-gray-800 border border-gray-700 rounded-xl p-5">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-4">
              Reservation Summary
            </p>

            <div className="flex items-start gap-3 mb-4 pb-4 border-b border-gray-700">
              <div
                className="w-10 h-10 rounded-lg bg-cyan-500/15 border border-cyan-500/30
                            flex items-center justify-center text-xl shrink-0"
              >
                🅿️
              </div>
              <div>
                <p className="text-white font-bold text-sm leading-tight">{facility.name}</p>
                <p className="text-gray-400 text-xs mt-0.5">📍 {facility.address}</p>
              </div>
            </div>

            <div className="space-y-0.5">
              <SummaryRow label="Rate" value={`$${facility.pricePerHour?.toFixed(2)}/hr`} />
              <SummaryRow
                label="Duration"
                value={`${durationHours} hour${durationHours !== 1 ? "s" : ""}`}
              />
              <SummaryRow label="Spots Available" value={String(facility.availableSpots)} />
            </div>

            <div className="border-t border-gray-700 mt-3 pt-3">
              <SummaryRow
                label="Estimated Total"
                value={`$${estimatedTotal.toFixed(2)}`}
                accent
              />
            </div>

            {facility.amenityTags?.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mt-4 pt-4 border-t border-gray-700">
                {facility.amenityTags.map((tag) => (
                  <span
                    key={tag}
                    className="text-xs px-2 py-0.5 rounded-full border border-gray-600 text-gray-400"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="bg-gray-800/50 border border-gray-700 rounded-xl p-4 mt-4">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-1">
              Cancellation Policy
            </p>
            <p className="text-gray-400 text-xs leading-relaxed">
              Cancel up to <span className="text-white">1 hour before</span> arrival for a
              full refund. Later cancellations may incur a fee.
            </p>
          </div>
        </aside>

        {/* RIGHT — Payment */}
        <main className="flex-1 overflow-y-auto p-6">
          <form onSubmit={handleConfirm} className="max-w-md">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-3">
              Payment Method
            </p>
            <div className="flex gap-2 mb-6">
              {(
                [
                  ["CREDIT", "💳 Credit Card"],
                  ["DEBIT", "🏦 Debit Card"],
                  ["CREDITS", "⭐ SUMMS Credits"],
                ] as [PaymentMethod, string][]
              ).map(([m, label]) => (
                <PaymentMethodBtn
                  key={m}
                  method={m}
                  label={label}
                  selected={paymentMethod === m}
                  onSelect={() => setPaymentMethod(m)}
                />
              ))}
            </div>

            {paymentMethod !== "CREDITS" && (
              <div className="space-y-4 mb-6">
                <div className="flex flex-col gap-1.5">
                  <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
                    Card Number
                  </label>
                  <input
                    type="text"
                    inputMode="numeric"
                    placeholder="•••• •••• •••• ••••"
                    value={cardNumber}
                    onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
                    className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2.5
                               text-white text-sm placeholder-gray-600 tracking-widest
                               focus:outline-none focus:border-cyan-500 transition-colors"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col gap-1.5">
                    <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
                      Expiry
                    </label>
                    <input
                      type="text"
                      placeholder="MM/YY"
                      value={expiry}
                      onChange={(e) => setExpiry(formatExpiry(e.target.value))}
                      className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2.5
                                 text-white text-sm placeholder-gray-600
                                 focus:outline-none focus:border-cyan-500 transition-colors"
                    />
                  </div>
                  <div className="flex flex-col gap-1.5">
                    <label className="text-gray-400 text-xs font-semibold uppercase tracking-wide">
                      CVV
                    </label>
                    <input
                      type="text"
                      inputMode="numeric"
                      placeholder="•••"
                      maxLength={4}
                      value={cvv}
                      onChange={(e) => setCvv(e.target.value.replace(/\D/g, ""))}
                      className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2.5
                                 text-white text-sm placeholder-gray-600
                                 focus:outline-none focus:border-cyan-500 transition-colors"
                    />
                  </div>
                </div>
              </div>
            )}

            {paymentMethod === "CREDITS" && (
              <div className="bg-cyan-500/10 border border-cyan-500/30 rounded-xl p-4 mb-6">
                <p className="text-cyan-400 text-sm">
                  ⭐ SUMMS Credits will be deducted from your balance on confirmation.
                </p>
              </div>
            )}

            <div className="flex items-center gap-2 mb-6 text-gray-500 text-xs">
              <span>🔒</span>
              <span>Payment secured · Card details are not stored</span>
            </div>

            {error && (
              <div
                className="flex items-center gap-2 bg-red-500/10 border border-red-500/30
                            rounded-xl p-3 mb-4 text-red-400 text-sm"
              >
                <span>⚠️</span> {error}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-cyan-500 hover:bg-cyan-400 disabled:opacity-50
                         disabled:cursor-not-allowed text-black font-bold py-3
                         rounded-xl text-base transition-colors"
            >
              {submitting
                ? "Processing…"
                : `Pay $${estimatedTotal.toFixed(2)} & Confirm Booking`}
            </button>

            <button
              type="button"
              onClick={() => navigate("/services/parking")}
              className="w-full mt-3 text-gray-500 hover:text-gray-300 text-sm
                         transition-colors py-2"
            >
              Cancel — go back to Parking
            </button>
          </form>
        </main>
      </div>
    </div>
  );
}

