import React, { useState, useEffect, useCallback } from 'react';
import { db } from './services/db';
import { Customer, Tx, Product, Expense, Invoice, StoreSettings, TransactionType } from './types';
import { Header } from './components/Header';
import { Navigation, TabType } from './components/Navigation';
import { DashboardView } from './components/DashboardView';
import { AccountsView } from './components/AccountsView';
import { CustomerLedgerView } from './components/CustomerLedgerView';
import { InvoicesView } from './components/InvoicesView';
import { InventoryView } from './components/InventoryView';
import { ExpensesView } from './components/ExpensesView';
import { ReportsView } from './components/ReportsView';
import { MoreView } from './components/MoreView';

// Modals
import { CustomerModal } from './components/CustomerModal';
import { TransactionModal } from './components/TransactionModal';
import { ProductModal } from './components/ProductModal';
import { ExpenseModal } from './components/ExpenseModal';
import { InvoiceModal } from './components/InvoiceModal';
import { SettingsModal } from './components/SettingsModal';
import { DatabaseToolsModal } from './components/DatabaseToolsModal';
import { BluetoothModal } from './components/BluetoothModal';
import { AboutModal } from './components/AboutModal';
import { PrintStatementModal } from './components/PrintStatementModal';

export function App() {
  // App state from local SQLite service
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [transactions, setTransactions] = useState<Tx[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [settings, setSettings] = useState<StoreSettings>({
    storeName: 'بقالة العزي للمواد الغذائية',
    phone: '776425052',
  });

  // Navigation State
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const [activeCustomerLedgerId, setActiveCustomerLedgerId] = useState<number | null>(null);
  const [subView, setSubView] = useState<'expenses' | 'reports' | null>(null);

  // Modals visibility
  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);

  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [txModalCustomer, setTxModalCustomer] = useState<Customer | null>(null);
  const [editingTx, setEditingTx] = useState<Tx | null>(null);

  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);

  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [invoiceType, setInvoiceType] = useState<'SALE' | 'PURCHASE'>('SALE');

  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
  const [isDbToolsModalOpen, setIsDbToolsModalOpen] = useState(false);
  const [isBluetoothModalOpen, setIsBluetoothModalOpen] = useState(false);
  const [isAboutModalOpen, setIsAboutModalOpen] = useState(false);

  const [isPrintModalOpen, setIsPrintModalOpen] = useState(false);
  const [printCustomer, setPrintCustomer] = useState<Customer | null>(null);

  // Load and sync data from db
  const reloadData = useCallback(() => {
    const s = db.getState();
    setCustomers(s.customers);
    setTransactions(s.transactions);
    setProducts(s.products);
    setExpenses(s.expenses);
    setInvoices(s.invoices);
    setSettings(s.settings);
  }, []);

  useEffect(() => {
    reloadData();
  }, [reloadData]);

  // Current active customer for ledger view
  const activeCustomer = customers.find(c => c.id === activeCustomerLedgerId) || null;
  const activeCustomerTxs = activeCustomer
    ? transactions.filter(t => t.customerId === activeCustomer.id)
    : [];

  // Handlers for customer
  const handleOpenLedger = (customerId: number) => {
    setActiveCustomerLedgerId(customerId);
    setSubView(null);
  };

  const handleSaveCustomer = (name: string, phone: string, notes: string) => {
    if (editingCustomer) {
      db.updateCustomer(editingCustomer.id, name, phone, notes);
    } else {
      db.addCustomer(name, phone, notes);
    }
    reloadData();
  };

  const handleDeleteCustomer = (customer: Customer) => {
    if (
      window.confirm(
        `هل أنت متأكد من حذف حساب (${customer.name}) وكافة العمليات المسجلة به نهائياً؟`
      )
    ) {
      db.deleteCustomer(customer.id);
      if (activeCustomerLedgerId === customer.id) {
        setActiveCustomerLedgerId(null);
      }
      reloadData();
    }
  };

  // Handlers for transactions
  const handleOpenAddTx = (customer?: Customer | null) => {
    setEditingTx(null);
    setTxModalCustomer(customer || activeCustomer || null);
    setIsTxModalOpen(true);
  };

  const handleOpenEditTx = (tx: Tx) => {
    setEditingTx(tx);
    const c = customers.find(item => item.id === tx.customerId) || null;
    setTxModalCustomer(c);
    setIsTxModalOpen(true);
  };

  const handleSaveTx = (
    customerId: number,
    type: TransactionType,
    amount: number,
    note: string,
    currency: string,
    date?: string
  ) => {
    if (editingTx) {
      db.updateTransaction(editingTx.id, type, amount, note, currency, date);
    } else {
      db.addTransaction(customerId, type, amount, note, currency, date);
    }
    reloadData();
  };

  const handleDeleteTx = (tx: Tx) => {
    if (window.confirm('هل تريد حذف هذه العملية نهائياً؟')) {
      db.deleteTransaction(tx.id);
      reloadData();
    }
  };

  // Handlers for products
  const handleSaveProduct = (
    name: string,
    barcode: string,
    buyPrice: number,
    sellPrice: number,
    stock: number,
    minStock: number,
    notes: string
  ) => {
    if (editingProduct) {
      db.updateProduct(editingProduct.id, name, barcode, buyPrice, sellPrice, stock, minStock, notes);
    } else {
      db.addProduct(name, barcode, buyPrice, sellPrice, stock, minStock, notes);
    }
    reloadData();
  };

  const handleDeleteProduct = (product: Product) => {
    if (window.confirm(`هل أنت متأكد من حذف صنف (${product.name})؟`)) {
      db.deleteProduct(product.id);
      reloadData();
    }
  };

  // Handlers for expenses
  const handleSaveExpense = (title: string, amount: number, note: string, currency: string) => {
    db.addExpense(title, amount, note, currency);
    reloadData();
  };

  const handleDeleteExpense = (expense: Expense) => {
    if (window.confirm(`هل أنت متأكد من حذف مصروف (${expense.title})؟`)) {
      db.deleteExpense(expense.id);
      reloadData();
    }
  };

  // Handlers for invoices
  const handleOpenAddInvoice = (type: 'SALE' | 'PURCHASE') => {
    setInvoiceType(type);
    setIsInvoiceModalOpen(true);
  };

  const handleSaveInvoice = (
    type: 'SALE' | 'PURCHASE',
    total: number,
    paid: number,
    note: string,
    customerId?: number | null,
    customerOrSupplierName?: string
  ) => {
    db.addInvoice(type, total, paid, note, customerId, customerOrSupplierName);
    reloadData();
  };

  const handleDeleteInvoice = (inv: Invoice) => {
    if (window.confirm(`هل تريد حذف الفاتورة رقم #${inv.id}؟`)) {
      db.deleteInvoice(inv.id);
      reloadData();
    }
  };

  // Reset Demo Data
  const handleResetDemoData = () => {
    db.seedDefaultData();
    setActiveCustomerLedgerId(null);
    setSubView(null);
    reloadData();
  };

  // Print statement
  const handlePrintStatement = (customer: Customer) => {
    setPrintCustomer(customer);
    setIsPrintModalOpen(true);
  };

  return (
    <div className="min-h-screen bg-[#F7F9F8] text-gray-900 font-sans antialiased flex flex-col selection:bg-[#146B50]/20">
      {/* Top Header Bar */}
      <Header
        title={`${settings.storeName} - دفتر الحسابات`}
        onRefresh={reloadData}
        onMenuClick={() => {
          setActiveTab('more');
          setActiveCustomerLedgerId(null);
          setSubView(null);
        }}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-3 sm:p-4">
        {/* Sub-view: Customer Ledger */}
        {activeCustomerLedgerId !== null && activeCustomer ? (
          <CustomerLedgerView
            customer={activeCustomer}
            transactions={activeCustomerTxs}
            storeName={settings.storeName}
            onBack={() => setActiveCustomerLedgerId(null)}
            onAddTransaction={handleOpenAddTx}
            onEditCustomer={c => {
              setEditingCustomer(c);
              setIsCustomerModalOpen(true);
            }}
            onEditTransaction={handleOpenEditTx}
            onDeleteTransaction={handleDeleteTx}
            onDeleteCustomer={handleDeleteCustomer}
            onPrintStatement={handlePrintStatement}
          />
        ) : subView === 'expenses' ? (
          <div className="space-y-3">
            <button
              onClick={() => setSubView(null)}
              className="text-xs font-bold text-[#0D4D3A] bg-white border border-[#E1E8E4] px-3 py-1.5 rounded-xl hover:bg-gray-50 mb-2"
            >
              ← العودة للقائمة
            </button>
            <ExpensesView
              expenses={expenses}
              onAddExpense={() => setIsExpenseModalOpen(true)}
              onDeleteExpense={handleDeleteExpense}
            />
          </div>
        ) : subView === 'reports' ? (
          <div className="space-y-3">
            <button
              onClick={() => setSubView(null)}
              className="text-xs font-bold text-[#0D4D3A] bg-white border border-[#E1E8E4] px-3 py-1.5 rounded-xl hover:bg-gray-50 mb-2"
            >
              ← العودة للقائمة
            </button>
            <ReportsView
              customers={customers}
              transactions={transactions}
              expenses={expenses}
              products={products}
            />
          </div>
        ) : activeTab === 'home' ? (
          <DashboardView
            customers={customers}
            transactions={transactions}
            products={products}
            onOpenCustomerLedger={handleOpenLedger}
            onAddCustomer={() => {
              setEditingCustomer(null);
              setIsCustomerModalOpen(true);
            }}
            onAddTransaction={() => handleOpenAddTx(null)}
            onAddInvoice={handleOpenAddInvoice}
            onAddProduct={() => {
              setEditingProduct(null);
              setIsProductModalOpen(true);
            }}
            onNavigateToTab={tab => {
              setActiveTab(tab);
              setActiveCustomerLedgerId(null);
              setSubView(null);
            }}
          />
        ) : activeTab === 'accounts' ? (
          <AccountsView
            customers={customers}
            onOpenLedger={handleOpenLedger}
            onAddCustomer={() => {
              setEditingCustomer(null);
              setIsCustomerModalOpen(true);
            }}
            onEditCustomer={c => {
              setEditingCustomer(c);
              setIsCustomerModalOpen(true);
            }}
            onDeleteCustomer={handleDeleteCustomer}
          />
        ) : activeTab === 'invoices' ? (
          <InvoicesView
            invoices={invoices}
            onAddInvoice={handleOpenAddInvoice}
            onDeleteInvoice={handleDeleteInvoice}
          />
        ) : activeTab === 'inventory' ? (
          <InventoryView
            products={products}
            onAddProduct={() => {
              setEditingProduct(null);
              setIsProductModalOpen(true);
            }}
            onEditProduct={p => {
              setEditingProduct(p);
              setIsProductModalOpen(true);
            }}
            onDeleteProduct={handleDeleteProduct}
          />
        ) : (
          <MoreView
            onOpenExpenses={() => setSubView('expenses')}
            onOpenReports={() => setSubView('reports')}
            onOpenDatabaseTools={() => setIsDbToolsModalOpen(true)}
            onOpenBluetooth={() => setIsBluetoothModalOpen(true)}
            onOpenSettings={() => setIsSettingsModalOpen(true)}
            onOpenAbout={() => setIsAboutModalOpen(true)}
            onResetDemoData={handleResetDemoData}
          />
        )}
      </main>

      {/* Bottom Sticky Navigation */}
      <Navigation
        activeTab={activeTab}
        onChangeTab={tab => {
          setActiveTab(tab);
          setActiveCustomerLedgerId(null);
          setSubView(null);
        }}
      />

      {/* Modal Dialogs */}
      <CustomerModal
        isOpen={isCustomerModalOpen}
        customer={editingCustomer}
        onClose={() => {
          setIsCustomerModalOpen(false);
          setEditingCustomer(null);
        }}
        onSave={handleSaveCustomer}
      />

      <TransactionModal
        isOpen={isTxModalOpen}
        customers={customers}
        selectedCustomer={txModalCustomer}
        existingTx={editingTx}
        onClose={() => {
          setIsTxModalOpen(false);
          setEditingTx(null);
          setTxModalCustomer(null);
        }}
        onSave={handleSaveTx}
      />

      <ProductModal
        isOpen={isProductModalOpen}
        product={editingProduct}
        onClose={() => {
          setIsProductModalOpen(false);
          setEditingProduct(null);
        }}
        onSave={handleSaveProduct}
      />

      <ExpenseModal
        isOpen={isExpenseModalOpen}
        onClose={() => setIsExpenseModalOpen(false)}
        onSave={handleSaveExpense}
      />

      <InvoiceModal
        isOpen={isInvoiceModalOpen}
        type={invoiceType}
        customers={customers}
        onClose={() => setIsInvoiceModalOpen(false)}
        onSave={handleSaveInvoice}
      />

      <SettingsModal
        isOpen={isSettingsModalOpen}
        settings={settings}
        onClose={() => setIsSettingsModalOpen(false)}
        onSave={s => {
          db.updateSettings(s);
          reloadData();
        }}
      />

      <DatabaseToolsModal
        isOpen={isDbToolsModalOpen}
        onClose={() => setIsDbToolsModalOpen(false)}
        onDataChanged={reloadData}
      />

      <BluetoothModal
        isOpen={isBluetoothModalOpen}
        onClose={() => setIsBluetoothModalOpen(false)}
      />

      <AboutModal
        isOpen={isAboutModalOpen}
        storeName={settings.storeName}
        phone={settings.phone}
        onClose={() => setIsAboutModalOpen(false)}
      />

      <PrintStatementModal
        isOpen={isPrintModalOpen}
        customer={printCustomer}
        transactions={
          printCustomer
            ? transactions.filter(t => t.customerId === printCustomer.id)
            : []
        }
        storeName={settings.storeName}
        phone={settings.phone}
        onClose={() => {
          setIsPrintModalOpen(false);
          setPrintCustomer(null);
        }}
      />
    </div>
  );
}
export default App;
