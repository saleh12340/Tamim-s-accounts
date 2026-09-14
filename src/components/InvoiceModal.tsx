import React, { useState, useEffect, useMemo } from 'react';
import { Customer, InvoiceItem, Invoice } from '../types';
import { formatMoney } from '../utils/formatters';
import { suggestionsService } from '../services/suggestions';
import {
  X,
  ShoppingCart,
  ShoppingBag,
  Plus,
  Trash2,
  User,
  Sparkles,
  Calculator,
} from 'lucide-react';

interface InvoiceModalProps {
  isOpen: boolean;
  type: 'SALE' | 'PURCHASE';
  customers: Customer[];
  existingInvoice?: Invoice | null;
  onClose: () => void;
  onSave: (
    type: 'SALE' | 'PURCHASE',
    total: number,
    paid: number,
    note: string,
    customerId?: number | null,
    customerOrSupplierName?: string,
    items?: InvoiceItem[],
    recordInCustomerLedger?: boolean
  ) => void;
}

interface LocalInvoiceItem {
  id: string;
  itemName: string;
  quantity: number;
  total: number;
  unitPrice: number;
}

export const InvoiceModal: React.FC<InvoiceModalProps> = ({
  isOpen,
  type,
  customers,
  existingInvoice,
  onClose,
  onSave,
}) => {
  // Invoice items state
  const [items, setItems] = useState<LocalInvoiceItem[]>([]);

  // Item input boxes (RTL: Total first on right -> Qty -> Details -> Add button)
  const [itemTotal, setItemTotal] = useState<string>('');
  const [itemQty, setItemQty] = useState<string>('1');
  const [itemDetails, setItemDetails] = useState<string>('');

  // Invoice general fields
  const [paid, setPaid] = useState<string>('');
  const [note, setNote] = useState<string>('');
  const [partyName, setPartyName] = useState<string>('');
  const [selectedCustomerId, setSelectedCustomerId] = useState<string>('');
  const [recordInCustomerLedger, setRecordInCustomerLedger] = useState<boolean>(true);
  const [error, setError] = useState<string>('');

  // Quick suggestions list
  const availableSuggestions = useMemo(() => {
    return suggestionsService.getSuggestions(itemDetails);
  }, [itemDetails]);

  // Derived Unit Price for the current inputs
  const parsedTotal = parseFloat(itemTotal) || 0;
  const parsedQty = parseFloat(itemQty) || 1;
  const derivedUnitPrice = parsedQty > 0 ? parsedTotal / parsedQty : 0;

  // Total of all items added
  const invoiceTotal = useMemo(() => {
    return items.reduce((sum, item) => sum + item.total, 0);
  }, [items]);

  const parsedPaid = parseFloat(paid) || 0;
  const remainingDebt = Math.max(0, invoiceTotal - parsedPaid);

  useEffect(() => {
    if (isOpen) {
      if (existingInvoice) {
        setItems(
          (existingInvoice.items || []).map(i => ({
            id: String(i.id || Date.now()),
            itemName: i.itemName,
            quantity: i.quantity,
            total: i.total,
            unitPrice: i.unitPrice,
          }))
        );
        setPaid(existingInvoice.paid.toString());
        setNote(existingInvoice.note || '');
        setPartyName(existingInvoice.customerOrSupplierName || '');
        setSelectedCustomerId(existingInvoice.customerId?.toString() || '');
        setRecordInCustomerLedger(false);
      } else {
        setItems([]);
        setPaid('');
        setNote('');
        setPartyName('');
        setSelectedCustomerId('');
        setRecordInCustomerLedger(true);
      }
      setItemTotal('');
      setItemQty('1');
      setItemDetails('');
      setError('');
    }
  }, [isOpen, type, existingInvoice]);

  if (!isOpen) return null;

  // Add single item to invoice
  const handleAddItem = (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    const tot = parseFloat(itemTotal);
    const qty = parseFloat(itemQty);
    const details = itemDetails.trim();

    if (isNaN(tot) || tot <= 0) {
      setError('يرجى كتابة القيمة الإجمالية للصنف بشكل صحيح');
      return;
    }
    if (isNaN(qty) || qty <= 0) {
      setError('الكمية / العدد يجب أن يكون أكبر من الصفر');
      return;
    }
    if (!details) {
      setError('يرجى كتابة اسم الصنف أو التفاصيل');
      return;
    }

    const unitPrice = tot / qty;

    const newItem: LocalInvoiceItem = {
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
      itemName: details,
      quantity: qty,
      total: tot,
      unitPrice: Math.round(unitPrice * 100) / 100,
    };

    setItems(prev => [...prev, newItem]);

    // Save details to suggestion bank permanently
    suggestionsService.addSuggestion(details);

    // Reset item inputs and set error to empty
    setItemTotal('');
    setItemQty('1');
    setItemDetails('');
    setError('');

    // Refocus on item total input for rapid continuous typing
    const totalInput = document.getElementById('input-item-total');
    if (totalInput) totalInput.focus();
  };

  // Remove single item from table
  const handleRemoveItem = (id: string) => {
    setItems(prev => prev.filter(item => item.id !== id));
  };

  // Submit invoice
  const handleSubmitInvoice = (e: React.FormEvent) => {
    e.preventDefault();

    if (items.length === 0) {
      setError('يرجى إضافة صنف واحد على الأقل إلى الفاتورة');
      return;
    }

    const cid = selectedCustomerId ? Number(selectedCustomerId) : null;
    const finalName =
      partyName.trim() || (cid ? customers.find(c => c.id === cid)?.name : '');

    // Format item objects for storage
    const invoiceItems: InvoiceItem[] = items.map(it => ({
      id: 0,
      invoiceId: 0,
      itemName: it.itemName,
      quantity: it.quantity,
      unitPrice: it.unitPrice,
      total: it.total,
    }));

    // If general note is empty, summarize items automatically
    const autoNote = note.trim() || items.map(i => `${i.itemName} (${i.quantity})`).join('، ');

    onSave(
      type,
      invoiceTotal,
      parsedPaid,
      autoNote,
      cid,
      finalName,
      invoiceItems,
      recordInCustomerLedger
    );

    onClose();
  };

  const isSale = type === 'SALE';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/50 backdrop-blur-2xs overflow-y-auto">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-xl p-4 sm:p-5 shadow-2xl space-y-3.5 my-auto max-h-[95vh] flex flex-col animate-in fade-in zoom-in-95 duration-150">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 pb-2.5 shrink-0">
          <div className="flex items-center gap-2">
            {isSale ? (
              <div className="w-8 h-8 rounded-xl bg-emerald-50 text-[#146B50] flex items-center justify-center">
                <ShoppingCart className="w-4 h-4" />
              </div>
            ) : (
              <div className="w-8 h-8 rounded-xl bg-indigo-50 text-indigo-700 flex items-center justify-center">
                <ShoppingBag className="w-4 h-4" />
              </div>
            )}
            <div>
              <h3 className="font-black text-sm sm:text-base text-gray-900">
                {isSale ? 'تسجيل فاتورة بيع بالأصناف' : 'تسجيل فاتورة شراء بالأصناف'}
              </h3>
              <p className="text-[11px] text-gray-500">
                إدخال الأصناف بنداً بنداً بحساب سعر الوحدة تلقائياً
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Error notification */}
        {error && (
          <div className="p-2.5 bg-red-50 border border-red-200 rounded-xl text-xs text-[#BE3232] font-semibold shrink-0">
            {error}
          </div>
        )}

        {/* Customer / Supplier Header Selection */}
        <div className="shrink-0 bg-gray-50/70 border border-gray-100 rounded-xl p-2.5">
          {isSale ? (
            <div>
              <label className="block text-[11px] font-bold text-gray-600 mb-1">
                العميل (نقدي أو آجل على الحساب)
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
                className="w-full bg-white border border-[#E1E8E4] rounded-lg py-1.5 px-2.5 text-xs font-semibold text-gray-800 focus:outline-hidden focus:border-[#146B50]"
              >
                <option value="">-- عميل نقدي عام (بدون تسجيل دين) --</option>
                {customers.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.name} (رصيده الحالي: {formatMoney(c.balance)} YER)
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div>
              <label className="block text-[11px] font-bold text-gray-600 mb-1">
                اسم المورد أو التاجر
              </label>
              <div className="relative">
                <input
                  id="input-invoice-supplier"
                  type="text"
                  placeholder="مثال: شركة البركة، تاجر الجملة..."
                  value={partyName}
                  onChange={e => setPartyName(e.target.value)}
                  className="w-full bg-white border border-[#E1E8E4] rounded-lg py-1.5 pr-7 pl-2 text-xs font-semibold focus:outline-hidden focus:border-indigo-700"
                />
                <User className="w-3.5 h-3.5 text-gray-400 absolute right-2 top-2 pointer-events-none" />
              </div>
            </div>
          )}
        </div>

        {/* Item Entry Section: RTL order (Total Right -> Qty Middle -> Details Left -> Add Button) */}
        <div className="bg-[#F4F8F6] border border-[#D5E5DD] rounded-2xl p-3 shrink-0 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-black text-[#0D4D3A] flex items-center gap-1">
              <Calculator className="w-3.5 h-3.5 text-[#146B50]" />
              إضافة صنف للفاتورة:
            </span>
            {parsedTotal > 0 && (
              <span className="text-[11px] font-bold text-[#146B50] bg-white px-2 py-0.5 rounded-md border border-[#D5E5DD]">
                سعر الحبة المستنتج:{' '}
                <strong>{derivedUnitPrice.toLocaleString(undefined, { maximumFractionDigits: 2 })} YER</strong>
              </span>
            )}
          </div>

          <div className="grid grid-cols-12 gap-1.5 sm:gap-2 items-end">
            {/* 1. First from Right: الإجمالي (Total Amount) */}
            <div className="col-span-4 sm:col-span-3">
              <label className="block text-[11px] font-bold text-gray-700 mb-0.5">
                القيمة الإجمالية <span className="text-[#BE3232]">*</span>
              </label>
              <input
                id="input-item-total"
                type="number"
                step="any"
                placeholder="0"
                value={itemTotal}
                onChange={e => setItemTotal(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddItem();
                  }
                }}
                className="w-full bg-white border border-[#C2D7CC] rounded-xl py-2 px-2 text-xs sm:text-sm font-black text-gray-900 focus:outline-hidden focus:border-[#146B50] font-mono shadow-2xs"
              />
            </div>

            {/* 2. Middle: العدد أو الكمية (Quantity) */}
            <div className="col-span-3 sm:col-span-2">
              <label className="block text-[11px] font-bold text-gray-700 mb-0.5">
                العدد / الكمية
              </label>
              <input
                id="input-item-qty"
                type="number"
                step="any"
                min="0.1"
                placeholder="1"
                value={itemQty}
                onChange={e => setItemQty(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddItem();
                  }
                }}
                className="w-full bg-white border border-[#C2D7CC] rounded-xl py-2 px-2 text-xs sm:text-sm font-bold text-gray-800 focus:outline-hidden focus:border-[#146B50] font-mono shadow-2xs text-center"
              />
            </div>

            {/* 3. Left: التفاصيل أو اسم الصنف (Details / Item Name) */}
            <div className="col-span-5 sm:col-span-5">
              <label className="block text-[11px] font-bold text-gray-700 mb-0.5">
                التفاصيل أو اسم الصنف <span className="text-[#BE3232]">*</span>
              </label>
              <input
                id="input-item-details"
                type="text"
                list="invoice-suggestions-list"
                placeholder="مثال: سكر، أرز، زيت..."
                value={itemDetails}
                onChange={e => setItemDetails(e.target.value)}
                onKeyDown={e => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddItem();
                  }
                }}
                className="w-full bg-white border border-[#C2D7CC] rounded-xl py-2 px-2.5 text-xs sm:text-sm font-medium text-gray-900 focus:outline-hidden focus:border-[#146B50] shadow-2xs"
              />
              <datalist id="invoice-suggestions-list">
                {availableSuggestions.map((sug, idx) => (
                  <option key={idx} value={sug} />
                ))}
              </datalist>
            </div>

            {/* 4. Action Button: زر الإضافة إلى الفاتورة */}
            <div className="col-span-12 sm:col-span-2">
              <button
                type="button"
                id="btn-add-item-to-invoice"
                onClick={() => handleAddItem()}
                className="w-full bg-[#146B50] hover:bg-[#0D4D3A] text-white font-bold py-2 px-2 rounded-xl text-xs flex items-center justify-center gap-1 shadow-2xs transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>إضافة</span>
              </button>
            </div>
          </div>

          {/* Quick Suggestion Chips for Details */}
          <div className="flex items-center gap-1 overflow-x-auto pt-1 pb-0.5 no-scrollbar text-[11px]">
            <span className="text-gray-500 shrink-0 font-medium flex items-center gap-0.5">
              <Sparkles className="w-3 h-3 text-[#146B50]" />
              اقتراحات:
            </span>
            {availableSuggestions.slice(0, 7).map((sug, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => setItemDetails(sug)}
                className="shrink-0 bg-white hover:bg-emerald-50 text-gray-700 hover:text-[#0D4D3A] border border-[#D5E5DD] hover:border-[#146B50] px-2 py-0.5 rounded-lg transition-colors cursor-pointer"
              >
                {sug}
              </button>
            ))}
          </div>
        </div>

        {/* Small Items Table (الجدولة الصغيرة) */}
        <div className="flex-1 overflow-y-auto min-h-[140px] max-h-[220px] border border-[#E1E8E4] rounded-xl bg-white shadow-2xs">
          {items.length === 0 ? (
            <div className="p-6 text-center text-gray-400 space-y-1">
              <p className="text-xs font-semibold text-gray-500">لا توجد أصناف مضافة حتى الآن</p>
              <p className="text-[11px] text-gray-400">
                أدخل القيمة الإجمالية، العدد، والتفاصيل في المربعات أعلاه ثم اضغط "إضافة"
              </p>
            </div>
          ) : (
            <table className="w-full text-right border-collapse text-xs">
              <thead className="bg-gray-50 text-gray-600 font-bold sticky top-0 border-b border-gray-100">
                <tr>
                  <th className="py-2 px-2.5 text-right">الصنف / التفاصيل</th>
                  <th className="py-2 px-2 text-center">الكمية</th>
                  <th className="py-2 px-2 text-center">سعر الوحدة (مستنتج)</th>
                  <th className="py-2 px-2 text-left">الإجمالي</th>
                  <th className="py-2 px-2 text-center w-8">حذف</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {items.map((item, index) => (
                  <tr key={item.id} className="hover:bg-gray-50/70">
                    <td className="py-2 px-2.5 font-bold text-gray-900">
                      <span className="text-gray-400 ml-1 font-mono text-[10px]">{index + 1}.</span>
                      {item.itemName}
                    </td>
                    <td className="py-2 px-2 text-center font-bold text-gray-700 font-mono">
                      {item.quantity}
                    </td>
                    <td className="py-2 px-2 text-center text-gray-500 font-mono">
                      {item.unitPrice.toLocaleString(undefined, { maximumFractionDigits: 2 })}
                    </td>
                    <td className="py-2 px-2 text-left font-black text-[#146B50] font-mono">
                      {formatMoney(item.total)} YER
                    </td>
                    <td className="py-2 px-2 text-center">
                      <button
                        type="button"
                        onClick={() => handleRemoveItem(item.id)}
                        className="p-1 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="حذف هذا الصنف"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Totals, Payment & Customer Ledger Options */}
        <div className="bg-gray-50 border border-gray-200 rounded-2xl p-3 shrink-0 space-y-2.5">
          <div className="grid grid-cols-3 gap-2 text-center">
            {/* Total of invoice */}
            <div className="bg-white p-2 rounded-xl border border-gray-200">
              <span className="text-[10px] text-gray-500 block">إجمالي الفاتورة:</span>
              <strong className="text-sm sm:text-base font-black text-gray-900 font-mono">
                {formatMoney(invoiceTotal)} YER
              </strong>
            </div>

            {/* Paid Input */}
            <div className="bg-white p-2 rounded-xl border border-gray-200">
              <span className="text-[10px] text-gray-500 block">المبلغ المدفوع (كاش):</span>
              <input
                id="input-invoice-paid-box"
                type="number"
                step="any"
                placeholder="0"
                value={paid}
                onChange={e => setPaid(e.target.value)}
                className="w-full text-center text-xs sm:text-sm font-black text-[#146B50] font-mono bg-emerald-50/50 rounded-lg py-0.5 border border-emerald-200 focus:outline-hidden"
              />
            </div>

            {/* Remainder */}
            <div className="bg-white p-2 rounded-xl border border-gray-200">
              <span className="text-[10px] text-gray-500 block">المتبقي (آجل):</span>
              <strong
                className={`text-sm sm:text-base font-black font-mono ${
                  remainingDebt > 0 ? 'text-[#BE3232]' : 'text-gray-400'
                }`}
              >
                {formatMoney(remainingDebt)} YER
              </strong>
            </div>
          </div>

          {/* Option to record remainder in customer's debt */}
          {isSale && selectedCustomerId && (
            <label className="flex items-center gap-2 text-xs font-semibold text-gray-700 cursor-pointer pt-0.5">
              <input
                type="checkbox"
                checked={recordInCustomerLedger}
                onChange={e => setRecordInCustomerLedger(e.target.checked)}
                className="w-4 h-4 rounded text-[#146B50] focus:ring-[#146B50] accent-[#146B50]"
              />
              <span>
                تسجيل المتبقي ({formatMoney(remainingDebt)} YER) تلقائياً كدين على العميل في دفتر الحسابات
              </span>
            </label>
          )}

          {/* General note */}
          <input
            id="input-invoice-general-note"
            type="text"
            placeholder="ملاحظات عامة على الفاتورة (اختياري)..."
            value={note}
            onChange={e => setNote(e.target.value)}
            className="w-full bg-white border border-gray-200 rounded-xl py-1.5 px-3 text-xs focus:outline-hidden focus:border-[#146B50]"
          />
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-between pt-1 border-t border-gray-100 shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100 transition-colors"
          >
            إلغاء
          </button>

          <button
            id="btn-save-final-invoice"
            type="button"
            onClick={handleSubmitInvoice}
            className={`px-6 py-2.5 rounded-xl text-xs sm:text-sm font-black text-white shadow-md flex items-center gap-1.5 transition-all ${
              isSale
                ? 'bg-[#146B50] hover:bg-[#0D4D3A]'
                : 'bg-indigo-700 hover:bg-indigo-800'
            }`}
          >
            <span>حفظ الفاتورة ({formatMoney(invoiceTotal)} YER)</span>
          </button>
        </div>

      </div>
    </div>
  );
};
