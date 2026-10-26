# YouTube Studio Clone — Admin Panel

A responsive, professional web admin dashboard designed to control and synchronize the data displayed inside your native Android YouTube Studio Clone app in real time via **Supabase**.

---

## Desired Login Flow

- **Username**: `admin`
- **Temporary Password**: `Admin123`
- **Password Management**: Once logged in, you can securely change your password at any time via the **Password** button in the header.

---

## 1. Supabase Initial Schema (Already Executed)
The first long SQL migration created the canonical tables:
- `channel_profile`
- `dashboard_metrics`
- `videos`
- `analytics_overview`
- `admin_users`

Do **not** re-run that script.

---

## 2. Admin User Setup (Clean 2-Step Process for iPhone / Desktop)

### Step 1: Create the User in Supabase Auth
1. In your [Supabase Dashboard](https://supabase.com/dashboard/project/zzswtrpdjllmagjjmxbx), open the navigation menu and tap **Authentication** &rarr; **Users**.
2. Tap **Add user** &rarr; **Create user**.
3. Enter:
   - **User Email**: `admin@ytstudio.local`
   - **User Password**: `Admin123`
   - **Auto Confirm User?**: **Yes (Checked / Enabled)**
4. Tap **Create user**.

*(Note: The login screen in your Admin Panel only asks you for Username `admin` and Password `Admin123`. The system automatically maps `admin` to `admin@ytstudio.local` in Supabase Auth so you never have to type an email address!)*

### Step 2: Run the Clean Second Migration
1. In Supabase Dashboard, open the menu and tap **SQL Editor** &rarr; **New query**.
2. Copy and paste the entire script from [`/admin-panel/update_admin_auth.sql`](./update_admin_auth.sql) (or `/supabase/migrations/20261023_fix_admin_authorization.sql`).
3. Tap **Run**.

**Why this fixes the previous error:**
- The previous error (`ERROR: 22P02: invalid input syntax for type uuid: ""`) was caused by an attempt to cast an empty string `''` to a UUID column.
- This clean script uses real PostgreSQL UUID references directly from `auth.users` with zero placeholder strings.
- It sets up an automatic trigger on `auth.users` and syncs `admin@ytstudio.local` directly into `public.admin_users`.

---

## 3. Log In to Admin Panel & Change Password

1. Open your Admin Website in Safari or Chrome.
2. Under **Username**, leave or enter: `admin`
3. Under **Password**, enter: `Admin123`
4. Tap **Sign In to Admin Panel**.
5. Once inside, tap the **Password** button in the top-right header (or the yellow warning banner) to set your own permanent password.

---

## 4. How to Deploy the Admin Website

Because the admin panel is built with standard HTML5, CSS3, and JavaScript, it can be deployed to any static host with **zero build step**:

### Option A: GitHub Pages
1. In your GitHub repository settings, go to **Pages**.
2. Under **Build and deployment**, select:
   - Source: **Deploy from a branch**
   - Branch: `main` / Folder: `/admin-panel`
3. Tap **Save**. Your site will be live at `https://<your-username>.github.io/<repo>/admin-panel/`.

### Option B: Vercel / Netlify / Cloudflare Pages
1. Connect your repository to Vercel or Netlify.
2. Set the **Root Directory** to `admin-panel`.
3. Set the **Build Command** to empty (None).
4. Tap **Deploy**.
