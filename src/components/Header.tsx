import React from 'react';
import { Menu, RefreshCw, BookOpen } from 'lucide-react';

interface HeaderProps {
  title: string;
  onRefresh: () => void;
  onMenuClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ title, onRefresh, onMenuClick }) => {
  return (
    <header className="sticky top-0 z-30 bg-[#F7F9F8]/95 backdrop-blur-sm px-3 py-2 border-b border-[#E1E8E4] no-print">
      <div className="max-w-4xl mx-auto flex items-center justify-between gap-2">
        {/* Title Box matching Android Green Badge */}
        <div className="flex-1 bg-[#146B50] hover:bg-[#0D4D3A] transition-colors text-white py-2.5 px-4 rounded-[18px] shadow-sm flex items-center justify-center gap-2">
          <BookOpen className="w-5 h-5 shrink-0 opacity-90" />
          <h1 className="text-base sm:text-lg font-bold truncate text-center tracking-wide">
            {title}
          </h1>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-1.5 shrink-0">
          <button
            id="btn-header-refresh"
            onClick={onRefresh}
            title="تحديث البيانات"
            className="w-10 h-10 rounded-[14px] bg-white border border-[#E1E8E4] text-[#0D4D3A] flex items-center justify-center hover:bg-[#146B50]/10 transition-colors active:scale-95 shadow-2xs"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
          <button
            id="btn-header-menu"
            onClick={onMenuClick}
            title="القائمة والمزيد"
            className="w-10 h-10 rounded-[14px] bg-white border border-[#E1E8E4] text-[#0D4D3A] flex items-center justify-center hover:bg-[#146B50]/10 transition-colors active:scale-95 shadow-2xs font-bold text-lg"
          >
            <Menu className="w-5 h-5" />
          </button>
        </div>
      </div>
    </header>
  );
};
