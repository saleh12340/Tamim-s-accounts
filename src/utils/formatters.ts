import { Customer, Tx } from '../types';

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
