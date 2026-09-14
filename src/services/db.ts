import { Customer, Tx, Product, Expense, Invoice, InvoiceItem, AppSettings, DatabaseState, IntegrityReport } from '../types';
import initSqlJs, { Database as SqlJsDatabase } from 'sql.js';
import { compareTxNewestFirst, parseDateToTimestamp } from '../utils/formatters';

const STORAGE_KEY = 'tamim_accounts_db_v1';

export const DEFAULT_SETTINGS: AppSettings = {
  storeName: 'بقالة العزي للمواد الغذائية',
  phone: '776425052',
};

export const INITIAL_STATE: DatabaseState = {
  customers: [
    { id: 1, name: 'محمد صالح العزي', phone: '771234567', balance: 4500, notes: 'عميل دائم' },
    { id: 2, name: 'أحمد عبده الحكيمي', phone: '772345678', balance: 12000, notes: 'حساب شهري' },
    { id: 3, name: 'خالد عبدالله مصلح', phone: '773456789', balance: -2000, notes: 'له رصيد فائض' },
    { id: 4, name: 'يحيى مسعد القاضي', phone: '774567890', balance: 8500, notes: '' },
    { id: 5, name: 'فؤاد سالم الريمي', phone: '775678901', balance: 0, notes: 'مسدد بالكامل' }
  ],
  transactions: [
    { id: 1, customerId: 1, type: 'DEBIT', amount: 5000, currency: 'YER', date: '2026-09-10 10:30', note: 'شراء مواد غذائية وسكر' },
    { id: 2, customerId: 1, type: 'CREDIT', amount: 500, currency: 'YER', date: '2026-09-11 16:15', note: 'دفعة سداد نقدية' },
    { id: 3, customerId: 2, type: 'DEBIT', amount: 15000, currency: 'YER', date: '2026-09-12 09:00', note: 'طلبية بقالة وأجبان' },
    { id: 4, customerId: 2, type: 'CREDIT', amount: 3000, currency: 'YER', date: '2026-09-13 18:20', note: 'سداد جزئي' },
    { id: 5, customerId: 3, type: 'CREDIT', amount: 2000, currency: 'YER', date: '2026-09-12 11:45', note: 'إيداع تحت الحساب' },
    { id: 6, customerId: 4, type: 'DEBIT', amount: 8500, currency: 'YER', date: '2026-09-13 14:00', note: 'أرز وزيت ودقيق' }
  ],
  products: [
    { id: 1, name: 'أرز الشعلان 10 كجم', barcode: '6281001001', buyPrice: 12500, sellPrice: 14000, stock: 15, minStock: 5, notes: '' },
    { id: 2, name: 'سكر السعيد 5 كجم', barcode: '6281001002', buyPrice: 4200, sellPrice: 4800, stock: 24, minStock: 8, notes: '' },
    { id: 3, name: 'زيت طبخ صافي 4 لتر', barcode: '6281001003', buyPrice: 6800, sellPrice: 7500, stock: 3, minStock: 6, notes: 'طلب دفعة جديدة' },
    { id: 4, name: 'حليب الممتاز مجفف 900 جم', barcode: '6281001004', buyPrice: 3100, sellPrice: 3600, stock: 18, minStock: 5, notes: '' },
    { id: 5, name: 'شاي الكبوس أحمر 250 جم', barcode: '6281001005', buyPrice: 1100, sellPrice: 1300, stock: 2, minStock: 10, notes: 'المخزون منخفض' }
  ],
  expenses: [
    { id: 1, title: 'فاتورة كهرباء البقالة', amount: 6500, currency: 'YER', date: '2026-09-08 19:30', note: 'شهر سبتمبر' },
    { id: 2, title: 'أكياس بلاستيك وتغليف', amount: 2500, currency: 'YER', date: '2026-09-10 12:00', note: 'شراء من الجملة' },
    { id: 3, title: 'صيانة مكيف وتبريد', amount: 4000, currency: 'YER', date: '2026-09-12 15:40', note: 'صيانة ثلاجة الألبان' }
  ],
  invoices: [
    { id: 1, type: 'SALE', customerId: 2, customerOrSupplierName: 'أحمد عبده الحكيمي', date: '2026-09-12 09:00', total: 15000, paid: 3000, note: 'فاتورة مبيعات مؤجلة' },
    { id: 2, type: 'PURCHASE', supplierId: null, customerOrSupplierName: 'شركة السعيد للتوزيع', date: '2026-09-09 11:20', total: 45000, paid: 45000, note: 'شراء زيت وسكر نقداً' }
  ],
  invoiceItems: [],
  suppliers: [],
  settings: DEFAULT_SETTINGS,
};

// Singleton storage manager
class DatabaseService {
  private state: DatabaseState;
  private listeners: Array<(state: DatabaseState) => void> = [];
  private sqlPromise: ReturnType<typeof initSqlJs> | null = null;

  constructor() {
    this.state = this.loadState();
    this.recalculateAllBalances();
  }

  private loadState(): DatabaseState {
    try {
      const serialized = localStorage.getItem(STORAGE_KEY);
      if (serialized) {
        const parsed = JSON.parse(serialized);
        return {
          customers: parsed.customers || [],
          transactions: parsed.transactions || [],
          products: parsed.products || [],
          expenses: parsed.expenses || [],
          invoices: parsed.invoices || [],
          invoiceItems: parsed.invoiceItems || [],
          suppliers: parsed.suppliers || [],
          settings: { ...DEFAULT_SETTINGS, ...(parsed.settings || {}) },
        };
      }
    } catch (e) {
      console.error('Failed to load state from localStorage', e);
    }
    return INITIAL_STATE;
  }

  private saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state));
    } catch (e) {
      console.error('Failed to save state to localStorage', e);
    }
    this.notify();
  }

  public subscribe(listener: (state: DatabaseState) => void): () => void {
    this.listeners.push(listener);
    listener(this.getState());
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private notify() {
    const s = this.getState();
    this.listeners.forEach(l => l(s));
  }

  public getState(): DatabaseState {
    return { ...this.state };
  }

  public now(): string {
    const d = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  // --- Recalculation ---
  private recalculateCustomer(customerId: number) {
    const txs = this.state.transactions.filter(t => t.customerId === customerId);
    const balance = txs.reduce((sum, t) => {
      return sum + (t.type === 'DEBIT' ? t.amount : -t.amount);
    }, 0);

    this.state.customers = this.state.customers.map(c =>
      c.id === customerId ? { ...c, balance } : c
    );
  }

  private recalculateAllBalances() {
    this.state.customers = this.state.customers.map(c => {
      const txs = this.state.transactions.filter(t => t.customerId === c.id);
      const balance = txs.reduce((sum, t) => sum + (t.type === 'DEBIT' ? t.amount : -t.amount), 0);
      return { ...c, balance };
    });
  }

  // --- Customers ---
  public getCustomerLastActivityMap(): Map<number, number> {
    const map = new Map<number, number>();
    for (const t of this.state.transactions) {
      const ts = parseDateToTimestamp(t.date);
      const prev = map.get(t.customerId) || 0;
      if (ts > prev) {
        map.set(t.customerId, ts);
      }
    }
    return map;
  }

  public getCustomers(sortBy: 'newest' | 'id_desc' | 'name' | 'balance' = 'newest'): Customer[] {
    const lastMap = this.getCustomerLastActivityMap();
    return [...this.state.customers].sort((a, b) => {
      if (sortBy === 'name') {
        return a.name.localeCompare(b.name, 'ar');
      }
      if (sortBy === 'id_desc') {
        return b.id - a.id;
      }
      if (sortBy === 'balance') {
        return Math.abs(b.balance) - Math.abs(a.balance);
      }
      // 'newest' (الأحدث أولاً): الترتيب حسب أحدث حركة تاريخياً، وإذا تساويا فحسب أحدث إضافة (ID تنازلياً)
      const timeA = lastMap.get(a.id) || 0;
      const timeB = lastMap.get(b.id) || 0;
      if (timeB !== timeA) {
        return timeB - timeA;
      }
      return b.id - a.id;
    });
  }

  public getCustomer(id: number): Customer | undefined {
    return this.state.customers.find(c => c.id === id);
  }

  public addCustomer(name: string, phone: string = '', notes: string = ''): number {
    const trimmed = name.trim();
    if (!trimmed) throw new Error('اسم العميل مطلوب');
    const id = this.state.customers.length > 0 ? Math.max(...this.state.customers.map(c => c.id)) + 1 : 1;
    const newCustomer: Customer = {
      id,
      name: trimmed,
      phone: phone.trim(),
      balance: 0,
      notes: notes.trim(),
    };
    this.state.customers.push(newCustomer);
    this.saveState();
    return id;
  }

  public updateCustomer(id: number, name: string, phone: string, notes: string) {
    const trimmed = name.trim();
    if (!trimmed) throw new Error('اسم العميل مطلوب');
    this.state.customers = this.state.customers.map(c =>
      c.id === id ? { ...c, name: trimmed, phone: phone.trim(), notes: notes.trim() } : c
    );
    this.saveState();
  }

  public deleteCustomer(id: number) {
    this.state.customers = this.state.customers.filter(c => c.id !== id);
    this.state.transactions = this.state.transactions.filter(t => t.customerId !== id);
    this.saveState();
  }

  // --- Transactions ---
  public getTransactions(customerId?: number): Tx[] {
    if (customerId != null) {
      return this.state.transactions
        .filter(t => t.customerId === customerId)
        .sort(compareTxNewestFirst);
    }
    return [...this.state.transactions].sort(compareTxNewestFirst);
  }

  public addTransaction(
    customerId: number,
    type: 'DEBIT' | 'CREDIT',
    amount: number,
    note: string = '',
    currency: string = 'YER',
    date: string = this.now()
  ): number {
    if (amount <= 0) throw new Error('المبلغ غير صحيح');
    const id = this.state.transactions.length > 0 ? Math.max(...this.state.transactions.map(t => t.id)) + 1 : 1;
    const newTx: Tx = {
      id,
      customerId,
      type,
      amount,
      currency: currency.trim().toUpperCase() || 'YER',
      date: date || this.now(),
      note: note.trim(),
    };
    this.state.transactions.push(newTx);
    this.recalculateCustomer(customerId);
    this.saveState();
    return id;
  }

  public updateTransaction(
    id: number,
    type: 'DEBIT' | 'CREDIT',
    amount: number,
    note: string,
    currency: string,
    date?: string
  ) {
    if (amount <= 0) throw new Error('المبلغ غير صحيح');
    const existing = this.state.transactions.find(t => t.id === id);
    if (!existing) return;
    this.state.transactions = this.state.transactions.map(t =>
      t.id === id
        ? {
            ...t,
            type,
            amount,
            note: note.trim(),
            currency: currency.trim().toUpperCase() || 'YER',
            date: date || t.date,
          }
        : t
    );
    this.recalculateCustomer(existing.customerId);
    this.saveState();
  }

  public deleteTransaction(id: number) {
    const existing = this.state.transactions.find(t => t.id === id);
    if (!existing) return;
    this.state.transactions = this.state.transactions.filter(t => t.id !== id);
    this.recalculateCustomer(existing.customerId);
    this.saveState();
  }

  // --- Products ---
  public getProducts(): Product[] {
    return [...this.state.products].sort((a, b) => a.name.localeCompare(b.name, 'ar'));
  }

  public addProduct(
    name: string,
    barcode: string,
    buyPrice: number,
    sellPrice: number,
    stock: number,
    minStock: number,
    notes: string = ''
  ): number {
    const trimmed = name.trim();
    if (!trimmed) throw new Error('اسم الصنف مطلوب');
    if (this.state.products.some(p => p.name.toLowerCase() === trimmed.toLowerCase())) {
      throw new Error('الصنف موجود مسبقاً');
    }
    const id = this.state.products.length > 0 ? Math.max(...this.state.products.map(p => p.id)) + 1 : 1;
    const newProduct: Product = {
      id,
      name: trimmed,
      barcode: barcode.trim(),
      buyPrice: Math.max(0, buyPrice),
      sellPrice: Math.max(0, sellPrice),
      stock: Math.max(0, stock),
      minStock: Math.max(0, minStock),
      notes: notes.trim(),
    };
    this.state.products.push(newProduct);
    this.saveState();
    return id;
  }

  public updateProduct(
    id: number,
    name: string,
    barcode: string,
    buyPrice: number,
    sellPrice: number,
    stock: number,
    minStock: number,
    notes: string
  ) {
    const trimmed = name.trim();
    if (!trimmed) throw new Error('اسم الصنف مطلوب');
    this.state.products = this.state.products.map(p =>
      p.id === id
        ? {
            ...p,
            name: trimmed,
            barcode: barcode.trim(),
            buyPrice: Math.max(0, buyPrice),
            sellPrice: Math.max(0, sellPrice),
            stock: Math.max(0, stock),
            minStock: Math.max(0, minStock),
            notes: notes.trim(),
          }
        : p
    );
    this.saveState();
  }

  public deleteProduct(id: number) {
    this.state.products = this.state.products.filter(p => p.id !== id);
    this.saveState();
  }

  // --- Expenses ---
  public getExpenses(): Expense[] {
    return [...this.state.expenses].sort(compareTxNewestFirst);
  }

  public addExpense(
    title: string,
    amount: number,
    note: string = '',
    currency: string = 'YER',
    date: string = this.now()
  ): number {
    const trimmed = title.trim();
    if (!trimmed) throw new Error('عنوان المصروف مطلوب');
    if (amount <= 0) throw new Error('المبلغ غير صحيح');
    const id = this.state.expenses.length > 0 ? Math.max(...this.state.expenses.map(e => e.id)) + 1 : 1;
    const newExpense: Expense = {
      id,
      title: trimmed,
      amount,
      currency: currency.trim().toUpperCase() || 'YER',
      date: date || this.now(),
      note: note.trim(),
    };
    this.state.expenses.push(newExpense);
    this.saveState();
    return id;
  }

  public deleteExpense(id: number) {
    this.state.expenses = this.state.expenses.filter(e => e.id !== id);
    this.saveState();
  }

  // --- Invoices ---
  public getInvoices(): Invoice[] {
    return [...this.state.invoices].sort(compareTxNewestFirst);
  }

  public addInvoice(
    type: 'SALE' | 'PURCHASE',
    total: number,
    paid: number,
    note: string = '',
    customerId?: number | null,
    customerOrSupplierName?: string,
    items?: InvoiceItem[],
    recordInCustomerLedger: boolean = true
  ): number {
    if (total <= 0) throw new Error('إجمالي الفاتورة يجب أن يكون أكبر من الصفر');
    const id = this.state.invoices.length > 0 ? Math.max(...this.state.invoices.map(i => i.id)) + 1 : 1;
    
    // Assign invoice items if provided
    const assignedItems: InvoiceItem[] = (items || []).map((itm, idx) => ({
      ...itm,
      id: itm.id || Date.now() + idx,
      invoiceId: id,
    }));

    const newInvoice: Invoice = {
      id,
      type,
      customerId: customerId ?? null,
      customerOrSupplierName: customerOrSupplierName || (customerId ? this.getCustomer(customerId)?.name : ''),
      date: this.now(),
      total,
      paid,
      note: note.trim(),
      items: assignedItems,
    };
    this.state.invoices.push(newInvoice);

    if (assignedItems.length > 0) {
      if (!this.state.invoiceItems) this.state.invoiceItems = [];
      this.state.invoiceItems.push(...assignedItems);
    }

    // Automatically record remaining debt in customer ledger if customer is selected
    if (recordInCustomerLedger && customerId && type === 'SALE') {
      const remainingDebt = total - paid;
      if (remainingDebt > 0) {
        const itemSummary = assignedItems.length > 0
          ? assignedItems.map(it => `${it.itemName} (${it.quantity}×${it.unitPrice})`).join('، ')
          : note.trim();
        const txNote = `فاتورة مبيعات #${id}: ${itemSummary || 'مشتريات بقالة'}${paid > 0 ? ` (مدفوع: ${paid})` : ''}`;
        this.addTransaction(customerId, 'DEBIT', remainingDebt, txNote, 'YER');
      }
    }

    this.saveState();
    return id;
  }

  public updateInvoice(
    id: number,
    type: 'SALE' | 'PURCHASE',
    total: number,
    paid: number,
    note: string = '',
    customerId?: number | null,
    customerOrSupplierName?: string,
    items?: InvoiceItem[],
    recordInCustomerLedger: boolean = false
  ) {
    if (total <= 0) throw new Error('إجمالي الفاتورة يجب أن يكون أكبر من الصفر');
    const existing = this.state.invoices.find(i => i.id === id);
    if (!existing) return;

    const assignedItems: InvoiceItem[] = (items || []).map((itm, idx) => ({
      ...itm,
      id: itm.id || Date.now() + idx,
      invoiceId: id,
    }));

    this.state.invoices = this.state.invoices.map(i =>
      i.id === id
        ? {
            ...i,
            type,
            total,
            paid,
            note: note.trim(),
            customerId: customerId ?? null,
            customerOrSupplierName: customerOrSupplierName || (customerId ? this.getCustomer(customerId)?.name : ''),
            items: assignedItems,
          }
        : i
    );

    if (this.state.invoiceItems) {
      this.state.invoiceItems = this.state.invoiceItems.filter(it => it.invoiceId !== id);
      this.state.invoiceItems.push(...assignedItems);
    }

    this.saveState();
  }

  public deleteInvoice(id: number) {
    this.state.invoices = this.state.invoices.filter(i => i.id !== id);
    this.saveState();
  }

  // --- Settings ---
  public getSettings(): AppSettings {
    return { ...this.state.settings };
  }

  public updateSettings(settings: Partial<AppSettings>) {
    this.state.settings = { ...this.state.settings, ...settings };
    this.saveState();
  }

  // --- SQLite & Import / Export / Normalizer ---
  private async fetchValidWasmBinary(): Promise<ArrayBuffer> {
    const sources = [
      '/sql-wasm.wasm',
      'https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.12.0/sql-wasm.wasm',
      'https://cdn.jsdelivr.net/npm/sql.js@1.12.0/dist/sql-wasm.wasm',
      'https://unpkg.com/sql.js@1.12.0/dist/sql-wasm.wasm',
    ];

    for (const src of sources) {
      try {
        const res = await fetch(src);
        if (!res.ok) continue;
        const buf = await res.arrayBuffer();
        if (buf.byteLength >= 4) {
          const header = new Uint8Array(buf, 0, 4);
          // Verify WebAssembly magic number: \0asm (0x00, 0x61, 0x73, 0x6d)
          if (header[0] === 0x00 && header[1] === 0x61 && header[2] === 0x73 && header[3] === 0x6d) {
            return buf;
          }
        }
      } catch {
        // try next source
      }
    }
    throw new Error('تعذر تحميل محرك SQLite WebAssembly بنجاح');
  }

  private async getSqlJs() {
    if (!this.sqlPromise) {
      this.sqlPromise = (async () => {
        try {
          const wasmBinary = await this.fetchValidWasmBinary();
          return await initSqlJs({ wasmBinary });
        } catch {
          // Final fallback to cdnjs locateFile
          return await initSqlJs({
            locateFile: () => 'https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.12.0/sql-wasm.wasm',
          });
        }
      })();
    }
    return this.sqlPromise;
  }

  public async exportSqlite(): Promise<Uint8Array> {
    const SQL = await this.getSqlJs();
    const db = new SQL.Database();

    // Create Schema
    db.run(`
      CREATE TABLE IF NOT EXISTS customers(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL DEFAULT '',
        phone TEXT DEFAULT '',
        balance REAL DEFAULT 0,
        group_id INTEGER,
        notes TEXT DEFAULT ''
      );
      CREATE TABLE IF NOT EXISTS transactions(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        customer_id INTEGER NOT NULL,
        type TEXT NOT NULL CHECK(type IN ('DEBIT','CREDIT')),
        amount REAL NOT NULL CHECK(amount>0),
        currency TEXT NOT NULL DEFAULT 'YER',
        date TEXT NOT NULL DEFAULT '',
        note TEXT DEFAULT '',
        share_ref TEXT
      );
      CREATE TABLE IF NOT EXISTS products(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE,
        barcode TEXT DEFAULT '',
        buy_price REAL DEFAULT 0,
        sell_price REAL DEFAULT 0,
        stock REAL DEFAULT 0,
        min_stock REAL DEFAULT 0,
        notes TEXT DEFAULT ''
      );
      CREATE TABLE IF NOT EXISTS expenses(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL DEFAULT '',
        amount REAL NOT NULL DEFAULT 0,
        currency TEXT DEFAULT 'YER',
        date TEXT NOT NULL DEFAULT '',
        note TEXT DEFAULT ''
      );
      CREATE TABLE IF NOT EXISTS invoices(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type TEXT NOT NULL CHECK(type IN ('SALE','PURCHASE')),
        customer_id INTEGER,
        date TEXT NOT NULL DEFAULT '',
        total REAL DEFAULT 0,
        paid REAL DEFAULT 0,
        note TEXT DEFAULT ''
      );
      CREATE TABLE IF NOT EXISTS settings(
        key TEXT PRIMARY KEY,
        value TEXT NOT NULL DEFAULT ''
      );
    `);

    // Insert Customers
    for (const c of this.state.customers) {
      db.run('INSERT INTO customers(id, name, phone, balance, notes) VALUES (?, ?, ?, ?, ?)', [
        c.id,
        c.name,
        c.phone,
        c.balance,
        c.notes,
      ]);
    }

    // Insert Transactions
    for (const t of this.state.transactions) {
      db.run('INSERT INTO transactions(id, customer_id, type, amount, currency, date, note) VALUES (?, ?, ?, ?, ?, ?, ?)', [
        t.id,
        t.customerId,
        t.type,
        t.amount,
        t.currency,
        t.date,
        t.note,
      ]);
    }

    // Insert Products
    for (const p of this.state.products) {
      db.run('INSERT INTO products(id, name, barcode, buy_price, sell_price, stock, min_stock, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)', [
        p.id,
        p.name,
        p.barcode,
        p.buyPrice,
        p.sellPrice,
        p.stock,
        p.minStock,
        p.notes,
      ]);
    }

    // Insert Expenses
    for (const e of this.state.expenses) {
      db.run('INSERT INTO expenses(id, title, amount, currency, date, note) VALUES (?, ?, ?, ?, ?, ?)', [
        e.id,
        e.title,
        e.amount,
        e.currency,
        e.date,
        e.note,
      ]);
    }

    // Insert Invoices
    for (const inv of this.state.invoices) {
      db.run('INSERT INTO invoices(id, type, customer_id, date, total, paid, note) VALUES (?, ?, ?, ?, ?, ?, ?)', [
        inv.id,
        inv.type,
        inv.customerId || null,
        inv.date,
        inv.total,
        inv.paid,
        inv.note,
      ]);
    }

    // Insert Settings
    db.run('INSERT INTO settings(key, value) VALUES (?, ?)', ['store_name', this.state.settings.storeName]);
    db.run('INSERT INTO settings(key, value) VALUES (?, ?)', ['phone', this.state.settings.phone]);

    const data = db.export();
    db.close();
    return data;
  }

  public async inspectSqlite(buffer: ArrayBuffer): Promise<IntegrityReport> {
    try {
      const SQL = await this.getSqlJs();
      const db = new SQL.Database(new Uint8Array(buffer));
      const warnings: string[] = [];

      // Check integrity
      let integrityOk = false;
      try {
        const integrityRes = db.exec('PRAGMA integrity_check');
        if (integrityRes.length > 0 && integrityRes[0].values.length > 0) {
          integrityOk = String(integrityRes[0].values[0][0]).toLowerCase() === 'ok';
        }
      } catch (e) {
        warnings.push('فشل استعلام فحص السلامة PRAGMA integrity_check');
      }

      // Get tables
      const tablesRes = db.exec("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name");
      const tables: Array<{ name: string; columns: string[]; rowCount: number }> = [];

      if (tablesRes.length > 0 && tablesRes[0].values) {
        for (const row of tablesRes[0].values) {
          const tableName = String(row[0]);
          // pragma table_info
          const infoRes = db.exec(`PRAGMA table_info("${tableName}")`);
          const columns: string[] = [];
          if (infoRes.length > 0 && infoRes[0].values) {
            for (const colRow of infoRes[0].values) {
              columns.push(String(colRow[1]));
            }
          }

          let rowCount = 0;
          try {
            const countRes = db.exec(`SELECT COUNT(*) FROM "${tableName}"`);
            if (countRes.length > 0 && countRes[0].values.length > 0) {
              rowCount = Number(countRes[0].values[0][0]) || 0;
            }
          } catch {
            rowCount = 0;
          }

          tables.push({ name: tableName, columns, rowCount });
        }
      }

      if (!tables.some(t => t.name.toLowerCase() === 'customers')) {
        warnings.push('جدول العملاء غير موجود');
      }
      if (!tables.some(t => t.name.toLowerCase() === 'transactions')) {
        warnings.push('جدول العمليات غير موجود');
      }

      db.close();
      return {
        validSqlite: true,
        integrityOk,
        tables,
        warnings,
      };
    } catch (e: any) {
      return {
        validSqlite: false,
        integrityOk: false,
        tables: [],
        warnings: [e.message || 'الملف المحدد ليس قاعدة SQLite صالحة'],
      };
    }
  }

  // Import SQLite with DatabaseNormalizer logic matching the Android app
  public async importSqlite(buffer: ArrayBuffer): Promise<{ ok: boolean; message: string; stats?: any }> {
    try {
      const SQL = await this.getSqlJs();
      const db = new SQL.Database(new Uint8Array(buffer));

      // Inspect tables
      const tablesRes = db.exec("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'");
      const tableNames: string[] = tablesRes.length > 0 ? tablesRes[0].values.map(v => String(v[0])) : [];

      const getColumns = (tbl: string): string[] => {
        try {
          const r = db.exec(`PRAGMA table_info("${tbl}")`);
          return r.length > 0 ? r[0].values.map(v => String(v[1])) : [];
        } catch {
          return [];
        }
      };

      const findCol = (cols: string[], ...candidates: string[]): string | null => {
        for (const c of candidates) {
          const found = cols.find(col => col.toLowerCase() === c.toLowerCase());
          if (found) return found;
        }
        for (const c of candidates) {
          const found = cols.find(col => col.toLowerCase().includes(c.toLowerCase()));
          if (found) return found;
        }
        return null;
      };

      let importedCustomers: Customer[] = [];
      let importedTransactions: Tx[] = [];
      let importedProducts: Product[] = [];
      let importedExpenses: Expense[] = [];
      let importedInvoices: Invoice[] = [];
      let importedSettings: AppSettings = { ...this.state.settings };

      // Map old customer id to new customer id
      const idMap = new Map<number, number>();

      // 1. Customers
      const custTable = tableNames.find(t => t.toLowerCase() === 'customers') ||
        tableNames.find(t => {
          const cols = getColumns(t);
          return findCol(cols, 'name', 'customer_name', 'اسم', 'العميل') != null;
        });

      if (custTable) {
        const cols = getColumns(custTable);
        const idCol = findCol(cols, 'id', 'customer_id', 'client_id', 'رقم');
        const nameCol = findCol(cols, 'name', 'customer_name', 'client_name', 'اسم', 'اسم العميل', 'العميل');
        const phoneCol = findCol(cols, 'phone', 'gsm', 'mobile', 'هاتف', 'الجوال');
        const balanceCol = findCol(cols, 'balance', 'رصيد', 'الرصيد');
        const notesCol = findCol(cols, 'notes', 'param1', 'note', 'ملاحظات');
        const groupCol = findCol(cols, 'g_id', 'group_id', 'group');

        const query = `SELECT * FROM "${custTable}"`;
        const res = db.exec(query);
        if (res.length > 0 && res[0].values) {
          const colNames = res[0].columns;
          let newIdSeq = 1;
          for (const row of res[0].values) {
            const getVal = (col: string | null) => {
              if (!col) return '';
              const idx = colNames.indexOf(col);
              return idx >= 0 && row[idx] != null ? String(row[idx]) : '';
            };

            const name = getVal(nameCol).trim();
            if (!name) continue;

            const oldId = Number(getVal(idCol)) || newIdSeq;
            const newId = newIdSeq++;
            idMap.set(oldId, newId);

            importedCustomers.push({
              id: newId,
              name,
              phone: getVal(phoneCol).trim(),
              balance: Number(getVal(balanceCol)) || 0,
              groupId: Number(getVal(groupCol)) || undefined,
              notes: getVal(notesCol).trim(),
            });
          }
        }
      }

      // 2. Transactions
      const txTable = tableNames.find(t => t.toLowerCase() === 'transactions') ||
        tableNames.find(t => {
          const cols = getColumns(t);
          return findCol(cols, 'customer_id', 'cus_id', 'client_id', 'معرف العميل') != null &&
            findCol(cols, 'amount', 'out', 'المبلغ', 'value') != null;
        });

      if (txTable) {
        const cols = getColumns(txTable);
        const cidCol = findCol(cols, 'cus_id', 'customer_id', 'client_id', 'account_id', 'العميل');
        const typeCol = findCol(cols, 'in', 'type', 'transaction_type', 'نوع');
        const amountCol = findCol(cols, 'out', 'amount', 'المبلغ', 'value', 'total');
        const debitCol = findCol(cols, 'debit', 'مدين');
        const creditCol = findCol(cols, 'credit', 'دائن');
        const currencyCol = findCol(cols, 'curr_id', 'currency', 'العملة');
        const dateCol = findCol(cols, 'date_', 'date', 'datetime', 'التاريخ');
        const noteCol = findCol(cols, 'remarks', 'note', 'description', 'البيان', 'ملاحظات');
        const shareRefCol = findCol(cols, 'share_ref');

        const res = db.exec(`SELECT * FROM "${txTable}"`);
        if (res.length > 0 && res[0].values) {
          const colNames = res[0].columns;
          let txSeq = 1;
          for (const row of res[0].values) {
            const getVal = (col: string | null) => {
              if (!col) return '';
              const idx = colNames.indexOf(col);
              return idx >= 0 && row[idx] != null ? String(row[idx]) : '';
            };

            const rawCid = Number(getVal(cidCol));
            const customerId = idMap.get(rawCid) || rawCid;
            if (!customerId) continue;

            let amount = parseFloat(getVal(amountCol).replace(/,/g, '')) || 0;
            const deb = parseFloat(getVal(debitCol).replace(/,/g, '')) || 0;
            const cred = parseFloat(getVal(creditCol).replace(/,/g, '')) || 0;
            if (amount <= 0) amount = Math.max(deb, cred);
            if (amount <= 0) continue;

            const rawType = getVal(typeCol).trim();
            const isCredit = rawType === '-1' || rawType.toUpperCase().includes('CREDIT') || rawType.includes('دائن') || cred > 0;
            const finalType: 'DEBIT' | 'CREDIT' = isCredit ? 'CREDIT' : 'DEBIT';

            const rawCurr = getVal(currencyCol).trim();
            let currCode = 'YER';
            if (rawCurr === '0' || rawCurr.includes('يمن')) currCode = 'YER';
            else if (rawCurr === '1' || rawCurr.includes('دولار') || rawCurr.toUpperCase() === 'USD') currCode = 'USD';
            else if (rawCurr === '2' || rawCurr.includes('سعود') || rawCurr.toUpperCase() === 'SAR') currCode = 'SAR';
            else if (rawCurr) currCode = rawCurr.toUpperCase();

            importedTransactions.push({
              id: txSeq++,
              customerId,
              type: finalType,
              amount,
              currency: currCode,
              date: getVal(dateCol) || this.now(),
              note: getVal(noteCol),
              shareRef: getVal(shareRefCol),
            });
          }
        }
      }

      // 3. Products
      const prodTable = tableNames.find(t => t.toLowerCase() === 'products');
      if (prodTable) {
        const cols = getColumns(prodTable);
        const nameCol = findCol(cols, 'name', 'اسم', 'اسم الصنف');
        const barcodeCol = findCol(cols, 'barcode', 'باركود');
        const buyCol = findCol(cols, 'buy_price', 'سعر الشراء');
        const sellCol = findCol(cols, 'sell_price', 'سعر البيع', 'السعر');
        const stockCol = findCol(cols, 'stock', 'المخزون', 'الكمية');
        const minCol = findCol(cols, 'min_stock', 'الحد الأدنى');
        const notesCol = findCol(cols, 'notes', 'ملاحظات');

        const res = db.exec(`SELECT * FROM "${prodTable}"`);
        if (res.length > 0 && res[0].values) {
          const colNames = res[0].columns;
          let pSeq = 1;
          for (const row of res[0].values) {
            const getVal = (col: string | null) => {
              if (!col) return '';
              const idx = colNames.indexOf(col);
              return idx >= 0 && row[idx] != null ? String(row[idx]) : '';
            };
            const name = getVal(nameCol).trim();
            if (!name) continue;
            importedProducts.push({
              id: pSeq++,
              name,
              barcode: getVal(barcodeCol),
              buyPrice: parseFloat(getVal(buyCol)) || 0,
              sellPrice: parseFloat(getVal(sellCol)) || 0,
              stock: parseFloat(getVal(stockCol)) || 0,
              minStock: parseFloat(getVal(minCol)) || 0,
              notes: getVal(notesCol),
            });
          }
        }
      }

      // 4. Expenses
      const expTable = tableNames.find(t => t.toLowerCase() === 'expenses');
      if (expTable) {
        const cols = getColumns(expTable);
        const titleCol = findCol(cols, 'title', 'اسم', 'البيان', 'الوصف');
        const amountCol = findCol(cols, 'amount', 'المبلغ');
        const curCol = findCol(cols, 'currency', 'العملة');
        const dateCol = findCol(cols, 'date', 'التاريخ');
        const noteCol = findCol(cols, 'note', 'ملاحظات');

        const res = db.exec(`SELECT * FROM "${expTable}"`);
        if (res.length > 0 && res[0].values) {
          const colNames = res[0].columns;
          let eSeq = 1;
          for (const row of res[0].values) {
            const getVal = (col: string | null) => {
              if (!col) return '';
              const idx = colNames.indexOf(col);
              return idx >= 0 && row[idx] != null ? String(row[idx]) : '';
            };
            const title = getVal(titleCol).trim();
            const amount = parseFloat(getVal(amountCol)) || 0;
            if (!title || amount <= 0) continue;
            importedExpenses.push({
              id: eSeq++,
              title,
              amount,
              currency: getVal(curCol).toUpperCase() || 'YER',
              date: getVal(dateCol) || this.now(),
              note: getVal(noteCol),
            });
          }
        }
      }

      // 5. Invoices
      const invTable = tableNames.find(t => t.toLowerCase() === 'invoices');
      if (invTable) {
        const cols = getColumns(invTable);
        const typeCol = findCol(cols, 'type', 'النوع');
        const totalCol = findCol(cols, 'total', 'الإجمالي');
        const paidCol = findCol(cols, 'paid', 'المدفوع');
        const dateCol = findCol(cols, 'date', 'التاريخ');
        const noteCol = findCol(cols, 'note', 'البيان');
        const custCol = findCol(cols, 'customer_id', 'العميل');

        const res = db.exec(`SELECT * FROM "${invTable}"`);
        if (res.length > 0 && res[0].values) {
          const colNames = res[0].columns;
          let invSeq = 1;
          for (const row of res[0].values) {
            const getVal = (col: string | null) => {
              if (!col) return '';
              const idx = colNames.indexOf(col);
              return idx >= 0 && row[idx] != null ? String(row[idx]) : '';
            };
            const total = parseFloat(getVal(totalCol)) || 0;
            if (total <= 0) continue;
            const cid = Number(getVal(custCol)) || null;
            importedInvoices.push({
              id: invSeq++,
              type: (getVal(typeCol).toUpperCase().includes('PURCHASE') ? 'PURCHASE' : 'SALE'),
              total,
              paid: parseFloat(getVal(paidCol)) || 0,
              date: getVal(dateCol) || this.now(),
              note: getVal(noteCol),
              customerId: cid ? idMap.get(cid) || cid : null,
            });
          }
        }
      }

      // 6. Settings
      const setTable = tableNames.find(t => t.toLowerCase() === 'settings');
      if (setTable) {
        const res = db.exec(`SELECT key, value FROM "${setTable}"`);
        if (res.length > 0 && res[0].values) {
          for (const row of res[0].values) {
            const k = String(row[0]);
            const v = String(row[1]);
            if (k === 'store_name') importedSettings.storeName = v;
            if (k === 'phone') importedSettings.phone = v;
          }
        }
      }

      db.close();

      if (importedCustomers.length === 0 && importedTransactions.length === 0) {
        return { ok: false, message: 'لم يتم العثور على بيانات صالحة للعملاء أو العمليات' };
      }

      this.state = {
        customers: importedCustomers,
        transactions: importedTransactions,
        products: importedProducts,
        expenses: importedExpenses,
        invoices: importedInvoices,
        invoiceItems: [],
        suppliers: [],
        settings: importedSettings,
      };

      this.recalculateAllBalances();
      this.saveState();

      return {
        ok: true,
        message: `تم الاستيراد بنجاح: ${importedCustomers.length} عميل، ${importedTransactions.length} عملية، ${importedProducts.length} صنف، ${importedExpenses.length} مصروف`,
        stats: {
          customers: importedCustomers.length,
          transactions: importedTransactions.length,
          products: importedProducts.length,
          expenses: importedExpenses.length,
        },
      };
    } catch (e: any) {
      return { ok: false, message: `فشل استيراد قاعدة البيانات: ${e.message || 'خطأ غير معروف'}` };
    }
  }

  // JSON Backup and Restore
  public exportJson(): string {
    return JSON.stringify(this.state, null, 2);
  }

  public importJson(jsonStr: string): { ok: boolean; message: string } {
    try {
      const parsed = JSON.parse(jsonStr);
      if (!parsed || (!parsed.customers && !parsed.transactions)) {
        return { ok: false, message: 'ملف النسخة الاحتياطية غير صالح' };
      }

      this.state = {
        customers: parsed.customers || [],
        transactions: parsed.transactions || [],
        products: parsed.products || [],
        expenses: parsed.expenses || [],
        invoices: parsed.invoices || [],
        invoiceItems: parsed.invoiceItems || [],
        suppliers: parsed.suppliers || [],
        settings: { ...DEFAULT_SETTINGS, ...(parsed.settings || {}) },
      };

      this.recalculateAllBalances();
      this.saveState();
      return { ok: true, message: 'تمت استعادة النسخة الاحتياطية بنجاح' };
    } catch (e: any) {
      return { ok: false, message: `تعذر قراءة النسخة الاحتياطية: ${e.message}` };
    }
  }

  public seedDefaultData() {
    this.resetToDefault();
  }

  public async checkIntegrity(): Promise<{ ok: boolean; report: string }> {
    try {
      const bytes = await this.exportSqlite();
      const report = await this.inspectSqlite(bytes.buffer as ArrayBuffer);
      return {
        ok: report.integrityOk,
        report: report.integrityOk ? 'ok' : report.warnings.join(', ') || 'failed',
      };
    } catch (e: any) {
      return { ok: false, report: e.message || 'فحص غير ناجح' };
    }
  }

  public async loadMarketDatabase(): Promise<{ ok: boolean; message: string; count: number }> {
    try {
      const res = await fetch('/marketData.json');
      if (!res.ok) {
        throw new Error('فشل قراءة ملف بيانات البقالة');
      }
      const data = await res.json();
      this.state = {
        customers: data.customers || [],
        transactions: data.transactions || [],
        products: data.products || [],
        expenses: data.expenses || [],
        invoices: data.invoices || [],
        invoiceItems: [],
        suppliers: [],
        settings: { ...DEFAULT_SETTINGS, ...(data.settings || {}) },
      };
      this.recalculateAllBalances();
      this.saveState();
      return {
        ok: true,
        message: `تم تحميل قاعدة بيانات البقالة بنجاح: ${data.customers.length} عميل و ${data.transactions.length} عملية مسجلة!`,
        count: data.customers.length,
      };
    } catch (e: any) {
      return {
        ok: false,
        message: e.message || 'تعذر تحميل بيانات البقالة',
        count: 0,
      };
    }
  }

  public resetToDefault() {
    this.state = JSON.parse(JSON.stringify(INITIAL_STATE));
    this.recalculateAllBalances();
    this.saveState();
  }
}

export const db = new DatabaseService();
