import React, { useState } from 'react';
import { X, Bluetooth, Printer, CheckCircle, AlertCircle, RefreshCw } from 'lucide-react';

interface BluetoothModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const BluetoothModal: React.FC<BluetoothModalProps> = ({ isOpen, onClose }) => {
  const [connectedDevice, setConnectedDevice] = useState<string | null>(null);
  const [status, setStatus] = useState<string>('الطابعة غير متصلة حالياً');
  const [loading, setLoading] = useState(false);
  const [paperWidth, setPaperWidth] = useState<'58' | '80'>('80');

  if (!isOpen) return null;

  const handleScanBluetooth = async () => {
    if (!('bluetooth' in navigator)) {
      setStatus('ميزة Web Bluetooth غير مدعومة في هذا المتصفح. يمكنك استخدام أمر الطباعة القياسي (Print Dialog) للطباعة عبر أي طابعة حرارية معرفة.');
      return;
    }

    try {
      setLoading(true);
      setStatus('جارٍ البحث عن طابعات بلوتوث قريبة...');
      const device = await (navigator as any).bluetooth.requestDevice({
        acceptAllDevices: true,
        optionalServices: ['000018f0-0000-1000-8000-00805f9b34fb'],
      });

      if (device && device.name) {
        setConnectedDevice(device.name);
        setStatus(`تم الاقتران بنجاح مع: ${device.name}`);
      } else {
        setStatus('تم العثور على جهاز غير مسمى.');
      }
    } catch (err: any) {
      if (err.name === 'NotFoundError') {
        setStatus('تم إلغاء عملية البحث.');
      } else {
        setStatus(`تعذر الاتصال عبر البلوتوث: ${err.message || 'يرجى التأكد من تشغيل البلوتوث والأذونات'}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleTestPrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-md p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <div className="flex items-center gap-2">
            <Bluetooth className="w-5 h-5 text-[#4664B4]" />
            <h3 className="font-black text-base text-gray-900">
              إعدادات الطابعة الحرارية والبلوتوث
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Status display */}
        <div className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-2xl p-3.5 space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold text-gray-700">
            <Printer className="w-4 h-4 text-[#146B50]" />
            <span>حالة الطابعة:</span>
          </div>
          <p className="text-xs text-gray-600 leading-relaxed">{status}</p>
          {connectedDevice && (
            <div className="flex items-center gap-1.5 text-xs text-[#146B50] font-bold pt-1">
              <CheckCircle className="w-4 h-4" />
              <span>متصل: {connectedDevice}</span>
            </div>
          )}
        </div>

        {/* Paper width setting */}
        <div>
          <label className="block text-xs font-bold text-gray-700 mb-1.5">
            عرض ورق الطباعة الحراري
          </label>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setPaperWidth('80')}
              className={`py-2 px-3 rounded-xl text-xs font-bold border transition-colors ${
                paperWidth === '80'
                  ? 'bg-[#146B50] text-white border-[#146B50]'
                  : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
              }`}
            >
              80 ملم (قياسي كبير)
            </button>
            <button
              type="button"
              onClick={() => setPaperWidth('58')}
              className={`py-2 px-3 rounded-xl text-xs font-bold border transition-colors ${
                paperWidth === '58'
                  ? 'bg-[#146B50] text-white border-[#146B50]'
                  : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
              }`}
            >
              58 ملم (صغير متنقل)
            </button>
          </div>
        </div>

        {/* Action buttons */}
        <div className="space-y-2 pt-2 border-t border-gray-100">
          <button
            id="btn-scan-bluetooth"
            disabled={loading}
            onClick={handleScanBluetooth}
            className="w-full flex items-center justify-center gap-2 bg-[#4664B4] hover:bg-[#344d91] text-white py-3 px-4 rounded-xl text-xs font-bold shadow-2xs transition-all active:scale-95 disabled:opacity-50"
          >
            <Bluetooth className="w-4 h-4" />
            <span>{loading ? 'جارٍ البحث عن أجهزة...' : 'بحث واقتران بطابعة بلوتوث'}</span>
          </button>

          <button
            id="btn-test-print"
            onClick={handleTestPrint}
            className="w-full flex items-center justify-center gap-2 bg-gray-100 hover:bg-gray-200 text-gray-800 py-2.5 px-4 rounded-xl text-xs font-bold transition-all"
          >
            <Printer className="w-4 h-4" />
            <span>طباعة صفحة تجريبية (نافذة الطباعة)</span>
          </button>
        </div>

        <div className="flex justify-end pt-1">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold text-gray-600 hover:bg-gray-100"
          >
            إغلاق
          </button>
        </div>
      </div>
    </div>
  );
};
