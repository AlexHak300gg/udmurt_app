# «Ӟеч-а бур-а удмурт кыл?» — мобильное приложение

Игровое приложение для изучения удмуртского языка детьми 5–10 лет.
**По умолчанию — полностью локальное (100% офлайн).** Supabase-слой написан и готов к включению, но выключен флагом `SUPABASE_ENABLED = false`.

## Стек

Kotlin 2.3 · Jetpack Compose + Material 3 · Clean Architecture + MVI (State → Intent → Effect) · Hilt · Coroutines/Flow · Room + DataStore · WorkManager · TTS (locale `udm`, fallback `ru-RU`) · Supabase (opt-in) · minSdk 26 / target 37 · Gradle KTS + Version Catalog.

## Сборка и запуск

```bash
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # unit-тесты (36)
./gradlew assembleDebugAndroidTest   # компиляция Compose UI-тестов
./gradlew connectedDebugAndroidTest  # Compose UI-тесты (нужен эмулятор/устройство)
./gradlew assembleRelease        # релиз (minify + shrink)
```

Секреты Supabase не нужны: сборка падает без них не будет — значения по умолчанию пустые.

## Структура

```
mobile-app/
├── gradle/libs.versions.toml        # Version Catalog
├── local.properties                 # sdk.dir + (опционально) supabase.*
├── supabase/                        # SQL-схема + сид (сейчас не применяются)
│   ├── migrations/0001_init.sql     # 10 таблиц, RLS, триггеры, индексы
│   └── seed.sql                     # 13 тем, 104 слова, 30 дней word_of_day
└── app/src/main/
    ├── assets/content/content.json  # весь словарь (офлайн-контент)
    ├── kotlin/org/udmurtlang/learn/
    │   ├── core/                    # designsystem, media(TTS/SFX/вибро), notifications, common
    │   ├── data/                    # Room, DataStore, assets, DI, репозитории, supabase, worker'ы
    │   ├── domain/                  # модели, use-cases, интерфейсы репозиториев (чистый Kotlin)
    │   └── feature/                 # экраны: splash, onboarding, auth, menu, learning,
    │                                #   games(6), stats, report, wordofday, profile, settings
    └── res/values(-udm)/            # строки: 144 ru + 144 udm
```

Слои разделены, зависимость идёт только внутрь (`feature → domain ← data`); DI-модули в `data/di`.

## Экраны

Splash → Onboarding (3 страницы, пропускается) → Auth (или гость) → Главное меню (приветствие, streak, слово дня, 4 плитки) → Обучение (13 тем, карточки со свайпом, «Знаю/Повторить», конфетти) · Игры (6: выбери перевод, лишнее слово, сортировка, память, собери слово, правда/ложь) · Статистика (графики, топ ошибок) · Отчёт для родителей (share) · Профиль (факты-карусель, медали, смена пароля, удаление аккаунта) · Настройки (звук, уведомления, геолокация, язык ru/udm, тема).

## Как включить Supabase позже

1. `local.properties`:
   ```properties
   supabase.enabled=true
   supabase.url=https://<project>.supabase.co
   supabase.anonKey=<anon-key>
   ```
2. Применить миграции: `supabase start` локально или создать проект в облаке → SQL Editor → выполнить `supabase/migrations/0001_init.sql` и `supabase/seed.sql` (подробнее — `supabase/README.md`).
3. Включить Auth (e-mail/password) и (опционально) Storage-бакет `media`.
4. Пересобрать: `./gradlew assembleDebug`.

Что меняется при включении (без правок экранов): `SupabaseProvider` инициализируется, экран входа переключается на Supabase Auth, `SyncWorker` каждые 15 мин отправляет офлайн-очередь (`user_progress`, `game_results`, `achievements`) upsert-ом, локальный прогресс сливается с облачным (максимум по слову, сумма статистики).

## Ключевые архитектурные решения

| Решение | Почему |
|---|---|
| Офлайн-первый: контент в `assets/content/content.json`, прогресс в Room/DataStore | Ни одного сетевого запроса в рантайме при `SUPABASE_ENABLED=false`; старт < 1.5 с, seeding на IO с ленивой инициализацией |
| Схема Room 1-в-1 с Supabase-схемой + офлайн-очередь с `client_uuid` | Синхронизация = маппинг payload → upsert, идемпотентно «last-write-wins» |
| Локальный аккаунт с PBKDF2-хешем через тот же `AccountRepository` | Один UI входа в обоих режимах; переключение = смена DI-бинда (`SupabaseModule`) |
| MVI (State→Intent→Effect) на каждый экран | Тестируемые вьюмодели без Android-зависимостей (JUnit + Turbine) |
| Озвучка TTS вместо записей | ~0 МБ аудио вместо 5–10 МБ, живое произношение, locale `udm` с fallback |
| Достижения пересчитываются use-case-ом после игры/занятия | Идемпотентно, без гонок: разблокировка — insert OR IGNORE + запись в очередь синхронизации |
| Язык интерфейса через `AppCompatDelegate.setApplicationLocales` + `autoStoreLocales` | Системное хранение per-app language, полный набор строк ru/udm, без перезапуска |
| Уведомление «Слово дня» — ежедневный `PeriodicWorkRequest` в 9:00 | Слово детерминировано по дате (офлайн), воркер сам отключается при выключенной настройке |
| Программные SFX (`ToneGenerator`) + вибрация | 0 байт аудио-ассетов, мгновенная обратная связь |
| Эмодзи-иллюстрации вместо WebP/VectorDrawable | Минимальный размер APK (≈25 МБ), единый стиль, оффлайн |

## Тесты

- **Unit (36):** игровые алгоритмы (`GameRoundGenerator`, `GameScoring`), пароль (PBKDF2), контент-JSON (валидирует реальный ассет), вьюмодели (auth, oddoneout, sorting), разблокировка достижений (`UnlockAchievementsUseCaseTest`).
- **Compose UI (smoke):** `MenuScreenTest`, `AuthScreenTest` (валидация формы), `GamesHubScreenTest` (все 6 игр + клик), `DesignSystemTest` (кнопки, карточка слова, орнамент). Hilt-тестовое приложение + in-memory Room (`TestModules.kt`).
