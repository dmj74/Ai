# CI / CD

## وضعیت

- ✅ `.github/workflows/ci.yml` **فعال است** — روی هر push (main و شاخه‌های `arena/**`) و هر PR:
  APK دیباگ → آپلود artifact `titanali-debug-apk` → تست‌های واحد → APK ریلیز مینیفای‌شده (فقط بررسی) → lint (گزارشی).
  اولین اجرا: موفق (run 35910573256).
- ⏸ `docs/ci/release.yml` — انتشار APK در GitHub Releases روی تگ `v*`. برای فعال‌سازی آن را به `.github/workflows/release.yml` منتقل کنید.

## ورک‌فلوهای قدیمی

`main.yml`، `build-apk.yml` و `titanali.yml` فقط روی شاخهٔ قدیمی `arena/01a0ca1e-ai` اجرا می‌شوند و روی این شاخه/`main` کاری نمی‌کنند.
پیشنهاد می‌شود (توسط کسی که دسترسی workflows دارد) حذف شوند:

```bash
git rm .github/workflows/main.yml .github/workflows/build-apk.yml .github/workflows/titanali.yml
git commit -m "ci: remove legacy workflows"
```

## دریافت APK

Actions → CI → آخرین اجرا → Artifacts → `titanali-debug-apk` (zip حاوی `app-debug.apk`).
