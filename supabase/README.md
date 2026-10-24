# Supabase для «Ӟеч-а бур-а удмурт кыл?»

Приложение **по умолчанию полностью локальное**: `SUPABASE_ENABLED = false`,
клиент не инициализируется, сетевых запросов нет. Этот каталог — готовый
«включаемый» бэкенд: схема, RLS, сид.

## Состав

| Файл | Назначение |
|---|---|
| `migrations/0001_init.sql` | Полная схема: profiles, topics, words, user_progress, game_results, daily_activity, word_of_day, settings, streaks, achievements; RLS, триггеры `updated_at`, индексы |
| `seed.sql` | Все 13 тем и 104 слова из `assets/content/content.json` + слово дня на 30 дней |

## Как включить облачный режим

1. **Поднимите проект** (облачный или локальный):
   - Облачный: создайте проект на [supabase.com](https://supabase.com), запомните
     `Project URL` и `anon key` (Settings → API).
   - Локальный: `supabase start` (Docker) → URL `http://10.0.2.2:54321` из эмулятора.
2. **Примените миграции и сид**:
   ```bash
   supabase db push          # применит migrations/0001_init.sql
   psql "$SUPABASE_DB_URL" -f supabase/seed.sql
   ```
  или через SQL Editor в дашборде: вставьте содержимое файлов по порядку.
3. **Включите Auth (e-mail + пароль)**: Authentication → Providers → Email;
   для детей/теста удобно выключить «Confirm email» (иначе после регистрации
   придётся подтвердить адрес письмом — приложение покажет об этом подсказку).
4. **Storage (опционально)**: создайте публичный бакет `media` для картинок/аудио
   обновлений контента без релиза:
   ```bash
   supabase storage create media --public
   ```
5. **Включите приложение** в `local.properties` (файл не попадает в VCS):
   ```properties
   supabase.enabled=true
   supabase.url=https://<project-ref>.supabase.co
   supabase.anonKey=<anon-key>
   ```
   Сборка `./gradlew assembleDebug` — и приложение переключится на Supabase Auth,
   фоновую синхронизацию (`SyncWorker`) и облачные отчёты **без правок экранов**.

## Что происходит при включении

- `SupabaseProvider` лениво создаёт `SupabaseClient` (Auth + Postgrest + Storage);
- DI подменяет `LocalAccountRepository` на `SupabaseAccountRepository`
  (одна строка в `SupabaseModule`);
- при облачном входе локальный прогресс **сливается** с облаком:
  по каждому слову берётся максимум счётчиков, `known` побеждает;
- каждое действие пишется в офлайн-очередь (`sync_queue` в Room) и отправляется
  upsert-ом идемпотентно (`client_uuid` unique, last-write-wins);
- `SyncWorker` ставится в очередь при старте приложения (сеть + каждые 15 минут).

## Проверка RLS

```sql
-- от имени обычного пользователя (anon/authorized) не должно вернуться чужое:
select count(*) from public.user_progress;  -- только свои строки
```
