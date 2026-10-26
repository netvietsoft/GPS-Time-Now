(() => {
  const canvas = document.getElementById('collageCanvas');
  const ctx = canvas.getContext('2d');

  const hudMile = document.getElementById('hudMile');
  const hudTimer = document.getElementById('hudTimer');
  const hudPairs = document.getElementById('hudPairs');
  const hudScore = document.getElementById('hudScore');

  const cntPdf = document.getElementById('cntPdf');
  const cntXls = document.getElementById('cntXls');
  const cntKmz = document.getElementById('cntKmz');
  const collageLog = document.getElementById('collageLog');

  const STORAGE_KEY = 'nextai_game_05_collage_best';
  let bestScore = parseInt(localStorage.getItem(STORAGE_KEY) || '0', 10);

  let audioCtx = null;
  function beep(freq = 640, dur = 0.08) {
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

  const state = {
    milestone: 1,
    timer: 50.0,
    score: 0,
    cards: [],
    selectedCard: null,
    matchedCount: 0,
    exportsCount: 0,
    lastFrame: performance.now()
  };

  function logMsg(msg, color = '#8ea4c2') {
    const d = document.createElement('div');
    d.style.color = color;
    d.textContent = `[${new Date().toLocaleTimeString('en-GB', { hour12: false })}] ${msg}`;
    collageLog.prepend(d);
  }

  function saveScore() {
    if (state.score > bestScore) {
      bestScore = state.score;
      localStorage.setItem(STORAGE_KEY, String(bestScore));
    }
    try {
      const all = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      all['05-field-collage-builder'] = {
        score: bestScore,
        lastPlayed: new Date().toISOString(),
        exportsCount: state.exportsCount
      };
      localStorage.setItem('nextai_game_scores', JSON.stringify(all));
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'NEXTAI_GAME_SCORE',
          gameId: '05-field-collage-builder',
          score: state.score,
          bestScore
        }, '*');
      }
    } catch (_) {}
  }

  function setupBoard() {
    const specs = [
      { pairId: 1, stake: 'K12+400', azimuth: '045° NE', title: 'Rebar Cage Assembly', color: '#00e5ff' },
      { pairId: 2, stake: 'K12+850', azimuth: '132° SE', title: 'Foundation Concrete Pour', color: '#00e676' },
      { pairId: 3, stake: 'K13+120', azimuth: '218° SW', title: 'HV Conduit Trenching', color: '#ffab00' },
      { pairId: 4, stake: 'K13+600', azimuth: '310° NW', title: 'Asphalt Surface Layer', color: '#b388ff' }
    ];

    const deck = [];
    for (const s of specs) {
      deck.push({ ...s, phase: 'BEFORE', matched: false, hint: false });
      deck.push({ ...s, phase: 'AFTER', matched: false, hint: false });
    }

    // Shuffle deck
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }

    // Position 4x2 grid
    state.cards = deck.map((c, idx) => {
      const col = idx % 4;
      const row = Math.floor(idx / 4);
      return {
        ...c,
        id: idx,
        x: 32 + col * 194,
        y: 65 + row * 225,
        w: 176,
        h: 198
      };
    });

    state.selectedCard = null;
    state.matchedCount = 0;
    state.timer = Math.max(30, 52 - state.milestone * 2);
    updateHud();
    logMsg(`Milestone #${state.milestone}: Match 4 BEFORE/AFTER engineering inspection pairs by Stake & Azimuth.`, '#00e5ff');
  }

  function updateHud() {
    hudMile.textContent = state.milestone;
    hudPairs.textContent = `${state.matchedCount} / 4`;
    hudScore.textContent = state.score;
    cntPdf.textContent = state.exportsCount;
    cntXls.textContent = state.exportsCount;
    cntKmz.textContent = state.exportsCount;
  }

  function drawBoard() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    ctx.fillStyle = '#070c16';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.fillStyle = '#00e5ff';
    ctx.font = 'bold 14px Segoe UI, sans-serif';
    ctx.fillText('NEXTAI BEFORE / AFTER COLLAGE BOARD • CLICK MATCHING STAKE + AZIMUTH PAIRS', 32, 36);

    for (const c of state.cards) {
      ctx.save();
      ctx.fillStyle = c.matched ? 'rgba(0, 230, 118, 0.14)' : '#121d30';
      ctx.fillRect(c.x, c.y, c.w, c.h);

      const isSel = state.selectedCard && state.selectedCard.id === c.id;
      ctx.lineWidth = isSel || c.hint ? 3 : 1.8;
      ctx.strokeStyle = c.matched
        ? '#00e676'
        : isSel
        ? '#00e5ff'
        : c.hint
        ? '#ffab00'
        : 'rgba(255,255,255,0.15)';
      ctx.strokeRect(c.x, c.y, c.w, c.h);

      // Phase Header Pill
      ctx.fillStyle = c.phase === 'BEFORE' ? '#ffab00' : '#00e676';
      ctx.fillRect(c.x + 10, c.y + 10, 74, 22);
      ctx.fillStyle = '#041218';
      ctx.font = 'bold 11px Consolas, monospace';
      ctx.fillText(c.phase, c.x + 20, c.y + 25);

      // Simulated Photo Illustration Box
      ctx.fillStyle = '#09111f';
      ctx.fillRect(c.x + 10, c.y + 40, c.w - 20, 78);
      ctx.strokeStyle = c.color;
      ctx.lineWidth = 2;
      ctx.strokeRect(c.x + 18, c.y + 48, c.w - 36, 62);

      // Stake & Azimuth Watermark Metadata
      ctx.fillStyle = '#eef5ff';
      ctx.font = 'bold 12px Segoe UI, sans-serif';
      ctx.fillText(c.title, c.x + 10, c.y + 138);

      ctx.fillStyle = c.color;
      ctx.font = 'bold 12px Consolas, monospace';
      ctx.fillText(`STAKE : ${c.stake}`, c.x + 10, c.y + 158);
      ctx.fillStyle = '#8ea4c2';
      ctx.fillText(`AZIMUTH: ${c.azimuth}`, c.x + 10, c.y + 176);

      if (c.matched) {
        ctx.fillStyle = '#00e676';
        ctx.font = 'bold 11px Consolas, monospace';
        ctx.fillText('✓ COLLAGE LINKED', c.x + 10, c.y + 192);
      }

      ctx.restore();
    }
  }

  canvas.addEventListener('mousedown', (e) => {
    const rect = canvas.getBoundingClientRect();
    const mx = (e.clientX - rect.left) * (canvas.width / rect.width);
    const my = (e.clientY - rect.top) * (canvas.height / rect.height);

    const clicked = state.cards.find(
      c => !c.matched && mx >= c.x && mx <= c.x + c.w && my >= c.y && my <= c.y + c.h
    );
    if (!clicked) return;

    state.cards.forEach(c => (c.hint = false));

    if (!state.selectedCard) {
      state.selectedCard = clicked;
      beep(520, 0.05);
      return;
    }

    if (state.selectedCard.id === clicked.id) {
      state.selectedCard = null;
      return;
    }

    if (
      state.selectedCard.pairId === clicked.pairId &&
      state.selectedCard.phase !== clicked.phase
    ) {
      state.selectedCard.matched = true;
      clicked.matched = true;
      state.matchedCount++;
      state.score += 220;
      beep(880, 0.12);
      logMsg(`Matched ${clicked.stake} (${clicked.azimuth}) BEFORE + AFTER pair! +220 pts`, '#00e676');
      state.selectedCard = null;
      updateHud();
      saveScore();
    } else {
      beep(210, 0.12);
      logMsg('Mismatch! Select one BEFORE and one AFTER photo with identical Stake & Azimuth.', '#ff5252');
      state.selectedCard = null;
    }
  });

  function compileExportBatch() {
    if (state.matchedCount < 4) {
      beep(240, 0.1);
      logMsg('Match all 4 BEFORE/AFTER pairs before compiling PDF/XLSX/KMZ deliverable!', '#ffab00');
      return;
    }
    const timeBonus = Math.round(state.timer * 10);
    state.score += 400 + timeBonus;
    state.exportsCount++;
    state.milestone++;
    beep(1020, 0.18);
    logMsg(
      `Exported NextAI_Milestone_${state.exportsCount}.pdf + .xlsx + .kmz! Bonus +${400 + timeBonus} pts`,
      '#00e5ff'
    );
    saveScore();
    setupBoard();
  }

  function showHint() {
    const unmatched = state.cards.filter(c => !c.matched);
    if (unmatched.length >= 2) {
      const targetPair = unmatched[0].pairId;
      state.cards.forEach(c => {
        if (c.pairId === targetPair) c.hint = true;
      });
      beep(660, 0.07);
    }
  }

  document.getElementById('btnExport').addEventListener('click', compileExportBatch);
  document.getElementById('btnAutoHint').addEventListener('click', showHint);
  document.getElementById('btnShuffle').addEventListener('click', setupBoard);

  window.addEventListener('keydown', (e) => {
    if (e.code === 'Space') {
      e.preventDefault();
      compileExportBatch();
    }
    if (e.code === 'KeyH') showHint();
    if (e.code === 'KeyR') setupBoard();
  });

  function loop(now) {
    const dt = Math.min(0.05, (now - state.lastFrame) / 1000);
    state.lastFrame = now;
    state.timer = Math.max(0, state.timer - dt);
    hudTimer.textContent = `${state.timer.toFixed(1)}s`;
    if (state.timer <= 0) {
      logMsg('Inspection window expired! Shuffling new engineering photo batch.', '#ffab00');
      setupBoard();
    }
    drawBoard();
    requestAnimationFrame(loop);
  }

  setupBoard();
  requestAnimationFrame(loop);
})();
