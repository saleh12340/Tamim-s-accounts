export type TransactionType = 'DEBIT' | 'CREDIT';
export type InvoiceType = 'SALE' | 'PURCHASE';

export interface Customer {
  id: number;
  name: string;
  phone: string;
  balance: number; // positive = عليه (owes store), negative = له (store owes customer)
  groupId?: number | null;
  notes: string;
}

export interface Tx {
  id: number;
  customerId: number;
  type: TransactionType;
  amount: number;
  currency: string;
  date: string;
  note: string;
  shareRef?: string;
}

export interface Product {
  id: number;
  name: string;
  barcode: string;
  buyPrice: number;
  sellPrice: number;
  stock: number;
  minStock: number;
  notes: string;
}

export interface Expense {
  id: number;
  title: string;
  amount: number;
  currency: string;
  date: string;
  note: string;
}

export interface Invoice {
  id: number;
  type: InvoiceType;
  customerId?: number | null;
  supplierId?: number | null;
  customerOrSupplierName?: string;
  date: string;
  total: number;
  paid: number;
  note: string;
  items?: InvoiceItem[];
}

export interface InvoiceItem {
  id: number;
  invoiceId: number;
  itemName: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface Supplier {
  id: number;
  name: string;
  phone: string;
  balance: number;
  notes: string;
}

export interface AppSettings {
  storeName: string;
  phone: string;
}

export type StoreSettings = AppSettings;

export interface TableInspection {
  name: string;
  columns: string[];
  rowCount: number;
}

export interface IntegrityReport {
  validSqlite: boolean;
  integrityOk: boolean;
  tables: TableInspection[];
  warnings: string[];
}

export interface DatabaseState {
  customers: Customer[];
  transactions: Tx[];
  products: Product[];
  expenses: Expense[];
  invoices: Invoice[];
  invoiceItems: InvoiceItem[];
  suppliers: Supplier[];
  settings: AppSettings;
}
