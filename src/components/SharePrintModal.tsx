import React, { useState } from 'react';
import { Customer, Tx, Invoice, StoreSettings } from '../types';
import { formatMoney } from '../utils/formatters';
import {
  formatTransactionReceiptText,
  formatInvoiceReceiptText,
  formatStatementReceiptText,
  openWhatsAppWithText,
  shareViaNativeOrClipboard,
  bluetoothPrinter,
} from '../services/shareAndPrint';
import {
  X,
  Share2,
  Printer,
  Bluetooth,
  MessageCircle,
  Copy,
  Check,
  CheckCircle2,
  FileText,
  AlertCircle,
  Loader2,
} from 'lucide-react';

export type SharePrintTarget =
  | { type: 'TRANSACTION'; transaction: Tx; customer: Customer | null }
  | { type: 'INVOICE'; invoice: Invoice }
  | { type: 'STATEMENT'; customer: Customer; transactions: Tx[] };

interface SharePrintModalProps {
  isOpen: boolean;
  target: SharePrintTarget | null;
  storeSettings: StoreSettings;
  onClose: () => void;
}

export const SharePrintModal: React.FC<SharePrintModalProps> = ({
  isOpen,
  target,
  storeSettings,
  onClose,
}) => {
  const [copied, setCopied] = useState(false);
  const [bluetoothLoading, setBluetoothLoading] = useState(false);
  const [bluetoothStatus, setBluetoothStatus] = useState<string | null>(null);
  const [paperWidth, setPaperWidth] = useState<'80' | '58'>('80');

  if (!isOpen || !target) return null;

  const storeName = storeSettings.storeName || 'بقالة العزي';
  const storePhone = storeSettings.phone || '';

  // Generate WhatsApp / Text representation
  let shareText = '';
  let customerPhone: string | undefined;
  let title = '';

  if (target.type === 'TRANSACTION') {
    title = target.transaction.type === 'DEBIT' ? 'سند قيد (دين)' : 'سند قبض (سداد)';
    customerPhone = target.customer?.phone;
    shareText = formatTransactionReceiptText(
      storeName,
      storePhone,
      target.customer,
      target.transaction,
      target.customer?.balance
    );
  } else if (target.type === 'INVOICE') {
    title = target.invoice.type === 'SALE' ? 'فاتورة مبيعات' : 'فاتورة مشتريات';
    shareText = formatInvoiceReceiptText(storeName, storePhone, target.invoice);
  } else if (target.type === 'STATEMENT') {
    title = `كشف حساب - ${target.customer.name}`;
    customerPhone = target.customer.phone;
    shareText = formatStatementReceiptText(
      storeName,
      storePhone,
      target.customer,
      target.transactions
    );
  }

  const handleWhatsApp = () => {
    openWhatsAppWithText(shareText, customerPhone);
  };

  const handleCopy = async () => {
    const ok = await shareViaNativeOrClipboard(title, shareText);
    if (ok) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const handleNativeShare = () => {
    shareViaNativeOrClipboard(title, shareText);
  };

  const handleStandardPrint = () => {
    window.print();
  };

  const handleBluetoothPrint = async () => {
    try {
      setBluetoothLoading(true);
      setBluetoothStatus('جارٍ الاتصال بالطابعة...');
      await bluetoothPrinter.printEscPos(shareText);
      setBluetoothStatus('تم إرسال أمر الطباعة بنجاح للطابعة الحرارية!');
    } catch (err: any) {
      setBluetoothStatus(
        err.message || 'تعذر الاتصال بالبلوتوث. تم تفعيل خيار الطباعة القياسي كبديل.'
      );
      // Seamlessly fallback to window.print
      setTimeout(() => {
        window.print();
      }, 800);
    } finally {
      setBluetoothLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-lg p-4 sm:p-5 shadow-2xl space-y-4 max-h-[92vh] flex flex-col animate-in fade-in zoom-in-95 duration-150">
        {/* Header Bar */}
        <div className="flex items-center justify-between border-b border-gray-100 pb-3 no-print">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-emerald-50 text-[#146B50] flex items-center justify-center">
              <Share2 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-black text-sm sm:text-base text-gray-900 leading-tight">
                مشاركة وطباعة: {title}
              </h3>
              <p className="text-[11px] text-gray-500">
                واتساب، مستند PDF، وطباعة حرارية بلوتوث
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-700 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Quick Action Buttons */}
        <div className="grid grid-cols-3 gap-2 no-print">
          {/* WhatsApp Share */}
          <button
            id="btn-modal-share-whatsapp"
            onClick={handleWhatsApp}
            className="flex flex-col items-center justify-center gap-1 bg-[#25D366] hover:bg-[#1EBE5D] text-white p-2.5 rounded-2xl text-xs font-bold shadow-2xs transition-all active:scale-95"
          >
            <MessageCircle className="w-5 h-5" />
            <span>واتساب مباشر</span>
          </button>

          {/* Standard Print / PDF */}
          <button
            id="btn-modal-print-pdf"
            onClick={handleStandardPrint}
            className="flex flex-col items-center justify-center gap-1 bg-[#146B50] hover:bg-[#0D4D3A] text-white p-2.5 rounded-2xl text-xs font-bold shadow-2xs transition-all active:scale-95"
          >
            <Printer className="w-5 h-5" />
            <span>طباعة / PDF</span>
          </button>

          {/* Bluetooth Print */}
          <button
            id="btn-modal-print-bluetooth"
            onClick={handleBluetoothPrint}
            disabled={bluetoothLoading}
            className="flex flex-col items-center justify-center gap-1 bg-[#4664B4] hover:bg-blue-700 text-white p-2.5 rounded-2xl text-xs font-bold shadow-2xs transition-all active:scale-95 disabled:opacity-60"
          >
            {bluetoothLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Bluetooth className="w-5 h-5" />
            )}
            <span>طباعة بلوتوث</span>
          </button>
        </div>

        {/* Extra actions & status */}
        <div className="flex items-center justify-between gap-2 no-print pt-1">
          <div className="flex items-center gap-2">
            <button
              onClick={handleCopy}
              className="flex items-center gap-1.5 text-xs font-bold text-gray-700 bg-gray-100 hover:bg-gray-200 px-3 py-1.5 rounded-xl transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-[#146B50]" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'تم النسخ' : 'نسخ النص'}</span>
            </button>
            <button
              onClick={handleNativeShare}
              className="flex items-center gap-1 text-xs font-bold text-[#146B50] hover:bg-emerald-50 px-2 py-1.5 rounded-xl transition-colors"
            >
              <Share2 className="w-3.5 h-3.5" />
              <span>مشاركة بالجهاز</span>
            </button>
          </div>

          <div className="flex items-center gap-1 bg-gray-100 p-1 rounded-xl text-[11px] font-bold text-gray-600">
            <span>عرض الورق:</span>
            <button
              onClick={() => setPaperWidth('80')}
              className={`px-2 py-0.5 rounded-lg transition-colors ${
                paperWidth === '80' ? 'bg-white text-[#146B50] shadow-2xs' : 'text-gray-500'
              }`}
            >
              80mm
            </button>
            <button
              onClick={() => setPaperWidth('58')}
              className={`px-2 py-0.5 rounded-lg transition-colors ${
                paperWidth === '58' ? 'bg-white text-[#146B50] shadow-2xs' : 'text-gray-500'
              }`}
            >
              58mm
            </button>
          </div>
        </div>

        {bluetoothStatus && (
          <div className="no-print bg-blue-50 border border-blue-200 text-blue-900 rounded-xl p-2.5 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 text-blue-600" />
            <span>{bluetoothStatus}</span>
          </div>
        )}

        {/* Live Printable Receipt Preview (Target for window.print & review) */}
        <div
          id="printable-area"
          className={`flex-1 overflow-y-auto p-4 sm:p-5 bg-white border border-dashed border-gray-300 rounded-2xl shadow-inner font-mono text-gray-900 space-y-3 leading-relaxed ${
            paperWidth === '58' ? 'max-w-[280px] mx-auto text-[11px]' : 'text-xs'
          }`}
        >
          {/* Store Header */}
          <div className="text-center border-b-2 border-dashed border-gray-800 pb-2 space-y-1">
            <div className="text-base font-black tracking-wide font-sans">{storeName}</div>
            {storePhone && <div className="text-gray-600">هاتف: {storePhone}</div>}
            <div className="font-bold text-[11px] pt-1">
              *** {title} ***
            </div>
          </div>

          {/* Content Body Based on Target */}
          {target.type === 'TRANSACTION' && (
            <div className="space-y-2 py-1">
              <div className="flex justify-between">
                <span className="text-gray-600">رقم السند:</span>
                <span className="font-bold">#{target.transaction.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">العميل:</span>
                <span className="font-bold">{target.customer ? target.customer.name : 'عميل نقدي'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">التاريخ:</span>
                <span>{target.transaction.date}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">نوع القيد:</span>
                <span className={`font-black ${target.transaction.type === 'DEBIT' ? 'text-red-700' : 'text-emerald-700'}`}>
                  {target.transaction.type === 'DEBIT' ? 'عليه (دين)' : 'له (سداد)'}
                </span>
              </div>
              <div className="flex justify-between border-t border-dashed border-gray-300 pt-1 text-sm font-black">
                <span>المبلغ:</span>
                <span>{formatMoney(target.transaction.amount)} {target.transaction.currency}</span>
              </div>
              {target.transaction.note && (
                <div className="pt-1 text-gray-700">
                  <span className="text-gray-500">البيان: </span>
                  <span>{target.transaction.note}</span>
                </div>
              )}
              {target.customer && (
                <div className="border-t-2 border-dashed border-gray-800 pt-2 flex justify-between font-bold">
                  <span>الرصيد الحالي:</span>
                  <span>
                    {formatMoney(Math.abs(target.customer.balance))} YER ({target.customer.balance >= 0 ? 'عليه' : 'له'})
                  </span>
                </div>
              )}
            </div>
          )}

          {target.type === 'INVOICE' && (
            <div className="space-y-2 py-1">
              <div className="flex justify-between">
                <span className="text-gray-600">رقم الفاتورة:</span>
                <span className="font-bold">#{target.invoice.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">الطرف:</span>
                <span className="font-bold">{target.invoice.customerOrSupplierName || 'عميل نقدي'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">التاريخ:</span>
                <span>{target.invoice.date}</span>
              </div>

              {/* Items Table */}
              <div className="border-t border-b border-dashed border-gray-800 py-1.5 space-y-1">
                <div className="flex justify-between text-[11px] font-bold text-gray-700 pb-0.5 border-b border-gray-200">
                  <span>الصنف</span>
                  <div className="flex gap-2">
                    <span>الكمية</span>
                    <span>الإجمالي</span>
                  </div>
                </div>
                {target.invoice.items && target.invoice.items.length > 0 ? (
                  target.invoice.items.map((it, idx) => (
                    <div key={idx} className="flex justify-between items-center text-[11px]">
                      <span className="truncate max-w-[140px]">{it.itemName}</span>
                      <div className="flex gap-3 font-mono">
                        <span className="text-gray-500">{it.quantity}x</span>
                        <span className="font-bold">{formatMoney(it.total)}</span>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-[11px] text-gray-600">
                    {target.invoice.note || 'مشتريات بقالة متنوعة'}
                  </div>
                )}
              </div>

              <div className="flex justify-between font-bold pt-1">
                <span>الإجمالي الكلي:</span>
                <span className="font-black text-sm">{formatMoney(target.invoice.total)} YER</span>
              </div>
              <div className="flex justify-between text-emerald-700 font-bold">
                <span>المدفوع نقداً:</span>
                <span>{formatMoney(target.invoice.paid)} YER</span>
              </div>
              {target.invoice.total - target.invoice.paid > 0 && (
                <div className="flex justify-between text-red-700 font-black border-t border-dashed border-gray-300 pt-1">
                  <span>المتبقي (آجل):</span>
                  <span>{formatMoney(target.invoice.total - target.invoice.paid)} YER</span>
                </div>
              )}
              {target.invoice.note && (
                <div className="text-[11px] text-gray-600 pt-1">
                  <span>ملاحظات: </span>
                  <span>{target.invoice.note}</span>
                </div>
              )}
            </div>
          )}

          {target.type === 'STATEMENT' && (
            <div className="space-y-2 py-1">
              <div className="flex justify-between">
                <span className="text-gray-600">العميل:</span>
                <span className="font-bold">{target.customer.name}</span>
              </div>
              {target.customer.phone && (
                <div className="flex justify-between">
                  <span className="text-gray-600">الهاتف:</span>
                  <span>{target.customer.phone}</span>
                </div>
              )}

              <div className="border-t border-b border-dashed border-gray-800 py-1.5 space-y-1">
                <div className="flex justify-between text-[11px] font-bold text-gray-700 pb-0.5 border-b border-gray-200">
                  <span>العملية / التاريخ</span>
                  <span>المبلغ</span>
                </div>
                {target.transactions.length === 0 ? (
                  <div className="text-center text-gray-400 py-2">لا توجد عمليات مسجلة</div>
                ) : (
                  target.transactions.map(tx => (
                    <div key={tx.id} className="text-[11px] border-b border-gray-100 pb-1">
                      <div className="flex justify-between">
                        <span className={tx.type === 'DEBIT' ? 'text-red-700 font-bold' : 'text-emerald-700 font-bold'}>
                          {tx.type === 'DEBIT' ? 'عليه: ' : 'له: '}
                          {tx.note || 'عملية'}
                        </span>
                        <span className="font-mono font-bold">
                          {formatMoney(tx.amount)} {tx.currency}
                        </span>
                      </div>
                      <div className="text-[10px] text-gray-400 font-mono">{tx.date}</div>
                    </div>
                  ))
                )}
              </div>

              <div className="border-t-2 border-dashed border-gray-900 pt-2 flex justify-between font-black text-sm">
                <span>الرصيد النهائي:</span>
                <span className={target.customer.balance > 0 ? 'text-red-700' : 'text-emerald-700'}>
                  {formatMoney(Math.abs(target.customer.balance))} YER (
                  {target.customer.balance >= 0 ? 'عليه' : 'له'})
                </span>
              </div>
            </div>
          )}

          {/* Receipt Footer */}
          <div className="text-center border-t border-dashed border-gray-800 pt-2 text-[10px] text-gray-500 space-y-0.5">
            <div>نسعد دائماً بخدمتكم</div>
            <div>بقالة العزي - نظام الحسابات</div>
          </div>
        </div>
      </div>
    </div>
  );
};
