import React, { useState, useEffect } from 'react';
import { Product } from '../types';
import { X, Package, Barcode } from 'lucide-react';

interface ProductModalProps {
  isOpen: boolean;
  product?: Product | null;
  onClose: () => void;
  onSave: (
    name: string,
    barcode: string,
    buyPrice: number,
    sellPrice: number,
    stock: number,
    minStock: number,
    notes: string
  ) => void;
}

export const ProductModal: React.FC<ProductModalProps> = ({
  isOpen,
  product,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState('');
  const [barcode, setBarcode] = useState('');
  const [buyPrice, setBuyPrice] = useState('');
  const [sellPrice, setSellPrice] = useState('');
  const [stock, setStock] = useState('');
  const [minStock, setMinStock] = useState('');
  const [notes, setNotes] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (product) {
      setName(product.name);
      setBarcode(product.barcode || '');
      setBuyPrice(product.buyPrice ? product.buyPrice.toString() : '');
      setSellPrice(product.sellPrice ? product.sellPrice.toString() : '');
      setStock(product.stock ? product.stock.toString() : '');
      setMinStock(product.minStock ? product.minStock.toString() : '');
      setNotes(product.notes || '');
    } else {
      setName('');
      setBarcode('');
      setBuyPrice('');
      setSellPrice('');
      setStock('');
      setMinStock('5');
      setNotes('');
    }
    setError('');
  }, [product, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('اسم الصنف مطلوب');
      return;
    }
    onSave(
      name.trim(),
      barcode.trim(),
      parseFloat(buyPrice) || 0,
      parseFloat(sellPrice) || 0,
      parseFloat(stock) || 0,
      parseFloat(minStock) || 0,
      notes.trim()
    );
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-black text-base text-[#146B50]">
            {product ? 'تعديل بيانات الصنف' : 'إضافة صنف جديد للمخزون'}
          </h3>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="p-2.5 bg-red-50 border border-red-200 rounded-xl text-xs text-[#BE3232] font-semibold">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              اسم الصنف <span className="text-[#BE3232]">*</span>
            </label>
            <div className="relative">
              <input
                id="input-product-name"
                type="text"
                required
                placeholder="مثال: سكر السعيد 5 كجم"
                value={name}
                onChange={e => setName(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-sm focus:bg-white focus:outline-hidden focus:border-[#146B50]"
                autoFocus
              />
              <Package className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              الباركود (اختياري)
            </label>
            <div className="relative">
              <input
                id="input-product-barcode"
                type="text"
                placeholder="رقم الباركود الدولي أو الداخلي"
                value={barcode}
                onChange={e => setBarcode(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 pr-8 pl-3 text-xs font-mono text-gray-800 focus:bg-white focus:outline-hidden focus:border-[#146B50]"
              />
              <Barcode className="w-4 h-4 text-gray-400 absolute right-2.5 top-3 pointer-events-none" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                سعر الشراء (التكلفة)
              </label>
              <input
                id="input-product-buy-price"
                type="number"
                step="any"
                placeholder="0"
                value={buyPrice}
                onChange={e => setBuyPrice(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm font-bold text-gray-800 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                سعر البيع
              </label>
              <input
                id="input-product-sell-price"
                type="number"
                step="any"
                placeholder="0"
                value={sellPrice}
                onChange={e => setSellPrice(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm font-bold text-[#146B50] focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                الكمية الحالية
              </label>
              <input
                id="input-product-stock"
                type="number"
                step="any"
                placeholder="0"
                value={stock}
                onChange={e => setStock(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm font-bold text-gray-800 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 mb-1">
                حد التنبيه الأدنى
              </label>
              <input
                id="input-product-min-stock"
                type="number"
                step="any"
                placeholder="5"
                value={minStock}
                onChange={e => setMinStock(e.target.value)}
                className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2.5 px-3 text-sm font-bold text-gray-800 focus:bg-white focus:outline-hidden focus:border-[#146B50] font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 mb-1">
              ملاحظات
            </label>
            <input
              id="input-product-notes"
              type="text"
              placeholder="ملاحظات حول المورد أو الصنف..."
              value={notes}
              onChange={e => setNotes(e.target.value)}
              className="w-full bg-[#F7F9F8] border border-[#E1E8E4] rounded-xl py-2 px-3 text-xs focus:bg-white focus:outline-hidden focus:border-[#146B50]"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100"
            >
              إلغاء
            </button>
            <button
              id="btn-save-product"
              type="submit"
              className="px-5 py-2 rounded-xl text-xs font-bold bg-[#915F28] hover:bg-[#724a1f] text-white shadow-2xs"
            >
              حفظ
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
