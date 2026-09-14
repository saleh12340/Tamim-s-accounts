import { Customer, Tx } from '../types';

export function parseDateToTimestamp(dStr?: string): number {
  if (!dStr) return 0;
  const s = dStr.trim();
  // Check DD-MM-YYYY or DD/MM/YYYY (with optional time)
  const dmyMatch = s.match(/^(\d{1,2})[-/](\d{1,2})[-/](\d{4})(?:\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?$/);
  if (dmyMatch) {
    const day = parseInt(dmyMatch[1], 10);
    const month = parseInt(dmyMatch[2], 10) - 1;
    const year = parseInt(dmyMatch[3], 10);
    const hour = dmyMatch[4] ? parseInt(dmyMatch[4], 10) : 0;
    const min = dmyMatch[5] ? parseInt(dmyMatch[5], 10) : 0;
    const sec = dmyMatch[6] ? parseInt(dmyMatch[6], 10) : 0;
    return new Date(year, month, day, hour, min, sec).getTime();
  }

  // Check YYYY-MM-DD or YYYY/MM/DD
  const ymdMatch = s.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})(?:\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?)?$/);
  if (ymdMatch) {
    const year = parseInt(ymdMatch[1], 10);
    const month = parseInt(ymdMatch[2], 10) - 1;
    const day = parseInt(ymdMatch[3], 10);
    const hour = ymdMatch[4] ? parseInt(ymdMatch[4], 10) : 0;
    const min = ymdMatch[5] ? parseInt(ymdMatch[5], 10) : 0;
    const sec = ymdMatch[6] ? parseInt(ymdMatch[6], 10) : 0;
    return new Date(year, month, day, hour, min, sec).getTime();
  }

  const parsed = Date.parse(s);
  return isNaN(parsed) ? 0 : parsed;
}

/**
 * Sorts items by date descending (newest first). If same date, by ID descending.
 */
export function compareTxNewestFirst<T extends { date?: string; id: number }>(a: T, b: T): number {
  const timeA = parseDateToTimestamp(a.date);
  const timeB = parseDateToTimestamp(b.date);
  if (timeB !== timeA) {
    return timeB - timeA;
  }
  return b.id - a.id;
}

/**
 * Sorts items by date ascending (oldest first). If same date, by ID ascending.
 */
export function compareTxOldestFirst<T extends { date?: string; id: number }>(a: T, b: T): number {
  const timeA = parseDateToTimestamp(a.date);
  const timeB = parseDateToTimestamp(b.date);
  if (timeA !== timeB) {
    return timeA - timeB;
  }
  return a.id - b.id;
}

export function formatMoney(amount: number): string {
  return new Intl.NumberFormat('en-US', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0,
  }).format(amount);
}

export function generateStatementText(storeName: string, customer: Customer, transactions: Tx[]): string {
  const lines: string[] = [];
  lines.push(storeName);
  lines.push(`حساب: ${customer.name}`);
  lines.push(`الهاتف: ${customer.phone || '—'}`);
  lines.push('');
  lines.push('--- كشف الحساب ---');

  if (transactions.length === 0) {
    lines.push('لا توجد عمليات مسجلة لهذا الحساب.');
  } else {
    let running = 0;
    transactions.forEach(tx => {
      running += tx.type === 'DEBIT' ? tx.amount : -tx.amount;
      const typeText = tx.type === 'DEBIT' ? 'عليه' : 'له';
      lines.push(`${tx.date} | ${typeText} | ${formatMoney(tx.amount)} ${tx.currency} | ${tx.note || 'بدون بيان'}`);
    });
  }

  lines.push('');
  const status = customer.balance >= 0 ? 'عليه' : 'له';
  lines.push(`الرصيد النهائي: ${formatMoney(Math.abs(customer.balance))} YER (${status})`);
  return lines.join('\n');
}

export function formatArabicDate(dStr?: string): string {
  if (!dStr) return '';
  return dStr;
}
