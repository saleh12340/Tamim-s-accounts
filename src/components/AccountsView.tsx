import React, { useState } from 'react';
import { Customer } from '../types';
import { formatMoney } from '../utils/formatters';
import { Search, UserPlus, Phone, BookOpen, Trash2, ArrowUpDown, CheckCircle2 } from 'lucide-react';

interface AccountsViewProps {
  customers: Customer[];
  onOpenLedger: (customerId: number) => void;
  onAddCustomer: () => void;
  onEditCustomer: (customer: Customer) => void;
  onDeleteCustomer: (customer: Customer) => void;
}

export const AccountsView: React.FC<AccountsViewProps> = ({
  customers,
  onOpenLedger,
  onAddCustomer,
  onEditCustomer,
  onDeleteCustomer,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'debit' | 'credit' | 'zero'>('all');
  const [sortOrder, setSortOrder] = useState<'newest' | 'id_desc' | 'balance_desc' | 'name_asc'>('newest');

  const filtered = customers.filter(c => {
    const matchesSearch =
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.phone.includes(searchTerm);
    if (!matchesSearch) return false;

    if (filterType === 'debit') return c.balance > 0;
    if (filterType === 'credit') return c.balance < 0;
    if (filterType === 'zero') return c.balance === 0;
    return true;
  });

  const sortedAndFiltered = [...filtered].sort((a, b) => {
    if (sortOrder === 'name_asc') {
      return a.name.localeCompare(b.name, 'ar');
    }
    if (sortOrder === 'id_desc') {
      return b.id - a.id;
    }
    if (sortOrder === 'balance_desc') {
      return Math.abs(b.balance) - Math.abs(a.balance);
    }
    // 'newest': preserves incoming newest-first sort order (recent activity / ID desc)
    return 0;
  });

  const totalBalance = customers.reduce((sum, c) => sum + c.balance, 0);

  return (
    <div className="space-y-3 pb-24">
      {/* Search Input matching ModernActivity */}
      <div className="relative">
        <input
          id="input-search-customers"
          type="text"
          placeholder="بحث باسم العميل أو رقم الهاتف..."
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="w-full bg-white border border-[#E1E8E4] rounded-2xl py-3 pr-10 pl-4 text-sm focus:outline-hidden focus:border-[#146B50] focus:ring-2 focus:ring-[#146B50]/20 shadow-2xs transition-all"
        />
        <Search className="w-4 h-4 text-gray-400 absolute right-3.5 top-3.5 pointer-events-none" />
      </div>

      {/* Filter Tabs & Action */}
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 overflow-x-auto py-1">
          <button
            onClick={() => setFilterType('all')}
            className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors whitespace-nowrap ${
              filterType === 'all'
                ? 'bg-[#146B50] text-white'
                : 'bg-white border border-[#E1E8E4] text-gray-600 hover:bg-gray-50'
            }`}
          >
            الكل ({customers.length})
          </button>
          <button
            onClick={() => setFilterType('debit')}
            className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors whitespace-nowrap ${
              filterType === 'debit'
                ? 'bg-[#BE3232] text-white'
                : 'bg-white border border-[#E1E8E4] text-[#BE3232] hover:bg-red-50'
            }`}
          >
            عليهم ({customers.filter(c => c.balance > 0).length})
          </button>
          <button
            onClick={() => setFilterType('credit')}
            className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors whitespace-nowrap ${
              filterType === 'credit'
                ? 'bg-[#146B50] text-white'
                : 'bg-white border border-[#E1E8E4] text-[#146B50] hover:bg-emerald-50'
            }`}
          >
            لهم ({customers.filter(c => c.balance < 0).length})
          </button>
          <button
            onClick={() => setFilterType('zero')}
            className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors whitespace-nowrap ${
              filterType === 'zero'
                ? 'bg-gray-700 text-white'
                : 'bg-white border border-[#E1E8E4] text-gray-500 hover:bg-gray-50'
            }`}
          >
            مسدد ({customers.filter(c => c.balance === 0).length})
          </button>
        </div>

        <button
          id="btn-add-customer-main"
          onClick={onAddCustomer}
          className="flex items-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white px-3.5 py-2 rounded-xl text-xs font-bold shadow-2xs transition-all active:scale-95 shrink-0"
        >
          <UserPlus className="w-4 h-4" />
          <span>إضافة حساب جديد</span>
        </button>
      </div>

      {/* Summary Header & Sort Selector */}
      <div className="bg-white border border-[#E1E8E4] rounded-2xl px-4 py-2.5 flex flex-wrap items-center justify-between gap-2 text-xs text-gray-600 shadow-2xs">
        <div className="flex items-center gap-3">
          <span>
            عدد الحسابات: <strong className="text-gray-900">{sortedAndFiltered.length}</strong>
          </span>
          <span>
            صافي الأرصدة:{' '}
            <strong className={totalBalance >= 0 ? 'text-[#BE3232]' : 'text-[#146B50]'}>
              {formatMoney(Math.abs(totalBalance))} YER ({totalBalance >= 0 ? 'عليه' : 'له'})
            </strong>
          </span>
        </div>

        {/* Sort Selector with Default Newest First */}
        <div className="flex items-center gap-1.5 text-xs">
          <ArrowUpDown className="w-3.5 h-3.5 text-[#146B50]" />
          <span className="text-gray-500 font-medium">الترتيب:</span>
          <select
            id="select-customer-sort"
            value={sortOrder}
            onChange={e => setSortOrder(e.target.value as any)}
            className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-lg px-2.5 py-1 text-xs font-bold text-[#0D4D3A] focus:outline-hidden focus:border-[#146B50] cursor-pointer"
          >
            <option value="newest">الأحدث أولاً (حسب آخر حركة)</option>
            <option value="id_desc">الأحدث إضافة (المعرف ID)</option>
            <option value="balance_desc">الأعلى رصيداً (الديون)</option>
            <option value="name_asc">أبجدياً (أ - ي)</option>
          </select>
        </div>
      </div>

      {/* Customer List */}
      {sortedAndFiltered.length === 0 ? (
        <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-10 text-center text-gray-500 space-y-3">
          <BookOpen className="w-10 h-10 mx-auto text-gray-300 stroke-1" />
          <p className="text-sm">
            {searchTerm
              ? 'لا توجد حسابات مطابقة لبحثك.'
              : 'لا توجد حسابات بعد. اضغط إضافة حساب جديد للبدء.'}
          </p>
          {!searchTerm && (
            <button
              onClick={onAddCustomer}
              className="bg-[#146B50] text-white text-xs font-bold px-4 py-2 rounded-xl hover:bg-[#0D4D3A] transition-colors"
            >
              ＋ إضافة أول حساب
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-2.5">
          {sortedAndFiltered.map(customer => {
            const isDebit = customer.balance > 0;
            const isCredit = customer.balance < 0;
            const isZero = customer.balance === 0;

            return (
              <div
                key={customer.id}
                id={`customer-card-${customer.id}`}
                className="bg-white border border-[#E1E8E4] rounded-[22px] p-3.5 sm:p-4 shadow-2xs hover:border-[#146B50]/60 transition-all cursor-pointer group"
                onClick={() => onOpenLedger(customer.id)}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-bold text-base text-gray-900 truncate">
                        {customer.name}
                      </h4>
                      {isZero && (
                        <span className="flex items-center gap-0.5 text-[11px] text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
                          <CheckCircle2 className="w-3 h-3 text-gray-400" />
                          مسدد
                        </span>
                      )}
                    </div>

                    <div className="flex items-center gap-3 text-xs text-gray-500 mt-1">
                      <span className="flex items-center gap-1 font-mono">
                        <Phone className="w-3.5 h-3.5 text-gray-400" />
                        {customer.phone ? customer.phone : 'بدون رقم هاتف'}
                      </span>
                      {customer.notes && (
                        <span className="truncate max-w-[180px] text-gray-400">
                          • {customer.notes}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Balance Display matching MainActivity */}
                  <div className="text-left shrink-0">
                    <div
                      className={`text-base sm:text-lg font-black ${
                        isDebit ? 'text-[#BE3232]' : isCredit ? 'text-[#146B50]' : 'text-gray-500'
                      }`}
                    >
                      {isDebit ? 'عليه' : isCredit ? 'له' : 'الرصيد'}:{' '}
                      {formatMoney(Math.abs(customer.balance))}
                      <span className="text-xs font-normal text-gray-400 mr-1">YER</span>
                    </div>
                  </div>
                </div>

                {/* Card Action Footer */}
                <div className="mt-3 pt-2.5 border-t border-gray-100 flex items-center justify-between gap-2">
                  <span className="text-[11px] text-gray-400 group-hover:text-[#146B50] font-medium transition-colors">
                    انقر لفتح كشف الحساب والعمليات ↵
                  </span>

                  <div className="flex items-center gap-1.5" onClick={e => e.stopPropagation()}>
                    <button
                      onClick={() => onOpenLedger(customer.id)}
                      className="px-2.5 py-1 bg-gray-50 hover:bg-[#146B50] hover:text-white text-[#0D4D3A] rounded-lg text-xs font-bold transition-colors"
                    >
                      فتح الحساب
                    </button>
                    <button
                      onClick={() => onEditCustomer(customer)}
                      className="px-2.5 py-1 bg-gray-50 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-medium transition-colors"
                    >
                      تعديل
                    </button>
                    <button
                      onClick={() => onDeleteCustomer(customer)}
                      className="p-1 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="حذف الحساب"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
