// Persistent suggestions service for details, item names, and notes

const STORAGE_KEY = 'tamim_custom_suggestions_v1';

// Initial built-in suggestions for grocery, supermarket, and debt records
const DEFAULT_SUGGESTIONS = [
  'سكر 1 كجم',
  'أرز الشعلان 10 كجم',
  'أرز هندي حبة طويلة',
  'دقيق كويتي فاخر',
  'دقيق بر صوامع',
  'زيت طبخ عافية',
  'زيت دوار الشمس',
  'شاي كبوس أحمر',
  'حليب دانو مجفف',
  'حليب مبخر ممتاز',
  'تونا الخير 185 جم',
  'فاصوليا حمراء مطبوخة',
  'صلصة طماطم هناء',
  'مكرونة قودي مشكلة',
  'بيض طبق 30 حبة',
  'زبادي الهناء عائلي',
  'جبنة كرافت شيدر',
  'جبنة مثلثات لافاش كيري',
  'صابون غسيل تايد',
  'معجون أسنان سيجنال',
  'مياه صحية كرتون',
  'سداد حساب نقداً',
  'دفعة من الحساب',
  'مشتريات بقالة بالآجل',
  'حساب قديم مقيد',
  'شراء مواد غذائية',
  'مشروبات وغازيات',
  'بسكويت وشوكولاتة',
  'شحن رصيد كروت',
];

export class SuggestionsService {
  private customSuggestions: string[] = [];

  constructor() {
    this.load();
  }

  private load() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed)) {
          this.customSuggestions = parsed;
        }
      }
    } catch {
      this.customSuggestions = [];
    }
  }

  private save() {
    try {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify(this.customSuggestions.slice(0, 300))
      );
    } catch {
      // Storage full or unavailable
    }
  }

  public addSuggestion(text: string) {
    const trimmed = text.trim();
    if (!trimmed || trimmed.length < 2) return;

    // Remove if already exists, then unshift to front
    this.customSuggestions = [
      trimmed,
      ...this.customSuggestions.filter(s => s !== trimmed),
    ].slice(0, 300);

    this.save();
  }

  public getSuggestions(query: string = '', extraContextNotes: string[] = []): string[] {
    const all = Array.from(
      new Set([
        ...this.customSuggestions,
        ...extraContextNotes.filter(n => n && n.trim().length > 1),
        ...DEFAULT_SUGGESTIONS,
      ])
    );

    const q = query.trim().toLowerCase();
    if (!q) {
      return all.slice(0, 25);
    }

    return all
      .filter(item => item.toLowerCase().includes(q))
      .slice(0, 25);
  }
}

export const suggestionsService = new SuggestionsService();
