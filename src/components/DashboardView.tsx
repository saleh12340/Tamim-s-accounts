import React from 'react';
import { Customer, Tx } from '../types';
import { formatMoney, compareTxNewestFirst } from '../utils/formatters';
import { Users, Receipt, BarChart3, ArrowDownLeft, ArrowUpRight, Plus, ShoppingCart, ShoppingBag, TrendingUp } from 'lucide-react';

interface DashboardViewProps {
  customers: Customer[];
  transactions: Tx[];
  onOpenCustomerLedger: (customerId: number) => void;
  onAddCustomer: () => void;
  onAddTransaction: () => void;
  onAddInvoice: (type: 'SALE' | 'PURCHASE') => void;
  onNavigateToTab: (tab: 'accounts' | 'invoices' | 'reports') => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  customers,
  transactions,
  onOpenCustomerLedger,
  onAddCustomer,
  onAddTransaction,
  onAddInvoice,
  onNavigateToTab,
}) => {
  const totalDebit = customers
    .filter(c => c.balance > 0)
    .reduce((sum, c) => sum + c.balance, 0);

  const totalCredit = customers
    .filter(c => c.balance < 0)
    .reduce((sum, c) => sum + Math.abs(c.balance), 0);

  const netBalance = totalDebit - totalCredit;

  // Recent transactions (الأحدث أولاً)
  const recentTxs = [...transactions]
    .sort(compareTxNewestFirst)
    .slice(0, 5);

  return (
    <div className="space-y-4 pb-24">
      {/* Overview Metric Cards matching ModernActivity layout */}
      <div className="grid grid-cols-3 gap-2.5">
        <button
          id="metric-card-customers"
          onClick={() => onNavigateToTab('accounts')}
          className="bg-white border border-[#E1E8E4] rounded-[20px] p-3 text-center shadow-2xs hover:border-[#146B50] transition-all text-right flex flex-col justify-between"
        >
          <div className="flex items-center justify-between text-[#146B50]">
            <span className="text-xs font-semibold text-gray-500">الحسابات</span>
            <Users className="w-4 h-4 opacity-75" />
          </div>
          <div className="text-xl sm:text-2xl font-black text-[#146B50] mt-2">
            {customers.length}
          </div>
          <span className="text-[11px] text-gray-400 mt-1">عميل مسجل</span>
        </button>

        <button
          id="metric-card-transactions"
          onClick={() => onNavigateToTab('accounts')}
          className="bg-white border border-[#E1E8E4] rounded-[20px] p-3 text-center shadow-2xs hover:border-[#4664B4] transition-all text-right flex flex-col justify-between"
        >
          <div className="flex items-center justify-between text-[#4664B4]">
            <span className="text-xs font-semibold text-gray-500">العمليات</span>
            <Receipt className="w-4 h-4 opacity-75" />
          </div>
          <div className="text-xl sm:text-2xl font-black text-[#4664B4] mt-2">
            {transactions.length}
          </div>
          <span className="text-[11px] text-gray-400 mt-1">عملية قيد</span>
        </button>

        <button
          id="metric-card-reports"
          onClick={() => onNavigateToTab('reports')}
          className="bg-white border border-[#E1E8E4] rounded-[20px] p-3 text-center shadow-2xs hover:border-[#146B50] transition-all text-right flex flex-col justify-between"
        >
          <div className="flex items-center justify-between text-[#146B50]">
            <span className="text-xs font-semibold text-gray-500">التقارير</span>
            <BarChart3 className="w-4 h-4 opacity-75" />
          </div>
          <div className="text-base sm:text-lg font-black text-[#146B50] mt-2">
            اليوم والشهر
          </div>
          <span className="text-[11px] text-gray-400 mt-1">سجل الحركات والأحدث</span>
        </button>
      </div>

      {/* Financial Summary Card matching MainActivity summary */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 shadow-2xs">
        <div className="flex items-center justify-between mb-3 border-b border-gray-100 pb-2">
          <div className="flex items-center gap-2 text-[#0D4D3A] font-bold text-sm">
            <TrendingUp className="w-4 h-4 text-[#146B50]" />
            <span>ملخص الأرصدة والديون الحالية</span>
          </div>
          <span className="text-xs text-gray-400 bg-gray-50 px-2 py-0.5 rounded-full">
            ريال يمني (YER)
          </span>
        </div>

        <div className="grid grid-cols-2 gap-3 mb-3">
          <div className="bg-red-50/60 border border-red-100 rounded-xl p-3">
            <div className="flex items-center gap-1 text-xs text-[#BE3232] font-semibold mb-1">
              <ArrowDownLeft className="w-3.5 h-3.5" />
              <span>إجمالي ديون العملاء (عليهم)</span>
            </div>
            <div className="text-lg font-bold text-[#BE3232]">
              {formatMoney(totalDebit)} <span className="text-xs font-normal">YER</span>
            </div>
          </div>

          <div className="bg-emerald-50/60 border border-emerald-100 rounded-xl p-3">
            <div className="flex items-center gap-1 text-xs text-[#146B50] font-semibold mb-1">
              <ArrowUpRight className="w-3.5 h-3.5" />
              <span>مبالغ للعملاء (لهم)</span>
            </div>
            <div className="text-lg font-bold text-[#146B50]">
              {formatMoney(totalCredit)} <span className="text-xs font-normal">YER</span>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-gray-100 text-sm">
          <span className="text-gray-600 font-medium">صافي الأرصدة المستحقة:</span>
          <span className={`font-black text-base ${netBalance >= 0 ? 'text-[#BE3232]' : 'text-[#146B50]'}`}>
            {formatMoney(Math.abs(netBalance))} YER {netBalance >= 0 ? '(مستحق لك)' : '(مستحق عليك)'}
          </span>
        </div>
      </div>

      {/* Quick Shortcuts matching ModernActivity */}
      <div className="space-y-2">
        <h3 className="text-sm font-bold text-[#0D4D3A] px-1 flex items-center gap-1.5">
          <span>اختصارات سريعة</span>
        </h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <button
            id="btn-shortcut-add-customer"
            onClick={onAddCustomer}
            className="flex items-center gap-2 bg-white border border-[#E1E8E4] hover:bg-[#146B50] hover:text-white hover:border-[#146B50] text-[#0D4D3A] p-3 rounded-2xl text-xs font-bold transition-all shadow-2xs group"
          >
            <div className="w-7 h-7 rounded-lg bg-[#146B50]/10 group-hover:bg-white/20 flex items-center justify-center shrink-0">
              <Plus className="w-4 h-4 text-[#146B50] group-hover:text-white" />
            </div>
            <span>إضافة حساب</span>
          </button>

          <button
            id="btn-shortcut-add-tx"
            onClick={onAddTransaction}
            className="flex items-center gap-2 bg-white border border-[#E1E8E4] hover:bg-[#4664B4] hover:text-white hover:border-[#4664B4] text-[#4664B4] p-3 rounded-2xl text-xs font-bold transition-all shadow-2xs group"
          >
            <div className="w-7 h-7 rounded-lg bg-[#4664B4]/10 group-hover:bg-white/20 flex items-center justify-center shrink-0">
              <Receipt className="w-4 h-4 text-[#4664B4] group-hover:text-white" />
            </div>
            <span>إضافة عملية</span>
          </button>

          <button
            id="btn-shortcut-add-sale"
            onClick={() => onAddInvoice('SALE')}
            className="flex items-center gap-2 bg-white border border-[#E1E8E4] hover:bg-emerald-600 hover:text-white hover:border-emerald-600 text-emerald-700 p-3 rounded-2xl text-xs font-bold transition-all shadow-2xs group"
          >
            <div className="w-7 h-7 rounded-lg bg-emerald-100/70 group-hover:bg-white/20 flex items-center justify-center shrink-0">
              <ShoppingCart className="w-4 h-4 text-emerald-700 group-hover:text-white" />
            </div>
            <span>فاتورة بيع</span>
          </button>

          <button
            id="btn-shortcut-add-purchase"
            onClick={() => onAddInvoice('PURCHASE')}
            className="flex items-center gap-2 bg-white border border-[#E1E8E4] hover:bg-indigo-600 hover:text-white hover:border-indigo-600 text-indigo-700 p-3 rounded-2xl text-xs font-bold transition-all shadow-2xs group"
          >
            <div className="w-7 h-7 rounded-lg bg-indigo-100/70 group-hover:bg-white/20 flex items-center justify-center shrink-0">
              <ShoppingBag className="w-4 h-4 text-indigo-700 group-hover:text-white" />
            </div>
            <span>فاتورة شراء</span>
          </button>

          <button
            id="btn-shortcut-reports"
            onClick={() => onNavigateToTab('reports')}
            className="col-span-2 sm:col-span-1 flex items-center gap-2 bg-white border border-[#E1E8E4] hover:bg-[#146B50] hover:text-white hover:border-[#146B50] text-[#146B50] p-3 rounded-2xl text-xs font-bold transition-all shadow-2xs group"
          >
            <div className="w-7 h-7 rounded-lg bg-emerald-50 group-hover:bg-white/20 flex items-center justify-center shrink-0">
              <BarChart3 className="w-4 h-4 text-[#146B50] group-hover:text-white" />
            </div>
            <span>التقارير اليومية</span>
          </button>
        </div>
      </div>

      {/* Recent Transactions List */}
      <div className="space-y-2">
        <div className="flex items-center justify-between px-1">
          <h3 className="text-sm font-bold text-[#0D4D3A]">آخر العمليات المسجلة</h3>
          <button
            onClick={() => onNavigateToTab('accounts')}
            className="text-xs text-[#146B50] hover:underline font-semibold"
          >
            عرض الكل
          </button>
        </div>

        {recentTxs.length === 0 ? (
          <div className="bg-white border border-[#E1E8E4] rounded-[20px] p-6 text-center text-gray-500 text-sm">
            لا توجد عمليات مسجلة بعد.
          </div>
        ) : (
          <div className="space-y-2">
            {recentTxs.map(tx => {
              const cust = customers.find(c => c.id === tx.customerId);
              return (
                <div
                  key={tx.id}
                  onClick={() => cust && onOpenCustomerLedger(cust.id)}
                  className="bg-white border border-[#E1E8E4] rounded-[18px] p-3 flex items-center justify-between gap-3 shadow-2xs hover:border-[#146B50] transition-colors cursor-pointer"
                >
                  <div className="min-w-0 flex-1">
                    <div className="font-bold text-gray-900 text-sm truncate">
                      {cust ? cust.name : 'عميل غير معروف'}
                    </div>
                    <div className="text-xs text-gray-500 flex items-center gap-2 mt-0.5">
                      <span>{tx.date}</span>
                      {tx.note && <span className="truncate">• {tx.note}</span>}
                    </div>
                  </div>

                  <div className="text-left shrink-0">
                    <div
                      className={`text-sm font-bold ${
                        tx.type === 'DEBIT' ? 'text-[#BE3232]' : 'text-[#146B50]'
                      }`}
                    >
                      {tx.type === 'DEBIT' ? 'عليه' : 'له'} {formatMoney(tx.amount)} {tx.currency}
                    </div>
                    <span className="text-[11px] text-gray-400">انقر لفتح الحساب</span>
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
