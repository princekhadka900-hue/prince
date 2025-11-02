async function loadTiles() {
  const r = await fetch('/api/indicators');
  const data = await r.json();
  const wrap = document.getElementById('tiles-wrap');
  wrap.innerHTML = '';
  Object.entries(data).forEach(([k,v]) => {
    const d = document.createElement('div');
    d.className = 'tile';
    d.innerHTML = `<h3>${k}</h3><p>${v.value ?? '—'} (${v.year ?? '—'})</p><small>Revision: ${v.revision_year ?? '—'}</small>`;
    wrap.appendChild(d);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  loadTiles();
  const form = document.getElementById('cmp');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(form);
    const q = new URLSearchParams(fd).toString();
    const r = await fetch('/api/compare?' + q);
    const json = await r.json();
    document.getElementById('chart').textContent = JSON.stringify(json, null, 2);
    document.getElementById('last-updated').textContent = String(json.last_updated ?? '—');
  });
  document.getElementById('toggle-defs').onclick = () => {
    const d = document.getElementById('defs');
    d.hidden = !d.hidden;
  };
});
