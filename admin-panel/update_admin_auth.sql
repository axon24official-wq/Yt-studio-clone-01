-- ==============================================================================
-- YOUTUBE STUDIO CLONE - ADMIN SETUP & AUTHORIZATION (CLEAN MIGRATION)
-- Project URL: https://zzswtrpdjllmagjjmxbx.supabase.co
--
-- PURPOSE:
-- 1. Fixes the "22P02: invalid input syntax for type uuid" error by NEVER
--    using empty strings ('') or placeholder strings for UUID columns.
-- 2. Automatically links the admin account (admin@ytstudio.local) to public.admin_users
--    using real PostgreSQL UUIDs directly from auth.users.
-- 3. Works cleanly whether you create the user in Supabase Auth before or after
--    running this SQL.
-- 4. Preserves all tables (channel_profile, dashboard_metrics, videos, analytics_overview)
--    and existing rows created by the first migration.
-- ==============================================================================

-- 1. Ensure uuid-ossp extension is enabled
create extension if not exists "uuid-ossp";

-- 2. Ensure public.admin_users exists (preserving existing table structure)
create table if not exists public.admin_users (
    user_id uuid references auth.users(id) on delete cascade primary key,
    email text not null,
    created_at timestamptz default now()
);

-- Ensure Row Level Security is active on admin_users
alter table public.admin_users enable row level security;

-- 3. Security helper function: is_admin()
-- Returns true if the currently authenticated caller's auth.uid() is in public.admin_users
create or replace function public.is_admin()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select coalesce(
    (select exists (
      select 1 from public.admin_users
      where user_id = auth.uid()
    )),
    false
  );
$$;

-- 4. RLS Policies on public.admin_users
drop policy if exists "Allow admins to read admin list" on public.admin_users;
create policy "Allow admins to read admin list"
    on public.admin_users for select
    using (auth.uid() = user_id or public.is_admin());

drop policy if exists "Prevent self-assignment to admin_users" on public.admin_users;
create policy "Prevent self-assignment to admin_users"
    on public.admin_users for insert
    with check (public.is_admin());

drop policy if exists "Only admins can remove from admin_users" on public.admin_users;
create policy "Only admins can remove from admin_users"
    on public.admin_users for delete
    using (public.is_admin());

-- 5. AUTOMATIC TRIGGER FOR ADMIN ENROLLMENT
-- Whenever 'admin@ytstudio.local' is created in Supabase Auth, automatically
-- insert their genuine UUID and email into public.admin_users.
create or replace function public.handle_admin_user_sync()
returns trigger
language plpgsql
security definer
set search_path = public, auth
as $$
begin
  if lower(new.email) = 'admin@ytstudio.local' then
    insert into public.admin_users (user_id, email)
    values (new.id, new.email)
    on conflict (user_id) do update set email = excluded.email;
  end if;
  return new;
end;
$$;

drop trigger if exists on_auth_user_admin_sync on auth.users;
create trigger on_auth_user_admin_sync
  after insert or update on auth.users
  for each row execute function public.handle_admin_user_sync();

-- 6. IMMEDIATE SYNC IF USER WAS ALREADY CREATED IN AUTH
-- If admin@ytstudio.local is already created in Authentication -> Users,
-- this will immediately register them without typing any UUID.
insert into public.admin_users (user_id, email)
select id, email
from auth.users
where lower(email) = 'admin@ytstudio.local'
on conflict (user_id) do update set email = excluded.email;

-- 7. OPTIONAL HELPER FUNCTION: grant_admin_access(target_email)
-- Allows authorizing any other email safely from SQL Editor without typing UUIDs
create or replace function public.grant_admin_access(target_email text)
returns text
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  matched_user record;
begin
  if target_email is null or trim(target_email) = '' then
    return 'Error: target_email cannot be empty.';
  end if;

  select id, email into matched_user
  from auth.users
  where lower(email) = lower(trim(target_email))
  limit 1;

  if matched_user.id is null then
    return 'Error: No user found in Supabase Auth with email "' || target_email || '". Please create the user in Authentication -> Users first.';
  end if;

  insert into public.admin_users (user_id, email)
  values (matched_user.id, matched_user.email)
  on conflict (user_id) do update
  set email = excluded.email;

  return 'Success: User "' || matched_user.email || '" (UUID: ' || matched_user.id::text || ') is now an authorized admin.';
end;
$$;

revoke all on function public.grant_admin_access(text) from public, anon, authenticated;
grant execute on function public.grant_admin_access(text) to postgres, service_role;

-- 8. CONFIRM RLS ON APP DATA TABLES
-- Public can read (for Android app & dashboard preview), admins can insert/update/delete
alter table if exists public.channel_profile enable row level security;
drop policy if exists "Allow public read on channel_profile" on public.channel_profile;
create policy "Allow public read on channel_profile" on public.channel_profile for select using (true);
drop policy if exists "Allow admins insert on channel_profile" on public.channel_profile;
create policy "Allow admins insert on channel_profile" on public.channel_profile for insert with check (public.is_admin());
drop policy if exists "Allow admins update on channel_profile" on public.channel_profile;
create policy "Allow admins update on channel_profile" on public.channel_profile for update using (public.is_admin()) with check (public.is_admin());
drop policy if exists "Allow admins delete on channel_profile" on public.channel_profile;
create policy "Allow admins delete on channel_profile" on public.channel_profile for delete using (public.is_admin());

alter table if exists public.dashboard_metrics enable row level security;
drop policy if exists "Allow public read on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow public read on dashboard_metrics" on public.dashboard_metrics for select using (true);
drop policy if exists "Allow admins insert on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins insert on dashboard_metrics" on public.dashboard_metrics for insert with check (public.is_admin());
drop policy if exists "Allow admins update on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins update on dashboard_metrics" on public.dashboard_metrics for update using (public.is_admin()) with check (public.is_admin());
drop policy if exists "Allow admins delete on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins delete on dashboard_metrics" on public.dashboard_metrics for delete using (public.is_admin());

alter table if exists public.videos enable row level security;
drop policy if exists "Allow public read on videos" on public.videos;
create policy "Allow public read on videos" on public.videos for select using (true);
drop policy if exists "Allow admins insert on videos" on public.videos;
create policy "Allow admins insert on videos" on public.videos for insert with check (public.is_admin());
drop policy if exists "Allow admins update on videos" on public.videos;
create policy "Allow admins update on videos" on public.videos for update using (public.is_admin()) with check (public.is_admin());
drop policy if exists "Allow admins delete on videos" on public.videos;
create policy "Allow admins delete on videos" on public.videos for delete using (public.is_admin());

alter table if exists public.analytics_overview enable row level security;
drop policy if exists "Allow public read on analytics_overview" on public.analytics_overview;
create policy "Allow public read on analytics_overview" on public.analytics_overview for select using (true);
drop policy if exists "Allow admins insert on analytics_overview" on public.analytics_overview;
create policy "Allow admins insert on analytics_overview" on public.analytics_overview for insert with check (public.is_admin());
drop policy if exists "Allow admins update on analytics_overview" on public.analytics_overview;
create policy "Allow admins update on analytics_overview" on public.analytics_overview for update using (public.is_admin()) with check (public.is_admin());
drop policy if exists "Allow admins delete on analytics_overview" on public.analytics_overview;
create policy "Allow admins delete on analytics_overview" on public.analytics_overview for delete using (public.is_admin());
