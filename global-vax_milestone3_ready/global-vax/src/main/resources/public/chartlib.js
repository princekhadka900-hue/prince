(function(){
  function drawChart(canvas, series){
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0,0,canvas.width,canvas.height);
    const pad=40;
    const W=canvas.width, H=canvas.height;
    const plotW=W-pad*2, plotH=H-pad*2;

    // get x domain (years) and y domain (values)
    const years = [...new Set(series.flatMap(s=>s.data.map(d=>d.x)))].sort((a,b)=>a-b);
    const ymin = Math.min(...series.flatMap(s=>s.data.map(d=>d.y)));
    const ymax = Math.max(...series.flatMap(s=>s.data.map(d=>d.y)));
    const y0 = Math.floor(ymin/5)*5, y1 = Math.ceil(ymax/5)*5;

    function xScale(x){ const i=years.indexOf(x); return pad + (i/(years.length-1))*plotW; }
    function yScale(y){ return H-pad - ((y - y0)/(y1 - y0))*plotH; }

    // axes
    ctx.strokeStyle = '#ccc';
    ctx.beginPath(); ctx.moveTo(pad,pad); ctx.lineTo(pad,H-pad); ctx.lineTo(W-pad,H-pad); ctx.stroke();

    // ticks
    ctx.fillStyle = '#666'; ctx.font = '12px system-ui';
    years.forEach((yr,i)=>{ const x=xScale(yr); ctx.fillText(yr, x-8, H-pad+16); });
    for(let y=y0; y<=y1; y+= (y1-y0)/5){ const yy=yScale(y); ctx.fillText(Math.round(y), 4, yy+4); ctx.strokeStyle='#eee'; ctx.beginPath(); ctx.moveTo(pad,yy); ctx.lineTo(W-pad,yy); ctx.stroke(); }

    // lines
    series.forEach((s,idx)=>{
      ctx.strokeStyle = ['#0b6bcb','#e94e1b','#2e8b57','#8a2be2'][idx%4];
      ctx.fillStyle = ctx.strokeStyle;
      ctx.beginPath();
      s.data.forEach((d,i)=>{
        const x=xScale(d.x), y=yScale(d.y);
        if(i===0) ctx.moveTo(x,y); else ctx.lineTo(x,y);
      });
      ctx.stroke();
      s.data.forEach(d=>{ const x=xScale(d.x), y=yScale(d.y); ctx.beginPath(); ctx.arc(x,y,3,0,Math.PI*2); ctx.fill(); });
      ctx.fillText(s.label, W-pad-140, pad+16+idx*16);
      ctx.fillRect(W-pad-160, pad+8+idx*16-8, 10,10);
    });
  }
  window.drawChart = drawChart;
})();