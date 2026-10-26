-- ==============================================================================
-- YOUTUBE STUDIO CLONE - SUPABASE DATABASE MIGRATION
-- Project URL: https://zzswtrpdjllmagjjmxbx.supabase.co
-- Description: Creates canonical tables matching Android app data structures,
-- Row Level Security (RLS) policies, and seeds initial data from StudioRepository.
-- ==============================================================================

-- 1. EXTENSIONS
create extension if not exists "uuid-ossp";

-- 2. ADMIN USERS TABLE
-- Tracks explicitly authorized admin users. Only users whose auth.uid() exists here can modify app data.
create table if not exists public.admin_users (
    user_id uuid references auth.users(id) on delete cascade primary key,
    email text not null,
    created_at timestamptz default now()
);

-- 3. CHANNEL PROFILE TABLE
create table if not exists public.channel_profile (
    id text primary key,
    name text not null,
    handle text not null,
    subscribers_count text not null,
    email text default 'creator@gmail.com',
    is_monetized boolean default true,
    avatar_color text default '#FF7043',
    created_at timestamptz default now(),
    updated_at timestamptz default now()
);

-- 4. DASHBOARD METRICS TABLE (2x2 Grid + Period)
create table if not exists public.dashboard_metrics (
    id text primary key,
    views text not null,
    watch_time text not null,
    subscribers text not null,
    revenue text not null,
    views_is_up boolean default true,
    watch_time_is_up boolean default true,
    subscribers_is_up boolean default true,
    revenue_is_up boolean default true,
    period_label text default 'Last 28 days',
    updated_at timestamptz default now()
);

-- 5. VIDEOS TABLE (Videos, Shorts, Live streams & Dashboard latest)
create table if not exists public.videos (
    id text primary key,
    title text not null,
    type text not null check (type in ('VIDEO', 'SHORT', 'LIVE')),
    duration text not null default '0:00',
    published_time_ago text not null default 'Just now',
    views bigint not null default 0,
    likes bigint not null default 0,
    comments bigint not null default 0,
    ranking_text text default '',
    avg_percentage_viewed text default '',
    real_time_views bigint default 0,
    thumbnail_title text default '',
    is_expanded boolean default false,
    category text default 'Entertainment',
    is_dashboard_latest boolean default false,
    display_order integer default 0,
    updated_at timestamptz default now()
);

-- 6. ANALYTICS OVERVIEW METRICS TABLE (Cards & Chart Data)
create table if not exists public.analytics_overview (
    id text primary key,
    tab text not null default 'overview',
    title text not null,
    value text not null,
    subtitle text default '',
    is_positive boolean default true,
    show_arrow boolean default true,
    is_checkmark boolean default false,
    trend_data jsonb default '[]'::jsonb,
    y_min_label text default '0',
    y_mid_label text default '',
    y_max_label text default '',
    start_date_label text default '24 Sept',
    end_date_label text default '21 Oct',
    display_order integer default 0,
    updated_at timestamptz default now()
);

-- 7. ROW LEVEL SECURITY (RLS) SETUP
alter table public.admin_users enable row level security;
alter table public.channel_profile enable row level security;
alter table public.dashboard_metrics enable row level security;
alter table public.videos enable row level security;
alter table public.analytics_overview enable row level security;

-- Security helper function to verify if current authenticated caller is an admin
create or replace function public.is_admin()
returns boolean
language sql
security definer
stable
as $$
  select exists (
    select 1 from public.admin_users
    where user_id = auth.uid()
  );
$$;

-- RLS POLICIES FOR ADMIN_USERS
drop policy if exists "Allow admins to read admin list" on public.admin_users;
create policy "Allow admins to read admin list"
    on public.admin_users for select
    using (auth.uid() = user_id or public.is_admin());

-- RLS POLICIES FOR CHANNEL_PROFILE
-- Public / Android App can read; only authenticated admins can write
drop policy if exists "Allow public read on channel_profile" on public.channel_profile;
create policy "Allow public read on channel_profile"
    on public.channel_profile for select
    using (true);

drop policy if exists "Allow admins insert on channel_profile" on public.channel_profile;
create policy "Allow admins insert on channel_profile"
    on public.channel_profile for insert
    with check (public.is_admin());

drop policy if exists "Allow admins update on channel_profile" on public.channel_profile;
create policy "Allow admins update on channel_profile"
    on public.channel_profile for update
    using (public.is_admin())
    with check (public.is_admin());

drop policy if exists "Allow admins delete on channel_profile" on public.channel_profile;
create policy "Allow admins delete on channel_profile"
    on public.channel_profile for delete
    using (public.is_admin());

-- RLS POLICIES FOR DASHBOARD_METRICS
drop policy if exists "Allow public read on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow public read on dashboard_metrics"
    on public.dashboard_metrics for select
    using (true);

drop policy if exists "Allow admins insert on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins insert on dashboard_metrics"
    on public.dashboard_metrics for insert
    with check (public.is_admin());

drop policy if exists "Allow admins update on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins update on dashboard_metrics"
    on public.dashboard_metrics for update
    using (public.is_admin())
    with check (public.is_admin());

drop policy if exists "Allow admins delete on dashboard_metrics" on public.dashboard_metrics;
create policy "Allow admins delete on dashboard_metrics"
    on public.dashboard_metrics for delete
    using (public.is_admin());

-- RLS POLICIES FOR VIDEOS
drop policy if exists "Allow public read on videos" on public.videos;
create policy "Allow public read on videos"
    on public.videos for select
    using (true);

drop policy if exists "Allow admins insert on videos" on public.videos;
create policy "Allow admins insert on videos"
    on public.videos for insert
    with check (public.is_admin());

drop policy if exists "Allow admins update on videos" on public.videos;
create policy "Allow admins update on videos"
    on public.videos for update
    using (public.is_admin())
    with check (public.is_admin());

drop policy if exists "Allow admins delete on videos" on public.videos;
create policy "Allow admins delete on videos"
    on public.videos for delete
    using (public.is_admin());

-- RLS POLICIES FOR ANALYTICS_OVERVIEW
drop policy if exists "Allow public read on analytics_overview" on public.analytics_overview;
create policy "Allow public read on analytics_overview"
    on public.analytics_overview for select
    using (true);

drop policy if exists "Allow admins insert on analytics_overview" on public.analytics_overview;
create policy "Allow admins insert on analytics_overview"
    on public.analytics_overview for insert
    with check (public.is_admin());

drop policy if exists "Allow admins update on analytics_overview" on public.analytics_overview;
create policy "Allow admins update on analytics_overview"
    on public.analytics_overview for update
    using (public.is_admin())
    with check (public.is_admin());

drop policy if exists "Allow admins delete on analytics_overview" on public.analytics_overview;
create policy "Allow admins delete on analytics_overview"
    on public.analytics_overview for delete
    using (public.is_admin());

-- 8. ENABLE REALTIME
-- Adds tables to the supabase_realtime publication so changes broadcast immediately to clients
alter publication supabase_realtime add table public.channel_profile;
alter publication supabase_realtime add table public.dashboard_metrics;
alter publication supabase_realtime add table public.videos;
alter publication supabase_realtime add table public.analytics_overview;

-- 9. SEED INITIAL DATA (Matching existing StudioRepository.kt)

-- Channel Profile
insert into public.channel_profile (id, name, handle, subscribers_count, email, is_monetized, avatar_color)
values
    ('channel_daniel', 'Daniel Dalen', '@danieldalen', '48,237', 'creator@gmail.com', true, '#FF7043')
on conflict (id) do update set
    name = excluded.name,
    handle = excluded.handle,
    subscribers_count = excluded.subscribers_count,
    is_monetized = excluded.is_monetized,
    updated_at = now();

-- Dashboard 2x2 Metrics
insert into public.dashboard_metrics (id, views, watch_time, subscribers, revenue, views_is_up, watch_time_is_up, subscribers_is_up, revenue_is_up, period_label)
values
    ('main', '887.7K', '66.2K', '+39.0K', '$0', true, true, true, true, 'Last 28 days')
on conflict (id) do update set
    views = excluded.views,
    watch_time = excluded.watch_time,
    subscribers = excluded.subscribers,
    revenue = excluded.revenue,
    updated_at = now();

-- Videos (Dashboard Latest + Content Tab Videos)
insert into public.videos (id, title, type, duration, published_time_ago, views, likes, comments, ranking_text, avg_percentage_viewed, thumbnail_title, is_expanded, is_dashboard_latest, display_order)
values
    ('vid_daniel_pov', 'pov: a 26 year old entrepreneur at a bus...', 'VIDEO', '8:42', 'First 1 day, 6 hours', 39500, 2700, 220, '1 of 10', '5:50', 'POV', true, true, 1),
    ('c_vid_1', '#eidkobhoga2026 #eid #...', 'VIDEO', '0:27', '217 days ago', 1, 0, 0, '9 of 10', '62.0%', 'सऊदी से आ गई बड़ी खबर चाँद हुआ या नहीं? 2026', false, false, 2),
    ('c_vid_2', 'Waz ওয়াজ Mizanur Rahm...', 'VIDEO', '12:15', '230 days ago', 2, 0, 0, '', '', 'ওয়াজ মিজানুর রহমান', false, false, 3),
    ('c_vid_3', 'UNOWAY - Swervin'' [No C...', 'VIDEO', '3:03', '240 days ago', 51, 2, 0, '', '', 'SWERVIN', false, false, 4),
    ('c_vid_4', 'Kun Anta (كن أنت) - Slowe...', 'VIDEO', '4:40', '250 days ago', 27, 2, 0, '', '', 'كن أنت Slowed', false, false, 5),
    ('s_1', '#unfrezzmyaccount #lifeisbutadrea...', 'SHORT', '0:15', '177 days ago', 2, 0, 0, '8 of 10', '95.0%', 'UNFREEZE', true, false, 6),
    ('s_2', '26 March 2026', 'SHORT', '0:12', '209 days ago', 6, 0, 0, '3 of 10', '84.2%', '26 MARCH', false, false, 7),
    ('s_3', '#Gaming#gami...', 'SHORT', '0:30', '150 days ago', 8, 0, 0, '', '', 'White Pigeon', false, false, 8),
    ('l_1', 'free fire live', 'LIVE', '12:08', '300 days ago', 1, 0, 0, '', '', 'FF LIVE', false, false, 9),
    ('l_2', 'Free Fire Chalenge Custom...', 'LIVE', '16:04', '310 days ago', 3, 0, 0, '', '', 'AJJU BHAI VS AMIT BHAI', false, false, 10)
on conflict (id) do nothing;

-- Analytics Overview Metrics
insert into public.analytics_overview (id, tab, title, value, subtitle, is_positive, show_arrow, is_checkmark, trend_data, y_min_label, y_mid_label, y_max_label, start_date_label, end_date_label, display_order)
values
    ('overview_views', 'overview', 'Views', '812', '612 more than usual', true, true, false, '[30, 32, 40, 62, 54, 48, 40, 62, 45, 25, 28, 20, 26, 24, 18, 22, 20, 32, 20, 25, 28]'::jsonb, '0', '30', '90', '24 Sept', '21 Oct', 1),
    ('overview_watch_time', 'overview', 'Watch time (hours)', '1.8', 'About the same as usual', true, false, true, '[0.02, 0.05, 0.08, 0.06, 0.07, 0.04, 0.09, 0.07, 0.03, 0.03, 0.04, 0.06, 0.02, 0.01, 0.03, 0.02, 0.04, 0.03, 0.05, 0.04]'::jsonb, '0.0', '', '0.1', '24 Sept', '21 Oct', 2),
    ('overview_subscribers', 'overview', 'Subscribers', '-2', '60% more than previous 28 days', true, true, false, '[0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0]'::jsonb, '-3', '-1', '0', '24 Sept', '21 Oct', 3),
    ('overview_revenue', 'overview', 'Estimated revenue', '$1,247.32', 'Simulated demo data', true, true, false, '[25, 30, 35, 45, 42, 50, 48, 55, 52, 40, 44, 48, 52, 58, 62, 59, 65, 68, 70, 75]'::jsonb, '$0', '$50', '$100', '24 Sept', '21 Oct', 4)
on conflict (id) do nothing;
