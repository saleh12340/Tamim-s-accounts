import React, { useState } from 'react';
import { Customer, Tx } from '../types';
import { formatMoney, generateStatementText } from '../utils/formatters';
import {
  ArrowRight,
  Plus,
  Edit2,
  Share2,
  Printer,
  Trash2,
  Phone,
  FileText,
  Calendar,
  AlertTriangle,
  Copy,
  Check,
} from 'lucide-react';

interface CustomerLedgerViewProps {
  customer: Customer;
  transactions: Tx[];
  storeName: string;
  onBack: () => void;
  onAddTransaction: (customer: Customer) => void;
  onEditCustomer: (customer: Customer) => void;
  onEditTransaction: (tx: Tx) => void;
  onDeleteTransaction: (tx: Tx) => void;
  onDeleteCustomer: (customer: Customer) => void;
  onPrintStatement: (customer: Customer, transactions: Tx[]) => void;
}

export const CustomerLedgerView: React.FC<CustomerLedgerViewProps> = ({
  customer,
  transactions,
  storeName,
  onBack,
  onAddTransaction,
  onEditCustomer,
  onEditTransaction,
  onDeleteTransaction,
  onDeleteCustomer,
  onPrintStatement,
}) => {
  const [copied, setCopied] = useState(false);

  // Compute running balance chronologically
  let running = 0;
  const sortedTxs = [...transactions].sort(
    (a, b) => a.date.localeCompare(b.date) || a.id - b.id
  );

  const txsWithRunning = sortedTxs.map(tx => {
    running += tx.type === 'DEBIT' ? tx.amount : -tx.amount;
    return { ...tx, runningBalance: running };
  });

  // Display reverse chronological for view (latest first)
  const displayTxs = [...txsWithRunning].reverse();

  const handleShare = async () => {
    const text = generateStatementText(storeName, customer, sortedTxs);
    if (navigator.share) {
      try {
        await navigator.share({
          title: `كشف حساب ${customer.name}`,
          text,
        });
        return;
      } catch (err) {
        // Fallback to clipboard
      }
    }
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const isDebit = customer.balance > 0;
  const isCredit = customer.balance < 0;

  return (
    <div className="space-y-4 pb-24">
      {/* Back Button & Title Header */}
      <div className="flex items-center justify-between gap-2">
        <button
          id="btn-back-to-accounts"
          onClick={onBack}
          className="flex items-center gap-1.5 text-xs font-bold text-[#0D4D3A] bg-white border border-[#E1E8E4] px-3 py-2 rounded-xl hover:bg-gray-50 transition-colors"
        >
          <ArrowRight className="w-4 h-4" />
          <span>العودة للحسابات</span>
        </button>

        <span className="text-xs text-gray-500 font-medium">
          {transactions.length} عملية مسجلة
        </span>
      </div>

      {/* Customer Header Card matching ModernActivity */}
      <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-4 sm:p-5 shadow-2xs space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-gray-100 pb-3">
          <div>
            <span className="text-xs text-gray-400 font-semibold block mb-0.5">حساب العميل</span>
            <h2 className="text-xl sm:text-2xl font-black text-[#146B50]">
              {customer.name}
            </h2>
            <div className="flex items-center gap-2 text-xs text-gray-500 mt-1 font-mono">
              <Phone className="w-3.5 h-3.5 text-gray-400" />
              <span>{customer.phone || 'بدون هاتف'}</span>
              {customer.notes && (
                <span className="text-gray-400 font-sans">
                  • ملاحظات: {customer.notes}
                </span>
              )}
            </div>
          </div>

          <div className="text-right sm:text-left bg-[#F7F9F8] p-3 rounded-2xl border border-gray-100 shrink-0">
            <span className="text-xs text-gray-500 block">الرصيد الحالي:</span>
            <div
              className={`text-xl sm:text-2xl font-black ${
                isDebit ? 'text-[#BE3232]' : isCredit ? 'text-[#146B50]' : 'text-gray-700'
              }`}
            >
              {isDebit ? 'عليه: ' : isCredit ? 'له: ' : ''}
              {formatMoney(Math.abs(customer.balance))}
              <span className="text-xs font-semibold mr-1">YER</span>
            </div>
          </div>
        </div>

        {/* Action Buttons Row matching ModernActivity */}
        <div className="grid grid-cols-4 gap-1.5 pt-1">
          <button
            id="btn-ledger-add-tx"
            onClick={() => onAddTransaction(customer)}
            className="flex items-center justify-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white py-2.5 px-2 rounded-xl text-xs font-bold shadow-2xs transition-all active:scale-95 col-span-2 sm:col-span-1"
          >
            <Plus className="w-4 h-4" />
            <span>＋ إضافة عملية</span>
          </button>

          <button
            id="btn-ledger-edit-customer"
            onClick={() => onEditCustomer(customer)}
            className="flex items-center justify-center gap-1.5 bg-gray-50 hover:bg-gray-100 text-gray-700 py-2.5 px-2 rounded-xl text-xs font-bold border border-[#E1E8E4] transition-colors"
          >
            <Edit2 className="w-3.5 h-3.5" />
            <span>تعديل</span>
          </button>

          <button
            id="btn-ledger-share-statement"
            onClick={handleShare}
            className="flex items-center justify-center gap-1.5 bg-gray-50 hover:bg-gray-100 text-gray-700 py-2.5 px-2 rounded-xl text-xs font-bold border border-[#E1E8E4] transition-colors"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-[#146B50]" /> : <Share2 className="w-3.5 h-3.5" />}
            <span>{copied ? 'تم النسخ' : 'مشاركة'}</span>
          </button>

          <button
            id="btn-ledger-print-statement"
            onClick={() => onPrintStatement(customer, sortedTxs)}
            className="flex items-center justify-center gap-1.5 bg-gray-50 hover:bg-gray-100 text-gray-700 py-2.5 px-2 rounded-xl text-xs font-bold border border-[#E1E8E4] transition-colors"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>طباعة</span>
          </button>
        </div>
      </div>

      {/* Transaction History Section matching ModernActivity & MainActivity */}
      <div className="space-y-2">
        <h3 className="text-sm font-bold text-[#0D4D3A] px-1 flex items-center justify-between">
          <span>سجل العمليات وكشف الحساب</span>
          <span className="text-xs text-gray-400 font-normal">الأحدث أولاً</span>
        </h3>

        {displayTxs.length === 0 ? (
          <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-8 text-center text-gray-500 text-sm space-y-2">
            <FileText className="w-8 h-8 mx-auto text-gray-300" />
            <p>لا توجد عمليات مسجلة لهذا الحساب بعد.</p>
            <button
              onClick={() => onAddTransaction(customer)}
              className="text-xs font-bold text-[#146B50] hover:underline"
            >
              اضغط هنا لإضافة أول قيد (عليه / له)
            </button>
          </div>
        ) : (
          <div className="space-y-2">
            {displayTxs.map(tx => {
              const isTxDebit = tx.type === 'DEBIT';

              return (
                <div
                  key={tx.id}
                  id={`tx-row-${tx.id}`}
                  className="bg-white border border-[#E1E8E4] rounded-[20px] p-3.5 shadow-2xs hover:border-[#146B50]/50 transition-colors"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1 min-w-0 flex-1">
                      {/* Amount & Type Tag */}
                      <div className="flex items-center gap-2">
                        <span
                          className={`text-xs font-black px-2 py-0.5 rounded-md ${
                            isTxDebit
                              ? 'bg-red-50 text-[#BE3232] border border-red-200'
                              : 'bg-emerald-50 text-[#146B50] border border-emerald-200'
                          }`}
                        >
                          {isTxDebit ? 'عليه' : 'له'}
                        </span>
                        <span
                          className={`text-base font-black ${
                            isTxDebit ? 'text-[#BE3232]' : 'text-[#146B50]'
                          }`}
                        >
                          {formatMoney(tx.amount)} {tx.currency}
                        </span>
                      </div>

                      {/* Note / Description */}
                      <div className="text-sm font-medium text-gray-800">
                        {tx.note ? tx.note : <span className="text-gray-400 italic">بدون بيان</span>}
                      </div>

                      {/* Date & Running Balance */}
                      <div className="flex flex-wrap items-center gap-3 text-xs text-gray-400 pt-0.5 font-mono">
                        <span className="flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          {tx.date}
                        </span>
                        <span className="text-gray-500 font-sans">
                          الرصيد بعد العملية:{' '}
                          <strong
                            className={
                              tx.runningBalance > 0
                                ? 'text-[#BE3232]'
                                : tx.runningBalance < 0
                                ? 'text-[#146B50]'
                                : 'text-gray-600'
                            }
                          >
                            {formatMoney(Math.abs(tx.runningBalance))} YER (
                            {tx.runningBalance >= 0 ? 'عليه' : 'له'})
                          </strong>
                        </span>
                      </div>
                    </div>

                    {/* Action controls for this transaction */}
                    <div className="flex items-center gap-1 shrink-0 pt-0.5">
                      <button
                        onClick={() => onEditTransaction(tx)}
                        className="p-1.5 text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                        title="تعديل العملية"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => onDeleteTransaction(tx)}
                        className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="حذف العملية"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Delete Customer Button matching MainActivity & ModernActivity */}
      <div className="pt-4 border-t border-gray-200">
        <button
          id="btn-delete-entire-customer"
          onClick={() => onDeleteCustomer(customer)}
          className="w-full flex items-center justify-center gap-2 bg-red-50 hover:bg-red-100 text-[#BE3232] border border-red-200 py-3 rounded-2xl text-xs font-bold transition-colors"
        >
          <AlertTriangle className="w-4 h-4" />
          <span>حذف الحساب بالكامل وجميع عملياته</span>
        </button>
      </div>
    </div>
  );
};
