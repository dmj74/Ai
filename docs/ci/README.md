# پیشنهاد بازسازی CI / Proposed CI setup

فایلهای این پوشه، جایگزین تمیزِ سه ورک‌فلوی تکراری فعلی (`.github/workflows/main.yml`، `build-apk.yml`، `titanali.yml`) هستند.

## چرا الان داخل `.github/workflows/` نیستند؟

توکن این نشستِ Arena دسترسی `workflows` را ندارد، بنابراین تغییر هر فایل زیرِ `.github/workflows/` توسط ربات ممنوع است. برای فعال‌سازی:

1. در تنظیمات GitHub App مربوطه، دسترسی **Repository permissions → Workflows = Read & write** را بدهید،
2. یا خودتان این دو فایل را جابه‌جا کنید:

```bash
git rm .github/workflows/main.yml .github/workflows/build-apk.yml .github/workflows/titanali.yml
git mv docs/ci/ci.yml .github/workflows/ci.yml
git mv docs/ci/release.yml .github/workflows/release.yml
git commit -m "ci: switch to single clean pipeline"
```

## چه چیزی تغییر می‌کند؟

- یک ورک‌فلوی `ci.yml`: روی هر push (main و شاخه‌های arena) و هر PR → تست‌های واحد + APK دیباگ + APK ریلیز مینیفای‌شده + lint گزارشی + آپلود artifact.
- یک ورک‌فلوی `release.yml`: روی تگ `v*` → انتشار APK در GitHub Releases.
- حذف push خودکار لاگ/APK به شاخهٔ `build-logs` و آپلود لاگ به gist/paste (که در اسکریپت‌های قدیمی وجود داشت و ریسک افشای لاگ داشت).

تا زمانی که این جابه‌جایی انجام نشده، برای ساختِ هر پوش می‌توان ورک‌فلوی فعلی را دستی روی همان شاخه اجرا کرد:

```bash
gh workflow run main.yml --ref <branch>
```
