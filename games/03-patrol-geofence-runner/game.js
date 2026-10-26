(() => {
  const canvas = document.getElementById('patrolCanvas');
  const ctx = canvas.getContext('2d');

  const hudSector = document.getElementById('hudSector');
  const hudTimer = document.getElementById('hudTimer');
  const hudQr = document.getElementById('hudQr');
  const hudGps = document.getElementById('hudGps');
  const hudScore = document.getElementById('hudScore');
  const cpList = document.getElementById('cpList');
  const patrolLog = document.getElementById('patrolLog');

  const STORAGE_KEY = 'nextai_game_03_patrol_best';
  let bestScore = parseInt(localStorage.getItem(STORAGE_KEY) || '0', 10);

  let audioCtx = null;
  function tone(freq = 640, dur = 0.08) {
    try {
      if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const o = audioCtx.createOscillator();
      const g = audioCtx.createGain();
      o.frequency.value = freq;
      g.gain.setValueAtTime(0.08, audioCtx.currentTime);
      g.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + dur);
      o.connect(g);
      g.connect(audioCtx.destination);
      o.start();
      o.stop(audioCtx.currentTime + dur);
    } catch (_) {}
  }

  const keys = {};
  const state = {
    sector: 1,
    timer: 60.0,
    score: 0,
    gpsIntegrity: 100,
    player: { x: 410, y: 270, vx: 0, vy: 0, speed: 195, targetX: null, targetY: null },
    checkpoints: [],
    spoofers: [],
    walls: [
      { x: 210, y: 120, w: 130, h: 45 },
      { x: 480, y: 120, w: 130, h: 45 },
      { x: 345, y: 245, w: 130, h: 50 },
      { x: 210, y: 375, w: 130, h: 45 },
      { x: 480, y: 375, w: 130, h: 45 }
    ],
    empEffect: 0,
    lastTime: performance.now()
  };

  function logPatrol(msg, color = '#8ea4c2') {
    const d = document.createElement('div');
    d.style.color = color;
    d.textContent = `[${new Date().toLocaleTimeString('en-GB', { hour12: false })}] ${msg}`;
    patrolLog.prepend(d);
  }

  function saveScore() {
    if (state.score > bestScore) {
      bestScore = state.score;
      localStorage.setItem(STORAGE_KEY, String(bestScore));
    }
    try {
      const all = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      all['03-patrol-geofence-runner'] = {
        score: bestScore,
        lastPlayed: new Date().toISOString(),
        sector: state.sector
      };
      localStorage.setItem('nextai_game_scores', JSON.stringify(all));
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'NEXTAI_GAME_SCORE',
          gameId: '03-patrol-geofence-runner',
          score: state.score,
          bestScore
        }, '*');
      }
    } catch (_) {}
  }

  function setupSector(sec) {
    state.sector = sec;
    state.timer = Math.max(40, 65 - sec * 3);
    state.player.x = 410;
    state.player.y = 320;
    state.player.targetX = null;
    state.player.targetY = null;

    const presets = [
      { code: 'QR-GATE-A1', name: 'North Substation Gate', x: 110, y: 95, r: 58 },
      { code: 'QR-TURB-B2', name: 'East Generator Vault', x: 710, y: 95, r: 58 },
      { code: 'QR-CORE-C3', name: 'Central Server Bunker', x: 410, y: 175, r: 54 },
      { code: 'QR-DOCK-D4', name: 'West Logistics Bay', x: 110, y: 445, r: 58 },
      { code: 'QR-HELIPAD', name: 'South Perimeter Pad', x: 710, y: 445, r: 58 }
    ];

    state.checkpoints = presets.map(cp => ({
      ...cp,
      scanned: false,
      lat: (10.775 + cp.y * 0.0001).toFixed(5),
      lng: (106.698 + cp.x * 0.0001).toFixed(5)
    }));

    state.spoofers = [];
    const spooferCount = Math.min(5, 1 + sec);
    for (let i = 0; i < spooferCount; i++) {
      state.spoofers.push({
        x: 120 + Math.random() * 580,
        y: 80 + Math.random() * 380,
        vx: (Math.random() < 0.5 ? -1 : 1) * (55 + sec * 12),
        vy: (Math.random() < 0.5 ? -1 : 1) * (55 + sec * 12),
        r: 16
      });
    }

    renderCpList();
    logPatrol(`Sector ${sec} initialized: 5 Geofence QR Checkpoints & ${spooferCount} Mock-GPS drones active.`, '#00e5ff');
  }

  function renderCpList() {
    cpList.innerHTML = '';
    for (const cp of state.checkpoints) {
      const row = document.createElement('div');
      row.className = 'cp-item';
      row.innerHTML = `
        <span><strong>${cp.code}</strong> • ${cp.name}</span>
        <span style="color:${cp.scanned ? '#00e676' : '#ffab00'}">${cp.scanned ? 'VERIFIED' : 'PENDING'}</span>
      `;
      cpList.appendChild(row);
    }
    const done = state.checkpoints.filter(c => c.scanned).length;
    hudQr.textContent = `${done} / ${state.checkpoints.length}`;
    hudSector.textContent = state.sector;
    hudScore.textContent = state.score;
  }

  function scanNearbyQr() {
    let found = false;
    for (const cp of state.checkpoints) {
      const dist = Math.hypot(state.player.x - cp.x, state.player.y - cp.y);
      if (dist <= cp.r && !cp.scanned) {
        cp.scanned = true;
        found = true;
        const pts = Math.round(300 * (state.gpsIntegrity / 100));
        state.score += pts;
        tone(860, 0.12);
        logPatrol(`Verified ${cp.code} inside ${cp.r}m Geofence (${cp.lat}N, ${cp.lng}E) +${pts} pts!`, '#00e676');
        saveScore();
        break;
      }
    }
    if (!found) {
      tone(220, 0.1);
      logPatrol('Geofence Error: Move inside an unverified green QR Geofence circle first!', '#ffab00');
    }
    renderCpList();

    if (state.checkpoints.every(c => c.scanned)) {
      const shiftBonus = Math.round(state.timer * 15);
      state.score += shiftBonus;
      saveScore();
      tone(1020, 0.2);
      logPatrol(`All 5 Checkpoints Verified! Shift Bonus +${shiftBonus} pts! Advancing to Sector ${state.sector + 1}.`, '#00e5ff');
      setupSector(state.sector + 1);
    }
  }

  function triggerEmp() {
    state.empEffect = 1.0;
    let neutralized = 0;
    state.spoofers.forEach(s => {
      const d = Math.hypot(s.x - state.player.x, s.y - state.player.y);
      if (d <= 170) {
        s.x = 60 + Math.random() * 700;
        s.y = 60 + Math.random() * 420;
        neutralized++;
      }
    });
    state.gpsIntegrity = Math.min(100, state.gpsIntegrity + 12);
    if (neutralized > 0) {
      state.score += neutralized * 90;
      tone(740, 0.14);
      logPatrol(`Aegis Anti-Spoof Pulse neutralized ${neutralized} Mock-GPS drone(s)!`, '#00e5ff');
      saveScore();
    } else {
      tone(420, 0.08);
    }
  }

  function update(dt) {
    state.timer = Math.max(0, state.timer - dt);
    hudTimer.textContent = `${state.timer.toFixed(1)}s`;
    if (state.timer <= 0) {
      logPatrol('Shift timer ended! Resetting sector patrol.', '#ff5252');
      setupSector(state.sector);
      return;
    }

    let dx = 0;
    let dy = 0;
    if (keys['KeyW'] || keys['ArrowUp']) dy -= 1;
    if (keys['KeyS'] || keys['ArrowDown']) dy += 1;
    if (keys['KeyA'] || keys['ArrowLeft']) dx -= 1;
    if (keys['KeyD'] || keys['ArrowRight']) dx += 1;

    if (dx !== 0 || dy !== 0) {
      state.player.targetX = null;
      state.player.targetY = null;
      const len = Math.hypot(dx, dy);
      dx /= len;
      dy /= len;
    } else if (state.player.targetX !== null) {
      const tx = state.player.targetX - state.player.x;
      const ty = state.player.targetY - state.player.y;
      const dist = Math.hypot(tx, ty);
      if (dist > 5) {
        dx = tx / dist;
        dy = ty / dist;
      } else {
        state.player.targetX = null;
        state.player.targetY = null;
      }
    }

    const nx = Math.max(24, Math.min(canvas.width - 24, state.player.x + dx * state.player.speed * dt));
    const ny = Math.max(24, Math.min(canvas.height - 24, state.player.y + dy * state.player.speed * dt));

    const blocked = state.walls.some(w => nx > w.x - 14 && nx < w.x + w.w + 14 && ny > w.y - 14 && ny < w.y + w.h + 14);
    if (!blocked) {
      state.player.x = nx;
      state.player.y = ny;
    }

    // Update Spoofer Drones
    for (const s of state.spoofers) {
      s.x += s.vx * dt;
      s.y += s.vy * dt;
      if (s.x < 25 || s.x > canvas.width - 25) s.vx *= -1;
      if (s.y < 25 || s.y > canvas.height - 25) s.vy *= -1;

      const d = Math.hypot(s.x - state.player.x, s.y - state.player.y);
      if (d < 34) {
        state.gpsIntegrity = Math.max(25, state.gpsIntegrity - 22 * dt);
      }
    }

    if (state.empEffect > 0) {
      state.empEffect = Math.max(0, state.empEffect - dt * 2.4);
    }

    hudGps.textContent = `${state.gpsIntegrity.toFixed(0)}%`;
    hudGps.style.color = state.gpsIntegrity > 75 ? '#00e676' : '#ff5252';
    hudScore.textContent = state.score;
  }

  function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Tactical grid
    ctx.fillStyle = '#070c16';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.06)';
    for (let x = 0; x < canvas.width; x += 40) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, canvas.height);
      ctx.stroke();
    }
    for (let y = 0; y < canvas.height; y += 40) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(canvas.width, y);
      ctx.stroke();
    }

    // Facility walls
    for (const w of state.walls) {
      ctx.fillStyle = '#19273d';
      ctx.fillRect(w.x, w.y, w.w, w.h);
      ctx.strokeStyle = '#334e78';
      ctx.lineWidth = 2;
      ctx.strokeRect(w.x, w.y, w.w, w.h);
    }

    // Geofence circles & QR nodes
    for (const cp of state.checkpoints) {
      const inside = Math.hypot(state.player.x - cp.x, state.player.y - cp.y) <= cp.r;
      ctx.beginPath();
      ctx.arc(cp.x, cp.y, cp.r, 0, Math.PI * 2);
      ctx.fillStyle = cp.scanned
        ? 'rgba(0, 230, 118, 0.12)'
        : inside
        ? 'rgba(0, 229, 255, 0.22)'
        : 'rgba(255, 171, 0, 0.09)';
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = cp.scanned ? '#00e676' : inside ? '#00e5ff' : '#ffab00';
      ctx.setLineDash([6, 4]);
      ctx.stroke();
      ctx.setLineDash([]);

      ctx.fillStyle = cp.scanned ? '#00e676' : '#ffab00';
      ctx.fillRect(cp.x - 10, cp.y - 10, 20, 20);
      ctx.fillStyle = '#05101a';
      ctx.font = 'bold 9px Consolas, monospace';
      ctx.textAlign = 'center';
      ctx.fillText('QR', cp.x, cp.y + 3);

      ctx.fillStyle = '#eef5ff';
      ctx.font = 'bold 11px Segoe UI, sans-serif';
      ctx.fillText(cp.code, cp.x, cp.y - 16);
    }

    // Mock-GPS Spoofers
    for (const s of state.spoofers) {
      ctx.beginPath();
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(255, 82, 82, 0.85)';
      ctx.fill();
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 1.5;
      ctx.stroke();
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 9px Consolas, monospace';
      ctx.textAlign = 'center';
      ctx.fillText('SPOOF', s.x, s.y + 3);
    }

    // EMP Ring
    if (state.empEffect > 0) {
      ctx.beginPath();
      ctx.arc(state.player.x, state.player.y, (1 - state.empEffect) * 175, 0, Math.PI * 2);
      ctx.strokeStyle = `rgba(0, 229, 255, ${state.empEffect})`;
      ctx.lineWidth = 4;
      ctx.stroke();
    }

    // Security Officer
    ctx.beginPath();
    ctx.arc(state.player.x, state.player.y, 14, 0, Math.PI * 2);
    ctx.fillStyle = '#00e5ff';
    ctx.fill();
    ctx.lineWidth = 3;
    ctx.strokeStyle = '#ffffff';
    ctx.stroke();
  }

  canvas.addEventListener('mousedown', (e) => {
    const rect = canvas.getBoundingClientRect();
    state.player.targetX = (e.clientX - rect.left) * (canvas.width / rect.width);
    state.player.targetY = (e.clientY - rect.top) * (canvas.height / rect.height);
  });

  window.addEventListener('keydown', (e) => {
    keys[e.code] = true;
    if (e.code === 'KeyE' || e.code === 'Space') {
      e.preventDefault();
      scanNearbyQr();
    }
    if (e.code === 'KeyQ') triggerEmp();
    if (e.code === 'KeyR') setupSector(state.sector);
  });

  window.addEventListener('keyup', (e) => {
    keys[e.code] = false;
  });

  document.getElementById('btnScanQr').addEventListener('click', scanNearbyQr);
  document.getElementById('btnEmp').addEventListener('click', triggerEmp);
  document.getElementById('btnNewShift').addEventListener('click', () => setupSector(state.sector + 1));

  function loop(now) {
    const dt = Math.min(0.05, (now - state.lastTime) / 1000);
    state.lastTime = now;
    update(dt);
    draw();
    requestAnimationFrame(loop);
  }

  setupSector(1);
  requestAnimationFrame(loop);
})();
