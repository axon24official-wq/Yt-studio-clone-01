// ==============================================================================
// YOUTUBE STUDIO ADMIN DASHBOARD — APPLICATION LOGIC
// Controls data displayed in the native Android App via Supabase PostgREST & Realtime
// ==============================================================================

(function () {
  'use strict';

  // State
  let supabaseClient = null;
  let currentUser = null;
  let isAuthorizedAdmin = false;
  let channelData = null;
  let dashboardMetrics = null;
  let videosList = [];
  let analyticsList = [];
  let currentVideoFilter = 'ALL';
  let realtimeChannel = null;

  // DOM Elements
  const statusBadge = document.getElementById('supabaseStatusBadge');
  const userProfileArea = document.getElementById('userProfileArea');
  const userEmailBadge = document.getElementById('userEmailBadge');
  const logoutBtn = document.getElementById('logoutBtn');
  const authBarrier = document.getElementById('authBarrier');
  const authAlert = document.getElementById('authAlert');
  const loginForm = document.getElementById('loginForm');
  const adminViews = document.getElementById('adminViews');
  const mobileMenuBtn = document.getElementById('mobileMenuBtn');
  const sidebar = document.getElementById('sidebar');

  // Initialize
  function init() {
    setupNavListeners();
    setupMobileMenu();
    setupModals();
    setupConfigForm();
    initSupabase();
  }

  // 1. Supabase Initialization
  function initSupabase() {
    const config = window.STUDIO_CONFIG || {};
    const url = config.supabaseUrl;
    const key = config.supabaseAnonKey;

    updateStatusBadge('checking', 'Connecting...');

    if (!url || !key) {
      updateStatusBadge('error', 'Missing Publishable Key');
      showAuthAlert('Supabase Publishable (Anon) Key is missing. Open the "Supabase Config" tab to enter your key.', 'error');
      showView('tab-settings');
      setActiveNavItem('settings');
      showAuthBarrier(false); // allow access to settings
      return;
    }

    try {
      supabaseClient = window.supabase.createClient(url, key, {
        auth: {
          persistSession: true,
          autoRefreshToken: true
        }
      });

      // Check Session & Auth
      checkAuthSession();

      // Listen to Auth State changes
      supabaseClient.auth.onAuthStateChange((event, session) => {
        if (session && session.user) {
          currentUser = session.user;
          verifyAdminAuthorization(currentUser);
        } else {
          currentUser = null;
          isAuthorizedAdmin = false;
          showAuthBarrier(true);
          updateStatusBadge('error', 'Login Required');
        }
      });

    } catch (err) {
      console.error('Supabase init error:', err);
      updateStatusBadge('error', 'Connection Error');
      showToast('Error connecting to Supabase: ' + err.message, 'error');
    }
  }

  // 2. Auth & Admin Authorization Verification
  async function checkAuthSession() {
    try {
      const { data: { session }, error } = await supabaseClient.auth.getSession();
      if (error) throw error;

      if (session && session.user) {
        currentUser = session.user;
        await verifyAdminAuthorization(currentUser);
      } else {
        showAuthBarrier(true);
        updateStatusBadge('checking', 'Waiting for Admin Login');
      }
    } catch (err) {
      console.error('Session check failed:', err);
      showAuthBarrier(true);
    }
  }

  async function verifyAdminAuthorization(user) {
    const isLocalAdmin = user.email && user.email.toLowerCase() === 'admin@ytstudio.local';
    userEmailBadge.textContent = isLocalAdmin ? 'admin' : (user.email || 'Admin');
    userProfileArea.classList.remove('hidden');

    try {
      // Query protected admin_users table
      const { data, error } = await supabaseClient
        .from('admin_users')
        .select('user_id, email')
        .eq('user_id', user.id)
        .maybeSingle();

      if (error && error.code !== 'PGRST116') {
        console.warn('admin_users check error:', error);
      }

      if (data && data.user_id) {
        // User is confirmed database admin!
        isAuthorizedAdmin = true;
        showAuthBarrier(false);
        updateStatusBadge('connected', 'Supabase Connected');
        showToast('Welcome, ' + (isLocalAdmin ? 'admin' : user.email) + '! Admin access granted.', 'success');
        
        // Check if temporary password banner should be displayed
        if (sessionStorage.getItem('using_temp_password') === 'true') {
          document.getElementById('tempPasswordNotice')?.classList.remove('hidden');
        } else {
          document.getElementById('tempPasswordNotice')?.classList.add('hidden');
        }

        // Load All Data & Set up Realtime
        loadAllData();
        setupRealtimeSubscriptions();
      } else {
        // User logged in to Supabase, but is not in admin_users!
        isAuthorizedAdmin = false;
        showAuthBarrier(true);
        updateStatusBadge('error', 'Unauthorized Admin');
        showAuthAlert(
          `Logged in as ${user.email}, but this account is not registered as an authorized admin in public.admin_users.<br>
          Run the clean SQL migration in Supabase SQL Editor to automatically link this admin account.`,
          'error'
        );
      }
    } catch (err) {
      console.error('Verification error:', err);
      showAuthAlert('Database authorization check error: ' + err.message, 'error');
    }
  }

  // Login Form Submission
  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('loginUsername');
    const rawUsername = (usernameInput ? usernameInput.value : '').trim();
    const password = document.getElementById('loginPassword').value;
    const submitBtn = document.getElementById('loginSubmitBtn');

    if (!supabaseClient) {
      showToast('Please configure Supabase URL and Key first.', 'error');
      return;
    }

    if (!rawUsername) {
      showAuthAlert('Please enter the admin username.', 'error');
      return;
    }

    // Map username to canonical Supabase Auth email
    let authEmail = '';
    if (rawUsername.toLowerCase() === 'admin') {
      authEmail = 'admin@ytstudio.local';
    } else if (rawUsername.includes('@')) {
      authEmail = rawUsername.toLowerCase();
    } else {
      authEmail = `${rawUsername.toLowerCase()}@ytstudio.local`;
    }

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>Signing In...</span>';
    hideAuthAlert();

    try {
      const { data, error } = await supabaseClient.auth.signInWithPassword({
        email: authEmail,
        password: password
      });

      if (error) throw error;

      // Track if temporary password was used
      if (password === 'Admin123') {
        sessionStorage.setItem('using_temp_password', 'true');
      } else {
        sessionStorage.removeItem('using_temp_password');
      }

      if (data.user) {
        currentUser = data.user;
        await verifyAdminAuthorization(currentUser);
      }
    } catch (err) {
      console.error('Login error:', err);
      const msg = err.message || 'Login failed. Please verify credentials.';
      if (msg.toLowerCase().includes('invalid login credentials')) {
        showAuthAlert('Invalid credentials. If this is your first time, create user <strong>admin@ytstudio.local</strong> with password <strong>Admin123</strong> in Supabase Authentication &rarr; Users.', 'error');
      } else {
        showAuthAlert(msg, 'error');
      }
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Sign In to Admin Panel</span>';
    }
  });

  // Logout
  logoutBtn.addEventListener('click', async () => {
    if (!supabaseClient) return;
    try {
      await supabaseClient.auth.signOut();
      currentUser = null;
      isAuthorizedAdmin = false;
      sessionStorage.removeItem('using_temp_password');
      document.getElementById('tempPasswordNotice')?.classList.add('hidden');
      userProfileArea.classList.add('hidden');
      showAuthBarrier(true);
      showToast('Logged out successfully.', 'success');
    } catch (err) {
      showToast('Logout error: ' + err.message, 'error');
    }
  });

  function showAuthBarrier(show) {
    if (show) {
      authBarrier.classList.remove('hidden');
      adminViews.classList.add('hidden');
    } else {
      authBarrier.classList.add('hidden');
      adminViews.classList.remove('hidden');
    }
  }

  function showAuthAlert(msg, type) {
    authAlert.innerHTML = msg;
    authAlert.className = 'alert-box ' + (type === 'error' ? 'alert-error' : 'alert-success');
    authAlert.classList.remove('hidden');
  }

  function hideAuthAlert() {
    authAlert.classList.add('hidden');
  }

  function updateStatusBadge(type, text) {
    statusBadge.className = 'status-badge ' + type;
    statusBadge.querySelector('.status-text').textContent = text;
  }

  // 3. Navigation
  function setupNavListeners() {
    document.querySelectorAll('.nav-item').forEach((item) => {
      item.addEventListener('click', () => {
        const tab = item.getAttribute('data-tab');
        setActiveNavItem(tab);
        showView('tab-' + tab);
        if (window.innerWidth <= 860) {
          sidebar.classList.remove('open');
        }
      });
    });

    document.querySelectorAll('[data-jump]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const jumpTarget = btn.getAttribute('data-jump');
        setActiveNavItem(jumpTarget);
        showView('tab-' + jumpTarget);
      });
    });

    document.getElementById('refreshAllBtn')?.addEventListener('click', () => {
      loadAllData();
      showToast('Data refreshed from Supabase!', 'success');
    });
  }

  function setActiveNavItem(tabKey) {
    document.querySelectorAll('.nav-item').forEach((item) => {
      if (item.getAttribute('data-tab') === tabKey) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });
  }

  function showView(viewId) {
    document.querySelectorAll('.tab-pane').forEach((pane) => {
      pane.classList.remove('active');
    });
    const target = document.getElementById(viewId);
    if (target) {
      target.classList.add('active');
    }
  }

  function setupMobileMenu() {
    mobileMenuBtn?.addEventListener('click', () => {
      sidebar.classList.toggle('open');
    });
  }

  // 4. Data Loading
  async function loadAllData() {
    if (!supabaseClient) return;
    await Promise.all([
      loadChannelProfile(),
      loadDashboardMetrics(),
      loadVideos(),
      loadAnalyticsOverview()
    ]);
  }

  // Load Channel Profile
  async function loadChannelProfile() {
    try {
      const { data, error } = await supabaseClient
        .from('channel_profile')
        .select('*')
        .limit(1)
        .maybeSingle();

      if (error) throw error;
      if (data) {
        channelData = data;
        populateChannelUI(data);
      }
    } catch (err) {
      console.warn('Channel profile fetch:', err);
    }
  }

  function populateChannelUI(data) {
    // Overview tab elements
    document.getElementById('overviewChannelName').textContent = data.name || 'Daniel Dalen';
    document.getElementById('overviewChannelSubs').textContent = (data.subscribers_count || '0') + ' total subscribers';
    
    // Sidebar elements
    document.getElementById('sidebarChannelName').textContent = data.name || 'Daniel Dalen';
    document.getElementById('sidebarChannelSubs').textContent = (data.subscribers_count || '0') + ' subs';
    
    const initials = (data.name || 'DD').split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
    document.getElementById('sidebarAvatar').textContent = initials;
    if (data.avatar_color) {
      document.getElementById('sidebarAvatar').style.background = data.avatar_color;
    }

    // Channel tab form
    document.getElementById('channelNameInput').value = data.name || '';
    document.getElementById('channelHandleInput').value = data.handle || '';
    document.getElementById('channelSubsInput').value = data.subscribers_count || '';
    document.getElementById('channelEmailInput').value = data.email || '';
    document.getElementById('channelAvatarColorInput').value = data.avatar_color || '#FF7043';
    document.getElementById('channelMonetizedToggle').checked = !!data.is_monetized;

    // Monetization elements
    document.getElementById('monetizationBigToggle').checked = !!data.is_monetized;
    updateMonetizationBadge(!!data.is_monetized);
  }

  function updateMonetizationBadge(isMonetized) {
    const badge = document.getElementById('overviewMonetizedBadge');
    const statusText = document.getElementById('monetizeStatusText');
    if (isMonetized) {
      badge.textContent = 'Monetized';
      badge.className = 'badge badge-success';
      statusText.textContent = 'ACTIVE';
      statusText.className = 'text-success';
    } else {
      badge.textContent = 'Unmonetized';
      badge.className = 'badge badge-neutral';
      statusText.textContent = 'INACTIVE';
      statusText.className = 'text-muted';
    }
  }

  // Load Dashboard 2x2 Metrics
  async function loadDashboardMetrics() {
    try {
      const { data, error } = await supabaseClient
        .from('dashboard_metrics')
        .select('*')
        .limit(1)
        .maybeSingle();

      if (error) throw error;
      if (data) {
        dashboardMetrics = data;
        populateDashboardMetricsUI(data);
      }
    } catch (err) {
      console.warn('Dashboard metrics fetch:', err);
    }
  }

  function populateDashboardMetricsUI(data) {
    // Overview stat cards
    document.getElementById('overviewViews').textContent = data.views || '887.7K';
    document.getElementById('overviewWatchTime').textContent = data.watch_time || '66.2K';
    document.getElementById('overviewRevenue').textContent = data.revenue || '$0';

    // Metrics Editor Form
    document.getElementById('metricsPeriodLabel').value = data.period_label || 'Last 28 days';
    document.getElementById('viewsValInput').value = data.views || '';
    document.getElementById('viewsUpCheckbox').checked = !!data.views_is_up;
    document.getElementById('watchTimeValInput').value = data.watch_time || '';
    document.getElementById('watchTimeUpCheckbox').checked = !!data.watch_time_is_up;
    document.getElementById('subscribersValInput').value = data.subscribers || '';
    document.getElementById('subscribersUpCheckbox').checked = !!data.subscribers_is_up;
    document.getElementById('revenueValInput').value = data.revenue || '';
    document.getElementById('revenueUpCheckbox').checked = !!data.revenue_is_up;
  }

  // Load Videos
  async function loadVideos() {
    try {
      const { data, error } = await supabaseClient
        .from('videos')
        .select('*')
        .order('display_order', { ascending: true });

      if (error) throw error;
      videosList = data || [];
      renderVideosTable();

      // Update Latest published video preview on overview tab
      const latest = videosList.find(v => v.is_dashboard_latest) || videosList[0];
      if (latest) {
        document.getElementById('latestVideoTitle').textContent = latest.title;
        document.getElementById('latestVideoMeta').textContent = 
          `${formatCount(latest.views)} views • ${formatCount(latest.likes)} likes • ${latest.comments} comments • Ranking: ${latest.ranking_text || '1 of 10'}`;
      }
    } catch (err) {
      console.warn('Videos fetch:', err);
      document.getElementById('videosTableBody').innerHTML = 
        `<tr><td colspan="10" class="text-center py-4 text-muted">Error loading videos: ${err.message}</td></tr>`;
    }
  }

  function renderVideosTable() {
    const tbody = document.getElementById('videosTableBody');
    const filtered = currentVideoFilter === 'ALL'
      ? videosList
      : videosList.filter(v => v.type === currentVideoFilter);

    if (filtered.length === 0) {
      tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-muted">No videos found. Tap "Add New Video" to create one.</td></tr>`;
      return;
    }

    tbody.innerHTML = filtered.map(v => `
      <tr>
        <td>
          <div class="table-thumb">${escapeHtml(v.thumbnail_title || 'VIDEO')}</div>
        </td>
        <td>
          <div class="table-video-title" title="${escapeHtml(v.title)}">${escapeHtml(v.title)}</div>
          <div class="table-video-id">ID: ${escapeHtml(v.id)}</div>
        </td>
        <td>
          <span class="badge ${v.type === 'SHORT' ? 'badge-neutral' : 'badge-success'}">${v.type}</span>
        </td>
        <td>${escapeHtml(v.duration || '0:00')}</td>
        <td>${escapeHtml(v.published_time_ago || '')}</td>
        <td><strong>${formatCount(v.views)}</strong></td>
        <td>${formatCount(v.likes)}</td>
        <td>${v.comments || 0}</td>
        <td>
          ${v.is_dashboard_latest 
            ? '<span class="badge badge-success">&#10003; Active Latest</span>' 
            : '<span class="text-muted">—</span>'}
        </td>
        <td>
          <div class="table-actions">
            <button class="btn btn-secondary btn-sm" onclick="window.editVideo('${v.id}')">Edit</button>
            <button class="btn btn-danger btn-sm" onclick="window.deleteVideo('${v.id}')">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  // Load Analytics Overview Cards & Trends
  async function loadAnalyticsOverview() {
    try {
      const { data, error } = await supabaseClient
        .from('analytics_overview')
        .select('*')
        .order('display_order', { ascending: true });

      if (error) throw error;
      analyticsList = data || [];
      renderAnalyticsOverview();
    } catch (err) {
      console.warn('Analytics overview fetch:', err);
    }
  }

  function renderAnalyticsOverview() {
    const container = document.getElementById('analyticsOverviewContainer');
    if (analyticsList.length === 0) {
      container.innerHTML = '<p class="text-muted">No analytics metric cards found in database.</p>';
      return;
    }

    container.innerHTML = analyticsList.map(item => {
      const trendArray = Array.isArray(item.trend_data) ? item.trend_data : [];
      const sparklineSvg = generateSparkline(trendArray);

      return `
        <div class="analytics-card" id="metric-card-${item.id}">
          <div class="analytics-card-title">${escapeHtml(item.title)}</div>
          <div class="analytics-card-val">${escapeHtml(item.value)}</div>
          <div class="analytics-card-sub">${escapeHtml(item.subtitle || '')}</div>
          
          <div class="chart-preview-box">
            ${sparklineSvg}
          </div>

          <div class="form-group mb-2">
            <label style="font-size: 11px;">Edit Metric Display Value</label>
            <input type="text" class="metric-live-input" id="input-${item.id}" value="${escapeHtml(item.value)}">
          </div>

          <div class="form-group mb-2">
            <label style="font-size: 11px;">Edit Subtitle Text</label>
            <input type="text" class="metric-live-subtitle" id="sub-${item.id}" value="${escapeHtml(item.subtitle || '')}">
          </div>

          <button class="btn btn-secondary btn-sm btn-full mt-2" onclick="window.saveAnalyticsMetric('${item.id}')">
            Update ${escapeHtml(item.title)}
          </button>
        </div>
      `;
    }).join('');
  }

  function generateSparkline(points) {
    if (!points || points.length < 2) {
      return '<div style="height: 60px; display: flex; align-items: center; justify-content: center; color: #555; font-size: 11px;">No trend points</div>';
    }

    const min = Math.min(...points);
    const max = Math.max(...points);
    const range = (max - min) || 1;
    const width = 280;
    const height = 60;
    const padding = 6;

    const coords = points.map((val, idx) => {
      const x = padding + (idx / (points.length - 1)) * (width - 2 * padding);
      const y = height - padding - ((val - min) / range) * (height - 2 * padding);
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    });

    const pathData = 'M ' + coords.join(' L ');

    return `
      <svg class="chart-svg" viewBox="0 0 ${width} ${height}">
        <path d="${pathData}" fill="none" stroke="#3ea6ff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    `;
  }

  // 5. Forms & Actions

  // Save Channel Profile
  document.getElementById('channelProfileForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!requireAdmin()) return;

    const saveBtn = document.getElementById('saveChannelBtn');
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    const targetId = channelData?.id || 'channel_daniel';
    const updated = {
      name: document.getElementById('channelNameInput').value.trim(),
      handle: document.getElementById('channelHandleInput').value.trim(),
      subscribers_count: document.getElementById('channelSubsInput').value.trim(),
      email: document.getElementById('channelEmailInput').value.trim(),
      avatar_color: document.getElementById('channelAvatarColorInput').value,
      is_monetized: document.getElementById('channelMonetizedToggle').checked,
      updated_at: new Date().toISOString()
    };

    try {
      const { data, error } = await supabaseClient
        .from('channel_profile')
        .upsert({ id: targetId, ...updated })
        .select();

      if (error) {
        console.error('Supabase write error on channel_profile:', error);
        throw error;
      }

      if (!data || data.length === 0) {
        throw new Error('Database write affected 0 rows. Please verify that your account has admin rights in public.admin_users.');
      }

      console.log('Channel profile saved successfully to Supabase:', data);
      showToast('Channel profile saved! Android app will update in realtime.', 'success');
      channelData = data[0];
      populateChannelUI(data[0]);
    } catch (err) {
      console.error('Save channel failed:', err);
      showToast('Error saving channel: ' + err.message, 'error');
    } finally {
      saveBtn.disabled = false;
      saveBtn.textContent = 'Save Channel Changes';
    }
  });

  // Save Dashboard 2x2 Metrics
  document.getElementById('dashboardMetricsForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!requireAdmin()) return;

    openConfirmModal(
      'Update Dashboard 2x2 Grid?',
      'This will immediately update the Views, Watch Time, Subscribers, and Revenue tiles on the Android app home screen.',
      async () => {
        const saveBtn = document.getElementById('saveMetricsBtn');
        saveBtn.disabled = true;
        saveBtn.textContent = 'Saving...';

        const targetId = dashboardMetrics?.id || 'main';
        const updated = {
          id: targetId,
          period_label: document.getElementById('metricsPeriodLabel').value.trim(),
          views: document.getElementById('viewsValInput').value.trim(),
          views_is_up: document.getElementById('viewsUpCheckbox').checked,
          watch_time: document.getElementById('watchTimeValInput').value.trim(),
          watch_time_is_up: document.getElementById('watchTimeUpCheckbox').checked,
          subscribers: document.getElementById('subscribersValInput').value.trim(),
          subscribers_is_up: document.getElementById('subscribersUpCheckbox').checked,
          revenue: document.getElementById('revenueValInput').value.trim(),
          revenue_is_up: document.getElementById('revenueUpCheckbox').checked,
          updated_at: new Date().toISOString()
        };

        try {
          const { data, error } = await supabaseClient
            .from('dashboard_metrics')
            .upsert(updated)
            .select();

          if (error) {
            console.error('Supabase write error on dashboard_metrics:', error);
            throw error;
          }

          if (!data || data.length === 0) {
            throw new Error('Database write affected 0 rows. Please verify that your account has admin rights in public.admin_users.');
          }

          console.log('Dashboard metrics saved successfully to Supabase:', data);
          showToast('Dashboard metrics updated in Android app!', 'success');
          dashboardMetrics = data[0];
          populateDashboardMetricsUI(data[0]);
        } catch (err) {
          console.error('Save metrics failed:', err);
          showToast('Error updating metrics: ' + err.message, 'error');
        } finally {
          saveBtn.disabled = false;
          saveBtn.textContent = 'Save 2x2 Grid Metrics';
        }
      }
    );
  });

  // Monetization Toggle
  document.getElementById('saveMonetizationBtn').addEventListener('click', async () => {
    if (!requireAdmin()) return;
    const isMonetized = document.getElementById('monetizationBigToggle').checked;
    const targetId = channelData?.id || 'channel_daniel';

    try {
      const { data, error } = await supabaseClient
        .from('channel_profile')
        .update({
          is_monetized: isMonetized,
          updated_at: new Date().toISOString()
        })
        .eq('id', targetId)
        .select();

      if (error) throw error;
      if (!data || data.length === 0) {
        throw new Error('Monetization update affected 0 rows. Check admin permissions.');
      }

      console.log('Monetization updated successfully in Supabase:', data);
      channelData = data[0];
      updateMonetizationBadge(isMonetized);
      showToast(`Monetization set to ${isMonetized ? 'ACTIVE' : 'INACTIVE'}!`, 'success');
    } catch (err) {
      console.error('Monetization update failed:', err);
      showToast('Monetization update error: ' + err.message, 'error');
    }
  });

  // Save Single Analytics Metric
  window.saveAnalyticsMetric = async function (id) {
    if (!requireAdmin()) return;
    const valInput = document.getElementById('input-' + id);
    const subInput = document.getElementById('sub-' + id);

    if (!valInput) return;

    try {
      const { data, error } = await supabaseClient
        .from('analytics_overview')
        .update({
          value: valInput.value.trim(),
          subtitle: subInput ? subInput.value.trim() : '',
          updated_at: new Date().toISOString()
        })
        .eq('id', id)
        .select();

      if (error) {
        console.error(`Save analytics metric [${id}] error:`, error);
        throw error;
      }

      if (!data || data.length === 0) {
        throw new Error(`Metric update for ${id} returned 0 rows. Check admin permissions.`);
      }

      console.log(`Metric [${id}] saved to Supabase:`, data);
      showToast('Metric updated in real time!', 'success');
    } catch (err) {
      console.error('Error updating metric:', err);
      showToast('Error updating metric: ' + err.message, 'error');
    }
  };

  // Video Management (Filter, Add, Edit, Delete)
  document.querySelectorAll('.filter-pills .pill-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-pills .pill-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentVideoFilter = btn.getAttribute('data-filter');
      renderVideosTable();
    });
  });

  document.getElementById('addVideoBtn')?.addEventListener('click', () => {
    if (!requireAdmin()) return;
    openVideoModal(null);
  });

  window.editVideo = function (id) {
    if (!requireAdmin()) return;
    const video = videosList.find(v => v.id === id);
    if (video) {
      openVideoModal(video);
    }
  };

  window.deleteVideo = function (id) {
    if (!requireAdmin()) return;
    const video = videosList.find(v => v.id === id);
    openConfirmModal(
      'Delete Video?',
      `Are you sure you want to delete "${video ? video.title : id}"? This will remove it from the Android app content feed.`,
      async () => {
        try {
          const { data, error } = await supabaseClient
            .from('videos')
            .delete()
            .eq('id', id)
            .select();

          if (error) {
            console.error('Delete video error:', error);
            throw error;
          }

          console.log(`Video [${id}] deleted successfully from Supabase:`, data);
          showToast('Video deleted from database.', 'success');
          await loadVideos();
        } catch (err) {
          console.error('Delete video failed:', err);
          showToast('Delete error: ' + err.message, 'error');
        }
      }
    );
  };

  function openVideoModal(video) {
    const modal = document.getElementById('videoModal');
    const titleHeader = document.getElementById('videoModalTitle');
    
    if (video) {
      titleHeader.textContent = 'Edit Video';
      document.getElementById('vFormId').value = video.id;
      document.getElementById('vFormTitle').value = video.title || '';
      document.getElementById('vFormType').value = video.type || 'VIDEO';
      document.getElementById('vFormDuration').value = video.duration || '0:00';
      document.getElementById('vFormViews').value = video.views || 0;
      document.getElementById('vFormLikes').value = video.likes || 0;
      document.getElementById('vFormComments').value = video.comments || 0;
      document.getElementById('vFormPublished').value = video.published_time_ago || '';
      document.getElementById('vFormThumbnailTitle').value = video.thumbnail_title || '';
      document.getElementById('vFormRanking').value = video.ranking_text || '';
      document.getElementById('vFormAvgPercentage').value = video.avg_percentage_viewed || '';
      document.getElementById('vFormIsDashboardLatest').checked = !!video.is_dashboard_latest;
    } else {
      titleHeader.textContent = 'Add New Video';
      const newId = 'vid_' + Math.random().toString(36).substring(2, 9);
      document.getElementById('vFormId').value = newId;
      document.getElementById('vFormTitle').value = '';
      document.getElementById('vFormType').value = 'VIDEO';
      document.getElementById('vFormDuration').value = '3:45';
      document.getElementById('vFormViews').value = 100;
      document.getElementById('vFormLikes').value = 10;
      document.getElementById('vFormComments').value = 0;
      document.getElementById('vFormPublished').value = 'Just now';
      document.getElementById('vFormThumbnailTitle').value = 'NEW';
      document.getElementById('vFormRanking').value = '';
      document.getElementById('vFormAvgPercentage').value = '50.0%';
      document.getElementById('vFormIsDashboardLatest').checked = false;
    }

    modal.classList.remove('hidden');
  }

  document.getElementById('videoForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!requireAdmin()) return;

    const id = document.getElementById('vFormId').value;
    const isLatest = document.getElementById('vFormIsDashboardLatest').checked;

    const payload = {
      id: id,
      title: document.getElementById('vFormTitle').value.trim(),
      type: document.getElementById('vFormType').value,
      duration: document.getElementById('vFormDuration').value.trim(),
      views: parseInt(document.getElementById('vFormViews').value) || 0,
      likes: parseInt(document.getElementById('vFormLikes').value) || 0,
      comments: parseInt(document.getElementById('vFormComments').value) || 0,
      published_time_ago: document.getElementById('vFormPublished').value.trim(),
      thumbnail_title: document.getElementById('vFormThumbnailTitle').value.trim(),
      ranking_text: document.getElementById('vFormRanking').value.trim(),
      avg_percentage_viewed: document.getElementById('vFormAvgPercentage').value.trim(),
      is_dashboard_latest: isLatest,
      updated_at: new Date().toISOString()
    };

    try {
      // If marking as dashboard latest, unmark other videos first
      if (isLatest) {
        await supabaseClient
          .from('videos')
          .update({ is_dashboard_latest: false })
          .neq('id', id);
      }

      const { data, error } = await supabaseClient
        .from('videos')
        .upsert(payload)
        .select();

      if (error) {
        console.error('Save video error:', error);
        throw error;
      }

      if (!data || data.length === 0) {
        throw new Error('Video write affected 0 rows. Check admin permissions.');
      }

      console.log('Video saved successfully to Supabase:', data);
      showToast('Video saved successfully!', 'success');
      document.getElementById('videoModal').classList.add('hidden');
      await loadVideos();
    } catch (err) {
      console.error('Save video failed:', err);
      showToast('Save video error: ' + err.message, 'error');
    }
  });

  // 6. Supabase Realtime Subscriptions
  function setupRealtimeSubscriptions() {
    if (!supabaseClient) return;

    if (realtimeChannel) {
      supabaseClient.removeChannel(realtimeChannel);
    }

    realtimeChannel = supabaseClient
      .channel('admin_realtime_changes')
      .on('postgres_changes', { event: '*', schema: 'public', table: 'channel_profile' }, (payload) => {
        console.log('Realtime channel_profile change:', payload);
        loadChannelProfile();
      })
      .on('postgres_changes', { event: '*', schema: 'public', table: 'dashboard_metrics' }, (payload) => {
        console.log('Realtime dashboard_metrics change:', payload);
        loadDashboardMetrics();
      })
      .on('postgres_changes', { event: '*', schema: 'public', table: 'videos' }, (payload) => {
        console.log('Realtime videos change:', payload);
        loadVideos();
      })
      .on('postgres_changes', { event: '*', schema: 'public', table: 'analytics_overview' }, (payload) => {
        console.log('Realtime analytics_overview change:', payload);
        loadAnalyticsOverview();
      })
      .subscribe((status) => {
        const diagBadge = document.getElementById('diagRealtimeStatus');
        if (status === 'SUBSCRIBED') {
          if (diagBadge) {
            diagBadge.textContent = 'Listening (Active)';
            diagBadge.className = 'badge badge-success';
          }
        }
      });
  }

  // 7. Modals & Helpers
  function setupModals() {
    // Video Modal
    document.getElementById('closeVideoModalBtn')?.addEventListener('click', () => {
      document.getElementById('videoModal').classList.add('hidden');
    });
    document.getElementById('cancelVideoModalBtn')?.addEventListener('click', () => {
      document.getElementById('videoModal').classList.add('hidden');
    });

    // Confirm Modal
    document.getElementById('closeConfirmBtn')?.addEventListener('click', () => {
      document.getElementById('confirmModal').classList.add('hidden');
    });
    document.getElementById('confirmCancelBtn')?.addEventListener('click', () => {
      document.getElementById('confirmModal').classList.add('hidden');
    });

    // Change Password Modal
    const changePwdModal = document.getElementById('changePasswordModal');
    const changePwdForm = document.getElementById('changePasswordForm');
    const changePwdAlert = document.getElementById('changePasswordAlert');
    const openChangePasswordBtn = document.getElementById('openChangePasswordBtn');
    const bannerChangePasswordBtn = document.getElementById('bannerChangePasswordBtn');
    const closeChangePasswordBtn = document.getElementById('closeChangePasswordBtn');
    const cancelChangePasswordBtn = document.getElementById('cancelChangePasswordBtn');

    function openChangePassword() {
      if (!changePwdModal) return;
      changePwdForm?.reset();
      changePwdAlert?.classList.add('hidden');
      changePwdModal.classList.remove('hidden');
    }

    function closeChangePassword() {
      if (!changePwdModal) return;
      changePwdModal.classList.add('hidden');
      changePwdAlert?.classList.add('hidden');
      changePwdForm?.reset();
    }

    openChangePasswordBtn?.addEventListener('click', openChangePassword);
    bannerChangePasswordBtn?.addEventListener('click', openChangePassword);
    closeChangePasswordBtn?.addEventListener('click', closeChangePassword);
    cancelChangePasswordBtn?.addEventListener('click', closeChangePassword);

    changePwdForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!supabaseClient) return;

      const newPassword = document.getElementById('newPasswordInput')?.value || '';
      const confirmPassword = document.getElementById('confirmPasswordInput')?.value || '';
      const saveBtn = document.getElementById('submitChangePasswordBtn');

      if (newPassword.length < 6) {
        changePwdAlert.textContent = 'Password must be at least 6 characters.';
        changePwdAlert.classList.remove('hidden');
        return;
      }

      if (newPassword !== confirmPassword) {
        changePwdAlert.textContent = 'Passwords do not match. Please re-enter.';
        changePwdAlert.classList.remove('hidden');
        return;
      }

      saveBtn.disabled = true;
      saveBtn.textContent = 'Updating Password...';
      changePwdAlert.classList.add('hidden');

      try {
        const { data, error } = await supabaseClient.auth.updateUser({
          password: newPassword
        });

        if (error) throw error;

        // Clear temporary password indicator
        sessionStorage.removeItem('using_temp_password');
        document.getElementById('tempPasswordNotice')?.classList.add('hidden');
        closeChangePassword();
        showToast('Password updated successfully! Use your new password next time you sign in.', 'success');
      } catch (err) {
        console.error('Password update error:', err);
        changePwdAlert.textContent = err.message || 'Failed to update password.';
        changePwdAlert.classList.remove('hidden');
      } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save New Password';
      }
    });
  }

  let onConfirmCallback = null;
  function openConfirmModal(title, message, onOk) {
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').textContent = message;
    onConfirmCallback = onOk;
    document.getElementById('confirmModal').classList.remove('hidden');
  }

  document.getElementById('confirmOkBtn')?.addEventListener('click', async () => {
    document.getElementById('confirmModal').classList.add('hidden');
    if (onConfirmCallback) {
      await onConfirmCallback();
      onConfirmCallback = null;
    }
  });

  // Settings / Reconnect
  function setupConfigForm() {
    const configForm = document.getElementById('supabaseConfigForm');
    const cfgUrlInput = document.getElementById('cfgSupabaseUrl');
    const cfgKeyInput = document.getElementById('cfgSupabaseAnonKey');

    // Pre-populate
    cfgUrlInput.value = window.STUDIO_CONFIG?.supabaseUrl || 'https://zzswtrpdjllmagjjmxbx.supabase.co';
    cfgKeyInput.value = window.STUDIO_CONFIG?.supabaseAnonKey || '';

    configForm?.addEventListener('submit', (e) => {
      e.preventDefault();
      const url = cfgUrlInput.value.trim();
      const key = cfgKeyInput.value.trim();

      localStorage.setItem('studio_supabase_url', url);
      localStorage.setItem('studio_supabase_anon_key', key);
      window.STUDIO_CONFIG.supabaseUrl = url;
      window.STUDIO_CONFIG.supabaseAnonKey = key;

      showToast('Config saved! Reconnecting...', 'success');
      initSupabase();
    });

    document.getElementById('testConnectionBtn')?.addEventListener('click', async () => {
      if (!supabaseClient) {
        showToast('Save config first.', 'error');
        return;
      }
      try {
        const { data, error } = await supabaseClient.from('channel_profile').select('count');
        if (error) throw error;
        showToast('Connection test SUCCESSFUL! Database reached.', 'success');
      } catch (err) {
        showToast('Connection test failed: ' + err.message, 'error');
      }
    });
  }

  function requireAdmin() {
    if (!isAuthorizedAdmin) {
      showToast('Action forbidden: Log in with an authorized admin account.', 'error');
      showAuthBarrier(true);
      return false;
    }
    return true;
  }

  function formatCount(val) {
    const num = Number(val) || 0;
    if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toString();
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'toast ' + (type === 'success' ? 'toast-success' : '');
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 3800);
  }

  // Start
  document.addEventListener('DOMContentLoaded', init);
})();
