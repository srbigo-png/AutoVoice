# 📋 Action.md — טבלת האמת של AutoVoice (live)

> זהו המקור היחיד למצב, פיצ'רים ושלבי עבודה. קרא אותי בתחילת כל סשן, ועדכן אותי + push בסוף כל צעד.
> Updated: 2026-06-29 | Synced by: Claude Code

---

## פרויקט
- **שם:** AutoVoice
- **ריפו:** https://github.com/srbigo-png/AutoVoice — ענף ברירת מחדל: `main`
- **שורש git מקומי:** `C:\Users\salon\Documents\my-project\AutoVoice\AutoVoiceClean`
- **פרויקט Gradle (מקונן בכוונה — ה-CI מצפה לזה):** `...\AutoVoiceClean\AutoVoiceClean`
- **CI:** `.github/workflows/build.yml` — `cd AutoVoiceClean && ./gradlew assembleDebug`, מעלה APK כ-artifact.

## מטרת האפליקציה
שליטה קולית דרך Android Auto:
- ניווט: Waze, Google Maps
- חיוג לאנשי קשר
- הפעלת YouTube
- ניתוק פלייבק וחזרה לרדיו הרכב

## טכנולוגיה (לפי הקוד בפועל — לא Compose)
- Kotlin
- **androidx.car.app 1.4.0** (Android Auto — Car App Library, תבניות `MessageTemplate` וכו')
- AppCompat + XML views (לא Jetpack Compose)
- `SpeechRecognizer` + `TextToSpeech` בעברית (`he-IL`)
- **package:** `com.autovoice.app` · **minSdk:** 23 · **targetSdk/compileSdk:** 34
- תלויות: `androidx.car.app:app:1.4.0`, `core-ktx:1.12.0`, `appcompat:1.6.1`

## עקרונות עבודה
- איכות מסחרית, בלי קיצורי דרך
- חדשנות במקום חיקוי
- אבן אחת בכל פעם, אימות מלא לפני המעבר הבא
- בנייה נקייה מחדש עדיפה על הצטברות טלאים (uninstall → install, בלי `install -r`)
- בלי הסברים חוזרים על נושאים שכבר הוחלטו
- תקשורת: עברית בלבד, ישיר ותמציתי

## כללי טבלת אמת
- Action.md = המקור היחיד. אסור לכתוב ל-WORK.md / STATUS.md (שייכים לפרויקטים אחרים).
- תחילת סשן: קרא את Action.md לפני כל פעולה (RULE 0 — דרך הסקיל `autovoice-startup`).
- סוף כל שינוי / באג שתוקן / צעד שהושלם: עדכן Action.md → commit → push (מסמך-בלבד = אוטומטי, בלי לשאול).
- "אומת" רק אחרי בדיקה בפועל (במכשיר / Desktop Head Unit), לא ניחוש. בנייה שעברה ≠ אומת.

---

## 🟢 מצב נוכחי
- קוד עובד קיים בריפו: שירות Car App ל-Android Auto + זיהוי דיבור עברי + פקודות Waze/חיוג/חיפוש + TTS.
- ⚠️ **הפרויקט לא נבנה כרגע** — חסרים קבצים (ראה באגים B1–B3).
- **הצעד הבא (קונקרטי):** להפוך את הפרויקט לבָּניע — להוסיף gradle-wrapper + אייקונים + `themes.xml`, ואז בנייה נקייה ראשונה.

## 🔧 באגים / חוסרים חוסמי-בנייה
| # | קובץ/אזור | בעיה | מצב |
|---|-----------|------|-----|
| B1 | `AutoVoiceClean/gradle/wrapper/` | חסר `gradle-wrapper.jar` + `gradle-wrapper.properties` → `gradlew` ייכשל (מקומי ו-CI) | ⬜ |
| B2 | `app/src/main/res/mipmap*` | חסרים אייקונים `ic_launcher` / `ic_launcher_round` שהמניפסט מפנה אליהם | ⬜ |
| B3 | `app/src/main/res/values/themes.xml` | חסר; המניפסט מפנה ל-`Theme.AppCompat.Translucent` (לא קיים מובנה) ול-DayNight.NoActionBar | ⬜ |
| B4 | `SpeechService.dial()` | מחייג עם שם איש קשר כ-`tel:<שם>` — לא יעבוד, צריך רזולוציית שם→מספר מאנשי הקשר | ⬜ |

## ⏳ פיצ'רים
| # | פיצ'ר | מצב |
|---|-------|-----|
| F1 | זיהוי קולי עברי + TTS משוב | 🟡 בקוד, טרם אומת במכשיר |
| F2 | ניווט Waze (פתיחה/יעד/חיפוש) | 🟡 בקוד, טרם אומת |
| F3 | חיוג לאנשי קשר | 🟡 חלקי (B4) |
| F4 | מסכי Car App (Main/Help) | 🟡 בקוד, טרם אומת |
| F5 | ניווט Google Maps | ⬜ לא הוחל |
| F6 | הפעלת YouTube | ⬜ לא הוחל |
| F7 | ניתוק פלייבק → רדיו הרכב | ⬜ לא הוחל |

## ✅ החלטות
- מבנה התיקיות המקונן (`AutoVoiceClean/AutoVoiceClean`) נשמר — ה-CI תלוי בו. לא משנים בלי לעדכן את build.yml.
- שיטת העבודה הופעלה דרך סקילים ייעודיים: `autovoice-startup`, `autovoice-qa`, `autovoice-sync` (תחת `.claude/skills/`).

## 📜 יומן צעדים
- 2026-06-29 — שוכפל הריפו, מופתה הקוד הקיים, נוצרה Action.md כטבלת אמת, הופעלה שיטת העבודה (3 סקילים).
