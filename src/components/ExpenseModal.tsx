import React, { useState, useEffect } from 'react';
import { X, Wallet, DollarSign, FileText } from 'lucide-react';

interface ExpenseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (title: string, amount: number, note: string, currency: string) => void;
}

export const ExpenseModal: React.FC<ExpenseModalProps> = ({
  isOpen,
  onClose,
  onSave,
}) => {
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const [currency, setCurrency] = useState('YER');
  const [error, setError] = useState('');

  useEffect(() => {
    setTitle('');
    setAmount('');
    setNote('');
    setCurrency('YER');
    setError('');
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(amount);
    if (!title.trim()) {
      setError('عنوان المصروف مطلوب');
      return;
    }
    if (isNaN(val) || val <= 0) {
      setError('أدخل مبلغاً صحيحاً أكبر من الصفر');
      return;
    }
    onSave(title.trim(), val, note.trim(), currency.trim().toUpperCase());
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-black text-base text-[#146B50]">
            إضافة مصروف نثري جديد
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

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              اسم / بيان المصروف <span className="text-[#BE3232]">*</span>
            </label>
            <div className="relative">
              <input
                id="input-expense-title"
                type="text"
                required
                placeholder="مثال: فاتورة كهرباء، كراتين تغليف..."
                value={title}
                onChange={e => setTitle(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
                autoFocus
              />
              <Wallet className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              المبلغ والعملة <span className="text-[#BE3232]">*</span>
            </label>
            <div className="flex items-center gap-2">
              <div className="relative flex-1">
                <input
                  id="input-expense-amount"
                  type="number"
                  step="any"
                  required
                  placeholder="0.00"
                  value={amount}
                  onChange={e => setAmount(e.target.value)}
                  className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-base font-bold text-gray-900 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
                />
                <DollarSign className="w-4 h-4 text-gray-400 absolute right-2.5 top-3.5 pointer-events-none" />
              </div>

              <select
                id="select-expense-currency"
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

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              ملاحظات أو تفاصيل إضافية
            </label>
            <div className="relative">
              <input
                id="input-expense-notes"
                type="text"
                placeholder="تفاصيل الفاتورة أو الجهة المدفوع لها..."
                value={note}
                onChange={e => setNote(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <FileText className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100"
            >
              إلغاء
            </button>
            <button
              id="btn-save-expense"
              type="submit"
              className="px-5 py-2 rounded-xl text-xs font-bold bg-[#146B50] hover:bg-[#0D4D3A] text-white shadow-2xs"
            >
              حفظ المصروف
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
