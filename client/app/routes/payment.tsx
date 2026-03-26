import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import { setActiveTrip } from "../utils/trips";
import type { Route } from "./+types/payment";

const BASE_RATE_PER_MINUTE = 0.50;

interface ReservationResponse {
  reservationId: number;
  userId: number;
  vehicleId: number;
  city: string;
  status: string;
  startDate: string;
  endDate: string;
}

interface ProcessPaymentResponse {
  success: boolean;
  message: string;
  transactionId: string;
  paymentAuthorizationCode: string | null;
  paymentMethod: string;
  amount: number;
  details: string;
}

interface TripStartResponse {
  tripId: number;
  reservationId: number;
  vehicleId: number;
  citizenId: number;
  startTime: string;
}

interface PaymentTransactionHistoryItem {
  id: number;
  reservationId: number;
  paymentMethod: string;
  amount: number;
  success: boolean;
  message: string;
  processorTransactionId: string;
  createdAt: string;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Payment | SUMMS" },
    {
      name: "description",
      content: "Pay for your reservation and start your trip.",
    },
  ];
}

export default function PaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const reservationId = useMemo(() => {
    const raw = searchParams.get("reservationId");
    if (!raw) {
      return null;
    }

    const parsed = Number.parseInt(raw, 10);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return null;
    }

    return parsed;
  }, [searchParams]);

  const [reservation, setReservation] = useState<ReservationResponse | null>(null);
  const [isLoadingReservation, setIsLoadingReservation] = useState(true);
  const [reservationError, setReservationError] = useState<string | null>(null);

  const [paymentMethod, setPaymentMethod] = useState("CREDIT_CARD");
  const [creditCardNumber, setCreditCardNumber] = useState("");
  const [paypalEmail, setPaypalEmail] = useState("");
  const [paypalPassword, setPaypalPassword] = useState("");

  const [includeServiceFee, setIncludeServiceFee] = useState(true);
  const [serviceFeeAmount, setServiceFeeAmount] = useState(2.50);

  const [includeTax, setIncludeTax] = useState(true);
  const [taxRate, setTaxRate] = useState(0.15);

  const [includeInsuranceFee, setIncludeInsuranceFee] = useState(false);
  const [insuranceFeeAmount, setInsuranceFeeAmount] = useState(3.00);

  const [includeDiscount, setIncludeDiscount] = useState(false);
  const [discountAmount, setDiscountAmount] = useState(1.00);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [paymentResult, setPaymentResult] = useState<ProcessPaymentResponse | null>(null);
  const [successToast, setSuccessToast] = useState<{
    message: string;
    visible: boolean;
  } | null>(null);
  const [transactions, setTransactions] = useState<PaymentTransactionHistoryItem[]>([]);
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(true);

  const loadTransactionHistory = async () => {
    setIsLoadingTransactions(true);
    try {
      const response = await apiFetch("/api/payments/transactions/me");
      if (!response.ok) {
        throw new Error("Failed to load transaction history");
      }

      const data = (await response.json()) as PaymentTransactionHistoryItem[];
      setTransactions(data);
    } catch {
      setTransactions([]);
    } finally {
      setIsLoadingTransactions(false);
    }
  };

  useEffect(() => {
    let isMounted = true;

    async function loadReservation() {
      if (!reservationId) {
        if (isMounted) {
          setReservation(null);
          setReservationError("Missing or invalid reservation id.");
          setIsLoadingReservation(false);
        }
        return;
      }

      try {
        const response = await apiFetch(`/api/reservations/${reservationId}`);
        if (!response.ok) {
          const message = await readErrorMessage(
            response,
            "Unable to load reservation details.",
          );
          throw new Error(message);
        }

        const data = (await response.json()) as ReservationResponse;
        if (!isMounted) {
          return;
        }

        setReservation(data);
        setReservationError(null);
      } catch (error) {
        if (!isMounted) {
          return;
        }

        setReservation(null);
        setReservationError(
          error instanceof Error
            ? error.message
            : "Unable to load reservation details.",
        );
      } finally {
        if (isMounted) {
          setIsLoadingReservation(false);
        }
      }
    }

    void loadReservation();

    return () => {
      isMounted = false;
    };
  }, [reservationId]);

  useEffect(() => {
    void loadTransactionHistory();
  }, []);

  const baseAmount = useMemo(() => {
    if (!reservation) {
      return 0;
    }

    const minutes = calculateDurationMinutes(reservation.startDate, reservation.endDate);
    return minutes * BASE_RATE_PER_MINUTE;
  }, [reservation]);

  const estimatedAmount = useMemo(() => {
    let amount = baseAmount;

    if (includeServiceFee) {
      amount += serviceFeeAmount;
    }
    if (includeTax) {
      amount *= 1 + taxRate;
    }
    if (includeInsuranceFee) {
      amount += insuranceFeeAmount;
    }
    if (includeDiscount) {
      amount = Math.max(0, amount - discountAmount);
    }

    return amount;
  }, [
    baseAmount,
    includeServiceFee,
    serviceFeeAmount,
    includeTax,
    taxRate,
    includeInsuranceFee,
    insuranceFeeAmount,
    includeDiscount,
    discountAmount,
  ]);

  const canSubmitPayment =
    !!reservation && reservation.status.toUpperCase() === "CONFIRMED" && !isSubmitting;

  const handlePayAndStartTrip = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setActionError(null);
    setPaymentResult(null);
    setSuccessToast(null);

    if (!reservation) {
      setActionError("Reservation details are not available.");
      return;
    }

    if (reservation.status.toUpperCase() !== "CONFIRMED") {
      setActionError("Reservation must be confirmed before payment.");
      return;
    }

    if (paymentMethod === "CREDIT_CARD" && creditCardNumber.trim().length === 0) {
      setActionError("Enter a credit card number.");
      return;
    }

    if (
      paymentMethod === "PAYPAL" &&
      (paypalEmail.trim().length === 0 || paypalPassword.trim().length === 0)
    ) {
      setActionError("Enter your PayPal email and password.");
      return;
    }

    setIsSubmitting(true);
    try {
      const paymentResponse = await apiFetch(
        `/api/payments/reservations/${reservation.reservationId}`,
        {
          method: "POST",
          body: JSON.stringify({
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
          }),
        },
      );

      if (!paymentResponse.ok) {
        const message = await readErrorMessage(paymentResponse, "Payment request failed.");
        setActionError(message);
        return;
      }

      const processedPayment = (await paymentResponse.json()) as ProcessPaymentResponse;
      setPaymentResult(processedPayment);
      await loadTransactionHistory();

      if (!processedPayment.success) {
        return;
      }

      if (!processedPayment.paymentAuthorizationCode) {
        setActionError("Payment authorization code is missing.");
        return;
      }

      const tripResponse = await apiFetch(`/api/rentals/${reservation.reservationId}/start`, {
        method: "POST",
        body: JSON.stringify({
          paymentAuthorizationCode: processedPayment.paymentAuthorizationCode,
        }),
      });

      if (!tripResponse.ok) {
        const message = await readErrorMessage(tripResponse, "Payment succeeded but trip could not be started.");
        setActionError(message);
        return;
      }

      const trip = (await tripResponse.json()) as TripStartResponse;
      setActiveTrip({
        tripId: trip.tripId,
        reservationId: trip.reservationId,
        vehicleId: trip.vehicleId,
        citizenId: trip.citizenId,
        startTime: trip.startTime,
      });

      setSuccessToast({
        message: "Payment successful. Starting your trip...",
        visible: true,
      });
      await delay(950);
      setSuccessToast((current) =>
        current
          ? {
              ...current,
              visible: false,
            }
          : null,
      );
      await delay(400);
      navigate("/trips/active");
    } catch {
      setActionError("Network error while processing payment.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <SiteNav />
      {successToast && (
        <div
          className={`fixed right-6 top-6 z-[100] max-w-sm rounded-lg border border-green-400/70 bg-green-500/25 px-4 py-3 text-sm text-green-100 shadow-lg backdrop-blur-sm transition-all duration-500 ${
            successToast.visible ? "translate-y-0 opacity-100" : "-translate-y-2 opacity-0"
          }`}
        >
          {successToast.message}
        </div>
      )}
      <main className="ml-56 min-h-screen bg-black px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Payment</h1>
          <p className="text-sm text-gray-300">Pay for your reservation and start your trip.</p>
        </header>

        {reservationError && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {reservationError}
          </div>
        )}
        {actionError && (
          <div className="mb-3 rounded-xl border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
            {actionError}
          </div>
        )}
        {paymentResult && !paymentResult.success && (
          <div className="mb-3 rounded-xl border border-amber-500/70 bg-amber-500/20 px-4 py-2 text-sm text-amber-200">
            <p>{paymentResult.message}</p>
            <p>Transaction ID: {paymentResult.transactionId}</p>
          </div>
        )}

        {isLoadingReservation ? (
          <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5 text-sm text-gray-300">
            Loading reservation...
          </section>
        ) : reservation ? (
          <div className="space-y-4">
            <div className="grid gap-4 lg:grid-cols-2">
            <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-3 text-lg font-semibold text-cyan-300">Reservation Summary</h2>
              <p className="text-sm text-gray-200">Reservation #{reservation.reservationId}</p>
              <p className="text-sm text-gray-300">Vehicle #{reservation.vehicleId}</p>
              <p className="text-sm text-gray-300">City: {reservation.city}</p>
              <p className="text-sm text-gray-300">Status: {reservation.status}</p>
              <p className="mt-2 text-sm text-gray-400">Start: {formatDateTime(reservation.startDate)}</p>
              <p className="text-sm text-gray-400">End: {formatDateTime(reservation.endDate)}</p>

              <div className="mt-4 rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2 text-sm">
                <p>Base amount: {formatMoney(baseAmount)}</p>
                <p className="font-semibold text-cyan-200">Estimated final: {formatMoney(estimatedAmount)}</p>
              </div>

              {reservation.status.toUpperCase() !== "CONFIRMED" && (
                <p className="mt-3 rounded-lg border border-amber-500/60 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
                  This reservation is not confirmed. Payment and trip start are disabled.
                </p>
              )}
            </section>

            <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-3 text-lg font-semibold text-cyan-300">Payment Options</h2>

              <form className="space-y-3" onSubmit={(event) => void handlePayAndStartTrip(event)}>
                <label className="block text-sm text-gray-200">
                  Payment Method
                  <select
                    value={paymentMethod}
                    onChange={(event) => setPaymentMethod(event.target.value)}
                    className="mt-1 w-full rounded-md border border-[#2d3d57] bg-[#101d34] px-3 py-2 text-white"
                  >
                    <option value="CREDIT_CARD">Credit Card</option>
                    <option value="PAYPAL">PayPal</option>
                    <option value="WALLET">Wallet</option>
                  </select>
                </label>

                {paymentMethod === "CREDIT_CARD" && (
                  <div className="rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2">
                    <label className="block text-sm text-gray-200">
                      Credit Card Number
                      <input
                        type="text"
                        value={creditCardNumber}
                        onChange={(event) => setCreditCardNumber(event.target.value)}
                        placeholder="12345678"
                        className="mt-1 w-full rounded-md border border-[#2d3d57] bg-[#071229] px-3 py-2 text-white"
                      />
                    </label>
                    <p className="mt-1 text-xs text-gray-400">
                      Test card for success: 12345678
                    </p>
                  </div>
                )}

                {paymentMethod === "PAYPAL" && (
                  <div className="space-y-2 rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2">
                    <label className="block text-sm text-gray-200">
                      PayPal Email
                      <input
                        type="email"
                        value={paypalEmail}
                        onChange={(event) => setPaypalEmail(event.target.value)}
                        placeholder="payment@test.com"
                        className="mt-1 w-full rounded-md border border-[#2d3d57] bg-[#071229] px-3 py-2 text-white"
                      />
                    </label>
                    <label className="block text-sm text-gray-200">
                      PayPal Password
                      <input
                        type="password"
                        value={paypalPassword}
                        onChange={(event) => setPaypalPassword(event.target.value)}
                        placeholder="Test123"
                        className="mt-1 w-full rounded-md border border-[#2d3d57] bg-[#071229] px-3 py-2 text-white"
                      />
                    </label>
                    <p className="text-xs text-gray-400">
                      Test PayPal for success: payment@test.com / Test123
                    </p>
                  </div>
                )}

                {paymentMethod === "WALLET" && (
                  <p className="rounded-lg border border-amber-500/60 bg-amber-500/10 px-3 py-2 text-xs text-amber-200">
                    Wallet is disabled in this simulation and will fail.
                  </p>
                )}

                <FeeRow
                  label="Service Fee"
                  checked={includeServiceFee}
                  onCheckedChange={setIncludeServiceFee}
                  amount={serviceFeeAmount}
                  onAmountChange={setServiceFeeAmount}
                  step={0.1}
                />

                <FeeRow
                  label="Tax Rate"
                  checked={includeTax}
                  onCheckedChange={setIncludeTax}
                  amount={taxRate}
                  onAmountChange={setTaxRate}
                  step={0.01}
                  hint="Use decimal format (0.15 = 15%)."
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

                <button
                  type="submit"
                  disabled={!canSubmitPayment}
                  className="w-full rounded-md bg-cyan-600 px-3 py-2 text-sm font-semibold text-white transition hover:bg-cyan-500 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmitting ? "Processing..." : "Pay & Start Trip"}
                </button>
              </form>
            </section>
            </div>

            <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-3 text-lg font-semibold text-cyan-300">Past Transactions</h2>
              {isLoadingTransactions ? (
                <p className="text-sm text-gray-300">Loading transaction history...</p>
              ) : transactions.length === 0 ? (
                <p className="text-sm text-gray-400">No transactions yet.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-[#2d3d57] text-left text-gray-300">
                        <th className="py-2 pr-3">Date</th>
                        <th className="py-2 pr-3">Reservation</th>
                        <th className="py-2 pr-3">Method</th>
                        <th className="py-2 pr-3">Amount</th>
                        <th className="py-2 pr-3">Status</th>
                        <th className="py-2 pr-3">Transaction ID</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transactions.slice(0, 10).map((transaction) => (
                        <tr key={transaction.id} className="border-b border-[#1d2a40] text-gray-200">
                          <td className="py-2 pr-3">{formatDateTime(transaction.createdAt)}</td>
                          <td className="py-2 pr-3">#{transaction.reservationId}</td>
                          <td className="py-2 pr-3">{transaction.paymentMethod}</td>
                          <td className="py-2 pr-3">{formatMoney(transaction.amount)}</td>
                          <td className="py-2 pr-3">
                            {transaction.success ? (
                              <span className="rounded px-2 py-1 text-xs text-green-200 bg-green-500/20 border border-green-500/50">
                                SUCCESS
                              </span>
                            ) : (
                              <span className="rounded px-2 py-1 text-xs text-amber-200 bg-amber-500/20 border border-amber-500/50">
                                FAILED
                              </span>
                            )}
                          </td>
                          <td className="py-2 pr-3">{transaction.processorTransactionId}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </div>
        ) : null}
      </main>
    </>
  );
}

interface FeeRowProps {
  label: string;
  checked: boolean;
  onCheckedChange: (value: boolean) => void;
  amount: number;
  onAmountChange: (value: number) => void;
  step: number;
  hint?: string;
}

function FeeRow({
  label,
  checked,
  onCheckedChange,
  amount,
  onAmountChange,
  step,
  hint,
}: FeeRowProps) {
  return (
    <div className="rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2">
      <div className="flex items-center justify-between gap-3">
        <label className="flex items-center gap-2 text-sm text-gray-200">
          <input
            type="checkbox"
            checked={checked}
            onChange={(event) => onCheckedChange(event.target.checked)}
          />
          {label}
        </label>

        <input
          type="number"
          min={0}
          step={step}
          value={amount}
          onChange={(event) => onAmountChange(parseNonNegativeNumber(event.target.value))}
          disabled={!checked}
          className="w-28 rounded-md border border-[#2d3d57] bg-[#071229] px-2 py-1 text-sm text-white disabled:opacity-60"
        />
      </div>
      {hint && <p className="mt-1 text-xs text-gray-400">{hint}</p>}
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

function calculateDurationMinutes(startDate: string, endDate: string): number {
  const start = new Date(startDate);
  const end = new Date(endDate);

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return 1;
  }

  const minutes = (end.getTime() - start.getTime()) / (1000 * 60);
  if (!Number.isFinite(minutes) || minutes <= 0) {
    return 1;
  }

  return minutes;
}

function formatMoney(amount: number): string {
  return `$${amount.toFixed(2)}`;
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString();
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

function delay(milliseconds: number): Promise<void> {
  return new Promise((resolve) => {
    window.setTimeout(resolve, milliseconds);
  });
}
