-- Ӟеч-а бур-а удмурт кыл? — полная схема Supabase
-- Применять опционально: приложение по умолчанию работает полностью локально.
-- Включение: см. supabase/README.md

create extension if not exists "pgcrypto";

-- ─────────────────────────── триггер updated_at ───────────────────────────

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

-- ─────────────────────────── profiles ───────────────────────────

create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  display_name text not null default 'Юный лингвист',
  avatar text not null default '🐿️',
  is_child boolean not null default true,
  locale text not null default 'ru',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

-- ─────────────────────────── topics ───────────────────────────

create table public.topics (
  id text primary key,
  slug text not null unique,
  title_udm text not null,
  title_ru text not null,
  color text not null,
  emoji text not null default '📚',
  sort_order int not null default 0,
  image_path text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger topics_set_updated_at
  before update on public.topics
  for each row execute function public.set_updated_at();

-- ─────────────────────────── words ───────────────────────────

create table public.words (
  id text primary key,
  topic_id text not null references public.topics (id) on delete cascade,
  udm text not null,
  ru text not null,
  transcription text not null default '',
  emoji text not null default '🖼️',
  image_path text,
  audio_path text,
  example_udm text,
  example_ru text,
  sort_order int not null default 0,
  updated_at timestamptz not null default now()
);

create index words_topic_id_idx on public.words (topic_id);

create trigger words_set_updated_at
  before update on public.words
  for each row execute function public.set_updated_at();

-- ─────────────────────────── user_progress ───────────────────────────

create table public.user_progress (
  user_id uuid not null references auth.users (id) on delete cascade,
  word_id text not null references public.words (id) on delete cascade,
  status text not null default 'new' check (status in ('new', 'learning', 'known')),
  correct_count int not null default 0,
  wrong_count int not null default 0,
  last_answer_at timestamptz,
  client_uuid uuid not null unique default gen_random_uuid(),
  updated_at timestamptz not null default now(),
  primary key (user_id, word_id)
);

create index user_progress_user_id_idx on public.user_progress (user_id);

create trigger user_progress_set_updated_at
  before update on public.user_progress
  for each row execute function public.set_updated_at();

-- ─────────────────────────── game_results ───────────────────────────

create table public.game_results (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  game_type text not null check (
    game_type in ('choice', 'odd_one_out', 'sorting', 'memory', 'word_builder', 'true_false')
  ),
  score int not null default 0,
  correct int not null default 0,
  total int not null default 0,
  duration_sec int not null default 0,
  played_at timestamptz not null default now(),
  client_uuid uuid unique
);

create index game_results_user_id_idx on public.game_results (user_id);

-- ─────────────────────────── daily_activity ───────────────────────────

create table public.daily_activity (
  user_id uuid not null references auth.users (id) on delete cascade,
  day date not null,
  words_learned int not null default 0,
  games_played int not null default 0,
  correct int not null default 0,
  wrong int not null default 0,
  updated_at timestamptz not null default now(),
  primary key (user_id, day)
);

create index daily_activity_user_id_idx on public.daily_activity (user_id);

create trigger daily_activity_set_updated_at
  before update on public.daily_activity
  for each row execute function public.set_updated_at();

-- ─────────────────────────── word_of_day ───────────────────────────

create table public.word_of_day (
  day date primary key,
  word_id text not null references public.words (id) on delete cascade
);

-- ─────────────────────────── settings ───────────────────────────

create table public.settings (
  user_id uuid primary key references auth.users (id) on delete cascade,
  sound_enabled boolean not null default true,
  notifications_enabled boolean not null default false,
  geolocation_enabled boolean not null default false,
  theme text not null default 'SYSTEM',
  locale text not null default 'ru',
  updated_at timestamptz not null default now()
);

create trigger settings_set_updated_at
  before update on public.settings
  for each row execute function public.set_updated_at();

-- ─────────────────────────── streaks ───────────────────────────

create table public.streaks (
  user_id uuid primary key references auth.users (id) on delete cascade,
  current_streak int not null default 0,
  best_streak int not null default 0,
  last_active_day date,
  updated_at timestamptz not null default now()
);

create trigger streaks_set_updated_at
  before update on public.streaks
  for each row execute function public.set_updated_at();

-- ─────────────────────────── achievements ───────────────────────────

create table public.achievements (
  user_id uuid not null references auth.users (id) on delete cascade,
  badge_slug text not null,
  unlocked_at timestamptz not null default now(),
  primary key (user_id, badge_slug)
);

create index achievements_user_id_idx on public.achievements (user_id);

-- ─────────────────────────── Row Level Security ───────────────────────────

alter table public.profiles enable row level security;
alter table public.topics enable row level security;
alter table public.words enable row level security;
alter table public.user_progress enable row level security;
alter table public.game_results enable row level security;
alter table public.daily_activity enable row level security;
alter table public.word_of_day enable row level security;
alter table public.settings enable row level security;
alter table public.streaks enable row level security;
alter table public.achievements enable row level security;

-- profiles: владелец читает и меняет свой профиль
create policy "profiles_select_own" on public.profiles
  for select using (auth.uid() = id);
create policy "profiles_insert_own" on public.profiles
  for insert with check (auth.uid() = id);
create policy "profiles_update_own" on public.profiles
  for update using (auth.uid() = id);

-- topics/words: читают все, меняет только service role
create policy "topics_read_all" on public.topics
  for select using (true);
create policy "words_read_all" on public.words
  for select using (true);

-- user_progress
create policy "progress_select_own" on public.user_progress
  for select using (auth.uid() = user_id);
create policy "progress_insert_own" on public.user_progress
  for insert with check (auth.uid() = user_id);
create policy "progress_update_own" on public.user_progress
  for update using (auth.uid() = user_id);

-- game_results
create policy "games_select_own" on public.game_results
  for select using (auth.uid() = user_id);
create policy "games_insert_own" on public.game_results
  for insert with check (auth.uid() = user_id);

-- daily_activity
create policy "activity_select_own" on public.daily_activity
  for select using (auth.uid() = user_id);
create policy "activity_insert_own" on public.daily_activity
  for insert with check (auth.uid() = user_id);
create policy "activity_update_own" on public.daily_activity
  for update using (auth.uid() = user_id);

-- word_of_day: читают все (переопределение общего «слова дня»)
create policy "word_of_day_read_all" on public.word_of_day
  for select using (true);

-- settings
create policy "settings_select_own" on public.settings
  for select using (auth.uid() = user_id);
create policy "settings_insert_own" on public.settings
  for insert with check (auth.uid() = user_id);
create policy "settings_update_own" on public.settings
  for update using (auth.uid() = user_id);

-- streaks
create policy "streaks_select_own" on public.streaks
  for select using (auth.uid() = user_id);
create policy "streaks_insert_own" on public.streaks
  for insert with check (auth.uid() = user_id);
create policy "streaks_update_own" on public.streaks
  for update using (auth.uid() = user_id);

-- achievements
create policy "achievements_select_own" on public.achievements
  for select using (auth.uid() = user_id);
create policy "achievements_insert_own" on public.achievements
  for insert with check (auth.uid() = user_id);
