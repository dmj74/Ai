# تیتانلی (Titanali) 🤖✨

اپلیکیشن اندروید هوش مصنوعی با چت متنی، مکالمهٔ صوتی با کارکتر **تیتانلی**، و بخش کامل **آموزش زبان** — متصل به چند سرویس هوش مصنوعی **رایگان**.

![CI](https://github.com/dmj74/Ai/actions/workflows/ci.yml/badge.svg)

## امکانات

| بخش | توضیح |
|---|---|
| 💬 **چت** | چت معمولی با پاسخ پلکانی (streaming)، تاریخچهٔ گفتگوها (Room)، انتخاب مدل از نوار بالا، کپی متن با نگه‌داشتن انگشت؛ بدون تزریق آنالیز طولانی |
| 🤖 **تیتانلی** | کارکتر هوش مصنوعی با شخصیت صمیمی، باهوش و بامزه — دو حالت: **چت متنی** و **مکالمهٔ صوتی** (شنود → فکر → پاسخ با صدا، به‌صورت حلقهٔ پیوسته) |
| 🌍 **بخش مستقل آنالیز** | از نوار پایین وارد «آنالیز» شو و برای آمار، علل، روند، مقایسهٔ جهانی، دیدگاه‌های مختلف و نتیجه‌گیری سؤال جداگانه بپرس |
| 📚 **آموزش زبان** | ۳ زبان (انگلیسی، آلمانی، عربی) با سطوح A1–A2، فلش‌کارت واژگان با نمونه‌جمله، گرامر دوزبانه، کلمهٔ روز، پیشرفت «یاد گرفتم» و **تمرین مکالمه با معلم AI** که اشتباهاتت را اصلاح می‌کند |
| 🔌 **۹ بک‌اند هوش مصنوعی** | Pollinations و LLM7 بدون کلید، به‌علاوهٔ Groq، Google Gemini، OpenRouter، Hugging Face، Cerebras، Mistral و Ollama (کاملاً محلی) |
| 🧪 **آزمایش اتصال** | دکمهٔ «آزمایش اتصال» در تنظیمات، کلید/مدل/نت را با یک درخواست واقعی بررسی می‌کند؛ فهرست مدل‌ها هم به‌صورت زنده از خود سرویس گرفته می‌شود |
| 🗣️ **صوت** | TTS و STT بدون هزینه از امکانات خود اندروید؛ زبان فارسی/انگلیسی و سرعت صدا قابل تنظیم |
| 🌐 **دوزبانه** | رابط فارسی (راست‌چین) + انگلیسی، قابل تغییر در تنظیمات |
| 🔒 **حریم خصوصی** | کلیدها فقط روی خود دستگاه ذخیره می‌شوند و از پشتیبان‌گیری ابری مستثنا هستند |

## دانلود APK

- **هر پوش به مخزن**: از تب **Actions → CI → آخرین اجرا → Artifacts** فایل `titanali-debug-apk` را بگیر.
- **نسخهٔ رسمی**: با تگ `v*` (مثلاً `v1.4.0`) ورک‌فلو **Release** یک APK مینیفای‌شده روی صفحهٔ Releases منتشر می‌کند.

## ساخت اپ

1. پروژه را در **Android Studio** (Hedgehog یا جدیدتر) باز کن: `File → Open` و پوشهٔ مخزن را انتخاب کن.
2. بگذار Sync شود (Gradle 8.7 + AGP 8.5.2 خودکار دانلود می‌شوند).
3. `Run` → APK روی گوشی/امولاتور نصب می‌شود.
   - یا از خط فرمان: `./gradlew assembleDebug`
   - تست‌های واحد: `./gradlew :app:testDebugUnitTest`

## وصل کردن هوش مصنوعی رایگان (یک‌بار، در صفحهٔ تنظیمات)

| سرویس | دریافت کلید رایگان | مدل‌های پیشنهادی (۲۰۲۶) |
|---|---|---|
| **Pollinations** (پیشنهادی — بدون کلید) | برای استفادهٔ پایه کلید لازم نیست؛ [کلید اختیاری](https://enter.pollinations.ai/keys)؛ در خطای موقت به LLM7 می‌رود | `openai-fast` |
| **LLM7** (بدون ثبت‌نام) | [توکن رایگان اختیاری](https://token.llm7.io/) | `mistral-Nemo-Instruct-2407`، `minimax-m2.7` |
| **Groq** (خیلی سریع) | [console.groq.com/keys](https://console.groq.com/keys) | `openai/gpt-oss-120b`، `openai/gpt-oss-20b` |
| **Google Gemini** | [aistudio.google.com](https://aistudio.google.com/app/apikey) | `gemini-2.5-flash`، `gemini-2.5-flash-lite` |
| **OpenRouter** (مدل‌های `:free`) | [openrouter.ai/keys](https://openrouter.ai/keys) | `openrouter/free` (انتخاب خودکار مدل رایگان) |
| **Hugging Face** | [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens) | `meta-llama/Llama-3.3-70B-Instruct` |
| **Cerebras** (خیلی سریع) | [cloud.cerebras.ai](https://cloud.cerebras.ai/) | `llama-3.3-70b`، `qwen-3-32b` |
| **Mistral** | [console.mistral.ai/api-keys](https://console.mistral.ai/api-keys) | `mistral-small-latest` |
| **Ollama** (مستقل و آفلاین) | روی PC نصب کن: `ollama pull llama3.1` | `llama3.1` |

چت معمولی عمداً پاسخ‌های کوتاه و مستقیم می‌دهد؛ تحلیل ساختاریافته فقط از بخش مستقل «آنالیز» اجرا می‌شود.

برای Ollama: نشانی را در تنظیمات بنویس — امولاتور: `http://10.0.2.2:11434`؛ گوشی واقعی: IP کامپیوتر مثل `http://192.168.1.10:11434`.

> نکته: کلید و مدل **به‌تفکیک هر سرویس** ذخیره می‌شود؛ یعنی با سوییچ بین سرویس‌ها هیچ چیزی گم نمی‌شود.
>
> برای حفظ امنیت، هیچ کلید خصوصی داخل APK یا مخزن قرار نگرفته است. دو گزینهٔ اول بدون کلید کار می‌کنند؛ کلیدهای سرویس‌های دیگر شخصی هستند و باید از لینک رسمی داخل تنظیمات ساخته و فقط روی دستگاه خودت وارد شوند.

## شخصیت تیتانلی

تیتانلی یک دوست هوشمند با قوت یک تایتان است: صمیمی، بامزه و پرانرژی، ولی همیشه تحلیل‌محور و صادق. در حالت صوتی جواب‌های کوتاه و قابل گفتار می‌دهد و مکالمهٔ زنده (مثل یک دوست واقعی) پیش می‌رود.

## ساختار پروژه

```
app/src/main/java/com/titanali/app/
├── TitanaliApp.kt            # Application + dependency wiring
├── MainActivity.kt           # Compose root + RTL/LTR + edge-to-edge
├── ai/                       # لایهٔ هوش مصنوعی
│   ├── AiProvider.kt         # رابط مشترک (کلید/مدل/مدل‌های زنده)
│   ├── AiErrors.kt           # نگاشت خطاها به کلیدهای قابل ترجمه
│   ├── SseParser.kt          # تجزیهٔ SSE/NDJSON (خالص و تست‌پذیر)
│   ├── Providers.kt          # ۹ سرویس + registry
│   ├── AiTypes.kt            # DTOها
│   └── Prompts.kt            # پرامپت‌ها: آنالیز جهانی + شخصیت تیتانلی + معلم زبان
├── data/
│   ├── db/                   # Room: گفتگوها، پیام‌ها، پیشرفت واژگان (v2 + migration)
│   ├── repo/SettingsRepo.kt  # تنظیمات (SharedPreferences + StateFlow، کلید per-provider)
│   └── learn/LessonData.kt   # درس‌های سه‌زبانه (افزودن زبان = یک LanguagePack جدید)
├── ui/
│   ├── nav/AppNav.kt         # ناوبری + نوار پایین
│   ├── voice/                # TtsManager (TTS) و ListenerManager (STT)
│   ├── components/           # حباب پیام، نوار ورودی، نگاشت متن خطا…
│   ├── theme/                # تم متریال 3 (روشن/تیره)
│   └── screens/              # Home، Chat، Analysis، Titanali، Learn، Lesson، Settings
└── vm/                       # ViewModelها (چت، آنالیز، تیتانلی، آموزش، تنظیمات)
app/src/test/                 # تست‌های واحد JVM (پارسر، خطاها، درس‌ها، پرامپت‌ها)
```

## CI / CD

- `.github/workflows/ci.yml` — روی هر پوش و PR: تست‌های واحد + APK دیباگ + APK ریلیز مینیفای‌شده + lint (گزارشی)؛ APK به‌صورت artifact آپلود می‌شود.
- `.github/workflows/release.yml` — روی تگ `v*`: ساخت و انتشار APK روی GitHub Releases.

## نکات

- کلیدها فقط روی خود دستگاه (SharedPreferences) ذخیره می‌شوند، مستقیم به API هر سرویس می‌روند و از backup ابری/انتقال دستگاه مستثنا هستند؛ سرور میانی وجود ندارد.
- برای مکالمهٔ صوتی، گوشی به Google TTS/شنودگر نیاز دارد (در بیشتر گوشی‌ها پیش‌فرض فعال است)؛ زبان فارسی TTS را می‌شود از اپ Google TTS نصب کرد.
- minSdk 26 (Android 8.0) | Kotlin + Jetpack Compose + Material 3 + Room + OkHttp.
- تاریخچهٔ تغییرات: [CHANGELOG.md](CHANGELOG.md)

---

## Titanali (English summary)

An Android AI companion app: streaming chat with history, a voiced Persian-speaking character ("Titanali") with a continuous listen→think→speak loop, a language-learning section (EN/DE/AR lessons, flashcards, AI tutor practice), and nine free AI backends (keyless Pollinations and LLM7, plus Groq, Gemini, OpenRouter, Hugging Face, Cerebras, Mistral, and local Ollama). Keys stay on-device. Built with Kotlin, Jetpack Compose (Material 3), Room and OkHttp; unit-tested and CI-built on GitHub Actions.
