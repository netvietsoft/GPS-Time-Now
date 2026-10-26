(() => {
  const handCanvas = document.getElementById('handCanvas');
  const ctx = handCanvas.getContext('2d');
  const imuCanvas = document.getElementById('imuCanvas');
  const imuCtx = imuCanvas.getContext('2d');

  const hudVis = document.getElementById('hudVis');
  const hudImu = document.getElementById('hudImu');
  const hudChunks = document.getElementById('hudChunks');
  const hudReward = document.getElementById('hudReward');
  const hudScore = document.getElementById('hudScore');

  const valTasks = document.getElementById('valTasks');
  const valVip = document.getElementById('valVip');
  const valPinch = document.getElementById('valPinch');
  const valBest = document.getElementById('valBest');
  const mcapLog = document.getElementById('mcapLog');

  const STORAGE_KEY = 'nextai_game_02_mcap_best';
  let bestScore = parseInt(localStorage.getItem(STORAGE_KEY) || '0', 10);
  valBest.textContent = bestScore;

  let audioCtx = null;
  function beep(freq = 600, dur = 0.07) {
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

  const fovBox = { x: 70, y: 55, w: 680, h: 430 };

  const bins = [
    { id: 'GEAR', label: 'SERVO GEAR BIN', color: '#00e5ff', x: 560, y: 95, w: 155, h: 105 },
    { id: 'CELL', label: 'LI-ION CELL BIN', color: '#00e676', x: 560, y: 220, w: 155, h: 105 },
    { id: 'CHIP', label: 'NCNN NPU BIN', color: '#ffab00', x: 560, y: 345, w: 155, h: 105 }
  ];

  const state = {
    handX: 320,
    handY: 270,
    prevX: 320,
    prevY: 270,
    pinching: false,
    grabbedItem: null,
    items: [],
    visFrames: 100,
    totalFrames: 100,
    imuStability: 96,
    mcapChunks: 0,
    tasksDone: 0,
    brlEarned: 0,
    vipDays: 0,
    score: 0,
    imuHistory: []
  };

  function logMcap(msg, color = '#8ea4c2') {
    const d = document.createElement('div');
    d.style.color = color;
    d.textContent = `[${new Date().toLocaleTimeString('en-GB', { hour12: false })}] ${msg}`;
    mcapLog.prepend(d);
  }

  function syncScore() {
    if (state.score > bestScore) {
      bestScore = state.score;
      localStorage.setItem(STORAGE_KEY, String(bestScore));
      valBest.textContent = bestScore;
    }
    try {
      const all = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      all['02-embodied-hand-mcap-sim'] = {
        score: bestScore,
        lastPlayed: new Date().toISOString(),
        mcapChunks: state.mcapChunks
      };
      localStorage.setItem('nextai_game_scores', JSON.stringify(all));
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'NEXTAI_GAME_SCORE',
          gameId: '02-embodied-hand-mcap-sim',
          score: state.score,
          bestScore
        }, '*');
      }
    } catch (_) {}
  }

  function spawnWorkpieces() {
    const types = [
      { type: 'GEAR', color: '#00e5ff', name: 'Harmonic Gear' },
      { type: 'CELL', color: '#00e676', name: '21700 Battery' },
      { type: 'CHIP', color: '#ffab00', name: 'NextAI NPU Die' }
    ];
    state.items = [];
    for (let i = 0; i < 6; i++) {
      const spec = types[i % types.length];
      state.items.push({
        id: i + 1,
        type: spec.type,
        color: spec.color,
        name: spec.name,
        x: 130 + (i % 2) * 140 + (Math.random() - 0.5) * 35,
        y: 130 + Math.floor(i / 2) * 120 + (Math.random() - 0.5) * 25,
        r: 24
      });
    }
    logMcap('Spawned 6 dexterous manipulation workpieces inside FOV.', '#00e5ff');
  }

  function compute21Keypoints(cx, cy, pinch) {
    // Returns 21 standard MediaPipe Hand Landmarks around wrist (cx, cy + 75)
    const wrist = { x: cx, y: cy + 78 };
    const pinchFactor = pinch ? 0.42 : 1.0;
    const fingers = [
      // Thumb (1..4)
      [
        { x: cx - 26, y: cy + 52 },
        { x: cx - 44, y: cy + 28 },
        { x: cx - 38 * pinchFactor, y: cy + 5 * pinchFactor },
        { x: cx - 18 * pinchFactor, y: cy - 16 * pinchFactor }
      ],
      // Index (5..8)
      [
        { x: cx - 18, y: cy + 20 },
        { x: cx - 20 * pinchFactor, y: cy - 8 },
        { x: cx - 14 * pinchFactor, y: cy - 28 * pinchFactor },
        { x: cx - 6 * pinchFactor, y: cy - 22 * pinchFactor }
      ],
      // Middle (9..12)
      [
        { x: cx - 2, y: cy + 16 },
        { x: cx - 2, y: cy - 16 },
        { x: cx - 1, y: cy - 42 },
        { x: cx, y: cy - 62 }
      ],
      // Ring (13..16)
      [
        { x: cx + 15, y: cy + 20 },
        { x: cx + 18, y: cy - 10 },
        { x: cx + 20, y: cy - 34 },
        { x: cx + 22, y: cy - 52 }
      ],
      // Pinky (17..20)
      [
        { x: cx + 30, y: cy + 30 },
        { x: cx + 36, y: cy + 4 },
        { x: cx + 40, y: cy - 16 },
        { x: cx + 44, y: cy - 32 }
      ]
    ];
    const pts = [wrist];
    for (const f of fingers) pts.push(...f);
    return pts;
  }

  function drawHandStage() {
    ctx.clearRect(0, 0, handCanvas.width, handCanvas.height);

    // Background
    ctx.fillStyle = '#070c16';
    ctx.fillRect(0, 0, handCanvas.width, handCanvas.height);

    // Check if hand is inside FOV
    const inFov =
      state.handX >= fovBox.x &&
      state.handX <= fovBox.x + fovBox.w &&
      state.handY >= fovBox.y &&
      state.handY <= fovBox.y + fovBox.h;

    state.totalFrames++;
    if (inFov) state.visFrames++;
    const visRatio = (state.visFrames / state.totalFrames) * 100;

    // FOV Bounding Box
    ctx.strokeStyle = inFov ? '#00e676' : '#ff5252';
    ctx.lineWidth = 2;
    ctx.setLineDash([8, 6]);
    ctx.strokeRect(fovBox.x, fovBox.y, fovBox.w, fovBox.h);
    ctx.setLineDash([]);

    ctx.fillStyle = inFov ? '#00e676' : '#ff5252';
    ctx.font = 'bold 12px Consolas, monospace';
    ctx.fillText(
      inFov
        ? 'CAMERA FOV LOCKED • HAND VISIBILITY OK (>85%)'
        : 'WARNING: HAND OUTSIDE FOV! QA RATIO DROPPING!',
      fovBox.x + 12,
      fovBox.y - 10
    );

    // Draw Target Bins
    for (const b of bins) {
      ctx.fillStyle = 'rgba(23, 34, 54, 0.85)';
      ctx.fillRect(b.x, b.y, b.w, b.h);
      ctx.strokeStyle = b.color;
      ctx.lineWidth = 2;
      ctx.strokeRect(b.x, b.y, b.w, b.h);
      ctx.fillStyle = b.color;
      ctx.font = 'bold 12px Segoe UI, sans-serif';
      ctx.fillText(b.label, b.x + 12, b.y + 24);
      ctx.fillStyle = '#8ea4c2';
      ctx.font = '11px Consolas, monospace';
      ctx.fillText(`DROP [${b.id}] HERE`, b.x + 12, b.y + 62);
    }

    // Draw Workpieces
    for (const item of state.items) {
      ctx.save();
      ctx.beginPath();
      ctx.arc(item.x, item.y, item.r, 0, Math.PI * 2);
      ctx.fillStyle = item.color;
      ctx.fill();
      ctx.lineWidth = 3;
      ctx.strokeStyle = '#ffffff';
      ctx.stroke();

      ctx.fillStyle = '#041019';
      ctx.font = 'bold 11px Consolas, monospace';
      ctx.textAlign = 'center';
      ctx.fillText(item.type, item.x, item.y + 4);
      ctx.restore();
    }

    // Draw 21-Keypoint Hand Skeleton
    const pts = compute21Keypoints(state.handX, state.handY, state.pinching);
    const chains = [
      [0, 1, 2, 3, 4],
      [0, 5, 6, 7, 8],
      [0, 9, 10, 11, 12],
      [0, 13, 14, 15, 16],
      [0, 17, 18, 19, 20],
      [5, 9, 13, 17]
    ];

    ctx.strokeStyle = state.pinching ? '#00e676' : '#00e5ff';
    ctx.lineWidth = 2.5;
    for (const chain of chains) {
      ctx.beginPath();
      chain.forEach((idx, i) => {
        if (i === 0) ctx.moveTo(pts[idx].x, pts[idx].y);
        else ctx.lineTo(pts[idx].x, pts[idx].y);
      });
      ctx.stroke();
    }

    pts.forEach((p, idx) => {
      ctx.beginPath();
      ctx.arc(p.x, p.y, idx === 4 || idx === 8 ? 6 : 4, 0, Math.PI * 2);
      ctx.fillStyle = idx === 4 || idx === 8 ? '#ffab00' : '#ffffff';
      ctx.fill();
    });

    // Update HUD
    hudVis.textContent = `${visRatio.toFixed(1)}%`;
    hudVis.style.color = visRatio >= 85 ? '#00e676' : '#ff5252';
    hudImu.textContent = `${state.imuStability.toFixed(1)}%`;
    hudChunks.textContent = state.mcapChunks;
    hudReward.textContent = `R$ ${state.brlEarned.toFixed(2)}`;
    hudScore.textContent = state.score;
  }

  function drawImuOscilloscope() {
    imuCtx.clearRect(0, 0, imuCanvas.width, imuCanvas.height);
    const channels = [
      { key: 'ax', color: '#00e5ff' },
      { key: 'ay', color: '#00e676' },
      { key: 'gz', color: '#ffab00' },
      { key: 'rv', color: '#b388ff' }
    ];

    for (const ch of channels) {
      imuCtx.beginPath();
      imuCtx.strokeStyle = ch.color;
      imuCtx.lineWidth = 1.5;
      state.imuHistory.forEach((sample, idx) => {
        const x = (idx / 60) * imuCanvas.width;
        const y = imuCanvas.height / 2 + sample[ch.key];
        if (idx === 0) imuCtx.moveTo(x, y);
        else imuCtx.lineTo(x, y);
      });
      imuCtx.stroke();
    }
  }

  handCanvas.addEventListener('mousemove', (e) => {
    const rect = handCanvas.getBoundingClientRect();
    state.prevX = state.handX;
    state.prevY = state.handY;
    state.handX = (e.clientX - rect.left) * (handCanvas.width / rect.width);
    state.handY = (e.clientY - rect.top) * (handCanvas.height / rect.height);

    const vx = state.handX - state.prevX;
    const vy = state.handY - state.prevY;
    const speed = Math.hypot(vx, vy);

    state.imuStability = Math.max(60, Math.min(99.9, 100 - speed * 0.45));
    state.imuHistory.push({
      ax: Math.max(-42, Math.min(42, vx * 1.4)),
      ay: Math.max(-42, Math.min(42, vy * 1.4)),
      gz: Math.sin(performance.now() * 0.01) * (speed * 0.5 + 4),
      rv: Math.cos(performance.now() * 0.008) * 12
    });
    if (state.imuHistory.length > 60) state.imuHistory.shift();

    if (state.grabbedItem) {
      state.grabbedItem.x = state.handX - 10;
      state.grabbedItem.y = state.handY - 18;
    }
  });

  function setPinch(active) {
    state.pinching = active;
    valPinch.textContent = active ? 'PINCH GRASP' : 'OPEN';
    valPinch.style.color = active ? '#00e676' : '#8ea4c2';

    if (active) {
      // Check if near any workpiece
      const pinchX = state.handX - 10;
      const pinchY = state.handY - 18;
      for (const item of state.items) {
        if (Math.hypot(item.x - pinchX, item.y - pinchY) <= item.r + 22) {
          state.grabbedItem = item;
          beep(540, 0.06);
          logMcap(`Grasped [${item.name}] with Thumb+Index landmarks.`, '#00e5ff');
          break;
        }
      }
    } else if (state.grabbedItem) {
      const dropped = state.grabbedItem;
      state.grabbedItem = null;

      // Check if inside matching bin
      for (const b of bins) {
        if (
          dropped.x >= b.x &&
          dropped.x <= b.x + b.w &&
          dropped.y >= b.y &&
          dropped.y <= b.y + b.h
        ) {
          if (b.id === dropped.type) {
            state.items = state.items.filter(i => i.id !== dropped.id);
            state.tasksDone++;
            state.mcapChunks += 12;
            state.brlEarned += 4.5;
            state.vipDays += 1;
            state.score += Math.round(250 * (state.imuStability / 100));
            valTasks.textContent = `${state.tasksDone} / 12`;
            valVip.textContent = `+${state.vipDays} Days`;
            beep(880, 0.12);
            logMcap(
              `Sorted ${dropped.name} -> ${b.label}! +12 .mcap chunks (+R$ 4.50).`,
              '#00e676'
            );
            syncScore();
            if (state.items.length === 0) {
              spawnWorkpieces();
            }
          } else {
            beep(220, 0.14);
            logMcap(`Wrong bin! Expected ${dropped.type} bin.`, '#ff5252');
          }
          break;
        }
      }
    }
  }

  handCanvas.addEventListener('mousedown', () => setPinch(true));
  window.addEventListener('mouseup', () => setPinch(false));

  function flushMcapEpisode() {
    const visRatio = (state.visFrames / state.totalFrames) * 100;
    if (visRatio < 85) {
      beep(200, 0.15);
      logMcap(`QA Rejected: Hand visibility ${visRatio.toFixed(1)}% < 85.0% threshold!`, '#ff5252');
      return;
    }
    const bonus = state.mcapChunks * 15;
    state.score += bonus;
    state.brlEarned += 12.0;
    beep(980, 0.18);
    logMcap(
      `Uploaded NextAI_Embodied_${Date.now()}.mcap (${state.mcapChunks} chunks) -> QA PASSED! +${bonus} pts`,
      '#00e676'
    );
    syncScore();
  }

  document.getElementById('btnFlushMcap').addEventListener('click', flushMcapEpisode);
  document.getElementById('btnSpawnParts').addEventListener('click', spawnWorkpieces);

  window.addEventListener('keydown', (e) => {
    if (e.code === 'Space' && !state.pinching) {
      e.preventDefault();
      setPinch(true);
    }
    if (e.code === 'KeyE') flushMcapEpisode();
    if (e.code === 'KeyN') spawnWorkpieces();
  });
  window.addEventListener('keyup', (e) => {
    if (e.code === 'Space') setPinch(false);
  });

  function loop() {
    drawHandStage();
    drawImuOscilloscope();
    requestAnimationFrame(loop);
  }

  spawnWorkpieces();
  requestAnimationFrame(loop);
})();
