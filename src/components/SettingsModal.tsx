import React, { useState, useEffect } from 'react';
import { StoreSettings } from '../types';
import { X, Store, Phone, Save } from 'lucide-react';

interface SettingsModalProps {
  isOpen: boolean;
  settings: StoreSettings;
  onClose: () => void;
  onSave: (settings: StoreSettings) => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  settings,
  onClose,
  onSave,
}) => {
  const [storeName, setStoreName] = useState(settings.storeName);
  const [phone, setPhone] = useState(settings.phone);
  const [message, setMessage] = useState('');

  useEffect(() => {
    setStoreName(settings.storeName);
    setPhone(settings.phone);
    setMessage('');
  }, [settings, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({
      storeName: storeName.trim() || 'بقالة العزي للمواد الغذائية',
      phone: phone.trim() || '776425052',
    });
    setMessage('تم حفظ الإعدادات بنجاح');
    setTimeout(() => {
      setMessage('');
      onClose();
    }, 1000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-black text-base text-[#146B50]">
            إعدادات المتجر وبيانات الفواتير
          </h3>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {message && (
          <div className="p-2.5 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-[#146B50] font-semibold">
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              اسم المتجر أو البقالة (يظهر في رأس الكشوفات والطباعة)
            </label>
            <div className="relative">
              <input
                id="input-settings-store-name"
                type="text"
                required
                value={storeName}
                onChange={e => setStoreName(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <Store className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              رقم هاتف المتجر / خدمة العملاء
            </label>
            <div className="relative">
              <input
                id="input-settings-phone"
                type="tel"
                dir="ltr"
                value={phone}
                onChange={e => setPhone(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm font-mono text-right focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <Phone className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
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
              id="btn-save-store-settings"
              type="submit"
              className="flex items-center gap-1.5 px-5 py-2 rounded-xl text-xs font-bold bg-[#146B50] hover:bg-[#0D4D3A] text-white shadow-2xs"
            >
              <Save className="w-3.5 h-3.5" />
              <span>حفظ الإعدادات</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
