import { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router";
import type { Route } from "./+types/parking-confirmation";
import {
  createParkingReservation,
  type ParkingFacility,
  type ParkingReservationResponse,
} from "../utils/api";

const FIXED_SERVICE_FEE_AMOUNT = 2.5;
const FIXED_TAX_RATE = 0.15;

export function meta({}: Route.MetaArgs) {
  return [{ title: "Confirm Parking | SUMMS" }];
}

type PaymentMethod = "CREDIT_CARD" | "PAYPAL" | "WALLET";

interface LocationState {
  facility: ParkingFacility;
  durationHours: number;
  arrivalDate: string;
  arrivalTime: string;
  city: string;
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
    <div className="flex justify-between items-center py-1.5 gap-3">
      <span className="text-gray-400 text-sm">{label}</span>
      <span
        className={
          accent
            ? "text-cyan-400 font-bold text-base text-right"
            : "text-white text-sm font-medium text-right"
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
}: {
  reservation: ParkingReservationResponse;
  onBack: () => void;
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
            value={formatMoney(reservation.totalCost)}
            accent
          />
        </div>
        <p className="text-gray-500 text-xs mt-3">
          Confirmed at {reservation.confirmedAt}
        </p>
      </div>

      <button
        onClick={onBack}
        className="bg-cyan-500 hover:bg-cyan-400 text-black font-bold
                   px-8 py-3 rounded-xl transition-colors"
      >
        Back to Parking
      </button>
    </div>
  );
}

interface FeeRowProps {
  label: string;
  amount: number;
  onAmountChange?: (value: number) => void;
  step?: number;
  hint?: string;
  required?: boolean;
  checked?: boolean;
  onCheckedChange?: (value: boolean) => void;
  readOnly?: boolean;
  displayValue?: string;
}

function FeeRow({
  label,
  amount,
  onAmountChange,
  step,
  hint,
  required = false,
  checked = true,
  onCheckedChange,
  readOnly = false,
  displayValue,
}: FeeRowProps) {
  return (
    <div className="rounded-lg border border-gray-700 bg-gray-800/60 px-3 py-3">
      <div className="flex items-center justify-between gap-3">
        {required ? (
          <div className="text-sm text-gray-200">
            {label}
            <span className="ml-2 text-xs uppercase tracking-wide text-cyan-300">
              {readOnly ? "Fixed" : "Required"}
            </span>
          </div>
        ) : (
          <label className="flex items-center gap-2 text-sm text-gray-200">
            <input
              type="checkbox"
              checked={checked}
              onChange={(event) => onCheckedChange?.(event.target.checked)}
            />
            {label}
          </label>
        )}

        {readOnly ? (
          <span className="rounded-md border border-gray-700 bg-gray-900 px-3 py-1 text-sm font-medium text-cyan-200">
            {displayValue ?? formatMoney(amount)}
          </span>
        ) : (
          <input
            type="number"
            min={0}
            step={step}
            value={amount}
            onChange={(event) => onAmountChange?.(parseNonNegativeNumber(event.target.value))}
            disabled={!required && !checked}
            className="w-28 rounded-md border border-gray-700 bg-gray-900 px-2 py-1 text-sm text-white disabled:opacity-60"
          />
        )}
      </div>
      {hint && <p className="mt-1 text-xs text-gray-400">{hint}</p>}
    </div>
  );
}

export default function ParkingConfirmationPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const state = location.state as LocationState | null;

  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("CREDIT_CARD");
  const [creditCardNumber, setCreditCardNumber] = useState("");
  const [paypalEmail, setPaypalEmail] = useState("");
  const [paypalPassword, setPaypalPassword] = useState("");

  const serviceFeeAmount = FIXED_SERVICE_FEE_AMOUNT;
  const taxRate = FIXED_TAX_RATE;

  const [includeInsuranceFee, setIncludeInsuranceFee] = useState(false);
  const [insuranceFeeAmount, setInsuranceFeeAmount] = useState(3.0);

  const [includeDiscount, setIncludeDiscount] = useState(false);
  const [discountAmount, setDiscountAmount] = useState(1.0);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirmed, setConfirmed] = useState<ParkingReservationResponse | null>(null);

  const includeServiceFee = true;
  const includeTax = true;

  const baseAmount = useMemo(() => {
    if (!state?.facility) {
      return 0;
    }

    return state.facility.estimatedTotal ?? state.facility.pricePerHour * state.durationHours;
  }, [state]);

  const estimatedAmount = useMemo(() => {
    let amount = baseAmount;

    amount += serviceFeeAmount;
    amount *= 1 + taxRate;
    if (includeInsuranceFee) {
      amount += insuranceFeeAmount;
    }
    if (includeDiscount) {
      amount = Math.max(0, amount - discountAmount);
    }

    return amount;
  }, [
    baseAmount,
    serviceFeeAmount,
    taxRate,
    includeInsuranceFee,
    insuranceFeeAmount,
    includeDiscount,
    discountAmount,
  ]);

  const handleConfirm = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    if (!state?.facility) {
      setError("No parking facility selected.");
      return;
    }

    if (paymentMethod === "CREDIT_CARD" && creditCardNumber.trim().length === 0) {
      setError("Enter a credit card number.");
      return;
    }

    if (
      paymentMethod === "PAYPAL" &&
      (paypalEmail.trim().length === 0 || paypalPassword.trim().length === 0)
    ) {
      setError("Enter your PayPal email and password.");
      return;
    }

    setSubmitting(true);
    try {
      const response = await createParkingReservation({
        facilityId: state.facility.facilityId,
        facilityName: state.facility.name,
        facilityAddress: state.facility.address,
        city: state.city || state.facility.city,
        arrivalDate: state.arrivalDate,
        arrivalTime: state.arrivalTime,
        durationHours: state.durationHours,
        totalCost: baseAmount,
        paymentMethod,
        creditCardNumber: creditCardNumber.trim(),
        paypalEmail: paypalEmail.trim(),
        paypalPassword: paypalPassword.trim(),
        includeServiceFee,
        serviceFeeAmount,
        includeTax,
        taxRate,
        includeInsuranceFee,
        insuranceFeeAmount,
        includeDiscount,
        discountAmount,
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

  const { facility, durationHours, arrivalDate, arrivalTime } = state;

  if (confirmed) {
    return (
      <div className="flex flex-col h-full bg-gray-900 overflow-y-auto">
        <SuccessScreen
          reservation={confirmed}
          onBack={() => navigate("/services/parking")}
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-gray-900 overflow-hidden">
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

      <div className="flex flex-1 overflow-hidden">
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
                P
              </div>
              <div>
                <p className="text-white font-bold text-sm leading-tight">{facility.name}</p>
                <p className="text-gray-400 text-xs mt-0.5">{facility.address}</p>
              </div>
            </div>

            <div className="space-y-0.5">
              <SummaryRow label="Rate" value={`${formatMoney(facility.pricePerHour)}/hr`} />
              <SummaryRow
                label="Arrival"
                value={formatArrivalDateTime(arrivalDate, arrivalTime)}
              />
              <SummaryRow
                label="Duration"
                value={`${durationHours} hour${durationHours !== 1 ? "s" : ""}`}
              />
              <SummaryRow label="Spots Available" value={String(facility.availableSpots)} />
              <SummaryRow label="Base Parking Cost" value={formatMoney(baseAmount)} />
            </div>

            <div className="border-t border-gray-700 mt-3 pt-3">
              <SummaryRow
                label="Estimated Final"
                value={formatMoney(estimatedAmount)}
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

        <main className="flex-1 overflow-y-auto p-6">
          <form onSubmit={(event) => void handleConfirm(event)} className="max-w-md">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wide mb-3">
              Payment Method
            </p>
            <div className="flex gap-2 mb-6">
              {(
                [
                  ["CREDIT_CARD", "Credit Card"],
                  ["PAYPAL", "PayPal"],
                  ["WALLET", "Wallet"],
                ] as [PaymentMethod, string][]
              ).map(([method, label]) => (
                <PaymentMethodBtn
                  key={method}
                  label={label}
                  selected={paymentMethod === method}
                  onSelect={() => setPaymentMethod(method)}
                />
              ))}
            </div>

            {paymentMethod === "CREDIT_CARD" && (
              <div className="space-y-3 rounded-xl border border-gray-700 bg-gray-800/60 p-4 mb-6">
                <label className="block text-sm text-gray-200">
                  Credit Card Number
                  <input
                    type="text"
                    value={creditCardNumber}
                    onChange={(event) => setCreditCardNumber(event.target.value)}
                    placeholder="12345678"
                    className="mt-1 w-full rounded-md border border-gray-700 bg-gray-900 px-3 py-2 text-white"
                  />
                </label>
                <p className="text-xs text-gray-400">
                  Test card for success: 12345678
                </p>
              </div>
            )}

            {paymentMethod === "PAYPAL" && (
              <div className="space-y-3 rounded-xl border border-gray-700 bg-gray-800/60 p-4 mb-6">
                <label className="block text-sm text-gray-200">
                  PayPal Email
                  <input
                    type="email"
                    value={paypalEmail}
                    onChange={(event) => setPaypalEmail(event.target.value)}
                    placeholder="payment@test.com"
                    className="mt-1 w-full rounded-md border border-gray-700 bg-gray-900 px-3 py-2 text-white"
                  />
                </label>
                <label className="block text-sm text-gray-200">
                  PayPal Password
                  <input
                    type="password"
                    value={paypalPassword}
                    onChange={(event) => setPaypalPassword(event.target.value)}
                    placeholder="Test123"
                    className="mt-1 w-full rounded-md border border-gray-700 bg-gray-900 px-3 py-2 text-white"
                  />
                </label>
                <p className="text-xs text-gray-400">
                  Test PayPal for success: payment@test.com / Test123
                </p>
              </div>
            )}

            {paymentMethod === "WALLET" && (
              <div className="rounded-xl border border-amber-500/40 bg-amber-500/10 px-4 py-3 mb-6 text-sm text-amber-200">
                Wallet is disabled in this simulation and will fail.
              </div>
            )}

            <div className="space-y-3 mb-6">
              <FeeRow
                label="Service Fee"
                amount={serviceFeeAmount}
                required
                readOnly
                displayValue={formatMoney(serviceFeeAmount)}
              />

              <FeeRow
                label="Tax Rate"
                amount={taxRate}
                required
                readOnly
                displayValue={`${(taxRate * 100).toFixed(0)}%`}
              />

              <FeeRow
                label="Insurance Fee"
                checked={includeInsuranceFee}
                onCheckedChange={setIncludeInsuranceFee}
                amount={insuranceFeeAmount}
                onAmountChange={setInsuranceFeeAmount}
                step={0.1}
              />

              <FeeRow
                label="Discount"
                checked={includeDiscount}
                onCheckedChange={setIncludeDiscount}
                amount={discountAmount}
                onAmountChange={setDiscountAmount}
                step={0.1}
              />
            </div>

            {error && (
              <div
                className="flex items-center gap-2 bg-red-500/10 border border-red-500/30
                            rounded-xl p-3 mb-4 text-red-400 text-sm"
              >
                <span>!</span> {error}
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
                ? "Processing..."
                : `Pay ${formatMoney(estimatedAmount)} & Confirm Booking`}
            </button>

            <button
              type="button"
              onClick={() => navigate("/services/parking")}
              className="w-full mt-3 text-gray-500 hover:text-gray-300 text-sm
                         transition-colors py-2"
            >
              Cancel and go back to Parking
            </button>
          </form>
        </main>
      </div>
    </div>
  );
}

function parseNonNegativeNumber(value: string): number {
  const parsed = Number.parseFloat(value);
  if (!Number.isFinite(parsed) || parsed < 0) {
    return 0;
  }

  return parsed;
}

function formatMoney(amount: number | null | undefined): string {
  return `$${(amount ?? 0).toFixed(2)}`;
}

function formatArrivalDateTime(arrivalDate: string, arrivalTime: string): string {
  const parsed = new Date(`${arrivalDate}T${arrivalTime}`);
  if (Number.isNaN(parsed.getTime())) {
    return `${arrivalDate} at ${arrivalTime}`;
  }

  return parsed.toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}
