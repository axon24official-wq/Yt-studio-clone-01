// ==============================================================================
// YOUTUBE STUDIO CLONE ADMIN — SUPABASE CONFIGURATION
// Project URL: https://zzswtrpdjllmagjjmxbx.supabase.co
//
// NOTE:
// 1. You only need the PUBLISHABLE / ANON KEY here (safe for browser client).
// 2. NEVER put the service_role secret key in this file.
// 3. You can also change this in the Admin Panel -> "Supabase Config" tab.
// ==============================================================================

window.STUDIO_CONFIG = {
  // Your Supabase project URL
  supabaseUrl: localStorage.getItem("studio_supabase_url") || "https://zzswtrpdjllmagjjmxbx.supabase.co",

  // Your Supabase Publishable / Anon public key
  // Found in Supabase Dashboard -> Project Settings -> API -> Project API keys -> anon (public)
  supabaseAnonKey: localStorage.getItem("studio_supabase_anon_key") || ""
};
