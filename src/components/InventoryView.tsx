import React, { useState } from 'react';
import { Product } from '../types';
import { formatMoney } from '../utils/formatters';
import { Package, Plus, Search, AlertTriangle, Edit2, Trash2, Barcode } from 'lucide-react';

interface InventoryViewProps {
  products: Product[];
  onAddProduct: () => void;
  onEditProduct: (product: Product) => void;
  onDeleteProduct: (product: Product) => void;
}

export const InventoryView: React.FC<InventoryViewProps> = ({
  products,
  onAddProduct,
  onEditProduct,
  onDeleteProduct,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showLowStockOnly, setShowLowStockOnly] = useState(false);

  const filtered = products.filter(p => {
    const matches =
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.barcode.includes(searchTerm);
    if (!matches) return false;
    if (showLowStockOnly) return p.stock <= p.minStock;
    return true;
  });

  const lowStockCount = products.filter(p => p.stock <= p.minStock).length;
  const totalStockValue = products.reduce((sum, p) => sum + p.stock * p.buyPrice, 0);

  return (
    <div className="space-y-3 pb-24">
      {/* Search and Action Bar */}
      <div className="relative">
        <input
          id="input-search-products"
          type="text"
          placeholder="بحث باسم الصنف أو الباركود..."
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          className="w-full bg-white border border-[#E1E8E4] rounded-2xl py-3 pr-10 pl-4 text-sm focus:outline-hidden focus:border-[#146B50] focus:ring-2 focus:ring-[#146B50]/20 shadow-2xs transition-all"
        />
        <Search className="w-4 h-4 text-gray-400 absolute right-3.5 top-3.5 pointer-events-none" />
      </div>

      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowLowStockOnly(false)}
            className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
              !showLowStockOnly
                ? 'bg-[#146B50] text-white'
                : 'bg-white border border-[#E1E8E4] text-gray-600 hover:bg-gray-50'
            }`}
          >
            كل الأصناف ({products.length})
          </button>
          <button
            onClick={() => setShowLowStockOnly(true)}
            className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
              showLowStockOnly
                ? 'bg-[#BE3232] text-white'
                : 'bg-white border border-[#E1E8E4] text-[#BE3232] hover:bg-red-50'
            }`}
          >
            <AlertTriangle className="w-3 h-3" />
            <span>نواقص المخزون ({lowStockCount})</span>
          </button>
        </div>

        <button
          id="btn-add-product-main"
          onClick={onAddProduct}
          className="flex items-center gap-1.5 bg-[#915F28] hover:bg-[#724a1f] text-white px-3.5 py-2 rounded-xl text-xs font-bold shadow-2xs transition-all active:scale-95 shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>＋ إضافة صنف جديد</span>
        </button>
      </div>

      {/* Summary Note */}
      <div className="bg-white border border-[#E1E8E4] rounded-2xl px-4 py-2.5 flex items-center justify-between text-xs text-gray-600 shadow-2xs">
        <span>
          عدد الأصناف: <strong className="text-gray-900">{filtered.length}</strong>
        </span>
        <span>
          القيمة التقديرية للتكلفة:{' '}
          <strong className="text-[#0D4D3A]">{formatMoney(totalStockValue)} YER</strong>
        </span>
      </div>

      {/* Products List */}
      {filtered.length === 0 ? (
        <div className="bg-white border border-[#E1E8E4] rounded-[22px] p-8 text-center text-gray-500 text-sm space-y-2">
          <Package className="w-8 h-8 mx-auto text-gray-300" />
          <p>
            {searchTerm
              ? 'لا توجد أصناف مطابقة للبحث.'
              : 'المخزون فارغ حالياً. اضغط إضافة صنف جديد للبدء.'}
          </p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {filtered.map(product => {
            const isLow = product.stock <= product.minStock;

            return (
              <div
                key={product.id}
                id={`product-card-${product.id}`}
                className="bg-white border border-[#E1E8E4] rounded-[22px] p-3.5 sm:p-4 shadow-2xs hover:border-[#146B50]/50 transition-colors"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="space-y-1 min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-bold text-base text-gray-900 truncate">
                        {product.name}
                      </h4>
                      {isLow && (
                        <span className="flex items-center gap-1 text-[11px] font-bold text-[#BE3232] bg-red-50 border border-red-200 px-2 py-0.5 rounded-full shrink-0">
                          <AlertTriangle className="w-3 h-3" />
                          ⚠ المخزون منخفض
                        </span>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500">
                      {product.barcode && (
                        <span className="flex items-center gap-1 font-mono text-gray-400">
                          <Barcode className="w-3.5 h-3.5" />
                          {product.barcode}
                        </span>
                      )}
                      {product.notes && (
                        <span className="text-gray-400 truncate">• {product.notes}</span>
                      )}
                    </div>

                    {/* Stock Quantity matching ModernActivity */}
                    <div className="flex items-center gap-4 text-xs pt-1">
                      <span className="text-gray-700">
                        الكمية بالمخزون:{' '}
                        <strong
                          className={`text-sm font-black ${
                            isLow ? 'text-[#BE3232]' : 'text-[#146B50]'
                          }`}
                        >
                          {formatMoney(product.stock)}
                        </strong>
                      </span>
                      <span className="text-gray-400">
                        الحد الأدنى: {product.minStock}
                      </span>
                    </div>
                  </div>

                  {/* Action Controls */}
                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      onClick={() => onEditProduct(product)}
                      className="p-1.5 text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors"
                      title="تعديل الصنف"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteProduct(product)}
                      className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="حذف الصنف"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                {/* Pricing Footer */}
                <div className="mt-3 pt-2.5 border-t border-gray-100 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-3">
                    <span className="text-gray-600">
                      سعر البيع:{' '}
                      <strong className="text-[#146B50] font-bold">
                        {formatMoney(product.sellPrice)} YER
                      </strong>
                    </span>
                    <span className="text-gray-300">•</span>
                    <span className="text-gray-600">
                      سعر الشراء:{' '}
                      <strong className="text-gray-800 font-bold">
                        {formatMoney(product.buyPrice)} YER
                      </strong>
                    </span>
                  </div>

                  {product.sellPrice > product.buyPrice && (
                    <span className="text-emerald-700 font-medium">
                      هامش الربح: +{formatMoney(product.sellPrice - product.buyPrice)} YER
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
