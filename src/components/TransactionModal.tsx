import React, { useState, useEffect } from 'react';
import { Customer, Tx, TransactionType } from '../types';
import { X, DollarSign, Calendar, FileText, ArrowDownLeft, ArrowUpRight } from 'lucide-react';

interface TransactionModalProps {
  isOpen: boolean;
  customers: Customer[];
  selectedCustomer?: Customer | null;
  existingTx?: Tx | null;
  onClose: () => void;
  onSave: (
    customerId: number,
    type: TransactionType,
    amount: number,
    note: string,
    currency: string,
    date?: string
  ) => void;
}

export const TransactionModal: React.FC<TransactionModalProps> = ({
  isOpen,
  customers,
  selectedCustomer,
  existingTx,
  onClose,
  onSave,
}) => {
  const [customerId, setCustomerId] = useState<number>(selectedCustomer?.id || (customers[0]?.id ?? 1));
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('YER');
  const [note, setNote] = useState('');
  const [date, setDate] = useState('');
  const [type, setType] = useState<TransactionType>('DEBIT');
  const [error, setError] = useState('');

  useEffect(() => {
    if (existingTx) {
      setCustomerId(existingTx.customerId);
      setAmount(existingTx.amount.toString());
      setCurrency(existingTx.currency || 'YER');
      setNote(existingTx.note || '');
      setDate(existingTx.date || '');
      setType(existingTx.type);
    } else {
      if (selectedCustomer) {
        setCustomerId(selectedCustomer.id);
      } else if (customers.length > 0) {
        setCustomerId(customers[0].id);
      }
      setAmount('');
      setCurrency('YER');
      setNote('');
      const d = new Date();
      const pad = (n: number) => n.toString().padStart(2, '0');
      setDate(`${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`);
      setType('DEBIT');
    }
    setError('');
  }, [existingTx, selectedCustomer, isOpen, customers]);

  if (!isOpen) return null;

  const handleSaveWithType = (chosenType: TransactionType) => {
    const val = parseFloat(amount);
    if (isNaN(val) || val <= 0) {
      setError('أدخل مبلغاً صحيحاً أكبر من الصفر');
      return;
    }
    if (!customerId) {
      setError('يرجى اختيار العميل أولاً');
      return;
    }
    onSave(customerId, chosenType, val, note.trim(), currency.trim().toUpperCase(), date);
    onClose();
  };

  const currentCustomerObj = customers.find(c => c.id === customerId);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-black text-base text-[#146B50]">
            {existingTx
              ? 'تعديل العملية'
              : selectedCustomer
              ? `إضافة عملية — ${selectedCustomer.name}`
              : 'إضافة عملية جديدة'}
          </h3>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="p-2.5 bg-red-50 border border-red-200 rounded-xl text-xs text-[#BE3232] font-semibold">
            {error}
          </div>
        )}

        <div className="space-y-3.5">
          {/* Customer selection if not locked */}
          {!selectedCustomer && !existingTx && (
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                اختر الحساب / العميل <span className="text-[#BE3232]">*</span>
              </label>
              <select
                id="select-tx-customer"
                value={customerId}
                onChange={e => setCustomerId(Number(e.target.value))}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              >
                {customers.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.phone || 'بدون هاتف'})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Amount & Currency */}
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              المبلغ والعملة <span className="text-[#BE3232]">*</span>
            </label>
            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <input
                  id="input-tx-amount"
                  type="number"
                  step="any"
                  required
                  placeholder="0.00"
                  value={amount}
                  onChange={e => setAmount(e.target.value)}
                  className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-base font-bold text-gray-900 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
                  autoFocus
                />
                <DollarSign className="w-4 h-4 text-gray-400 absolute right-2.5 top-3.5 pointer-events-none" />
              </div>

              <select
                id="select-tx-currency"
                value={currency}
                onChange={e => setCurrency(e.target.value)}
                className="w-28 bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-2 text-xs font-bold text-gray-800 focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              >
                <option value="YER">YER ريال يمني</option>
                <option value="SAR">SAR ريال سعودي</option>
                <option value="USD">USD دولار</option>
              </select>
            </div>
          </div>

          {/* Note / Description */}
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              البيان (التفاصيل أو الأصناف)
            </label>
            <div className="relative">
              <input
                id="input-tx-note"
                type="text"
                placeholder="مثال: شراء كيس سكر، دفعة نقدية..."
                value={note}
                onChange={e => setNote(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <FileText className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          {/* Date & Time */}
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              التاريخ والوقت
            </label>
            <div className="relative">
              <input
                id="input-tx-date"
                type="text"
                value={date}
                onChange={e => setDate(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-xs font-mono text-gray-700 focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <Calendar className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          {/* Edit Mode Type Selector */}
          {existingTx && (
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                نوع القيد
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setType('DEBIT')}
                  className={`py-2 rounded-xl text-xs font-bold border transition-colors ${
                    type === 'DEBIT'
                      ? 'bg-red-50 text-[#BE3232] border-[#BE3232]'
                      : 'bg-gray-50 text-gray-600 border-gray-200'
                  }`}
                >
                  عليه (دين مسحوبات)
                </button>
                <button
                  type="button"
                  onClick={() => setType('CREDIT')}
                  className={`py-2 rounded-xl text-xs font-bold border transition-colors ${
                    type === 'CREDIT'
                      ? 'bg-emerald-50 text-[#146B50] border-[#146B50]'
                      : 'bg-gray-50 text-gray-600 border-gray-200'
                  }`}
                >
                  له (دفعة سداد / إيداع)
                </button>
              </div>
            </div>
          )}

          {/* Actions Matching Android Dialog */}
          <div className="pt-2 border-t border-gray-100">
            {existingTx ? (
              <div className="flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100"
                >
                  إلغاء
                </button>
                <button
                  id="btn-save-edited-tx"
                  type="button"
                  onClick={() => handleSaveWithType(type)}
                  className="px-6 py-2 rounded-xl text-xs font-bold bg-[#146B50] hover:bg-[#0D4D3A] text-white shadow-2xs"
                >
                  حفظ التعديل
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                <div className="text-xs text-gray-500 text-center font-medium mb-1">
                  اختر نوع العملية للحفظ الفوري:
                </div>
                <div className="grid grid-cols-2 gap-3">
                  {/* DEBIT button in Red matching Android */}
                  <button
                    id="btn-submit-debit"
                    type="button"
                    onClick={() => handleSaveWithType('DEBIT')}
                    className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#BE3232] hover:bg-red-700 text-white shadow-md active:scale-95 transition-all group"
                  >
                    <div className="flex items-center gap-1 text-sm font-black">
                      <ArrowDownLeft className="w-4 h-4" />
                      <span>عليه (دين مسحوبات)</span>
                    </div>
                    <span className="text-[11px] opacity-80 mt-0.5">يزيد دين العميل للبقالة</span>
                  </button>

                  {/* CREDIT button in Green matching Android */}
                  <button
                    id="btn-submit-credit"
                    type="button"
                    onClick={() => handleSaveWithType('CREDIT')}
                    className="flex flex-col items-center justify-center p-3 rounded-2xl bg-[#146B50] hover:bg-[#0D4D3A] text-white shadow-md active:scale-95 transition-all group"
                  >
                    <div className="flex items-center gap-1 text-sm font-black">
                      <ArrowUpRight className="w-4 h-4" />
                      <span>له (دفعة سداد)</span>
                    </div>
                    <span className="text-[11px] opacity-80 mt-0.5">يخفض دين العميل</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
