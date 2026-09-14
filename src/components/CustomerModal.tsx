import React, { useState, useEffect } from 'react';
import { Customer } from '../types';
import { X, User, Phone, FileText } from 'lucide-react';

interface CustomerModalProps {
  isOpen: boolean;
  customer?: Customer | null;
  onClose: () => void;
  onSave: (name: string, phone: string, notes: string) => void;
}

export const CustomerModal: React.FC<CustomerModalProps> = ({
  isOpen,
  customer,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (customer) {
      setName(customer.name);
      setPhone(customer.phone);
      setNotes(customer.notes);
    } else {
      setName('');
      setPhone('');
      setNotes('');
    }
    setError('');
  }, [customer, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('اسم العميل مطلوب');
      return;
    }
    onSave(name.trim(), phone.trim(), notes.trim());
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-black text-base text-[#146B50]">
            {customer ? 'تعديل بيانات الحساب' : 'إضافة حساب عميل جديد'}
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
              اسم العميل <span className="text-[#BE3232]">*</span>
            </label>
            <div className="relative">
              <input
                id="input-customer-name"
                type="text"
                required
                placeholder="مثال: محمد صالح العزي"
                value={name}
                onChange={e => setName(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-9 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50] focus:ring-1 focus:ring-[#146B50]"
                autoFocus
              />
              <User className="w-4 h-4 text-gray-400 absolute right-3 top-3 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              رقم الهاتف
            </label>
            <div className="relative">
              <input
                id="input-customer-phone"
                type="tel"
                dir="ltr"
                placeholder="776425052"
                value={phone}
                onChange={e => setPhone(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-9 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50] focus:ring-1 focus:ring-[#146B50] text-right font-mono"
              />
              <Phone className="w-4 h-4 text-gray-400 absolute right-3 top-3 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              ملاحظات إضافية
            </label>
            <div className="relative">
              <textarea
                id="input-customer-notes"
                rows={2}
                placeholder="ملاحظات حول الحساب أو العنوان..."
                value={notes}
                onChange={e => setNotes(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2 pr-9 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50] focus:ring-1 focus:ring-[#146B50]"
              />
              <FileText className="w-4 h-4 text-gray-400 absolute right-3 top-3 pointer-events-none" />
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100 transition-colors"
            >
              إلغاء
            </button>
            <button
              id="btn-save-customer"
              type="submit"
              className="px-5 py-2 rounded-xl text-xs font-bold bg-[#146B50] hover:bg-[#0D4D3A] text-white transition-colors shadow-2xs"
            >
              حفظ
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
