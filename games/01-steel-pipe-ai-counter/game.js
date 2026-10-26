(() => {
  const canvas = document.getElementById('steelCanvas');
  const ctx = canvas.getContext('2d');

  const hudLevel = document.getElementById('hudLevel');
  const hudTimer = document.getElementById('hudTimer');
  const hudCount = document.getElementById('hudCount');
  const hudScore = document.getElementById('hudScore');
  const hudBest = document.getElementById('hudBest');

  const valPrecision = document.getElementById('valPrecision');
  const valRecall = document.getElementById('valRecall');
  const valF1 = document.getElementById('valF1');
  const valAssists = document.getElementById('valAssists');
  const auditLog = document.getElementById('auditLog');

  const STORAGE_KEY = 'nextai_game_01_steel_best';
  let bestScore = parseInt(localStorage.getItem(STORAGE_KEY) || '0', 10);
  hudBest.textContent = bestScore;

  let audioCtx = null;
  function playTone(freq = 520, duration = 0.07, type = 'sine') {
    try {
      if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = type;
      osc.frequency.value = freq;
      gain.gain.setValueAtTime(0.09, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + duration);
    } catch (_) {}
  }

  const state = {
    level: 1,
    maxLevel: 6,
    score: 0,
    timeLeft: 45.0,
    assistsLeft: 3,
    showBBoxes: true,
    pipes: [],
    falseClicks: [],
    dragStart: null,
    dragCurrent: null,
    roundOver: false,
    lastFrame: performance.now()
  };

  function logEvent(msg, color = '#8da2c0') {
    const line = document.createElement('div');
    const ts = new Date().toLocaleTimeString('en-GB', { hour12: false });
    line.style.color = color;
    line.textContent = `[${ts}] ${msg}`;
    auditLog.prepend(line);
  }

  function reportScoreToPortal() {
    try {
      const allScores = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      allScores['01-steel-pipe-ai-counter'] = {
        score: Math.max(state.score, bestScore),
        lastPlayed: new Date().toISOString(),
        levelReached: state.level
      };
      localStorage.setItem('nextai_game_scores', JSON.stringify(allScores));
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'NEXTAI_GAME_SCORE',
          gameId: '01-steel-pipe-ai-counter',
          score: state.score,
          bestScore,
          level: state.level
        }, '*');
      }
    } catch (_) {}
  }

  function generateBundle(level) {
    state.pipes = [];
    state.falseClicks = [];
    state.roundOver = false;
    state.timeLeft = Math.max(25, 48 - level * 3);

    const rows = 4 + Math.min(4, level);
    const baseRadius = Math.max(15, 29 - level * 2);
    const centerX = canvas.width / 2;
    const baseY = canvas.height - 85;

    let idCounter = 1;
    for (let r = 0; r < rows; r++) {
      const cols = rows - r + 3;
      const rowWidth = cols * (baseRadius * 2.08);
      const startX = centerX - rowWidth / 2 + baseRadius;
      const y = baseY - r * (baseRadius * 1.82);

      for (let c = 0; c < cols; c++) {
        const jitterX = (Math.random() - 0.5) * 4;
        const jitterY = (Math.random() - 0.5) * 4;
        const radius = baseRadius + (Math.random() - 0.5) * 2.5;
        const isDecoy = level >= 2 && Math.random() < (0.08 + level * 0.02);

        state.pipes.push({
          id: idCounter++,
          x: startX + c * (baseRadius * 2.08) + jitterX,
          y: y + jitterY,
          r: radius,
          isDecoy, // PVC conduit or wood dowel that should NOT be counted as structural steel
          tagged: false,
          aiSuggested: false,
          rustShade: 38 + Math.floor(Math.random() * 28),
          confidence: (0.82 + Math.random() * 0.17).toFixed(2)
        });
      }
    }

    const targetTotal = state.pipes.filter(p => !p.isDecoy).length;
    logEvent(`Level ${level}: Loaded truck bundle (${targetTotal} steel pipes, ${state.pipes.length - targetTotal} PVC decoys).`, '#00e5ff');
    updateMetrics();
  }

  function computeStats() {
    let tp = 0;
    let fp = state.falseClicks.length;
    let fn = 0;

    for (const p of state.pipes) {
      if (!p.isDecoy && p.tagged) tp++;
      if (!p.isDecoy && !p.tagged) fn++;
      if (p.isDecoy && p.tagged) fp++;
    }

    const totalSteel = tp + fn;
    const precision = (tp + fp) === 0 ? 1 : tp / (tp + fp);
    const recall = totalSteel === 0 ? 1 : tp / totalSteel;
    const f1 = (precision + recall) === 0 ? 0 : (2 * precision * recall) / (precision + recall);

    return { tp, fp, fn, totalSteel, precision, recall, f1 };
  }

  function updateMetrics() {
    const s = computeStats();
    hudLevel.textContent = `${state.level} / ${state.maxLevel}`;
    hudCount.textContent = `${s.tp} / ${s.totalSteel}`;
    hudScore.textContent = state.score;
    valPrecision.textContent = `${(s.precision * 100).toFixed(0)}%`;
    valRecall.textContent = `${(s.recall * 100).toFixed(0)}%`;
    valF1.textContent = `${(s.f1 * 100).toFixed(1)}%`;
    valAssists.textContent = state.assistsLeft;
  }

  function drawScene() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Construction yard background & truck flatbed
    const grad = ctx.createLinearGradient(0, 0, 0, canvas.height);
    grad.addColorStop(0, '#0b1322');
    grad.addColorStop(1, '#141e30');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Grid lines
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.05)';
    ctx.lineWidth = 1;
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

    // Truck bed stanchions
    ctx.fillStyle = '#26354d';
    ctx.fillRect(90, canvas.height - 54, canvas.width - 180, 18);
    ctx.fillStyle = '#d9822b';
    ctx.fillRect(95, 130, 14, canvas.height - 170);
    ctx.fillRect(canvas.width - 109, 130, 14, canvas.height - 170);

    // Draw pipes
    let tagIndex = 1;
    for (const p of state.pipes) {
      ctx.save();
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
      if (p.isDecoy) {
        ctx.fillStyle = '#7c5329'; // PVC / orange-brown decoy
      } else {
        ctx.fillStyle = `rgb(${p.rustShade}, ${p.rustShade + 12}, ${p.rustShade + 26})`;
      }
      ctx.fill();
      ctx.lineWidth = 3;
      ctx.strokeStyle = p.isDecoy ? '#d99b52' : '#7f95b5';
      ctx.stroke();

      // Inner hollow bore
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.r * 0.62, 0, Math.PI * 2);
      ctx.fillStyle = '#070b12';
      ctx.fill();

      // Bounding box overlay
      if (state.showBBoxes && (p.tagged || p.aiSuggested)) {
        ctx.strokeStyle = p.isDecoy
          ? '#ff5252'
          : (p.tagged ? '#00e676' : 'rgba(0, 229, 255, 0.75)');
        ctx.lineWidth = 1.6;
        ctx.strokeRect(p.x - p.r, p.y - p.r, p.r * 2, p.r * 2);
      }

      if (p.tagged) {
        ctx.beginPath();
        ctx.arc(p.x, p.y, 7, 0, Math.PI * 2);
        ctx.fillStyle = p.isDecoy ? '#ff5252' : '#00e676';
        ctx.fill();

        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 10px Consolas, monospace';
        ctx.textAlign = 'center';
        ctx.fillText(String(tagIndex++), p.x, p.y - p.r - 3);
      }
      ctx.restore();
    }

    // False positive clicks on background
    for (const fc of state.falseClicks) {
      ctx.strokeStyle = '#ff5252';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(fc.x - 6, fc.y - 6);
      ctx.lineTo(fc.x + 6, fc.y + 6);
      ctx.moveTo(fc.x + 6, fc.y - 6);
      ctx.lineTo(fc.x - 6, fc.y + 6);
      ctx.stroke();
    }

    // Drag selection rectangle
    if (state.dragStart && state.dragCurrent) {
      const x = Math.min(state.dragStart.x, state.dragCurrent.x);
      const y = Math.min(state.dragStart.y, state.dragCurrent.y);
      const w = Math.abs(state.dragCurrent.x - state.dragStart.x);
      const h = Math.abs(state.dragCurrent.y - state.dragStart.y);
      ctx.fillStyle = 'rgba(0, 229, 255, 0.14)';
      ctx.strokeStyle = '#00e5ff';
      ctx.lineWidth = 1.5;
      ctx.fillRect(x, y, w, h);
      ctx.strokeRect(x, y, w, h);
    }

    // NextAI Field Watermark Stamp bottom-left
    ctx.fillStyle = 'rgba(7, 13, 25, 0.84)';
    ctx.fillRect(14, canvas.height - 44, 415, 32);
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.4)';
    ctx.strokeRect(14, canvas.height - 44, 415, 32);
    ctx.fillStyle = '#00e5ff';
    ctx.font = 'bold 12px Consolas, monospace';
    ctx.fillText('NEXTAI STEEL-DETECT NCNN • GPS 10.7769°N, 106.7009°E • VERIFIED', 24, canvas.height - 24);
  }

  function getCanvasPos(evt) {
    const rect = canvas.getBoundingClientRect();
    return {
      x: (evt.clientX - rect.left) * (canvas.width / rect.width),
      y: (evt.clientY - rect.top) * (canvas.height / rect.height)
    };
  }

  canvas.addEventListener('mousedown', (evt) => {
    if (state.roundOver) return;
    const pos = getCanvasPos(evt);
    state.dragStart = pos;
    state.dragCurrent = pos;
  });

  canvas.addEventListener('mousemove', (evt) => {
    if (!state.dragStart) return;
    state.dragCurrent = getCanvasPos(evt);
  });

  canvas.addEventListener('mouseup', (evt) => {
    if (!state.dragStart || state.roundOver) return;
    const endPos = getCanvasPos(evt);
    const dx = Math.abs(endPos.x - state.dragStart.x);
    const dy = Math.abs(endPos.y - state.dragStart.y);

    if (dx > 16 && dy > 16) {
      // Box region select
      const x1 = Math.min(state.dragStart.x, endPos.x);
      const y1 = Math.min(state.dragStart.y, endPos.y);
      const x2 = Math.max(state.dragStart.x, endPos.x);
      const y2 = Math.max(state.dragStart.y, endPos.y);
      let added = 0;
      for (const p of state.pipes) {
        if (!p.isDecoy && p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2 && !p.tagged) {
          p.tagged = true;
          added++;
        }
      }
      if (added > 0) {
        playTone(680, 0.09, 'triangle');
        logEvent(`Region Box tagged +${added} steel pipes.`, '#00e676');
      }
    } else {
      // Single click toggle
      let hit = null;
      for (const p of state.pipes) {
        const dist = Math.hypot(endPos.x - p.x, endPos.y - p.y);
        if (dist <= p.r) {
          hit = p;
          break;
        }
      }
      if (hit) {
        hit.tagged = !hit.tagged;
        if (hit.isDecoy && hit.tagged) {
          playTone(210, 0.14, 'sawtooth');
          logEvent('Warning: Tagged a PVC Decoy pipe! Precision dropped.', '#ff5252');
        } else {
          playTone(hit.tagged ? 620 : 410, 0.06, 'sine');
        }
      } else {
        state.falseClicks.push(endPos);
        playTone(180, 0.12, 'sawtooth');
        logEvent('Missed pipe center (False Positive mark recorded).', '#ffab00');
      }
    }

    state.dragStart = null;
    state.dragCurrent = null;
    updateMetrics();
  });

  function runAiAssist() {
    if (state.assistsLeft <= 0 || state.roundOver) {
      logEvent('No NCNN Auto-Detect sweeps remaining for this level.', '#ff5252');
      return;
    }
    state.assistsLeft--;
    const untaggedSteel = state.pipes.filter(p => !p.isDecoy && !p.tagged);
    const batchCount = Math.min(untaggedSteel.length, Math.max(5, Math.ceil(untaggedSteel.length * 0.55)));
    for (let i = 0; i < batchCount; i++) {
      untaggedSteel[i].tagged = true;
      untaggedSteel[i].aiSuggested = true;
    }
    playTone(840, 0.12, 'triangle');
    logEvent(`NCNN SteelDetect FP16 sweep auto-tagged ${batchCount} pipes in 14.2ms.`, '#00e5ff');
    updateMetrics();
  }

  function verifyRound() {
    if (state.roundOver) return;
    const s = computeStats();
    const timeBonus = Math.round(state.timeLeft * 12);
    const roundPoints = Math.max(0, Math.round(s.f1 * 1000 + timeBonus - s.fp * 60));
    state.score += roundPoints;

    if (state.score > bestScore) {
      bestScore = state.score;
      localStorage.setItem(STORAGE_KEY, String(bestScore));
      hudBest.textContent = bestScore;
    }

    reportScoreToPortal();
    playTone(960, 0.18, 'triangle');
    logEvent(
      `Verified Level ${state.level}: F1 ${(s.f1 * 100).toFixed(1)}% | +${roundPoints} pts!`,
      '#00e676'
    );

    if (state.level < state.maxLevel) {
      state.level++;
      generateBundle(state.level);
    } else {
      logEvent(`Shift Complete! Final Score: ${state.score}. Starting New Shift Cycle.`, '#00e5ff');
      state.level = 1;
      state.assistsLeft = 3;
      generateBundle(state.level);
    }
  }

  document.getElementById('btnAiAssist').addEventListener('click', runAiAssist);
  document.getElementById('btnToggleBoxes').addEventListener('click', () => {
    state.showBBoxes = !state.showBBoxes;
  });
  document.getElementById('btnVerify').addEventListener('click', verifyRound);
  document.getElementById('btnResetLevel').addEventListener('click', () => generateBundle(state.level));

  window.addEventListener('keydown', (e) => {
    if (e.code === 'KeyA') runAiAssist();
    if (e.code === 'KeyB') state.showBBoxes = !state.showBBoxes;
    if (e.code === 'Space') {
      e.preventDefault();
      verifyRound();
    }
    if (e.code === 'KeyR') generateBundle(state.level);
  });

  function loop(now) {
    const dt = (now - state.lastFrame) / 1000;
    state.lastFrame = now;

    if (!state.roundOver) {
      state.timeLeft = Math.max(0, state.timeLeft - dt);
      hudTimer.textContent = `${state.timeLeft.toFixed(1)}s`;
      if (state.timeLeft <= 0) {
        logEvent('Timer expired! Auto-submitting current inspection count.', '#ffab00');
        verifyRound();
      }
    }

    drawScene();
    requestAnimationFrame(loop);
  }

  generateBundle(1);
  requestAnimationFrame(loop);
})();
