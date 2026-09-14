import React from 'react';
import {
  Wallet,
  BarChart3,
  Database,
  Bluetooth,
  Store,
  Info,
  ShieldCheck,
  RefreshCcw,
  ChevronLeft,
} from 'lucide-react';

interface MoreViewProps {
  onOpenExpenses: () => void;
  onOpenReports: () => void;
  onOpenDatabaseTools: () => void;
  onOpenBluetooth: () => void;
  onOpenSettings: () => void;
  onOpenAbout: () => void;
  onResetDemoData: () => void;
}

export const MoreView: React.FC<MoreViewProps> = ({
  onOpenExpenses,
  onOpenReports,
  onOpenDatabaseTools,
  onOpenBluetooth,
  onOpenSettings,
  onOpenAbout,
  onResetDemoData,
}) => {
  const menuItems = [
    {
      id: 'expenses',
      title: 'المصروفات النثرية',
      desc: 'تسجيل ومتابعة مصاريف المتجر والتشغيل',
      icon: Wallet,
      color: 'text-amber-600 bg-amber-50',
      action: onOpenExpenses,
    },
    {
      id: 'reports',
      title: 'التقارير المالية والملخص',
      desc: 'إجمالي العمليات، الديون، وتقييم المخزون',
      icon: BarChart3,
      color: 'text-[#146B50] bg-emerald-50',
      action: onOpenReports,
    },
    {
      id: 'database',
      title: 'إدارة قاعدة البيانات والنسخ الاحتياطي',
      desc: 'استيراد/تصدير ملفات SQLite (.db) و JSON وفحص السلامة',
      icon: Database,
      color: 'text-[#4664B4] bg-blue-50',
      action: onOpenDatabaseTools,
    },
    {
      id: 'bluetooth',
      title: 'الطابعة الحرارية والبلوتوث',
      desc: 'الاقتران بطابعات الإيصالات وإعداد حجم الورق (58mm/80mm)',
      icon: Bluetooth,
      color: 'text-indigo-600 bg-indigo-50',
      action: onOpenBluetooth,
    },
    {
      id: 'settings',
      title: 'إعدادات المتجر وبيانات الفواتير',
      desc: 'تعديل اسم البقالة ورقم الهاتف الظاهر في الكشوفات',
      icon: Store,
      color: 'text-slate-600 bg-slate-100',
      action: onOpenSettings,
    },
    {
      id: 'about',
      title: 'حول التطبيق',
      desc: 'معلومات الترخيص والدعم الفني وهيكل التخزين المحلي',
      icon: Info,
      color: 'text-teal-600 bg-teal-50',
      action: onOpenAbout,
    },
  ];

  return (
    <div className="space-y-4 pb-24">
      <div className="flex items-center gap-2">
        <h3 className="text-base font-bold text-[#0D4D3A]">
          المزيد من الأدوات والإعدادات
        </h3>
      </div>

      <div className="space-y-2">
        {menuItems.map(item => {
          const Icon = item.icon;
          return (
            <button
              key={item.id}
              id={`more-menu-btn-${item.id}`}
              onClick={item.action}
              className="w-full bg-white border border-[#E1E8E4] rounded-[22px] p-3.5 sm:p-4 flex items-center justify-between gap-3 shadow-2xs hover:border-[#146B50]/50 transition-all text-right group"
            >
              <div className="flex items-center gap-3 min-w-0">
                <div
                  className={`w-10 h-10 rounded-2xl flex items-center justify-center shrink-0 ${item.color}`}
                >
                  <Icon className="w-5 h-5" />
                </div>
                <div className="min-w-0">
                  <h4 className="font-bold text-sm text-gray-900 group-hover:text-[#146B50] transition-colors">
                    {item.title}
                  </h4>
                  <p className="text-xs text-gray-500 truncate">{item.desc}</p>
                </div>
              </div>

              <ChevronLeft className="w-5 h-5 text-gray-400 group-hover:text-[#146B50] transition-colors shrink-0" />
            </button>
          );
        })}
      </div>

      {/* Reset Demo Data Button */}
      <div className="pt-2">
        <button
          id="btn-reset-demo-data"
          onClick={() => {
            if (
              window.confirm(
                'هل تريد استعادة البيانات التجريبية الافتراضية؟ سيتم تحديث السجلات.'
              )
            ) {
              onResetDemoData();
            }
          }}
          className="w-full flex items-center justify-center gap-2 bg-gray-50 hover:bg-gray-100 text-gray-600 border border-gray-200 py-3 rounded-2xl text-xs font-semibold transition-colors"
        >
          <RefreshCcw className="w-3.5 h-3.5" />
          <span>استعادة عينة البيانات التجريبية</span>
        </button>
      </div>
    </div>
  );
};
