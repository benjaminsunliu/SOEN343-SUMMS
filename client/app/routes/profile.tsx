import { useEffect, useMemo, useState } from "react";
import { SiteNav } from "../root";
import { getAuthUser } from "../utils/auth";
import { apiFetch } from "../utils/api";
import type { Route } from "./+types/profile";

interface PaymentTransactionHistoryItem {
  id: number;
  reservationId: number;
  paymentMethod: string;
  amount: number;
  success: boolean;
  message: string;
  processorTransactionId: string;
  paymentAuthorizationCode: string | null;
  createdAt: string;
}

interface Co2SavingsData {
  totalCo2SavedKg: number;
  sustainableTripCount: number;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Profile | SUMMS" },
    {
      name: "description",
      content: "View your profile details and payment transaction history.",
    },
  ];
}

export default function ProfilePage() {
  const authUser = useMemo(() => getAuthUser(), []);
  const [transactions, setTransactions] = useState<PaymentTransactionHistoryItem[]>([]);
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(true);
  const [transactionError, setTransactionError] = useState<string | null>(null);
  const [co2Data, setCo2Data] = useState<Co2SavingsData | null>(null);
  const [isLoadingCo2, setIsLoadingCo2] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadTransactionHistory() {
      setIsLoadingTransactions(true);
      setTransactionError(null);

      try {
        const response = await apiFetch("/api/payments/transactions/me");
        if (!response.ok) {
          throw new Error(`Failed to load transactions (${response.status})`);
        }

        const data = (await response.json()) as PaymentTransactionHistoryItem[];
        if (!isMounted) {
          return;
        }

        setTransactions(data ?? []);
      } catch (error) {
        if (!isMounted) {
          return;
        }
        setTransactions([]);
        setTransactionError(
          error instanceof Error ? error.message : "Unable to load transaction history.",
        );
      } finally {
        if (isMounted) {
          setIsLoadingTransactions(false);
        }
      }
    }

    if (authUser) {
      void loadTransactionHistory();
    } else {
      setIsLoadingTransactions(false);
    }

    return () => {
      isMounted = false;
    };
  }, [authUser]);

  useEffect(() => {
    let isMounted = true;

    async function loadUserCo2() {
      if (!authUser) {
        setCo2Data(null);
        return;
      }

      setIsLoadingCo2(true);

      try {
        const response = await apiFetch(`/api/analytics/co2/user/${authUser.id}`);
        if (!response.ok) {
          throw new Error("Failed to load CO₂ data");
        }

        const data = (await response.json()) as Co2SavingsData;
        if (isMounted) {
          setCo2Data(data);
        }
      } catch {
        if (isMounted) {
          setCo2Data({ totalCo2SavedKg: 0, sustainableTripCount: 0 });
        }
      } finally {
        if (isMounted) {
          setIsLoadingCo2(false);
        }
      }
    }

    void loadUserCo2();

    return () => {
      isMounted = false;
    };
  }, [authUser]);

  const successfulTransactions = transactions.filter((transaction) => transaction.success);
  const totalSpent = successfulTransactions.reduce(
    (sum, transaction) => sum + transaction.amount,
    0,
  );

  return (
    <>
      <SiteNav />
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
        <header className="mb-4 border-b border-[#253047] pb-3">
          <h1 className="text-2xl font-bold tracking-tight text-cyan-400">Profile</h1>
          <p className="text-sm text-gray-300">Your account details and payment transactions.</p>
        </header>

        {!authUser ? (
          <section className="rounded-2xl border border-red-500/70 bg-red-500/20 p-5 text-sm text-red-200">
            You are not signed in.
          </section>
        ) : (
          <div className="space-y-4">
            <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-3 text-lg font-semibold text-cyan-300">User Details</h2>
              <div className="grid gap-3 sm:grid-cols-2">
                <ProfileField label="User ID" value={`#${authUser.id}`} />
                <ProfileField label="Name" value={authUser.name} />
                <ProfileField label="Email" value={authUser.email} />
                <ProfileField label="Role" value={authUser.role} />
              </div>
            </section>

            <section className="rounded-2xl border border-green-500/50 bg-green-500/10 p-5">
              <h2 className="mb-3 text-lg font-semibold text-green-400">Environmental Impact</h2>
              <div className="grid gap-3 sm:grid-cols-2">
                {isLoadingCo2 ? (
                  <p className="text-sm text-gray-400">Loading CO₂ data...</p>
                ) : (
                  <>
                    <div className="rounded-xl bg-green-900/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-wider text-green-300">Total CO₂ Saved</p>
                      <p className="mt-2 text-3xl font-bold text-green-400">
                        {(co2Data?.totalCo2SavedKg ?? 0).toFixed(2)} <span className="text-lg">kg</span>
                      </p>
                      <p className="mt-1 text-xs text-green-300">
                        Equivalent to {((co2Data?.totalCo2SavedKg ?? 0) / 2.4).toFixed(1)} tree seedlings grown for 10 years
                      </p>
                    </div>
                    <div className="rounded-xl bg-blue-900/20 px-4 py-3">
                      <p className="text-xs uppercase tracking-wider text-blue-300">Sustainable Trips</p>
                      <p className="mt-2 text-3xl font-bold text-blue-400">
                        {co2Data?.sustainableTripCount ?? 0}
                      </p>
                      <p className="mt-1 text-xs text-blue-300">
                        Average ~{co2Data && co2Data.sustainableTripCount > 0 ? (co2Data.totalCo2SavedKg / co2Data.sustainableTripCount).toFixed(2) : "0.00"} kg CO₂ saved per trip
                      </p>
                    </div>
                  </>
                )}
              </div>
            </section>

            <section className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <StatCard label="Total Transactions" value={String(transactions.length)} />
              <StatCard label="Successful Payments" value={String(successfulTransactions.length)} />
              <StatCard label="Total Spent" value={`$${totalSpent.toFixed(2)}`} />
            </section>

            <section className="rounded-2xl border border-[#2a354a] bg-[#06142b] p-5">
              <h2 className="mb-3 text-lg font-semibold text-cyan-300">Transaction History</h2>
              {transactionError && (
                <div className="mb-3 rounded-lg border border-red-500/70 bg-red-500/20 px-4 py-2 text-sm text-red-200">
                  {transactionError}
                </div>
              )}

              {isLoadingTransactions ? (
                <p className="text-sm text-gray-300">Loading transactions...</p>
              ) : transactions.length === 0 ? (
                <p className="text-sm text-gray-400">No transactions found.</p>
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
                      {transactions.map((transaction) => (
                        <tr key={transaction.id} className="border-b border-[#1d2a40] text-gray-200">
                          <td className="py-2 pr-3">{formatDateTime(transaction.createdAt)}</td>
                          <td className="py-2 pr-3">#{transaction.reservationId}</td>
                          <td className="py-2 pr-3">{transaction.paymentMethod}</td>
                          <td className="py-2 pr-3">${transaction.amount.toFixed(2)}</td>
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
        )}
      </main>
    </>
  );
}

interface ProfileFieldProps {
  label: string;
  value: string;
}

function ProfileField({ label, value }: ProfileFieldProps) {
  return (
    <div className="rounded-lg border border-[#2d3d57] bg-[#101d34] px-3 py-2">
      <p className="text-xs uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-1 text-sm text-gray-100">{value}</p>
    </div>
  );
}

interface StatCardProps {
  label: string;
  value: string;
}

function StatCard({ label, value }: StatCardProps) {
  return (
    <div className="rounded-xl border border-[#2d3d57] bg-[#06142b] p-4">
      <p className="text-xs uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-2 text-xl font-semibold text-cyan-300">{value}</p>
    </div>
  );
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}
