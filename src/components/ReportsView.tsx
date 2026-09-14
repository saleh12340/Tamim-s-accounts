import React from 'react';
import { Customer, Tx, Expense, Product } from '../types';
import { formatMoney } from '../utils/formatters';
import { BarChart3, TrendingUp, TrendingDown, ArrowDownLeft, ArrowUpRight, Wallet, Package } from 'lucide-react';

interface ReportsViewProps {
  customers: Customer[];
  transactions: Tx[];
  expenses: Expense[];
  products: Product[];
}

export const ReportsView: React.FC<ReportsViewProps> = ({
  customers,
  transactions,
  expenses,
  products,
}) => {
  const debitYER = transactions
    .filter(t => t.type === 'DEBIT' && t.currency === 'YER')
    .reduce((sum, t) => sum + t.amount, 0);

  const creditYER = transactions
    .filter(t => t.type === 'CREDIT' && t.currency === 'YER')
    .reduce((sum, t) => sum + t.amount, 0);

  const expensesYER = expenses
    .filter(e => e.currency === 'YER')
    .reduce((sum, e) => sum + e.amount, 0);

  const netOperations = debitYER - creditYER;

  // Inventory stats
  const totalStockItems = products.reduce((sum, p) => sum + p.stock, 0);
  const totalBuyVal = products.reduce((sum, p) => sum + p.stock * p.buyPrice, 0);
  const totalSellVal = products.reduce((sum, p) => sum + p.stock * p.sellPrice, 0);
  const expectedProfit = totalSellVal - totalBuyVal;

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center gap-2">
        <BarChart3 className="w-5 h-5 text-[#146B50]" />
        <h3 className="text-base font-bold text-[#0D4D3A]">التقارير المالية والملخص العام</h3>
      </div>

      {/* Main Currency Summary matching MainActivity reports() */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 sm:p-5 shadow-2xs space-y-4">
        <div className="border-b border-gray-100 pb-2 flex items-center justify-between">
          <span className="font-bold text-sm text-gray-800">ملخص ريال يمني (YER)</span>
          <span className="text-xs text-gray-400">شامل كافة العمليات المسجلة</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="bg-red-50/60 border border-red-100 rounded-xl p-3.5 flex items-center justify-between">
            <div>
              <span className="text-xs font-semibold text-[#BE3232] flex items-center gap-1">
                <ArrowDownLeft className="w-3.5 h-3.5" />
                إجمالي ديون العملاء (عليه)
              </span>
              <div className="text-lg font-black text-[#BE3232] mt-1">
                {formatMoney(debitYER)} YER
              </div>
            </div>
          </div>

          <div className="bg-emerald-50/60 border border-emerald-100 rounded-xl p-3.5 flex items-center justify-between">
            <div>
              <span className="text-xs font-semibold text-[#146B50] flex items-center gap-1">
                <ArrowUpRight className="w-3.5 h-3.5" />
                إجمالي مقبوضات وسداد (له)
              </span>
              <div className="text-lg font-black text-[#146B50] mt-1">
                {formatMoney(creditYER)} YER
              </div>
            </div>
          </div>

          <div className="bg-amber-50/60 border border-amber-100 rounded-xl p-3.5 flex items-center justify-between">
            <div>
              <span className="text-xs font-semibold text-amber-800 flex items-center gap-1">
                <Wallet className="w-3.5 h-3.5" />
                إجمالي المصروفات التشغيلية
              </span>
              <div className="text-lg font-black text-amber-900 mt-1">
                {formatMoney(expensesYER)} YER
              </div>
            </div>
          </div>

          <div className="bg-slate-50 border border-slate-200 rounded-xl p-3.5 flex items-center justify-between">
            <div>
              <span className="text-xs font-semibold text-slate-700 flex items-center gap-1">
                <TrendingUp className="w-3.5 h-3.5" />
                صافي العمليات (عليه - له)
              </span>
              <div
                className={`text-lg font-black mt-1 ${
                  netOperations >= 0 ? 'text-[#BE3232]' : 'text-[#146B50]'
                }`}
              >
                {formatMoney(Math.abs(netOperations))} YER{' '}
                <span className="text-xs font-normal">
                  ({netOperations >= 0 ? 'مستحق لك' : 'مستحق عليك'})
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Inventory Valuation Report */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 sm:p-5 shadow-2xs space-y-3">
        <div className="border-b border-gray-100 pb-2 flex items-center justify-between">
          <span className="font-bold text-sm text-gray-800 flex items-center gap-1.5">
            <Package className="w-4 h-4 text-[#915F28]" />
            تقييم المخزون والبضاعة
          </span>
          <span className="text-xs text-gray-400">{products.length} صنف</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
          <div className="p-3 bg-gray-50 rounded-xl border border-gray-100">
            <span className="text-gray-500 block mb-1">قيمة المخزون بسعر الشراء (التكلفة):</span>
            <div className="text-base font-black text-gray-900">
              {formatMoney(totalBuyVal)} YER
            </div>
          </div>

          <div className="p-3 bg-gray-50 rounded-xl border border-gray-100">
            <span className="text-gray-500 block mb-1">قيمة المخزون بسعر البيع التقديري:</span>
            <div className="text-base font-black text-[#146B50]">
              {formatMoney(totalSellVal)} YER
            </div>
          </div>

          <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-100">
            <span className="text-emerald-800 block mb-1">هامش الربح المتوقع عند البيع:</span>
            <div className="text-base font-black text-[#146B50]">
              +{formatMoney(expectedProfit)} YER
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
