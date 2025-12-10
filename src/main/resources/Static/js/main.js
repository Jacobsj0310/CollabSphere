// src/main/resources/static/js/main.js
// Simple helper + ApiClient for same-origin requests
class ApiClient {
  constructor(base = '') {
    this.base = base;
  }

  // default options: same-origin so session cookie is sent
  async request(path, { method = 'GET', body = null, headers = {} } = {}) {
    const opts = {
      method,
      headers,
      credentials: 'same-origin'
    };

    if (body && !(body instanceof FormData)) {
      opts.headers = { 'Content-Type': 'application/json', ...headers };
      opts.body = JSON.stringify(body);
    } else if (body instanceof FormData) {
      // let browser set content-type boundary for multipart
      opts.body = body;
    }

    const res = await fetch(this.base + path, opts);
    // try to parse JSON if content-type says so
    const ct = res.headers.get('content-type') || '';
    const isJson = ct.indexOf('application/json') !== -1;

    if (!res.ok) {
      let errBody = null;
      try { errBody = isJson ? await res.json() : await res.text(); } catch (e) { errBody = null; }
      const message = (errBody && errBody.message) ? errBody.message : (typeof errBody === 'string' ? errBody : res.statusText);
      const err = new Error(message || `HTTP ${res.status}`);
      err.status = res.status;
      err.body = errBody;
      throw err;
    }

    return isJson ? await res.json() : await res.text();
  }

  get(path) { return this.request(path, { method: 'GET' }); }
  post(path, body) { return this.request(path, { method: 'POST', body }); }
  put(path, body) { return this.request(path, { method: 'PUT', body }); }
  del(path) { return this.request(path, { method: 'DELETE' }); }
}

const api = new ApiClient(''); // same origin
// small DOM utilities
const $ = sel => document.querySelector(sel);
const $$ = sel => Array.from(document.querySelectorAll(sel));

function showAlert(container, message, type = 'danger', timeout = 5000) {
  if (!container) { alert(message); return; }
  const div = document.createElement('div');
  div.className = `alert alert-${type}`;
  div.textContent = message;
  container.prepend(div);
  if (timeout > 0) setTimeout(() => div.remove(), timeout);
}

// export for other modules when loaded via <script>
window.CollabSphere = { api, showAlert, $ , $$ };