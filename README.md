# تیتانالی (Titanali) 🤖✨

اپلیکیشن اندروید هوش مصنوعی با چت متنی، مکالمه صوتی با کارکتر **تیتانالی**، و بخش کامل **آموزش زبان** — متصل به چند سرویس هوش مصنوعی **رایگان**.

## امکانات

| بخش | توضیح |
|---|---|
| 💬 **چت** | چت مثل ChatGPT با پاسخ پلکانی (streaming)، تاریخچه گفتگوها (Room)، انتخاب مدل از نوار بالا |
| 🤖 **تیتانالی** | کارکتر هوش مصنوعی با شخصیت صمیمی، باهوش و بامزه — دو حالت: **چت متنی** و **مکالمه صوتی** (شنود → فکر → پاسخ با صدا، به‌صورت حلقهٔ پیوسته) |
| 🌍 **آنالیز کامل از داده‌های جهانی** | سوئیچ در تنظیمات؛ پاسخ‌ها با ساختار کامل: پاسخ مستقیم، آمار و ارقام، علل، روند، مقایسهٔ جهانی، دیدگاه‌های مختلف، نتیجه‌گیری |
| 📚 **آموزش زبان** | ۳ زبان (انگلیسی، آلمانی، عربی) با سطوح A1–A2، فلش‌کارت واژگان با نمونه‌جمله، گرامر دوزبانه، کلمهٔ روز، پیشرفت «یاد گرفتم» و **تمرین مکالمه با معلم AI** که اشتباهاتت را اصلاح می‌کند |
| 🔌 **چند هوش مصنوعی رایگان** | Groq، Google Gemini، Hugging Face، OpenRouter و Ollama (کاملاً محلی) — کلید رایگان هرکدام از صفحهٔ تنظیمات |
| 🗣️ **صوت** | TTS و STT بدون هزینه از امکانات خود اندروید؛ زبان فارسی/انگلیسی و سرعت صدا قابل تنظیم |
| 🌐 **دوزبانه** | رابط فارسی (راست‌چین) + انگلیسی، قابل تغییر در تنظیمات |

## ساخت اپ

1. پروژه را در **Android Studio** (Hedgehog یا جدیدتر) باز کن: `File → Open` و پوشهٔ `Ai` را انتخاب کن.
2. بگذار Sync شود (Gradle 8.7 + AGP 8.5.2 خودکار دانلود می‌شوند).
3. `Run` → APK روی گوشی/امولاتور نصب می‌شود.
   - یا از خط فرمان: `./gradlew assembleDebug` (در صورت نبود wrapper: یک‌بار `gradle wrapper` اجرا کن).

> ⚠️ این مخزن فایلهای پروژهٔ کامل اندروید است؛ برای نصب روی گوشی باید با Android Studio build شود.

## وصل کردن هوش مصنوعی رایگان (یک‌بار، در صفحهٔ تنظیمات)

هر سرویس رایگان است؛ فقط کلید لازم داری (Ollama هیچ‌چیز لازم ندارد):

| سرویس | دریافت کلید رایگان | مدل‌های پیشنهادی |
|---|---|---|
| **Groq** (پیشنهادی — خیلی سریع) | [console.groq.com/keys](https://console.groq.com/keys) | `llama-3.3-70b-versatile` |
| **Google Gemini** | [aistudio.google.com](https://aistudio.google.com/app/apikey) | `gemini-2.0-flash` |
| **Hugging Face** | [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens) | `meta-llama/Meta-Llama-3.1-8B-Instruct` |
| **OpenRouter** (مدل‌های `:free`) | [openrouter.ai/keys](https://openrouter.ai/keys) | `meta-llama/llama-3.3-70b-instruct:free` |
| **Ollama** (مستقل و آفلاین) | روی PC نصب کن: `ollama pull llama3.1` | `llama3.1` |

برای Ollama: نشانی را در تنظیمات بنویس — امولاتور: `http://10.0.2.2:11434`؛ گوشی واقعی: IP کامپیوتر مثل `http://192.168.1.10:11434`.

## شخصیت تیتانالی

تیتانالی یک دوست هوشمند با قوت یک تایتان است: صمیمی، بامزه و پرانرژی، ولی همیشه تحلیل‌محور و صادق. در حالت صوتی جواب‌های کوتاه و قابل گفتار می‌دهد و مکالمهٔ زنده (مثل یک دوست واقعی) پیش می‌رود.

## ساختار پروژه

```
app/src/main/java/com/titanali/app/
├── TitanaliApp.kt            # Application + dependency wiring
├── MainActivity.kt           # Compose root + RTL/LTR
├── ai/                       # لایهٔ هوش مصنوعی
│   ├── AiProvider.kt         # رابط مشترک
│   ├── OpenAiCompatible.kt   # پایهٔ Groq/OpenRouter/HF (SSE streaming)
│   ├── Providers.kt          # 5 سرویس + registry
│   ├── AiTypes.kt            # DTOها
│   └── Prompts.kt            # پرامپت‌ها: آنالیز جهانی + شخصیت تیتانالی + معلم زبان
├── data/
│   ├── db/                   # Room: گفتگوها، پیام‌ها، پیشرفت واژگان
│   ├── repo/SettingsRepo.kt  # تنظیمات (SharedPreferences + StateFlow)
│   └── learn/LessonData.kt   # درس‌های سه‌زبانه (افزودن زبان = یک LanguagePack جدید)
├── ui/
│   ├── nav/AppNav.kt         # ناوبری + نوار پایین
│   ├── voice/                # TtsManager (TTS) و ListenerManager (STT)
│   ├── components/           # حباب پیام، نوار ورودی، …
│   ├── theme/                # تم متریال 3 (روشن/تیره)
│   └── screens/              # Home، Chat، Titanali، Learn، Lesson، Settings
└── vm/                       # ViewModelها (حالت چت، تیتانالی، آموزش)
```

## نکات

- کلیدها فقط روی خود دستگاه (SharedPreferences) ذخیره می‌شوند و مستقیم به API هر سرویس می‌روند؛ سرور میانی وجود ندارد.
- برای مکالمه صوتی، گوشی به Google TTS/شنودگر نیاز دارد (در بیشتر گوشی‌ها پیش‌فرض فعال است)؛ زبان فارسی TTS را می‌شود از اپ Google TTS نصب کرد.
- minSdk 26 (Android 8.0) | Kotlin + Jetpack Compose + Material 3 + Room + OkHttp.
<!-- build verified: 20260922 -->
