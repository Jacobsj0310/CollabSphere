// src/main/resources/static/js/teams.js
// Depends on main.js (window.CollabSphere.api, showAlert)
(function(ns) {
  if (!ns) return;
  const { api, showAlert, $ } = ns;

  // Loads teams into #teams-list (expects server endpoint /api/teams or controller model provided teams)
  async function loadTeams(containerSelector = '#teams-list', endpoint = '/api/teams') {
    const container = document.querySelector(containerSelector);
    if (!container) return;
    container.innerHTML = '<div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div>';
    try {
      const teams = await api.get(endpoint);
      if (!Array.isArray(teams) || teams.length === 0) {
        container.innerHTML = '<div class="alert alert-info">No teams found.</div>';
        return;
      }
      container.innerHTML = '';
      teams.forEach(t => {
        const el = document.createElement('div');
        el.className = 'list-group-item';
        el.innerHTML = `
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <a href="/ui/teams/${t.id}" class="h5 mb-1">${escapeHtml(t.name)}</a>
              <div class="small text-muted">${escapeHtml(t.description || '')}</div>
            </div>
            <div>
              <a class="btn btn-sm btn-outline-primary" href="/ui/teams/${t.id}">Open</a>
            </div>
          </div>
        `;
        container.appendChild(el);
      });
    } catch (err) {
      container.innerHTML = '';
      showAlert(container, `Failed to load teams: ${err.message || err}`);
      console.error(err);
    }
  }

  // Create team form handler (AJAX)
  async function attachCreateForm(formSelector = '#create-team-form', feedbackSelector = '#team-form-feedback') {
    const form = document.querySelector(formSelector);
    const feedback = document.querySelector(feedbackSelector);
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const formData = new FormData(form);
      const name = formData.get('name')?.trim();
      const description = formData.get('description')?.trim();
      if (!name) { showAlert(feedback, 'Name is required'); return; }
      try {
        const payload = { name, description };
        const result = await api.post('/api/teams', payload);
        showAlert(feedback, 'Team created', 'success');
        // redirect to team page if id present in response
        if (result && result.id) {
          window.location.href = `/ui/teams/${result.id}`;
        } else {
          // else reload teams list if present
          await loadTeams();
        }
      } catch (err) {
        showAlert(feedback, `Create failed: ${err.message || err}`, 'danger');
        console.error(err);
      }
    });
  }

  function escapeHtml(s) {
    if (!s) return '';
    return s.replace(/[&<>"'`=\/]/g, function (c) {
      return ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;','/':'&#x2F;','`':'&#x60;','=':'&#x3D;' })[c];
    });
  }

  // Auto-init if DOM has containers
  document.addEventListener('DOMContentLoaded', () => {
    if (document.querySelector('#teams-list')) loadTeams('#teams-list', '/api/teams');
    if (document.querySelector('#create-team-form')) attachCreateForm('#create-team-form', '#team-form-feedback');
  });

  // expose for debugging
  ns.TeamsUI = { loadTeams, attachCreateForm };
})(window.CollabSphere);