(() => {
  const canvas = document.getElementById('forensicCanvas');
  const ctx = canvas.getContext('2d');

  const hudCase = document.getElementById('hudCase');
  const hudAcc = document.getElementById('hudAcc');
  const hudStreak = document.getElementById('hudStreak');
  const hudScore = document.getElementById('hudScore');
  const inspectorOut = document.getElementById('inspectorOut');
  const caseLog = document.getElementById('caseLog');

  const STORAGE_KEY = 'nextai_game_04_forensics_best';
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
    caseNum: 101,
    totalCases: 0,
    correctCases: 0,
    streak: 0,
    score: 0,
    scanLineY: 0,
    currentPhoto: null
  };

  function logCase(msg, color = '#8ea4c2') {
    const d = document.createElement('div');
    d.style.color = color;
    d.textContent = msg;
    caseLog.prepend(d);
  }

  function syncPortalScore() {
    if (state.score > bestScore) {
      bestScore = state.score;
      localStorage.setItem(STORAGE_KEY, String(bestScore));
    }
    try {
      const all = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      all['04-truth-forensics-detective'] = {
        score: bestScore,
        lastPlayed: new Date().toISOString(),
        casesSolved: state.correctCases
      };
      localStorage.setItem('nextai_game_scores', JSON.stringify(all));
      if (window.parent && window.parent !== window) {
        window.parent.postMessage({
          type: 'NEXTAI_GAME_SCORE',
          gameId: '04-truth-forensics-detective',
          score: state.score,
          bestScore
        }, '*');
      }
    } catch (_) {}
  }

  function randomBase62(len = 12) {
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    let s = '';
    for (let i = 0; i < len; i++) s += chars[Math.floor(Math.random() * chars.length)];
    return s;
  }

  function generateCase() {
    const isTampered = Math.random() < 0.55;
    let tamperType = 'NONE';
    if (isTampered) {
      const types = ['STEGO_MISMATCH', 'HMAC_INVALID', 'NTP_CLOCK_SPOOF', 'AEGIS_MOCK_GPS'];
      tamperType = types[Math.floor(Math.random() * types.length)];
    }

    const trueCode = randomBase62(12);
    const shownCode = tamperType === 'HMAC_INVALID' ? randomBase62(12) : trueCode;
    const ntpTime = '2026-10-26 09:42:18 UTC';
    const sysTime = tamperType === 'NTP_CLOCK_SPOOF' ? '2026-10-24 21:15:04 UTC' : '2026-10-26 09:42:19 UTC';
    const clockDriftSec = tamperType === 'NTP_CLOCK_SPOOF' ? 131234 : 1;

    const sites = [
      'Metro Line 4 Viaduct Pier #18',
      'Solar Substation HV Transformer B',
      'NextAI Embodied Lab Cleanroom #3',
      'Deepwater Port Crane Inspection #09'
    ];
    const siteName = sites[Math.floor(Math.random() * sites.length)];

    state.currentPhoto = {
      caseId: `CASE-${state.caseNum}`,
      siteName,
      isTampered,
      tamperType,
      shownCode,
      computedHmacCode: trueCode,
      ntpTime,
      sysTime,
      clockDriftSec,
      stegoMagic: tamperType === 'STEGO_MISMATCH' ? 'CORRUPTED_EOI_SLICE' : 'NEXTAI_JPEG_PACK_END_2026',
      stegoOrigHash: 'sha256:8f94d2c1b07e4a11',
      stegoCanvasHash: tamperType === 'STEGO_MISMATCH' ? 'sha256:00deadbeef991234' : 'sha256:8f94d2c1b07e4a11',
      aegisEcdsaValid: tamperType !== 'AEGIS_MOCK_GPS',
      mockGpsFlag: tamperType === 'AEGIS_MOCK_GPS'
    };

    hudCase.textContent = `#${state.caseNum}`;
    inspectorOut.textContent =
      `Loaded Evidence ${state.currentPhoto.caseId}\n` +
      `Site: ${siteName}\n` +
      `Watermark AntiFakeCode: ${shownCode}\n` +
      `Run Diagnostic Tools [1-4] before rendering your verdict!`;
  }

  function runTool(num) {
    const p = state.currentPhoto;
    beep(580 + num * 70, 0.06);
    if (num === 1) {
      inspectorOut.textContent =
        `[TOOL 1: JpegPacker Binary Trailer Unpacker]\n` +
        `JPEG EOI Marker: 0xFFD9 Found at offset 0x04A810\n` +
        `Trailer Magic: "${p.stegoMagic}"\n` +
        `Unpacked Original Slice Hash: ${p.stegoOrigHash}\n` +
        `Visible Pixels Region Hash : ${p.stegoCanvasHash}\n` +
        `Status: ${p.stegoOrigHash === p.stegoCanvasHash ? 'SLICE MATCH (OK)' : 'PIXEL SPLICE TAMPER DETECTED!'}`;
    } else if (num === 2) {
      inspectorOut.textContent =
        `[TOOL 2: AntiFakeCode Base62 HMAC-SHA256]\n` +
        `Printed Watermark Code : ${p.shownCode}\n` +
        `Recomputed HMAC Base62 : ${p.computedHmacCode}\n` +
        `Key Derivation         : PBKDF2-HMAC-SHA256\n` +
        `Status: ${p.shownCode === p.computedHmacCode ? 'SIGNATURE VALID (OK)' : 'FORGED WATERMARK CODE!'}`;
    } else if (num === 3) {
      inspectorOut.textContent =
        `[TOOL 3: Trusted NTP vs System Clock Analyzer]\n` +
        `Trusted NTP Atomic Time: ${p.ntpTime}\n` +
        `Device System Clock    : ${p.sysTime}\n` +
        `Delta Drift            : ${p.clockDriftSec}s (Max Allowed: 30s)\n` +
        `Status: ${p.clockDriftSec <= 30 ? 'CLOCK SYNC VERIFIED (OK)' : 'SYSTEM CLOCK BACKDATED / SPOOFED!'}`;
    } else if (num === 4) {
      inspectorOut.textContent =
        `[TOOL 4: Aegis Security ECDSA & GPS Guard]\n` +
        `X-NextAI-Sign Header   : ECDSA-P256-${p.aegisEcdsaValid ? 'VERIFIED' : 'INVALID_SIG'}\n` +
        `Android MockLocation   : ${p.mockGpsFlag ? 'DETECTED (TRUE)' : 'CLEAN (FALSE)'}\n` +
        `Status: ${p.aegisEcdsaValid && !p.mockGpsFlag ? 'HARDWARE ATTESTATION OK' : 'MOCK GPS / UNSIGNED DEVICE!'}`;
    }
  }

  function submitVerdict(claimedTampered) {
    const p = state.currentPhoto;
    state.totalCases++;
    const correct = claimedTampered === p.isTampered;

    if (correct) {
      state.correctCases++;
      state.streak++;
      const pts = 250 + state.streak * 45;
      state.score += pts;
      beep(920, 0.14);
      logCase(
        `[PASS] ${p.caseId}: Correctly ruled ${p.isTampered ? 'TAMPERED (' + p.tamperType + ')' : 'AUTHENTIC'} (+${pts} pts)`,
        '#00e676'
      );
    } else {
      state.streak = 0;
      beep(210, 0.18);
      logCase(
        `[FAIL] ${p.caseId}: Missed ${p.isTampered ? 'Forgery (' + p.tamperType + ')' : 'Authentic Photo'}!`,
        '#ff5252'
      );
    }

    const acc = (state.correctCases / state.totalCases) * 100;
    hudAcc.textContent = `${acc.toFixed(0)}%`;
    hudStreak.textContent = state.streak;
    hudScore.textContent = state.score;
    syncPortalScore();

    state.caseNum++;
    generateCase();
  }

  function drawEvidenceCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const p = state.currentPhoto;
    if (!p) return;

    // Simulated site engineering photo
    const grad = ctx.createLinearGradient(0, 0, 0, canvas.height);
    grad.addColorStop(0, '#0f203c');
    grad.addColorStop(1, '#1a2d42');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Structural steel / tower crane illustration
    ctx.strokeStyle = '#3e628f';
    ctx.lineWidth = 4;
    ctx.strokeRect(90, 70, 620, 340);

    // Grid inside photo
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.12)';
    ctx.lineWidth = 1;
    for (let x = 130; x < 700; x += 60) {
      ctx.beginPath();
      ctx.moveTo(x, 70);
      ctx.lineTo(x, 410);
      ctx.stroke();
    }

    // Simulated Original Unpacked Thumbnail on Top-Right
    ctx.fillStyle = 'rgba(8, 13, 24, 0.9)';
    ctx.fillRect(540, 85, 155, 110);
    ctx.strokeStyle = '#b388ff';
    ctx.lineWidth = 2;
    ctx.strokeRect(540, 85, 155, 110);
    ctx.fillStyle = '#b388ff';
    ctx.font = 'bold 10px Consolas, monospace';
    ctx.fillText('JPEGPACKER RAW SLICE', 550, 103);
    ctx.fillStyle = p.tamperType === 'STEGO_MISMATCH' ? '#ff5252' : '#00e676';
    ctx.fillText(p.stegoCanvasHash.slice(0, 18), 550, 145);

    // Watermark Card Stamp Overlay
    ctx.fillStyle = 'rgba(7, 12, 22, 0.88)';
    ctx.fillRect(105, 275, 420, 120);
    ctx.strokeStyle = '#00e5ff';
    ctx.lineWidth = 2;
    ctx.strokeRect(105, 275, 420, 120);

    ctx.fillStyle = '#00e5ff';
    ctx.font = 'bold 14px Segoe UI, sans-serif';
    ctx.fillText(`NEXTAI VERIFYAUTH STAMP • ${p.caseId}`, 120, 300);

    ctx.fillStyle = '#eef5ff';
    ctx.font = '12px Consolas, monospace';
    ctx.fillText(`SITE : ${p.siteName}`, 120, 324);
    ctx.fillText(`CLOCK: ${p.sysTime}`, 120, 344);
    ctx.fillText(`CODE : ${p.shownCode}`, 120, 364);
    ctx.fillStyle = '#00e676';
    ctx.fillText(`TRAILER: ${p.stegoMagic}`, 120, 384);

    // Animated forensic laser scan line
    state.scanLineY = (state.scanLineY + 2.4) % canvas.height;
    ctx.strokeStyle = 'rgba(0, 229, 255, 0.55)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(0, state.scanLineY);
    ctx.lineTo(canvas.width, state.scanLineY);
    ctx.stroke();
  }

  document.getElementById('btnUnpack').addEventListener('click', () => runTool(1));
  document.getElementById('btnHmac').addEventListener('click', () => runTool(2));
  document.getElementById('btnNtp').addEventListener('click', () => runTool(3));
  document.getElementById('btnAegis').addEventListener('click', () => runTool(4));
  document.getElementById('btnApprove').addEventListener('click', () => submitVerdict(false));
  document.getElementById('btnReject').addEventListener('click', () => submitVerdict(true));

  window.addEventListener('keydown', (e) => {
    if (e.key === '1') runTool(1);
    if (e.key === '2') runTool(2);
    if (e.key === '3') runTool(3);
    if (e.key === '4') runTool(4);
    if (e.code === 'KeyV') submitVerdict(false);
    if (e.code === 'KeyF') submitVerdict(true);
  });

  function loop() {
    drawEvidenceCanvas();
    requestAnimationFrame(loop);
  }

  generateCase();
  requestAnimationFrame(loop);
})();
