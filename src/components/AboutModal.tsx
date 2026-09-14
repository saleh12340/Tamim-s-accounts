import React from 'react';
import { X, BookOpen, Phone, ShieldCheck, Database, Check } from 'lucide-react';

interface AboutModalProps {
  isOpen: boolean;
  storeName: string;
  phone: string;
  onClose: () => void;
}

export const AboutModal: React.FC<AboutModalProps> = ({
  isOpen,
  storeName,
  phone,
  onClose,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <div className="flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-[#146B50]" />
            <h3 className="font-black text-base text-[#146B50]">
              حول التطبيق
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="text-center py-2 space-y-2">
          <div className="w-16 h-16 bg-[#146B50]/10 text-[#146B50] rounded-2xl flex items-center justify-center mx-auto">
            <BookOpen className="w-8 h-8" />
          </div>
          <h4 className="font-black text-lg text-gray-900">{storeName}</h4>
          <p className="text-xs text-gray-500">نظام إدارة الحسابات، المخزون، والمصروفات</p>
        </div>

        <div className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-2xl p-4 space-y-2.5 text-xs text-gray-700">
          <div className="flex items-center justify-between">
            <span className="text-gray-500">رقم الهاتف والتواصل:</span>
            <span className="font-mono font-bold text-[#146B50]">{phone}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">محرك قواعد البيانات:</span>
            <span className="font-bold flex items-center gap-1 text-gray-800">
              <Database className="w-3.5 h-3.5 text-[#146B50]" />
              SQLite (sql.js Engine)
            </span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">حالة التخزين:</span>
            <span className="font-bold text-emerald-700 flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5" />
              تخزين محلي آمن (Offline-First)
            </span>
          </div>
        </div>

        <p className="text-[11px] text-gray-500 leading-relaxed text-center px-2">
          تمت إعادة بناء وبرمجة التطبيق لتوفير تجربة ويب فائقة السرعة مع الحفاظ الكامل على كافة قواعد العمل وتوافقية قواعد بيانات SQLite السابقة.
        </p>

        <div className="flex justify-center pt-1 border-t border-gray-100">
          <button
            id="btn-close-about"
            type="button"
            onClick={onClose}
            className="px-6 py-2 rounded-xl text-xs font-bold bg-[#146B50] hover:bg-[#0D4D3A] text-white transition-colors shadow-2xs"
          >
            حسناً
          </button>
        </div>
      </div>
    </div>
  );
};
