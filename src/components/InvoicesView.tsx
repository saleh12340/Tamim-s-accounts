import React, { useState } from 'react';
import { Invoice } from '../types';
import { formatMoney } from '../utils/formatters';
import { ShoppingBag, ShoppingCart, Plus, Trash2, Calendar, FileText, CheckCircle } from 'lucide-react';

interface InvoicesViewProps {
  invoices: Invoice[];
  onAddInvoice: (type: 'SALE' | 'PURCHASE') => void;
  onDeleteInvoice: (invoice: Invoice) => void;
}

export const InvoicesView: React.FC<InvoicesViewProps> = ({
  invoices,
  onAddInvoice,
  onDeleteInvoice,
}) => {
  const [filterType, setFilterType] = useState<'ALL' | 'SALE' | 'PURCHASE'>('ALL');

  const filtered = invoices.filter(inv => {
    if (filterType === 'ALL') return true;
    return inv.type === filterType;
  });

  const totalSales = invoices
    .filter(i => i.type === 'SALE')
    .reduce((sum, i) => sum + i.total, 0);

  const totalPurchases = invoices
    .filter(i => i.type === 'PURCHASE')
    .reduce((sum, i) => sum + i.total, 0);

  return (
    <div className="space-y-4 pb-24">
      {/* Top Action Buttons matching ModernActivity */}
      <div className="grid grid-cols-2 gap-2.5">
        <button
          id="btn-add-sale-invoice"
          onClick={() => onAddInvoice('SALE')}
          className="flex items-center justify-center gap-2 bg-[#146B50] hover:bg-[#0D4D3A] text-white py-3 px-3 rounded-[20px] text-xs sm:text-sm font-bold shadow-2xs transition-all active:scale-95"
        >
          <ShoppingCart className="w-4 h-4" />
          <span>＋ فاتورة بيع</span>
        </button>

        <button
          id="btn-add-purchase-invoice"
          onClick={() => onAddInvoice('PURCHASE')}
          className="flex items-center justify-center gap-2 bg-indigo-700 hover:bg-indigo-800 text-white py-3 px-3 rounded-[20px] text-xs sm:text-sm font-bold shadow-2xs transition-all active:scale-95"
        >
          <ShoppingBag className="w-4 h-4" />
          <span>＋ فاتورة شراء</span>
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 gap-2 text-xs">
        <div className="bg-white border border-[#E1E8E4] rounded-2xl p-3 shadow-2xs">
          <span className="text-gray-500 block mb-1">إجمالي المبيعات:</span>
          <div className="text-base font-black text-[#146B50]">
            {formatMoney(totalSales)} <span className="text-xs font-normal">YER</span>
          </div>
        </div>
        <div className="bg-white border border-[#E1E8E4] rounded-2xl p-3 shadow-2xs">
          <span className="text-gray-500 block mb-1">إجمالي المشتريات:</span>
          <div className="text-base font-black text-indigo-700">
            {formatMoney(totalPurchases)} <span className="text-xs font-normal">YER</span>
          </div>
        </div>
      </div>

      {/* Filter Chips */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => setFilterType('ALL')}
          className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
            filterType === 'ALL'
              ? 'bg-gray-800 text-white'
              : 'bg-white border border-[#E1E8E4] text-gray-600 hover:bg-gray-50'
          }`}
        >
          الكل ({invoices.length})
        </button>
        <button
          onClick={() => setFilterType('SALE')}
          className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
            filterType === 'SALE'
              ? 'bg-[#146B50] text-white'
              : 'bg-white border border-[#E1E8E4] text-[#146B50] hover:bg-emerald-50'
          }`}
        >
          فواتير بيع ({invoices.filter(i => i.type === 'SALE').length})
        </button>
        <button
          onClick={() => setFilterType('PURCHASE')}
          className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
            filterType === 'PURCHASE'
              ? 'bg-indigo-700 text-white'
              : 'bg-white border border-[#E1E8E4] text-indigo-700 hover:bg-indigo-50'
          }`}
        >
          فواتير شراء ({invoices.filter(i => i.type === 'PURCHASE').length})
        </button>
      </div>

      {/* Invoices List */}
      <div className="space-y-2.5">
        <h3 className="text-sm font-bold text-[#0D4D3A] px-1">الفواتير المحفوظة</h3>

        {filtered.length === 0 ? (
          <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-8 text-center text-gray-500 text-sm space-y-2">
            <FileText className="w-8 h-8 mx-auto text-gray-300" />
            <p>لا توجد فواتير مسجلة في هذا القسم.</p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {filtered.map(inv => {
              const isSale = inv.type === 'SALE';
              const remaining = inv.total - inv.paid;
              const isPaidInFull = remaining <= 0;

              return (
                <div
                  key={inv.id}
                  id={`invoice-card-${inv.id}`}
                  className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 shadow-2xs hover:border-[#146B50]/50 transition-colors"
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="space-y-1 min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span
                          className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${
                            isSale
                              ? 'bg-emerald-50 text-[#146B50] border border-emerald-200'
                              : 'bg-indigo-50 text-indigo-700 border border-indigo-200'
                          }`}
                        >
                          {isSale ? 'فاتورة بيع' : 'فاتورة شراء'} #{inv.id}
                        </span>

                        {isPaidInFull && (
                          <span className="flex items-center gap-0.5 text-[11px] text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full font-medium">
                            <CheckCircle className="w-3 h-3" />
                            مدفوعة بالكامل
                          </span>
                        )}
                      </div>

                      <div className="text-sm font-bold text-gray-900 truncate">
                        {inv.customerOrSupplierName || (isSale ? 'عميل عام نقدي' : 'مورد نقدي')}
                      </div>

                      {inv.note && (
                        <p className="text-xs text-gray-600 truncate">{inv.note}</p>
                      )}

                      <div className="flex items-center gap-1.5 text-xs text-gray-400 font-mono">
                        <Calendar className="w-3 h-3" />
                        <span>{inv.date}</span>
                      </div>
                    </div>

                    <button
                      onClick={() => onDeleteInvoice(inv)}
                      className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors shrink-0"
                      title="حذف الفاتورة"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>

                  {/* Pricing Footer */}
                  <div className="mt-3 pt-2.5 border-t border-gray-100 flex items-center justify-between text-xs">
                    <div className="space-x-2 space-x-reverse">
                      <span className="text-gray-500">
                        الإجمالي: <strong className="text-gray-900 text-sm">{formatMoney(inv.total)}</strong> YER
                      </span>
                      <span className="text-gray-400">|</span>
                      <span className="text-gray-500">
                        المدفوع: <strong className="text-emerald-700">{formatMoney(inv.paid)}</strong> YER
                      </span>
                    </div>

                    {!isPaidInFull && (
                      <span className="text-[#BE3232] font-bold">
                        المتبقي: {formatMoney(remaining)} YER
                      </span>
                    )}
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
