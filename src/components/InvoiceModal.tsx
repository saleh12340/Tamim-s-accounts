import React, { useState, useEffect } from 'react';
import { Customer } from '../types';
import { X, ShoppingCart, ShoppingBag, DollarSign, FileText, User } from 'lucide-react';

interface InvoiceModalProps {
  isOpen: boolean;
  type: 'SALE' | 'PURCHASE';
  customers: Customer[];
  onClose: () => void;
  onSave: (
    type: 'SALE' | 'PURCHASE',
    total: number,
    paid: number,
    note: string,
    customerId?: number | null,
    customerOrSupplierName?: string
  ) => void;
}

export const InvoiceModal: React.FC<InvoiceModalProps> = ({
  isOpen,
  type,
  customers,
  onClose,
  onSave,
}) => {
  const [total, setTotal] = useState('');
  const [paid, setPaid] = useState('');
  const [note, setNote] = useState('');
  const [partyName, setPartyName] = useState('');
  const [selectedCustomerId, setSelectedCustomerId] = useState<string>('');
  const [error, setError] = useState('');

  useEffect(() => {
    setTotal('');
    setPaid('');
    setNote('');
    setPartyName('');
    setSelectedCustomerId('');
    setError('');
  }, [isOpen, type]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const tot = parseFloat(total);
    const pd = parseFloat(paid) || 0;

    if (isNaN(tot) || tot <= 0) {
      setError('أدخل إجمالي الفاتورة بشكل صحيح');
      return;
    }

    const cid = selectedCustomerId ? Number(selectedCustomerId) : null;
    const finalName = partyName.trim() || (cid ? customers.find(c => c.id === cid)?.name : '');

    onSave(type, tot, pd, note.trim(), cid, finalName);
    onClose();
  };

  const isSale = type === 'SALE';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <div className="flex items-center gap-2">
            {isSale ? (
              <ShoppingCart className="w-5 h-5 text-[#146B50]" />
            ) : (
              <ShoppingBag className="w-5 h-5 text-indigo-700" />
            )}
            <h3 className="font-black text-base text-gray-900">
              {isSale ? 'تسجيل فاتورة بيع' : 'تسجيل فاتورة شراء'}
            </h3>
          </div>
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
          {/* Customer selection for Sale, or Supplier name for Purchase */}
          {isSale ? (
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                العميل (اختياري / أو نقدي)
              </label>
              <select
                id="select-invoice-customer"
                value={selectedCustomerId}
                onChange={e => {
                  setSelectedCustomerId(e.target.value);
                  if (e.target.value) {
                    const found = customers.find(c => c.id === Number(e.target.value));
                    if (found) setPartyName(found.name);
                  }
                }}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              >
                <option value="">-- عميل نقدي عام / بدون حساب --</option>
                {customers.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.phone || 'بدون هاتف'})
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                اسم المورد / الشركة
              </label>
              <div className="relative">
                <input
                  id="input-invoice-supplier"
                  type="text"
                  placeholder="مثال: شركة السعيد للتجارة، تاجر الجملة..."
                  value={partyName}
                  onChange={e => setPartyName(e.target.value)}
                  className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-indigo-700"
                />
                <User className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                إجمالي الفاتورة <span className="text-[#BE3232]">*</span>
              </label>
              <div className="relative">
                <input
                  id="input-invoice-total"
                  type="number"
                  step="any"
                  required
                  placeholder="0.00"
                  value={total}
                  onChange={e => setTotal(e.target.value)}
                  className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-7 pl-3 text-sm font-bold text-gray-900 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
                  autoFocus
                />
                <DollarSign className="w-3.5 h-3.5 text-gray-400 absolute right-2 top-3.5 pointer-events-none" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                المبلغ المدفوع
              </label>
              <div className="relative">
                <input
                  id="input-invoice-paid"
                  type="number"
                  step="any"
                  placeholder="0.00"
                  value={paid}
                  onChange={e => setPaid(e.target.value)}
                  className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-7 pl-3 text-sm font-bold text-[#146B50] focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
                />
                <DollarSign className="w-3.5 h-3.5 text-gray-400 absolute right-2 top-3.5 pointer-events-none" />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              البيان أو الملاحظات
            </label>
            <div className="relative">
              <input
                id="input-invoice-note"
                type="text"
                placeholder="تفاصيل الفاتورة أو الأصناف..."
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
              id="btn-save-invoice"
              type="submit"
              className={`px-5 py-2 rounded-xl text-xs font-bold text-white shadow-2xs ${
                isSale ? 'bg-[#146B50] hover:bg-[#0D4D3A]' : 'bg-indigo-700 hover:bg-indigo-800'
              }`}
            >
              حفظ الفاتورة
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
