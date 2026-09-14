import React, { useState } from 'react';
import { Customer, Tx } from '../types';
import { formatMoney, compareTxNewestFirst, compareTxOldestFirst } from '../utils/formatters';
import { X, Printer, ArrowUpDown } from 'lucide-react';

interface PrintStatementModalProps {
  isOpen: boolean;
  customer: Customer | null;
  transactions: Tx[];
  storeName: string;
  phone: string;
  onClose: () => void;
}

export const PrintStatementModal: React.FC<PrintStatementModalProps> = ({
  isOpen,
  customer,
  transactions,
  storeName,
  phone,
  onClose,
}) => {
  const [sortMode, setSortMode] = useState<'newest' | 'oldest'>('newest');

  if (!isOpen || !customer) return null;

  const sortedTxs = [...transactions].sort(
    sortMode === 'newest' ? compareTxNewestFirst : compareTxOldestFirst
  );

  const isDebit = customer.balance > 0;
  const isCredit = customer.balance < 0;

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-lg p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150 max-h-[92vh] flex flex-col">
        {/* Modal Top Bar (hidden in actual print) */}
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-gray-100 pb-3 no-print">
          <div className="flex items-center gap-2">
            <Printer className="w-5 h-5 text-[#146B50]" />
            <h3 className="font-black text-base text-gray-900">
              معاينة كشف الحساب والطباعة
            </h3>
          </div>

          <div className="flex items-center gap-2">
            {/* Sort Toggle */}
            <div className="flex items-center gap-1 bg-gray-50 border border-gray-200 rounded-lg px-2 py-1 text-xs">
              <ArrowUpDown className="w-3 h-3 text-gray-500" />
              <select
                value={sortMode}
                onChange={e => setSortMode(e.target.value as 'newest' | 'oldest')}
                className="bg-transparent font-medium text-gray-700 focus:outline-hidden text-xs cursor-pointer"
              >
                <option value="newest">الأحدث أولاً</option>
                <option value="oldest">الأقدم أولاً</option>
              </select>
            </div>

            <button
              id="btn-trigger-browser-print"
              onClick={handlePrint}
              className="flex items-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all shadow-2xs"
            >
              <Printer className="w-4 h-4" />
              <span>طباعة</span>
            </button>
            <button
              onClick={onClose}
              className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Printable Area */}
        <div
          id="printable-statement"
          className="flex-1 overflow-y-auto p-4 bg-white rounded-xl border border-dashed border-gray-200 text-gray-900 font-sans space-y-4 printable-content"
        >
          {/* Header */}
          <div className="text-center border-b-2 border-gray-900 pb-3 space-y-1">
            <h2 className="text-lg font-black text-gray-900">{storeName}</h2>
            <div className="text-xs font-mono text-gray-700">هاتف: {phone}</div>
            <div className="text-xs font-bold pt-1 text-gray-800">
              --- كشف حساب تفصيلي ---
            </div>
          </div>

          {/* Customer info */}
          <div className="flex justify-between text-xs py-1 border-b border-gray-200">
            <div>
              <span className="text-gray-500">العميل: </span>
              <strong className="text-sm font-black">{customer.name}</strong>
            </div>
            <div>
              <span className="text-gray-500">الهاتف: </span>
              <span className="font-mono">{customer.phone || '—'}</span>
            </div>
          </div>

          <div className="text-[11px] text-gray-400 font-mono text-left">
            تاريخ الطباعة: {new Date().toLocaleString('ar-YE')}
          </div>

          {/* Table */}
          <table className="w-full text-xs border-collapse">
            <thead>
              <tr className="border-b-2 border-gray-300 text-gray-700 font-bold">
                <th className="py-1.5 text-right">التاريخ</th>
                <th className="py-1.5 text-center">النوع</th>
                <th className="py-1.5 text-left">المبلغ</th>
                <th className="py-1.5 text-right pr-2">البيان</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {sortedTxs.map(tx => {
                const txDebit = tx.type === 'DEBIT';
                return (
                  <tr key={tx.id} className="py-1.5">
                    <td className="py-1 text-right text-[11px] font-mono text-gray-600 whitespace-nowrap">
                      {tx.date.slice(0, 10)}
                    </td>
                    <td className="py-1 text-center font-bold">
                      <span
                        className={
                          txDebit ? 'text-red-700' : 'text-emerald-700'
                        }
                      >
                        {txDebit ? 'عليه' : 'له'}
                      </span>
                    </td>
                    <td className="py-1 text-left font-mono font-bold whitespace-nowrap">
                      {formatMoney(tx.amount)} {tx.currency}
                    </td>
                    <td className="py-1 text-right pr-2 text-gray-700 truncate max-w-[120px]">
                      {tx.note || '—'}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          {/* Balance summary footer */}
          <div className="border-t-2 border-gray-900 pt-3 space-y-1">
            <div className="flex justify-between items-center text-sm font-black">
              <span>الرصيد النهائي:</span>
              <span className={isDebit ? 'text-red-700' : 'text-emerald-700'}>
                {formatMoney(Math.abs(customer.balance))} YER (
                {isDebit ? 'عليه' : isCredit ? 'له' : 'مسدد'})
              </span>
            </div>
          </div>

          <div className="text-center text-[10px] text-gray-400 pt-2">
            شكراً لتعاملكم معنا • {storeName}
          </div>
        </div>

        {/* Modal footer (hidden in print) */}
        <div className="flex justify-end pt-2 border-t border-gray-100 no-print">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold bg-gray-100 hover:bg-gray-200 text-gray-700"
          >
            إغلاق
          </button>
        </div>
      </div>
    </div>
  );
};
