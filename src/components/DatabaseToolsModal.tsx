import React, { useState, useRef } from 'react';
import { db } from '../services/db';
import { X, Database, Download, Upload, CheckCircle2, AlertCircle, RefreshCw, ShieldCheck } from 'lucide-react';

interface DatabaseToolsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDataChanged: () => void;
}

export const DatabaseToolsModal: React.FC<DatabaseToolsModalProps> = ({
  isOpen,
  onClose,
  onDataChanged,
}) => {
  const [loading, setLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ text: string; type: 'success' | 'error' | 'info' } | null>(null);
  const [integrityResult, setIntegrityResult] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  // Export SQLite .db file (compatible with Android app_database.db)
  const handleExportSqlite = async () => {
    try {
      setLoading(true);
      setStatusMessage({ text: 'جارٍ تجهيز ملف SQLite .db...', type: 'info' });
      const binary = await db.exportSqlite();
      const blob = new Blob([new Uint8Array(binary).buffer as ArrayBuffer], { type: 'application/x-sqlite3' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `tamim_accounts_${new Date().toISOString().slice(0, 10)}.db`;
      a.click();
      URL.revokeObjectURL(url);
      setStatusMessage({ text: 'تم تصدير قاعدة بيانات SQLite بنجاح!', type: 'success' });
    } catch (err: any) {
      setStatusMessage({ text: `فشل التصدير: ${err.message || err}`, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // Export JSON backup
  const handleExportJson = () => {
    try {
      const json = db.exportJson();
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `tamim_accounts_backup_${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
      setStatusMessage({ text: 'تم تصدير النسخة الاحتياطية JSON بنجاح!', type: 'success' });
    } catch (err: any) {
      setStatusMessage({ text: `فشل التصدير: ${err.message || err}`, type: 'error' });
    }
  };

  // Handle file import (SQLite .db or JSON)
  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLoading(true);
    setStatusMessage({ text: `جارٍ فحص واستيراد الملف: ${file.name}...`, type: 'info' });

    try {
      if (file.name.endsWith('.json')) {
        const text = await file.text();
        const res = db.importJson(text);
        if (res.ok) {
          setStatusMessage({ text: res.message, type: 'success' });
          onDataChanged();
        } else {
          setStatusMessage({ text: res.message, type: 'error' });
        }
      } else {
        // SQLite file (.db, .sqlite, etc.)
        const buffer = await file.arrayBuffer();
        const result = await db.importSqlite(buffer);
        if (result.ok) {
          setStatusMessage({
            text: result.message,
            type: 'success',
          });
          onDataChanged();
        } else {
          setStatusMessage({ text: result.message, type: 'error' });
        }
      }
    } catch (err: any) {
      setStatusMessage({ text: `حدث خطأ أثناء قراءة الملف: ${err.message || err}`, type: 'error' });
    } finally {
      setLoading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // Run SQLite Integrity Check
  const handleRunIntegrityCheck = async () => {
    try {
      setLoading(true);
      const res = await db.checkIntegrity();
      if (res.ok) {
        setIntegrityResult(`سلامة قاعدة البيانات: ${res.report}. جميع الجداول سليمة 100%.`);
      } else {
        setIntegrityResult(`نتيجة الفحص: ${res.report}`);
      }
    } catch (err: any) {
      setIntegrityResult(`خطأ أثناء الفحص: ${err.message || err}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-2xs">
      <div className="bg-white border border-[#E1E8E4] rounded-[24px] w-full max-w-lg p-5 shadow-2xl space-y-4 animate-in fade-in zoom-in-95 duration-150 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <div className="flex items-center gap-2">
            <Database className="w-5 h-5 text-[#146B50]" />
            <h3 className="font-black text-base text-gray-900">
              إدارة قاعدة البيانات والنسخ الاحتياطي
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {statusMessage && (
          <div
            className={`p-3 rounded-xl text-xs font-semibold flex items-start gap-2 ${
              statusMessage.type === 'success'
                ? 'bg-emerald-50 text-[#146B50] border border-emerald-200'
                : statusMessage.type === 'error'
                ? 'bg-red-50 text-[#BE3232] border border-red-200'
                : 'bg-blue-50 text-blue-700 border border-blue-200'
            }`}
          >
            {statusMessage.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
            ) : (
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        {/* Section 0: Imported Market Database (from saleh12340/Database-only) */}
        <div className="bg-emerald-50/70 border border-emerald-200 rounded-2xl p-4 space-y-2.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-[#0D4D3A] flex items-center gap-1.5">
              <Database className="w-4 h-4 text-[#146B50]" />
              قاعدة بيانات البقالة المستوردة (saleh12340/Database-only)
            </span>
            <span className="text-[10px] bg-emerald-100 text-[#146B50] font-bold px-2 py-0.5 rounded-md">
              279 عميل • 15,902 عملية
            </span>
          </div>
          <p className="text-[11px] text-emerald-800">
            تم جلب وتضمين قاعدة بيانات البقالة الرسمية من المستودع. يمكنك تفعيلها فوراً واستعراض كافة حسابات العملاء والديون والعمليات.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
            <button
              id="btn-load-market-db"
              disabled={loading}
              onClick={async () => {
                setLoading(true);
                setStatusMessage({ text: 'جارٍ استيراد بيانات البقالة الرسمية...', type: 'info' });
                const res = await db.loadMarketDatabase();
                if (res.ok) {
                  setStatusMessage({ text: res.message, type: 'success' });
                  onDataChanged();
                } else {
                  setStatusMessage({ text: res.message, type: 'error' });
                }
                setLoading(false);
              }}
              className="flex items-center justify-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white py-2.5 px-3 rounded-xl text-xs font-bold transition-all shadow-xs disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              <span>تطبيق بيانات البقالة الآن</span>
            </button>

            <a
              href="/market.db"
              download="20260913165909-market.db"
              className="flex items-center justify-center gap-1.5 bg-white border border-emerald-300 hover:bg-emerald-50 text-emerald-900 py-2.5 px-3 rounded-xl text-xs font-bold transition-all shadow-2xs text-center"
            >
              <Download className="w-3.5 h-3.5" />
              <span>تنزيل ملف SQLite الأصلي (.db)</span>
            </a>
          </div>
        </div>

        {/* Section 1: Backup & Export */}
        <div className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-2xl p-4 space-y-2.5">
          <div className="font-bold text-xs text-gray-700">
            تصدير نسخة احتياطية (Backup)
          </div>
          <p className="text-[11px] text-gray-500">
            يمكنك حفظ نسخة من بياناتك بتنسيق SQLite متوافق مع نظام أندرويد الأصلي أو بتنسيق JSON.
          </p>
          <div className="grid grid-cols-2 gap-2 pt-1">
            <button
              id="btn-export-sqlite"
              disabled={loading}
              onClick={handleExportSqlite}
              className="flex items-center justify-center gap-1.5 bg-[#146B50] hover:bg-[#0D4D3A] text-white py-2.5 px-3 rounded-xl text-xs font-bold transition-all shadow-2xs disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              <span>تحميل SQLite (.db)</span>
            </button>

            <button
              id="btn-export-json"
              disabled={loading}
              onClick={handleExportJson}
              className="flex items-center justify-center gap-1.5 bg-white border border-[#E1E8E4] hover:bg-gray-50 text-gray-700 py-2.5 px-3 rounded-xl text-xs font-bold transition-all shadow-2xs disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              <span>تحميل ملف JSON</span>
            </button>
          </div>
        </div>

        {/* Section 2: Import & Restore (matching DatabaseImportManager & DatabaseNormalizer) */}
        <div className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-2xl p-4 space-y-2.5">
          <div className="font-bold text-xs text-gray-700">
            استيراد واستعادة قاعدة بيانات (Restore & Normalization)
          </div>
          <p className="text-[11px] text-gray-500">
            يدعم استيراد ملفات SQLite (.db) المأخوذة من تطبيق أندرويد مع المعالجة التلقائية لهيكل الجداول (Schema Normalizer)، أو ملفات JSON.
          </p>

          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept=".db,.sqlite,.sqlite3,.json"
            className="hidden"
          />

          <button
            id="btn-trigger-import-file"
            disabled={loading}
            onClick={() => fileInputRef.current?.click()}
            className="w-full flex items-center justify-center gap-2 bg-indigo-700 hover:bg-indigo-800 text-white py-3 px-4 rounded-xl text-xs font-bold transition-all shadow-2xs disabled:opacity-50"
          >
            <Upload className="w-4 h-4" />
            <span>{loading ? 'جارٍ المعالجة...' : 'اختيار ملف قاعدة بيانات (.db أو .json)'}</span>
          </button>
        </div>

        {/* Section 3: Integrity Check (matching DatabaseHelper PRAGMA integrity_check) */}
        <div className="bg-[#F7F9F8] border border-[#E1E8E4] rounded-2xl p-4 space-y-2.5">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-gray-700">فحص سلامة قاعدة البيانات</span>
            <button
              id="btn-run-integrity-check"
              disabled={loading}
              onClick={handleRunIntegrityCheck}
              className="flex items-center gap-1 text-[11px] font-bold text-[#146B50] hover:underline"
            >
              <RefreshCw className="w-3 h-3" />
              <span>بدء الفحص (PRAGMA)</span>
            </button>
          </div>

          {integrityResult && (
            <div className="p-2.5 bg-white border border-emerald-200 rounded-xl text-xs text-[#0D4D3A] flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-[#146B50] shrink-0" />
              <span>{integrityResult}</span>
            </div>
          )}
        </div>

        <div className="flex justify-end pt-1">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold bg-gray-100 hover:bg-gray-200 text-gray-700"
          >
            إغلاق
          </button>
        </div>
      </div>
    </div>
  );
};
