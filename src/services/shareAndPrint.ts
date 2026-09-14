import { Customer, Tx, Invoice, StoreSettings } from '../types';
import { formatMoney } from '../utils/formatters';

export function cleanPhoneNumber(rawPhone?: string): string {
  if (!rawPhone) return '';
  let cleaned = rawPhone.replace(/\D/g, '');
  // If Yemen local phone starting with 7 (9 digits)
  if (cleaned.startsWith('7') && cleaned.length === 9) {
    return '967' + cleaned;
  }
  // If starts with 07 (10 digits)
  if (cleaned.startsWith('07') && cleaned.length === 10) {
    return '967' + cleaned.substring(1);
  }
  return cleaned;
}

export function formatTransactionReceiptText(
  storeName: string,
  storePhone: string,
  customer: Customer | null,
  tx: Tx,
  balanceAfter?: number
): string {
  const isDebit = tx.type === 'DEBIT';
  const typeLabel = isDebit ? 'سند قيد (دين / عليه)' : 'سند قبض (سداد / له)';
  const party = customer ? customer.name : 'عميل عام';

  const lines: string[] = [
    `🧾 *${storeName}*`,
    storePhone ? `📞 هاتف: ${storePhone}` : '',
    '--------------------------------',
    `📄 *${typeLabel}* #${tx.id}`,
    `👤 العميل: ${party}`,
    `📅 التاريخ: ${tx.date}`,
    `💰 المبلغ: *${formatMoney(tx.amount)} ${tx.currency}*`,
    tx.note ? `📝 البيان: ${tx.note}` : '📝 البيان: بدون بيان',
  ];

  if (customer) {
    const bal = balanceAfter !== undefined ? balanceAfter : customer.balance;
    const status = bal > 0 ? 'عليه' : bal < 0 ? 'له' : 'خالص';
    lines.push('--------------------------------');
    lines.push(`⚖️ الرصيد الحالي: *${formatMoney(Math.abs(bal))} YER (${status})*`);
  }

  lines.push('--------------------------------');
  lines.push('🙏 شكراً لتعاملكم معنا!');

  return lines.filter(Boolean).join('\n');
}

export function formatInvoiceReceiptText(
  storeName: string,
  storePhone: string,
  invoice: Invoice
): string {
  const isSale = invoice.type === 'SALE';
  const typeLabel = isSale ? 'فاتورة مبيعات' : 'فاتورة مشتريات';
  const party = invoice.customerOrSupplierName || (isSale ? 'عميل عام نقدي' : 'مورد نقدي');
  const remaining = Math.max(0, invoice.total - invoice.paid);

  const lines: string[] = [
    `🧾 *${storeName}*`,
    storePhone ? `📞 هاتف: ${storePhone}` : '',
    '--------------------------------',
    `📑 *${typeLabel}* #${invoice.id}`,
    `👤 الطرف: ${party}`,
    `📅 التاريخ: ${invoice.date}`,
    '--------------------------------',
    '📋 *تفاصيل الأصناف:*',
  ];

  if (invoice.items && invoice.items.length > 0) {
    invoice.items.forEach((item, idx) => {
      lines.push(
        `${idx + 1}. ${item.itemName} | ${item.quantity} × ${formatMoney(item.unitPrice)} = *${formatMoney(item.total)} YER*`
      );
    });
  } else if (invoice.note) {
    lines.push(`• ${invoice.note} = *${formatMoney(invoice.total)} YER*`);
  } else {
    lines.push(`• إجمالي مشتريات = *${formatMoney(invoice.total)} YER*`);
  }

  lines.push('--------------------------------');
  lines.push(`💵 الإجمالي: *${formatMoney(invoice.total)} YER*`);
  lines.push(`🟢 المدفوع: *${formatMoney(invoice.paid)} YER*`);
  if (remaining > 0) {
    lines.push(`🔴 المتبقي: *${formatMoney(remaining)} YER*`);
  }
  if (invoice.note && invoice.items && invoice.items.length > 0) {
    lines.push(`📝 ملاحظة: ${invoice.note}`);
  }

  lines.push('--------------------------------');
  lines.push('🙏 نسعد دائماً بخدمتكم!');

  return lines.filter(Boolean).join('\n');
}

export function formatStatementReceiptText(
  storeName: string,
  storePhone: string,
  customer: Customer,
  transactions: Tx[]
): string {
  const lines: string[] = [
    `🧾 *${storeName}*`,
    storePhone ? `📞 هاتف: ${storePhone}` : '',
    '--------------------------------',
    `📊 *كشف حساب تفصيلي*`,
    `👤 العميل: ${customer.name}`,
    customer.phone ? `📱 هاتف: ${customer.phone}` : '',
    `📅 تاريخ الكشف: ${new Date().toLocaleDateString('ar-YE', { dateStyle: 'medium' })}`,
    '--------------------------------',
    '📜 *سجل العمليات:*',
  ];

  if (transactions.length === 0) {
    lines.push('لا توجد عمليات مسجلة لهذا الحساب.');
  } else {
    let running = 0;
    // Compute in chronological order
    const sorted = [...transactions].sort((a, b) => a.id - b.id);
    sorted.forEach((tx, idx) => {
      running += tx.type === 'DEBIT' ? tx.amount : -tx.amount;
      const typeLabel = tx.type === 'DEBIT' ? 'عليه (دين)' : 'له (سداد)';
      lines.push(
        `${idx + 1}. [${tx.date}] ${typeLabel}: *${formatMoney(tx.amount)} ${tx.currency}*`
      );
      if (tx.note) {
        lines.push(`   بيان: ${tx.note}`);
      }
    });
  }

  const status = customer.balance > 0 ? 'عليه (مطلوب منه)' : customer.balance < 0 ? 'له (دائن)' : 'خالص (0)';
  lines.push('--------------------------------');
  lines.push(`⚖️ *الرصيد النهائي: ${formatMoney(Math.abs(customer.balance))} YER* (${status})`);
  lines.push('--------------------------------');
  lines.push('بقالة العزي - نسعد بخدمتكم');

  return lines.filter(Boolean).join('\n');
}

export function openWhatsAppWithText(text: string, phone?: string) {
  const cleaned = cleanPhoneNumber(phone);
  let url = `https://api.whatsapp.com/send?text=${encodeURIComponent(text)}`;
  if (cleaned) {
    url = `https://api.whatsapp.com/send?phone=${cleaned}&text=${encodeURIComponent(text)}`;
  }
  window.open(url, '_blank');
}

export async function shareViaNativeOrClipboard(title: string, text: string): Promise<boolean> {
  if (navigator.share) {
    try {
      await navigator.share({ title, text });
      return true;
    } catch {
      // User cancelled or share failed, fallback to clipboard
    }
  }
  if (navigator.clipboard) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch {
      return false;
    }
  }
  return false;
}

// Web Bluetooth Thermal Printer Helper
class BluetoothPrinterService {
  private device: any = null;
  private characteristic: any = null;

  public async connect(): Promise<string> {
    if (!('bluetooth' in navigator)) {
      throw new Error('Web Bluetooth غير مدعوم في متصفحك. يمكنك استخدام الطباعة الحرارية المباشرة عبر أمر الطباعة.');
    }

    this.device = await (navigator as any).bluetooth.requestDevice({
      acceptAllDevices: true,
      optionalServices: [
        '000018f0-0000-1000-8000-00805f9b34fb',
        'e7810a71-73ae-499d-8c15-faa9aef0c3f2',
        '49535343-fe7d-4ae5-8fa9-9fafd205e455',
        '0000ffe0-0000-1000-8000-00805f9b34fb',
      ],
    });

    const server = await this.device.gatt.connect();
    const services = await server.getPrimaryServices();
    
    for (const service of services) {
      const characteristics = await service.getCharacteristics();
      for (const char of characteristics) {
        if (char.properties.write || char.properties.writeWithoutResponse) {
          this.characteristic = char;
          return this.device.name || 'طابعة حرارية بلوتوث';
        }
      }
    }

    throw new Error('تم الاتصال بالجهاز لكن لم يتم العثور على منفذ طباعة متوافق.');
  }

  public async printEscPos(text: string): Promise<void> {
    if (!this.characteristic) {
      await this.connect();
    }

    const encoder = new TextEncoder();
    const initCmd = new Uint8Array([0x1b, 0x40]); // ESC @ (Initialize)
    const cutCmd = new Uint8Array([0x1d, 0x56, 0x41, 0x10]); // GS V A (Cut)
    const textBytes = encoder.encode(text + '\n\n\n');

    const payload = new Uint8Array(initCmd.length + textBytes.length + cutCmd.length);
    payload.set(initCmd, 0);
    payload.set(textBytes, initCmd.length);
    payload.set(cutCmd, initCmd.length + textBytes.length);

    // Send in chunks of 512 bytes if needed
    const CHUNK_SIZE = 512;
    for (let i = 0; i < payload.length; i += CHUNK_SIZE) {
      const chunk = payload.slice(i, i + CHUNK_SIZE);
      if (this.characteristic.writeValueWithoutResponse) {
        await this.characteristic.writeValueWithoutResponse(chunk);
      } else {
        await this.characteristic.writeValue(chunk);
      }
    }
  }

  public isConnected(): boolean {
    return !!(this.device && this.device.gatt?.connected && this.characteristic);
  }

  public getConnectedDeviceName(): string | null {
    return this.device?.name || null;
  }
}

export const bluetoothPrinter = new BluetoothPrinterService();
