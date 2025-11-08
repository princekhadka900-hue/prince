async function init(){
  // populate countries
  const cs = await fetch('/api/countries').then(r=>r.json());
  const c1 = document.getElementById('country1');
  const c2 = document.getElementById('country2');
  cs.forEach(x=>{
    const o1=document.createElement('option'); o1.value=o1.textContent=x; c1.appendChild(o1);
    const o2=document.createElement('option'); o2.value=o2.textContent=x; c2.appendChild(o2);
  });
  c1.value='Australia'; c2.value='Nepal';

  const meta = await fetch('/api/metadata').then(r=>r.json());
  const defs = document.getElementById('defs');
  for(const [k,v] of Object.entries(meta.definitions)){
    const li=document.createElement('li'); li.textContent = k + ': ' + v; defs.appendChild(li);
  }
  document.getElementById('rev').textContent = meta.revisionYear;
  document.getElementById('src').textContent = meta.source;
  document.getElementById('lu').textContent = meta.lastUpdate;

  const canvas = document.getElementById('chart');

  async function loadMCV2(){
    const from = document.getElementById('from').value;
    const to = document.getElementById('to').value;
    const country1 = c1.value, country2 = c2.value;
    const data = await fetch(`/api/coverage?indicator=MCV2&country1=${encodeURIComponent(country1)}&country2=${encodeURIComponent(country2)}&from=${from}&to=${to}`).then(r=>r.json());
    const s1 = data.filter(d=>d.country===country1).map(d=>({x:d.year, y:d.value}));
    const s2 = data.filter(d=>d.country===country2).map(d=>({x:d.year, y:d.value}));
    drawChart(canvas, [
      {label: country1 + ' MCV2', data: s1},
      {label: country2 + ' MCV2', data: s2}
    ]);
  }

  async function showZeroDoseReduction(){
    const from = document.getElementById('from').value;
    const to = document.getElementById('to').value;
    const list = await fetch(`/api/zeroDoseReduction?from=${from}&to=${to}`).then(r=>r.json());
    // Draw a simple bar chart of top 5
    const canvas = document.getElementById('chart');
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0,0,canvas.width,canvas.height);
    const pad=40, W=canvas.width, H=canvas.height;
    const bars = list.slice(0,5);
    const maxVal = Math.max(...bars.map(b=>b[1]));
    const barW = (W - pad*2) / (bars.length*1.5);
    ctx.font='12px system-ui'; ctx.fillStyle='#666';
    ctx.fillText('Zero-dose reduction (higher is better)', pad, pad-8);
    bars.forEach((b, i)=>{
      const x = pad + i*barW*1.5;
      const h = (b[1]/maxVal) * (H - pad*2);
      ctx.fillStyle = ['#0b6bcb','#e94e1b','#2e8b57','#8a2be2','#ff9800'][i%5];
      ctx.fillRect(x, H-pad-h, barW, h);
      ctx.fillStyle='#333';
      ctx.fillText(b[0], x, H-pad+14);
      ctx.fillText(Math.round(b[1]), x, H-pad-h-4);
    });
    // axes
    ctx.strokeStyle='#ccc'; ctx.beginPath(); ctx.moveTo(pad,pad); ctx.lineTo(pad,H-pad); ctx.lineTo(W-pad,H-pad); ctx.stroke();
  }

  function downloadCanvas(){
    const canvas = document.getElementById('chart');
    const a = document.createElement('a');
    a.download = 'vaccination_chart.png';
    a.href = canvas.toDataURL('image/png');
    a.click();
  }

  document.getElementById('load').addEventListener('click', loadMCV2);
  document.getElementById('compareZeroDose').addEventListener('click', showZeroDoseReduction);
  document.getElementById('download').addEventListener('click', downloadCanvas);

  // initial
  loadMCV2();
}
init();