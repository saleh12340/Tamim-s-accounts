import React, { useState, useMemo } from 'react';
import { Customer, Tx, Expense } from '../types';
import { formatMoney, parseDateToTimestamp, compareTxNewestFirst, formatArabicDate } from '../utils/formatters';
import {
  BarChart3,
  Calendar,
  ArrowDownLeft,
  ArrowUpRight,
  Wallet,
  Receipt,
  Search,
  Printer,
  Share2,
  Clock,
  User,
} from 'lucide-react';

type PeriodType = 'today' | 'yesterday' | 'week' | 'month' | 'all';

interface ReportsViewProps {
  customers: Customer[];
  transactions: Tx[];
  expenses: Expense[];
  onOpenCustomerLedger?: (customerId: number) => void;
}

export const ReportsView: React.FC<ReportsViewProps> = ({
  customers,
  transactions,
  expenses,
  onOpenCustomerLedger,
}) => {
  const [period, setPeriod] = useState<PeriodType>('today');
  const [searchTerm, setSearchTerm] = useState('');
  const [txTypeFilter, setTxTypeFilter] = useState<'all' | 'DEBIT' | 'CREDIT'>('all');

  // Customer map for fast O(1) lookup
  const customerMap = useMemo(() => {
    const map = new Map<number, Customer>();
    for (const c of customers) {
      map.set(c.id, c);
    }
    return map;
  }, [customers]);

  // Compute time bounds for filters based on current date
  const { startTimestamp, endTimestamp, periodLabel } = useMemo(() => {
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0).getTime();
    const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999).getTime();

    if (period === 'today') {
      return {
        startTimestamp: todayStart,
        endTimestamp: todayEnd,
        periodLabel: 'اليوم',
      };
    }
    if (period === 'yesterday') {
      const yestStart = todayStart - 24 * 60 * 60 * 1000;
      const yestEnd = todayStart - 1;
      return {
        startTimestamp: yestStart,
        endTimestamp: yestEnd,
        periodLabel: 'أمس',
      };
    }
    if (period === 'week') {
      // Last 7 days
      const weekStart = todayStart - 6 * 24 * 60 * 60 * 1000;
      return {
        startTimestamp: weekStart,
        endTimestamp: todayEnd,
        periodLabel: 'آخر 7 أيام (الأسبوع)',
      };
    }
    if (period === 'month') {
      // Current month from 1st day or last 30 days
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0).getTime();
      return {
        startTimestamp: monthStart,
        endTimestamp: todayEnd,
        periodLabel: 'هذا الشهر',
      };
    }
    return {
      startTimestamp: 0,
      endTimestamp: Infinity,
      periodLabel: 'جميع الفترات',
    };
  }, [period]);

  // Filter transactions by period
  const periodTransactions = useMemo(() => {
    return transactions.filter(t => {
      const ts = parseDateToTimestamp(t.date);
      return ts >= startTimestamp && ts <= endTimestamp;
    });
  }, [transactions, startTimestamp, endTimestamp]);

  // Filter expenses by period
  const periodExpenses = useMemo(() => {
    return expenses.filter(e => {
      const ts = parseDateToTimestamp(e.date);
      return ts >= startTimestamp && ts <= endTimestamp;
    });
  }, [expenses, startTimestamp, endTimestamp]);

  // Financial calculations for the selected period
  const debitYER = periodTransactions
    .filter(t => t.type === 'DEBIT' && t.currency === 'YER')
    .reduce((sum, t) => sum + t.amount, 0);

  const creditYER = periodTransactions
    .filter(t => t.type === 'CREDIT' && t.currency === 'YER')
    .reduce((sum, t) => sum + t.amount, 0);

  const expensesYER = periodExpenses
    .filter(e => e.currency === 'YER')
    .reduce((sum, e) => sum + e.amount, 0);

  const netOperations = debitYER - creditYER;

  // Filtered & Sorted Transaction Log for the period
  // MANDATE: Always sort by newest first (الأحدث أولاً)
  const filteredAndSortedTxs = useMemo(() => {
    const list = periodTransactions.filter(t => {
      // Type filter
      if (txTypeFilter !== 'all' && t.type !== txTypeFilter) {
        return false;
      }
      // Search filter
      if (searchTerm.trim()) {
        const q = searchTerm.trim().toLowerCase();
        const custName = customerMap.get(t.customerId)?.name.toLowerCase() || '';
        const note = (t.note || '').toLowerCase();
        const amountStr = t.amount.toString();
        if (!custName.includes(q) && !note.includes(q) && !amountStr.includes(q)) {
          return false;
        }
      }
      return true;
    });

    // Sort newest first
    return list.sort(compareTxNewestFirst);
  }, [periodTransactions, txTypeFilter, searchTerm, customerMap]);

  // Print report
  const handlePrint = () => {
    window.print();
  };

  // Share report summary via WhatsApp or Share API
  const handleShareSummary = async () => {
    const text = `📊 *تقرير الحسابات والعمليات (${periodLabel})*
------------------------------
🔴 إجمالي ديون العملاء (عليه): ${formatMoney(debitYER)} YER
🟢 إجمالي المقبوضات والسداد (له): ${formatMoney(creditYER)} YER
📦 إجمالي المصروفات: ${formatMoney(expensesYER)} YER
⚖️ صافي حركة الفترة: ${formatMoney(Math.abs(netOperations))} YER (${netOperations >= 0 ? 'مستحق لك' : 'مستحق عليك'})
📝 عدد العمليات المنفذة: ${periodTransactions.length} عملية
------------------------------
تم الاستخراج من نظام تميم للحسابات`;

    if (navigator.share) {
      try {
        await navigator.share({ text });
      } catch {
        // Ignored
      }
    } else {
      navigator.clipboard.writeText(text);
      alert('تم نسخ ملخص التقرير إلى الحافظة');
    }
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Top Title & Period Switcher */}
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <BarChart3 className="w-5 h-5 text-[#146B50]" />
          <h3 className="text-base font-black text-[#0D4D3A]">
            تقارير العمليات وسجل الحركات
          </h3>
        </div>

        <div className="flex items-center gap-1.5 no-print">
          <button
            onClick={handlePrint}
            className="flex items-center gap-1 bg-white border border-[#E1E8E4] hover:bg-gray-50 text-gray-700 px-2.5 py-1.5 rounded-xl text-xs font-bold transition-colors"
            title="طباعة التقرير"
          >
            <Printer className="w-3.5 h-3.5 text-gray-500" />
            <span className="hidden sm:inline">طباعة</span>
          </button>
          <button
            onClick={handleShareSummary}
            className="flex items-center gap-1 bg-white border border-[#E1E8E4] hover:bg-gray-50 text-[#146B50] px-2.5 py-1.5 rounded-xl text-xs font-bold transition-colors"
            title="مشاركة التقرير"
          >
            <Share2 className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">مشاركة</span>
          </button>
        </div>
      </div>

      {/* Time Period Filter Tabs: اليوم / أمس / الأسبوع / الشهر / الكل */}
      <div className="bg-white border border-[#E1E8E4] rounded-2xl p-1.5 shadow-2xs no-print">
        <div className="grid grid-cols-5 gap-1 text-center">
          <button
            id="tab-report-today"
            onClick={() => setPeriod('today')}
            className={`py-2 px-1 rounded-xl text-xs font-bold transition-all ${
              period === 'today'
                ? 'bg-[#146B50] text-white shadow-2xs'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            اليوم
          </button>

          <button
            id="tab-report-yesterday"
            onClick={() => setPeriod('yesterday')}
            className={`py-2 px-1 rounded-xl text-xs font-bold transition-all ${
              period === 'yesterday'
                ? 'bg-[#146B50] text-white shadow-2xs'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            أمس
          </button>

          <button
            id="tab-report-week"
            onClick={() => setPeriod('week')}
            className={`py-2 px-1 rounded-xl text-xs font-bold transition-all ${
              period === 'week'
                ? 'bg-[#146B50] text-white shadow-2xs'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            الأسبوع
          </button>

          <button
            id="tab-report-month"
            onClick={() => setPeriod('month')}
            className={`py-2 px-1 rounded-xl text-xs font-bold transition-all ${
              period === 'month'
                ? 'bg-[#146B50] text-white shadow-2xs'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            الشهر
          </button>

          <button
            id="tab-report-all"
            onClick={() => setPeriod('all')}
            className={`py-2 px-1 rounded-xl text-xs font-bold transition-all ${
              period === 'all'
                ? 'bg-[#146B50] text-white shadow-2xs'
                : 'text-gray-600 hover:bg-gray-50'
            }`}
          >
            الكل
          </button>
        </div>
      </div>

      {/* Financial Summary Cards for the Selected Period */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 sm:p-5 shadow-2xs space-y-3">
        <div className="border-b border-gray-100 pb-2.5 flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-[#146B50]" />
            <span className="font-bold text-sm text-gray-900">
              ملخص عمليات: <strong className="text-[#146B50]">{periodLabel}</strong>
            </span>
          </div>
          <span className="text-xs text-gray-500 bg-gray-50 border border-gray-100 px-2.5 py-0.5 rounded-full font-mono">
            {periodTransactions.length} عملية مسجلة
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2.5">
          {/* Debits (عليه) */}
          <div className="bg-red-50/70 border border-red-100 rounded-xl p-3">
            <span className="text-[11px] font-bold text-[#BE3232] flex items-center gap-1">
              <ArrowDownLeft className="w-3.5 h-3.5" />
              ديون ومبيعات جديدة (عليه)
            </span>
            <div className="text-lg font-black text-[#BE3232] mt-1 font-mono">
              {formatMoney(debitYER)} <span className="text-xs font-normal">YER</span>
            </div>
          </div>

          {/* Credits (له) */}
          <div className="bg-emerald-50/70 border border-emerald-100 rounded-xl p-3">
            <span className="text-[11px] font-bold text-[#146B50] flex items-center gap-1">
              <ArrowUpRight className="w-3.5 h-3.5" />
              مقبوضات وسداد نقد (له)
            </span>
            <div className="text-lg font-black text-[#146B50] mt-1 font-mono">
              {formatMoney(creditYER)} <span className="text-xs font-normal">YER</span>
            </div>
          </div>

          {/* Expenses */}
          <div className="bg-amber-50/70 border border-amber-100 rounded-xl p-3">
            <span className="text-[11px] font-bold text-amber-800 flex items-center gap-1">
              <Wallet className="w-3.5 h-3.5" />
              المصروفات النثرية
            </span>
            <div className="text-lg font-black text-amber-900 mt-1 font-mono">
              {formatMoney(expensesYER)} <span className="text-xs font-normal">YER</span>
            </div>
          </div>

          {/* Net Operations */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-3">
            <span className="text-[11px] font-bold text-slate-700 flex items-center gap-1">
              <Receipt className="w-3.5 h-3.5" />
              صافي حركة الفترة (عليه - له)
            </span>
            <div
              className={`text-lg font-black mt-1 font-mono ${
                netOperations >= 0 ? 'text-[#BE3232]' : 'text-[#146B50]'
              }`}
            >
              {formatMoney(Math.abs(netOperations))} <span className="text-xs font-normal">YER</span>{' '}
              <span className="text-[10px] font-normal">
                ({netOperations >= 0 ? 'فارق ديون' : 'فارق سداد'})
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Detailed Operations Log Section (سجل العمليات في الفترة) */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 sm:p-5 shadow-2xs space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-gray-100 pb-3">
          <div>
            <h4 className="font-black text-sm text-gray-900 flex items-center gap-1.5">
              <Clock className="w-4 h-4 text-[#146B50]" />
              سجل العمليات التفصيلي ({periodLabel})
            </h4>
            <p className="text-[11px] text-gray-500">
              مرتبة تلقائياً بحسب **الأحدث أولاً** ({filteredAndSortedTxs.length} حركة)
            </p>
          </div>

          {/* Filter Pills for Log */}
          <div className="flex items-center gap-1 text-xs">
            <button
              onClick={() => setTxTypeFilter('all')}
              className={`px-2.5 py-1 rounded-lg font-bold transition-colors ${
                txTypeFilter === 'all'
                  ? 'bg-gray-800 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              الكل ({periodTransactions.length})
            </button>
            <button
              onClick={() => setTxTypeFilter('DEBIT')}
              className={`px-2.5 py-1 rounded-lg font-bold transition-colors ${
                txTypeFilter === 'DEBIT'
                  ? 'bg-[#BE3232] text-white'
                  : 'bg-red-50 text-[#BE3232] hover:bg-red-100'
              }`}
            >
              عليه ({periodTransactions.filter(t => t.type === 'DEBIT').length})
            </button>
            <button
              onClick={() => setTxTypeFilter('CREDIT')}
              className={`px-2.5 py-1 rounded-lg font-bold transition-colors ${
                txTypeFilter === 'CREDIT'
                  ? 'bg-[#146B50] text-white'
                  : 'bg-emerald-50 text-[#146B50] hover:bg-emerald-100'
              }`}
            >
              له ({periodTransactions.filter(t => t.type === 'CREDIT').length})
            </button>
          </div>
        </div>

        {/* Search inside the period's log */}
        <div className="relative">
          <input
            type="text"
            placeholder="بحث باسم العميل أو البيان أو المبلغ..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2 pr-9 pl-3 text-xs focus:bg-white focus:outline-hidden focus:border-[#146B50]"
          />
          <Search className="w-3.5 h-3.5 text-gray-400 absolute right-3 top-2.5 pointer-events-none" />
        </div>

        {/* Transactions List: Strictly Newest First */}
        {filteredAndSortedTxs.length === 0 ? (
          <div className="p-8 text-center text-gray-400 space-y-2 border border-dashed border-gray-200 rounded-2xl">
            <Receipt className="w-8 h-8 mx-auto text-gray-300 stroke-1" />
            <p className="text-xs font-bold text-gray-600">
              لا توجد عمليات مسجلة في فترة ({periodLabel})
            </p>
            <p className="text-[11px] text-gray-400">
              عند تسجيل أي عملية أو فاتورة جديدة ستظهر هنا في قمة السجل فوراً
            </p>
          </div>
        ) : (
          <div className="space-y-2 max-h-[600px] overflow-y-auto pr-0.5">
            {filteredAndSortedTxs.map(tx => {
              const customer = customerMap.get(tx.customerId);
              const isDebit = tx.type === 'DEBIT';

              return (
                <div
                  key={tx.id}
                  id={`report-tx-card-${tx.id}`}
                  onClick={() => {
                    if (onOpenCustomerLedger && tx.customerId) {
                      onOpenCustomerLedger(tx.customerId);
                    }
                  }}
                  className="bg-white border border-[#E1E8E4] hover:border-[#146B50]/60 rounded-xl p-3 flex items-center justify-between gap-3 shadow-2xs hover:shadow-xs transition-all cursor-pointer group"
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${
                        isDebit
                          ? 'bg-red-50 text-[#BE3232]'
                          : 'bg-emerald-50 text-[#146B50]'
                      }`}
                    >
                      {isDebit ? (
                        <ArrowDownLeft className="w-4 h-4" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4" />
                      )}
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className="font-bold text-xs text-gray-900 group-hover:text-[#146B50] transition-colors">
                          {customer?.name || `عميل #${tx.customerId}`}
                        </span>
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.2 rounded-md ${
                            isDebit
                              ? 'bg-red-50 text-[#BE3232] border border-red-100'
                              : 'bg-emerald-50 text-[#146B50] border border-emerald-100'
                          }`}
                        >
                          {isDebit ? 'عليه (دين)' : 'له (سداد)'}
                        </span>
                      </div>

                      {tx.note && (
                        <p className="text-[11px] text-gray-600 truncate mt-0.5 max-w-xs sm:max-w-md">
                          {tx.note}
                        </p>
                      )}

                      <div className="flex items-center gap-1 text-[10px] text-gray-400 mt-0.5">
                        <Clock className="w-2.5 h-2.5" />
                        <span>{formatArabicDate(tx.date)}</span>
                        {customer?.phone && (
                          <>
                            <span className="mx-1">•</span>
                            <span>{customer.phone}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="text-left shrink-0">
                    <div
                      className={`text-xs sm:text-sm font-black font-mono ${
                        isDebit ? 'text-[#BE3232]' : 'text-[#146B50]'
                      }`}
                    >
                      {isDebit ? '+' : '-'}
                      {formatMoney(tx.amount)}
                      <span className="text-[10px] font-normal text-gray-500 mr-1">
                        {tx.currency}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
