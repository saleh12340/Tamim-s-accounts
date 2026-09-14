import React from 'react';
import { Home, Users, FileText, Package, MoreHorizontal } from 'lucide-react';

export type TabType = 'home' | 'accounts' | 'invoices' | 'inventory' | 'more';

interface NavigationProps {
  activeTab: TabType;
  onChangeTab: (tab: TabType) => void;
}

export const Navigation: React.FC<NavigationProps> = ({ activeTab, onChangeTab }) => {
  const tabs = [
    { id: 'home' as TabType, label: 'الرئيسية', icon: Home },
    { id: 'accounts' as TabType, label: 'الحسابات', icon: Users },
    { id: 'invoices' as TabType, label: 'الفواتير', icon: FileText },
    { id: 'inventory' as TabType, label: 'المخزون', icon: Package },
    { id: 'more' as TabType, label: 'المزيد', icon: MoreHorizontal },
  ];

  return (
    <nav className="fixed bottom-0 inset-x-0 z-30 bg-white/95 backdrop-blur-md border-t border-[#E1E8E4] px-2 py-1.5 shadow-lg no-print">
      <div className="max-w-md mx-auto grid grid-cols-5 gap-1">
        {tabs.map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              id={`tab-btn-${tab.id}`}
              onClick={() => onChangeTab(tab.id)}
              className={`flex flex-col items-center justify-center py-1.5 px-1 rounded-xl transition-all ${
                isActive
                  ? 'bg-[#146B50]/10 text-[#146B50] font-bold shadow-2xs'
                  : 'text-gray-500 hover:text-[#0D4D3A] hover:bg-gray-50'
              }`}
            >
              <Icon className={`w-5 h-5 ${isActive ? 'stroke-[2.5]' : 'stroke-2'}`} />
              <span className="text-[12px] mt-0.5 tracking-tight whitespace-nowrap">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
