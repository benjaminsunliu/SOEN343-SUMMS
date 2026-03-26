import { useEffect, useMemo, useState } from "react";
import { SiteNav } from "../root";
import { apiFetch } from "../utils/api";
import type { Route } from "./+types/analytics-rentals";

interface ProviderPaymentAnalytics {
  totalTransactions: number;
  successfulTransactions: number;
  failedTransactions: number;
  totalRevenue: number;
  successRatePercentage: number;
  revenueByPaymentMethod: Record<string, number>;
}

interface ProviderTransaction {
  id: number;
  reservationId: number;
  paymentMethod: string;
  amount: number;
  success: boolean;
  processorTransactionId: string;
  createdAt: string;
}

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Rental Analytics | SUMMS" },
    {
      name: "description",
      content: "Analytics related to rentals and payment patterns.",
    },
  ];
}

export default function RentalAnalyticsPage() {
  const [analytics, setAnalytics] = useState<ProviderPaymentAnalytics | null>(null);
  const [transactions, setTransactions] = useState<ProviderTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadAnalytics() {
      setLoading(true);
      setError(null);

      try {
        const [analyticsResponse, transactionsResponse] = await Promise.all([
          apiFetch("/api/payments/transactions/analytics/provider/me"),
          apiFetch("/api/payments/transactions/provider/me"),
        ]);

        if (!analyticsResponse.ok) {
          throw new Error(`Failed to load analytics (${analyticsResponse.status})`);
        }
        if (!transactionsResponse.ok) {
          throw new Error(`Failed to load transactions (${transactionsResponse.status})`);
        }

        const analyticsData = (await analyticsResponse.json()) as ProviderPaymentAnalytics;
        const transactionData = (await transactionsResponse.json()) as ProviderTransaction[];

        if (!isMounted) {
          return;
        }

        setAnalytics(analyticsData);
        setTransactions(transactionData ?? []);
      } catch (err) {
        if (!isMounted) {
          return;
        }

        setAnalytics(null);
        setTransactions([]);
        setError(err instanceof Error ? err.message : "Unable to load rental analytics.");
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }

    void loadAnalytics();

    return () => {
      isMounted = false;
    };
  }, []);

  const revenueByMethod = useMemo(() => {
    if (!analytics) {
      return [] as Array<[string, number]>;
    }

    return Object.entries(analytics.revenueByPaymentMethod)
      .sort((a, b) => b[1] - a[1]);
  }, [analytics]);

  return (
    <>
      <SiteNav />
      <main className="min-h-screen bg-gray-900 px-5 py-4 text-white">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <header className="mb-6">
            <h1 className="text-3xl font-bold text-white">Rental Analytics</h1>
            <p className="text-sm text-gray-400 mt-1">
              Transaction-based analytics for provider operations.
            </p>
          </header>

          {error && (
            <div className="mb-6 rounded-lg border border-red-600 bg-red-900/20 px-4 py-3 text-red-300">
              {error}
            </div>
          )}

          {loading ? (
            <div className="rounded-lg border border-gray-700 bg-gray-800 px-4 py-6 text-gray-300">
              Loading analytics...
            </div>
          ) : analytics ? (
            <>
              <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
                <MetricCard label="Total Transactions" value={String(analytics.totalTransactions)} />
                <MetricCard label="Successful" value={String(analytics.successfulTransactions)} />
                <MetricCard label="Failed" value={String(analytics.failedTransactions)} />
                <MetricCard label="Total Revenue" value={`$${analytics.totalRevenue.toFixed(2)}`} />
                <MetricCard label="Success Rate" value={`${analytics.successRatePercentage.toFixed(1)}%`} />
              </section>

              <section className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                <div className="bg-gray-800 rounded-lg border border-gray-700 p-5">
                  <h2 className="text-lg font-semibold text-white mb-3">Revenue by Payment Method</h2>
                  {revenueByMethod.length === 0 ? (
                    <p className="text-sm text-gray-400">No successful payment revenue yet.</p>
                  ) : (
                    <div className="space-y-3">
                      {revenueByMethod.map(([method, revenue]) => (
                        <div
                          key={method}
                          className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2"
                        >
                          <span className="text-gray-300">{method}</span>
                          <span className="font-semibold text-cyan-400">${revenue.toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="bg-gray-800 rounded-lg border border-gray-700 p-5">
                  <h2 className="text-lg font-semibold text-white mb-3">Top Outcomes</h2>
                  <div className="space-y-3">
                    <InsightRow
                      label="Latest Transaction"
                      value={transactions.length > 0 ? formatDateTime(transactions[0].createdAt) : "N/A"}
                    />
                    <InsightRow
                      label="Latest Successful Payment"
                      value={
                        transactions.find((transaction) => transaction.success)
                          ? formatDateTime(
                              transactions.find((transaction) => transaction.success)!.createdAt,
                            )
                          : "N/A"
                      }
                    />
                    <InsightRow
                      label="Recent Failures"
                      value={String(
                        transactions.slice(0, 20).filter((transaction) => !transaction.success).length,
                      )}
                    />
                  </div>
                </div>
              </section>

              <section className="bg-gray-800 rounded-lg border border-gray-700 p-5">
                <h2 className="text-lg font-semibold text-white mb-3">Recent Transactions</h2>
                {transactions.length === 0 ? (
                  <p className="text-sm text-gray-400">No transaction history yet.</p>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="border-b border-gray-700">
                          <th className="text-left font-semibold text-gray-300 py-3 px-4">Date</th>
                          <th className="text-left font-semibold text-gray-300 py-3 px-4">Reservation</th>
                          <th className="text-left font-semibold text-gray-300 py-3 px-4">Method</th>
                          <th className="text-right font-semibold text-gray-300 py-3 px-4">Amount</th>
                          <th className="text-left font-semibold text-gray-300 py-3 px-4">Status</th>
                          <th className="text-left font-semibold text-gray-300 py-3 px-4">Transaction ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        {transactions.slice(0, 15).map((transaction) => (
                          <tr key={transaction.id} className="border-b border-gray-700 hover:bg-gray-700/40">
                            <td className="py-3 px-4 text-gray-300">{formatDateTime(transaction.createdAt)}</td>
                            <td className="py-3 px-4 text-gray-300">#{transaction.reservationId}</td>
                            <td className="py-3 px-4 text-gray-300">{transaction.paymentMethod}</td>
                            <td className="py-3 px-4 text-right text-cyan-400">${transaction.amount.toFixed(2)}</td>
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
              </section>
            </>
          ) : null}
        </div>
      </main>
    </>
  );
}

interface MetricCardProps {
  label: string;
  value: string;
}

function MetricCard({ label, value }: MetricCardProps) {
  return (
    <div className="bg-gray-800 rounded-lg border border-gray-700 p-4">
      <p className="text-sm text-gray-400">{label}</p>
      <p className="text-xl font-bold text-cyan-300 mt-2">{value}</p>
    </div>
  );
}

interface InsightRowProps {
  label: string;
  value: string;
}

function InsightRow({ label, value }: InsightRowProps) {
  return (
    <div className="flex items-center justify-between rounded border border-gray-700 bg-gray-900 px-3 py-2">
      <span className="text-sm text-gray-400">{label}</span>
      <span className="text-sm font-semibold text-gray-200">{value}</span>
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
