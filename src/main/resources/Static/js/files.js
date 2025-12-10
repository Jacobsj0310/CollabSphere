// src/main/resources/static/js/files.js
// File upload with progress & listing. Depends on main.js ApiClient
(function(ns) {
  if (!ns) return;
  const { api, showAlert, $ } = ns;

  // Upload a file via multipart to /api/files/upload
  async function uploadFile(inputSelector = '#file-input', teamId = null, progressSelector = '#upload-progress', resultSelector = '#upload-result') {
    const input = document.querySelector(inputSelector);
    const progressEl = document.querySelector(progressSelector);
    const resultEl = document.querySelector(resultSelector);

    if (!input || input.files.length === 0) {
      showAlert(resultEl, 'Please select a file to upload');
      return;
    }
    const file = input.files[0];
    const fd = new FormData();
    fd.append('file', file);
    if (teamId) fd.append('teamId', String(teamId));

    // Use raw fetch with signal and progress via XHR (fetch doesn't provide upload progress widely)
    try {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', '/api/files/upload', true);
      xhr.withCredentials = true; // include same-origin cookies
      xhr.upload.onprogress = function(e) {
        if (!progressEl) return;
        if (e.lengthComputable) {
          const pct = Math.round((e.loaded / e.total) * 100);
          progressEl.style.width = pct + '%';
          progressEl.textContent = pct + '%';
        }
      };
      xhr.onload = function() {
        if (xhr.status >= 200 && xhr.status < 300) {
          try {
            const res = JSON.parse(xhr.responseText || '{}');
            showAlert(resultEl, 'Upload successful', 'success');
            // if server returned id, redirect to files list or show link
            if (res && res.id) {
              window.location.href = `/ui/files?teamId=${teamId || ''}`;
            } else {
              // reload list if page provides a list fetch
              if (typeof window.CollabSphere.FilesUI !== 'undefined' && window.CollabSphere.FilesUI.loadFiles) {
                window.CollabSphere.FilesUI.loadFiles(teamId);
              }
            }
          } catch (e) {
            showAlert(resultEl, 'Upload completed (unknown response)', 'success');
          }
        } else {
          showAlert(resultEl, 'Upload failed: ' + xhr.statusText, 'danger');
          console.error('Upload failed', xhr.responseText);
        }
      };
      xhr.onerror = function() {
        showAlert(resultEl, 'Upload error', 'danger');
      };

      // clear progress bar
      if (progressEl) {
        progressEl.style.width = '0%';
        progressEl.textContent = '0%';
      }

      xhr.send(fd);
    } catch (err) {
      showAlert(resultEl, 'Upload failed: ' + (err.message || err), 'danger');
      console.error(err);
    }
  }

  // Load files for a team & render in #files-table-body (expects server API /api/files/team/{teamId})
  async function loadFiles(teamId, tableBodySelector = '#files-table-body') {
    const tbody = document.querySelector(tableBodySelector);
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="3">Loading...</td></tr>';
    try {
      const files = await api.get(`/api/files/team/${teamId}`);
      if (!Array.isArray(files) || files.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3">No files</td></tr>';
        return;
      }
      tbody.innerHTML = '';
      files.forEach(f => {
        const tr = document.createElement('tr');
        const nameTd = document.createElement('td');
        nameTd.textContent = f.fileName;
        const sizeTd = document.createElement('td');
        sizeTd.textContent = humanFileSize(f.size || 0);
        const actionsTd = document.createElement('td');
        const dl = document.createElement('a');
        dl.className = 'btn btn-sm btn-outline-primary';
        dl.textContent = 'Download';
        dl.href = `/ui/files/${f.id}/download`; // redirect endpoint in file UI
        actionsTd.appendChild(dl);
        tr.appendChild(nameTd);
        tr.appendChild(sizeTd);
        tr.appendChild(actionsTd);
        tbody.appendChild(tr);
      });
    } catch (err) {
      tbody.innerHTML = '';
      showAlert(tbody, 'Failed to load files: ' + (err.message || err), 'danger');
      console.error(err);
    }
  }

  function humanFileSize(bytes) {
    if (bytes === 0) return '0 B';
    const units = ['B','KB','MB','GB','TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes / Math.pow(1024, i)).toFixed( (i===0?0:2) ) + ' ' + units[i];
  }

  // Attach upload handler to a form/button (form doesn't need to submit)
  function attachUploadButton(buttonSelector = '#upload-button', inputSelector = '#file-input', teamIdInputSelector = '#team-id-input', progressBarSelector = '#upload-progress', resultSelector = '#upload-result') {
    const btn = document.querySelector(buttonSelector);
    if (!btn) return;
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      const input = document.querySelector(inputSelector);
      const teamIdEl = document.querySelector(teamIdInputSelector);
      const teamId = teamIdEl ? teamIdEl.value : null;
      uploadFile(inputSelector, teamId, progressBarSelector, resultSelector);
    });
  }

  // public API
  window.CollabSphere.FilesUI = {
    uploadFile,
    loadFiles,
    attachUploadButton,
    humanFileSize
  };

  // auto-init: if table exists and teamId present in DOM, load files
  document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.querySelector('#files-table-body');
    const teamIdInput = document.querySelector('input[name="teamId"], #team-id-input');
    if (tableBody && teamIdInput && teamIdInput.value) {
      loadFiles(teamIdInput.value, '#files-table-body');
    }

    // attach upload button if present
    if (document.querySelector('#upload-button')) {
      attachUploadButton('#upload-button', '#file-input', '#team-id-input', '#upload-progress', '#upload-result');
    }
  });

})(window.CollabSphere);