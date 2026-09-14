import React from 'react';
import { Expense } from '../types';
import { formatMoney } from '../utils/formatters';
import { Wallet, Plus, Trash2, Calendar } from 'lucide-react';

interface ExpensesViewProps {
  expenses: Expense[];
  onAddExpense: () => void;
  onDeleteExpense: (expense: Expense) => void;
  onBack?: () => void;
}

export const ExpensesView: React.FC<ExpensesViewProps> = ({
  expenses,
  onAddExpense,
  onDeleteExpense,
  onBack,
}) => {
  const total = expenses.reduce((sum, e) => sum + e.amount, 0);

  return (
    <div className="space-y-4 pb-24">
      {/* Top Header & Add Button */}
      <div className="flex items-center justify-between gap-2">
        <div>
          <h3 className="text-base font-bold text-[#0D4D3A]">المصروفات النثرية والتشغيلية</h3>
          <span className="text-xs text-gray-500">{expenses.length} مصروف مسجل</span>
        </div>

        <button
          id="btn-add-expense-main"
          onClick={onAddExpense}
          className="flex items-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white px-3.5 py-2 rounded-xl text-xs font-bold shadow-2xs transition-all active:scale-95 shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>＋ إضافة مصروف</span>
        </button>
      </div>

      {/* Summary Card */}
      <div className="bg-white border border-[#E1E8E4] rounded-2xl p-4 flex items-center justify-between shadow-2xs">
        <div className="flex items-center gap-2 text-gray-600 text-xs">
          <Wallet className="w-4 h-4 text-[#BE3232]" />
          <span>إجمالي المصروفات:</span>
        </div>
        <div className="text-lg font-black text-[#BE3232]">
          {formatMoney(total)} <span className="text-xs font-normal">YER</span>
        </div>
      </div>

      {/* Expenses List */}
      {expenses.length === 0 ? (
        <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-8 text-center text-gray-500 text-sm space-y-2">
          <Wallet className="w-8 h-8 mx-auto text-gray-300" />
          <p>لا توجد مصروفات مسجلة بعد.</p>
        </div>
      ) : (
        <div className="space-y-2">
          {expenses.map(expense => (
            <div
              key={expense.id}
              id={`expense-card-${expense.id}`}
              className="bg-white border border-[#E1E8E4] rounded-[20px] p-3.5 shadow-2xs hover:border-[#146B50]/50 transition-colors flex items-center justify-between gap-3"
            >
              <div className="space-y-1 min-w-0 flex-1">
                <div className="font-bold text-sm text-gray-900 truncate">
                  {expense.title}
                </div>
                {expense.note && (
                  <p className="text-xs text-gray-500 truncate">{expense.note}</p>
                )}
                <div className="flex items-center gap-1.5 text-xs text-gray-400 font-mono">
                  <Calendar className="w-3 h-3" />
                  <span>{expense.date}</span>
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <div className="text-left">
                  <div className="text-sm font-black text-[#BE3232]">
                    {formatMoney(expense.amount)} {expense.currency}
                  </div>
                </div>

                <button
                  onClick={() => onDeleteExpense(expense)}
                  className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  title="حذف المصروف"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
