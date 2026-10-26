(() => {
  // =========================================================================
  // 1. MASTER CATALOG OF 5 STANDALONE HTML5 GAMES
  // =========================================================================
  const GAMES_CATALOG = [
    {
      id: '01-steel-pipe-ai-counter',
      title: 'NextAI Steel Detect Challenge',
      badge: 'NCNN FP16 VISION',
      accent: '#00e5ff',
      path: '../../games/01-steel-pipe-ai-counter/index.html',
      storageKey: 'nextai_game_01_steel_best',
      desc: 'Count & tag stacked construction steel pipes with NCNN AI bounding-box assist while avoiding PVC decoys.'
    },
    {
      id: '02-embodied-hand-mcap-sim',
      title: 'NextAI Embodied Robotics Hand Lab',
      badge: '21-KEYPOINT .MCAP',
      accent: '#00e676',
      path: '../../games/02-embodied-hand-mcap-sim/index.html',
      storageKey: 'nextai_game_02_mcap_best',
      desc: 'Control a 21-landmark hand skeleton & 4-channel IMU stream to pinch-sort robotics parts with >85% FOV visibility.'
    },
    {
      id: '03-patrol-geofence-runner',
      title: 'NextAI Security Patrol & QR Geofence Runner',
      badge: 'QR GEOFENCE GUARD',
      accent: '#ffab00',
      path: '../../games/03-patrol-geofence-runner/index.html',
      storageKey: 'nextai_game_03_patrol_best',
      desc: 'Navigate a security officer across sectors, scan QR checkpoints inside Haversine GPS radii, and block Mock-GPS drones.'
    },
    {
      id: '04-truth-forensics-detective',
      title: 'NextAI Photo Truth Detective (VerifyAuth)',
      badge: 'VERIFYAUTH LAB',
      accent: '#b388ff',
      path: '../../games/04-truth-forensics-detective/index.html',
      storageKey: 'nextai_game_04_forensics_best',
      desc: 'Inspect field photos using JpegPacker stego-slice unpacker, AntiFakeCode Base62 HMAC, and NTP clock drift audit.'
    },
    {
      id: '05-field-collage-builder',
      title: 'NextAI Construction Before/After Puzzle',
      badge: 'PDF / XLSX / KMZ',
      accent: '#00b0ff',
      path: '../../games/05-field-collage-builder/index.html',
      storageKey: 'nextai_game_05_collage_best',
      desc: 'Match BEFORE and AFTER construction site cards by Stake & Azimuth to compile verified PDF/Excel/KMZ milestones.'
    }
  ];

  // =========================================================================
  // 2. TAB & SUBTAB NAVIGATION
  // =========================================================================
  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.nav-tab').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(btn.dataset.tab).classList.add('active');
    });
  });

  document.querySelectorAll('.sub-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.sub-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.sub-screen').forEach(s => s.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(btn.dataset.sub).classList.add('active');
    });
  });

  // =========================================================================
  // 3. TAB 1: GAME HUB, EMBEDDED PLAYER & LEADERBOARD
  // =========================================================================
  const gamesCatalogGrid = document.getElementById('gamesCatalogGrid');
  const gameFrame = document.getElementById('gameFrame');
  const activeGameTitle = document.getElementById('activeGameTitle');
  const btnOpenStandalone = document.getElementById('btnOpenStandalone');
  const leaderboardList = document.getElementById('leaderboardList');
  const totalCombinedScore = document.getElementById('totalCombinedScore');
  const arcadeTelemetryLog = document.getElementById('arcadeTelemetryLog');

  function getBestScoreForGame(g) {
    const direct = parseInt(localStorage.getItem(g.storageKey) || '0', 10);
    try {
      const map = JSON.parse(localStorage.getItem('nextai_game_scores') || '{}');
      const mapScore = map[g.id] ? Number(map[g.id].score || 0) : 0;
      return Math.max(direct, mapScore);
    } catch (_) {
      return direct;
    }
  }

  function launchGameInStage(gameObj) {
    gameFrame.src = gameObj.path;
    activeGameTitle.textContent = `Playing: ${gameObj.title}`;
    btnOpenStandalone.href = gameObj.path;
    appendTelemetry(`Launched [${gameObj.id}] (${gameObj.title}) into Arcade Stage.`);
  }

  function appendTelemetry(msg) {
    const ts = new Date().toLocaleTimeString('en-GB', { hour12: false });
    arcadeTelemetryLog.textContent = `[${ts}] ${msg}\n` + arcadeTelemetryLog.textContent;
  }

  function renderGamesAndLeaderboard() {
    gamesCatalogGrid.innerHTML = '';
    leaderboardList.innerHTML = '';
    let combined = 0;

    GAMES_CATALOG.forEach((g, idx) => {
      const best = getBestScoreForGame(g);
      combined += best;

      const card = document.createElement('div');
      card.className = 'game-card';
      card.innerHTML = `
        <div>
          <span class="game-tag" style="background:${g.accent}">${g.badge}</span>
          <h3>${idx + 1}. ${g.title}</h3>
          <p>${g.desc}</p>
        </div>
        <div>
          <div class="game-meta">
            <span>HIGH SCORE: ${best}</span>
            <span>HTML5 CANVAS</span>
          </div>
          <div class="btn-group">
            <button class="primary" data-play="${g.id}">▶ Play Here</button>
            <a class="btn-link" href="${g.path}" target="_blank" rel="noopener">New Tab ↗</a>
          </div>
        </div>
      `;
      card.querySelector('[data-play]').addEventListener('click', () => launchGameInStage(g));
      gamesCatalogGrid.appendChild(card);

      const lbRow = document.createElement('div');
      lbRow.className = 'lb-item';
      lbRow.innerHTML = `
        <span>#${idx + 1} ${g.title}</span>
        <strong>${best.toLocaleString()} pts</strong>
      `;
      leaderboardList.appendChild(lbRow);
    });

    totalCombinedScore.textContent = combined.toLocaleString();
  }

  window.addEventListener('message', (evt) => {
    if (evt.data && evt.data.type === 'NEXTAI_GAME_SCORE') {
      const { gameId, score, bestScore } = evt.data;
      appendTelemetry(`Telemetry from [${gameId}] -> Score: ${score} | Best: ${bestScore}`);
      renderGamesAndLeaderboard();
    }
  });

  document.getElementById('btnReloadGame').addEventListener('click', () => {
    gameFrame.src = gameFrame.src;
  });

  document.getElementById('btnSeedScores').addEventListener('click', () => {
    const sample = [1840, 2450, 1920, 2780, 2160];
    const map = {};
    GAMES_CATALOG.forEach((g, i) => {
      localStorage.setItem(g.storageKey, String(sample[i]));
      map[g.id] = { score: sample[i], lastPlayed: new Date().toISOString() };
    });
    localStorage.setItem('nextai_game_scores', JSON.stringify(map));
    renderGamesAndLeaderboard();
    appendTelemetry('Seeded sample high scores across all 5 NextAI HTML5 games.');
  });

  document.getElementById('btnResetScores').addEventListener('click', () => {
    GAMES_CATALOG.forEach(g => localStorage.removeItem(g.storageKey));
    localStorage.removeItem('nextai_game_scores');
    renderGamesAndLeaderboard();
    appendTelemetry('Cleared local leaderboard scores.');
  });

  renderGamesAndLeaderboard();

  // =========================================================================
  // 4. TAB 2 — SCREEN 1: LIVE CAMERA VIEWFINDER & WATERMARK CAPTURE
  // =========================================================================
  const camCanvas = document.getElementById('liveCameraCanvas');
  const camCtx = camCanvas.getContext('2d');
  const camTemplateSelect = document.getElementById('camTemplateSelect');
  const camProjectInput = document.getElementById('camProjectInput');
  const camGpsInput = document.getElementById('camGpsInput');
  const camWeatherInput = document.getElementById('camWeatherInput');
  const camNoteInput = document.getElementById('camNoteInput');
  const capturedGallery = document.getElementById('capturedGallery');
  const camLastCaptureMeta = document.getElementById('camLastCaptureMeta');

  let webcamVideo = null;
  let webcamActive = false;

  document.getElementById('btnToggleWebcam').addEventListener('click', async () => {
    if (webcamActive && webcamVideo) {
      const tracks = webcamVideo.srcObject ? webcamVideo.srcObject.getTracks() : [];
      tracks.forEach(t => t.stop());
      webcamActive = false;
      webcamVideo = null;
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { width: 840, height: 500 } });
      webcamVideo = document.createElement('video');
      webcamVideo.srcObject = stream;
      webcamVideo.play();
      webcamActive = true;
    } catch (err) {
      camLastCaptureMeta.textContent = `Webcam unavailable (${err.message}) — running animated NextAI 3D Construction Viewfinder.`;
    }
  });

  function generateBase62Code(seedStr) {
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    let h = 2166136261;
    for (let i = 0; i < seedStr.length; i++) {
      h ^= seedStr.charCodeAt(i);
      h = Math.imul(h, 16777619);
    }
    let out = 'NX';
    for (let i = 0; i < 10; i++) {
      const idx = Math.abs((h >> (i * 2)) + i * 31) % chars.length;
      out += chars[idx];
    }
    return out;
  }

  let currentAntiFake = generateBase62Code(String(Date.now()));

  function renderCameraFrame() {
    camCtx.clearRect(0, 0, camCanvas.width, camCanvas.height);

    if (webcamActive && webcamVideo && webcamVideo.readyState >= 2) {
      camCtx.drawImage(webcamVideo, 0, 0, camCanvas.width, camCanvas.height);
    } else {
      // Animated engineering site scene
      const grad = camCtx.createLinearGradient(0, 0, 0, camCanvas.height);
      grad.addColorStop(0, '#0a192f');
      grad.addColorStop(0.6, '#142d4c');
      grad.addColorStop(1, '#1c2536');
      camCtx.fillStyle = grad;
      camCtx.fillRect(0, 0, camCanvas.width, camCanvas.height);

      // Perspective grid & structural viaduct piers
      camCtx.strokeStyle = 'rgba(0, 229, 255, 0.12)';
      for (let x = 0; x < camCanvas.width; x += 60) {
        camCtx.beginPath();
        camCtx.moveTo(x, 0);
        camCtx.lineTo(x, camCanvas.height);
        camCtx.stroke();
      }

      const t = performance.now() * 0.001;
      camCtx.fillStyle = '#243b55';
      camCtx.fillRect(140, 140, 90, 280);
      camCtx.fillRect(380, 120, 95, 300);
      camCtx.fillRect(620, 150, 85, 270);

      // Crane boom
      camCtx.strokeStyle = '#ffab00';
      camCtx.lineWidth = 4;
      camCtx.beginPath();
      camCtx.moveTo(100, 95);
      camCtx.lineTo(740, 95);
      camCtx.stroke();

      const hookX = 420 + Math.sin(t) * 140;
      camCtx.beginPath();
      camCtx.moveTo(hookX, 95);
      camCtx.lineTo(hookX, 210);
      camCtx.stroke();
    }

    // Live Watermark Overlay Card (Bottom-Left)
    const now = new Date();
    const timeStr = now.toLocaleTimeString('en-GB', { hour12: false });
    const dateStr = now.toISOString().slice(0, 10);
    const tpl = camTemplateSelect.value;

    camCtx.fillStyle = 'rgba(6, 12, 24, 0.86)';
    camCtx.fillRect(22, camCanvas.height - 178, 490, 156);
    camCtx.strokeStyle = '#00e5ff';
    camCtx.lineWidth = 2;
    camCtx.strokeRect(22, camCanvas.height - 178, 490, 156);

    // Header bar inside watermark
    camCtx.fillStyle = '#00e5ff';
    camCtx.fillRect(22, camCanvas.height - 178, 490, 28);
    camCtx.fillStyle = '#03101f';
    camCtx.font = 'bold 13px Segoe UI, sans-serif';
    camCtx.fillText(`NEXTAI VERIFIED WATERMARK • [${tpl}]`, 34, camCanvas.height - 159);

    // Big Clock
    camCtx.fillStyle = '#ffffff';
    camCtx.font = 'bold 28px Consolas, monospace';
    camCtx.fillText(timeStr, 34, camCanvas.height - 118);

    camCtx.fillStyle = '#00e676';
    camCtx.font = 'bold 12px Consolas, monospace';
    camCtx.fillText(`${dateStr} UTC • NTP SYNCED`, 185, camCanvas.height - 124);

    // Fields
    camCtx.fillStyle = '#eef5ff';
    camCtx.font = '13px Segoe UI, sans-serif';
    camCtx.fillText(`PROJECT : ${camProjectInput.value}`, 34, camCanvas.height - 92);
    camCtx.fillText(`GPS LOC : ${camGpsInput.value}`, 34, camCanvas.height - 72);
    camCtx.fillText(`TELEM   : ${camWeatherInput.value}`, 34, camCanvas.height - 52);
    camCtx.fillStyle = '#ffab00';
    camCtx.font = 'bold 12px Consolas, monospace';
    camCtx.fillText(`NOTE: ${camNoteInput.value} | CODE: ${currentAntiFake}`, 34, camCanvas.height - 32);

    // Top-right VerifyAuth QR & AntiFake Badge
    camCtx.fillStyle = 'rgba(6, 12, 24, 0.85)';
    camCtx.fillRect(camCanvas.width - 195, 20, 175, 70);
    camCtx.strokeStyle = '#00e676';
    camCtx.strokeRect(camCanvas.width - 195, 20, 175, 70);
    camCtx.fillStyle = '#00e676';
    camCtx.font = 'bold 11px Consolas, monospace';
    camCtx.fillText('NEXTAI VERIFYAUTH', camCanvas.width - 182, 42);
    camCtx.fillStyle = '#ffffff';
    camCtx.fillText(currentAntiFake, camCanvas.width - 182, 62);
    camCtx.fillStyle = '#8ea4c4';
    camCtx.fillText('NEXTAI_JPEG_PACK_2026', camCanvas.width - 182, 79);

    requestAnimationFrame(renderCameraFrame);
  }
  requestAnimationFrame(renderCameraFrame);

  function playShutterSound() {
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(920, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(240, ctx.currentTime + 0.09);
      gain.gain.setValueAtTime(0.15, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.09);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.09);
    } catch (_) {}
  }

  document.getElementById('btnShutterCapture').addEventListener('click', () => {
    playShutterSound();
    currentAntiFake = generateBase62Code(camProjectInput.value + Date.now());
    const dataUrl = camCanvas.toDataURL('image/png');

    const link = document.createElement('a');
    link.href = dataUrl;
    link.download = `NextAI_${currentAntiFake}.png`;

    const img = document.createElement('img');
    img.src = dataUrl;
    img.className = 'photo-thumb';
    img.title = `Click to download NextAI_${currentAntiFake}.png`;
    link.appendChild(img);
    capturedGallery.prepend(link);

    camLastCaptureMeta.textContent = JSON.stringify(
      {
        file: `NextAI_${currentAntiFake}.jpg`,
        templateId: camTemplateSelect.value,
        antiFakeCode: currentAntiFake,
        ntpSyncedUtc: new Date().toISOString(),
        jpegPackerMagic: 'NEXTAI_JPEG_PACK_END_2026',
        aegisSignature: 'ECDSA_P256_VERIFIED'
      },
      null,
      2
    );
  });

  // =========================================================================
  // 5. TAB 2 — SCREEN 2: WATERMARK STORE & 24 FIELDS
  // =========================================================================
  const WM_MODULES = [
    { id: 'WM-10', name: 'Construction Engineering QA', fields: ['time', 'latlng', 'stake', 'azimuth', 'inspector', 'antifake'] },
    { id: 'WM-20', name: 'Security Patrol QR Guard', fields: ['time', 'latlng', 'qr_checkpoint', 'geofence_radius', 'officer_id'] },
    { id: 'WM-34', name: 'Enterprise Attendance Card', fields: ['ntp_clock', 'company_logo', 'employee_id', 'shift_status'] },
    { id: 'WM-60', name: 'SteelDetect NCNN Counter', fields: ['pipe_count', 'bbox_overlay', 'precision_f1', 'bundle_id'] },
    { id: 'WM-90', name: 'Before / After Collage', fields: ['phase_tag', 'azimuth_lock', 'weather', 'milestone_id'] },
    { id: 'WM-210', name: 'Embodied .MCAP Telemetry', fields: ['hand_21_kp', 'imu_4ch', 'fov_ratio', 'mcap_chunk_id'] }
  ];

  const wmStoreCatalog = document.getElementById('wmStoreCatalog');
  const wmFieldSchemaOut = document.getElementById('wmFieldSchemaOut');

  WM_MODULES.forEach(m => {
    const div = document.createElement('div');
    div.className = 'game-card';
    div.innerHTML = `
      <div>
        <span class="game-tag" style="background:#00e5ff">${m.id}</span>
        <h3>${m.name}</h3>
        <p>Active Fields: ${m.fields.join(', ')}</p>
      </div>
      <button class="primary">Apply to Viewfinder</button>
    `;
    div.querySelector('button').addEventListener('click', () => {
      camTemplateSelect.value = m.id;
      document.querySelector('[data-sub="sim-1"]').click();
    });
    wmStoreCatalog.appendChild(div);
  });

  wmFieldSchemaOut.textContent = JSON.stringify(
    {
      totalWatermarkModules: 11,
      dynamicFieldTypes: 24,
      supportedFields: [
        '1. ntp_atomic_time', '2. wgs84_lat_lng', '3. altitude_msl', '4. compass_azimuth',
        '5. speed_kmh', '6. weather_temp_humidity', '7. project_name', '8. construction_stake',
        '9. inspector_signature', '10. antifake_base62_hmac', '11. verify_qr_matrix',
        '12. mini_satellite_map', '13. custom_brand_logo', '14. ncnn_steel_count',
        '15. geofence_compliance', '16. before_after_tag', '17. embodied_mcap_chunk',
        '18. imu_stability_index', '19. work_order_uid', '20. concrete_slump_spec',
        '21. patrol_route_id', '22. cold_chain_celsius', '23. decibel_noise_db', '24. aegis_ecdsa_nonce'
      ]
    },
    null,
    2
  );

  // =========================================================================
  // 6. TAB 2 — SCREEN 3: TEAMSPACE GEOFENCE MAP
  // =========================================================================
  const geoCanvas = document.getElementById('teamGeofenceCanvas');
  const geoCtx = geoCanvas.getContext('2d');
  const geoRadiusInput = document.getElementById('geoRadiusInput');
  const geoStatusConsole = document.getElementById('geoStatusConsole');

  const geoCenter = { x: 400, y: 230, lat: 10.7769, lng: 106.7009 };
  const geoOfficer = { x: 445, y: 205 };

  function drawGeoMap() {
    geoCtx.clearRect(0, 0, geoCanvas.width, geoCanvas.height);
    geoCtx.fillStyle = '#070e1b';
    geoCtx.fillRect(0, 0, geoCanvas.width, geoCanvas.height);

    const radiusMeters = Number(geoRadiusInput.value || 150);
    const distMeters = Math.round(Math.hypot(geoOfficer.x - geoCenter.x, geoOfficer.y - geoCenter.y) * 1.2);
    const inside = distMeters <= radiusMeters;

    // Geofence zone
    geoCtx.beginPath();
    geoCtx.arc(geoCenter.x, geoCenter.y, radiusMeters / 1.2, 0, Math.PI * 2);
    geoCtx.fillStyle = inside ? 'rgba(0, 230, 118, 0.14)' : 'rgba(255, 82, 82, 0.14)';
    geoCtx.fill();
    geoCtx.strokeStyle = inside ? '#00e676' : '#ff5252';
    geoCtx.lineWidth = 2;
    geoCtx.stroke();

    // Center HQ
    geoCtx.fillStyle = '#00e5ff';
    geoCtx.fillRect(geoCenter.x - 8, geoCenter.y - 8, 16, 16);

    // Officer pin
    geoCtx.beginPath();
    geoCtx.arc(geoOfficer.x, geoOfficer.y, 10, 0, Math.PI * 2);
    geoCtx.fillStyle = inside ? '#00e676' : '#ff5252';
    geoCtx.fill();

    geoStatusConsole.textContent = JSON.stringify(
      {
        siteCenter: `${geoCenter.lat}°N, ${geoCenter.lng}°E`,
        allowedRadiusMeters: radiusMeters,
        haversineDistanceMeters: distMeters,
        attendanceVerdict: inside ? 'COMPLIANT (INSIDE GEOFENCE)' : 'VIOLATION (OUTSIDE GEOFENCE)',
        mockLocationDetected: false
      },
      null,
      2
    );
  }

  geoCanvas.addEventListener('mousedown', (e) => {
    const r = geoCanvas.getBoundingClientRect();
    geoOfficer.x = (e.clientX - r.left) * (geoCanvas.width / r.width);
    geoOfficer.y = (e.clientY - r.top) * (geoCanvas.height / r.height);
    drawGeoMap();
  });
  geoRadiusInput.addEventListener('input', drawGeoMap);
  drawGeoMap();

  // =========================================================================
  // 7. TAB 2 — SCREEN 4: BATCH EXPORT & JPEGPACKER
  // =========================================================================
  const exportConsoleOut = document.getElementById('exportConsoleOut');

  function downloadBlob(filename, content, mime) {
    const blob = new Blob([content], { type: mime });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.click();
  }

  document.getElementById('btnGenHtmlPdf').addEventListener('click', () => {
    const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>NextAI Inspection Report</title></head><body style="font-family:sans-serif;padding:30px;"><h1>NextAI Verified Field Inspection Report</h1><p><strong>Project:</strong> ${camProjectInput.value}</p><p><strong>GPS:</strong> ${camGpsInput.value}</p><p><strong>AntiFakeCode:</strong> ${currentAntiFake}</p><p><strong>Trailer Signature:</strong> NEXTAI_JPEG_PACK_END_2026</p></body></html>`;
    downloadBlob(`NextAI_Report_${currentAntiFake}.html`, html, 'text/html');
    exportConsoleOut.textContent = `Generated & downloaded NextAI_Report_${currentAntiFake}.html (Printable PDF Report).`;
  });

  document.getElementById('btnGenCsv').addEventListener('click', () => {
    const csv = `PhotoID,Project,GPS,AntiFakeCode,JpegPackerTrailer,Status\n1,"${camProjectInput.value}","${camGpsInput.value}",${currentAntiFake},NEXTAI_JPEG_PACK_END_2026,VERIFIED\n`;
    downloadBlob(`NextAI_Export_${currentAntiFake}.csv`, csv, 'text/csv');
    exportConsoleOut.textContent = `Generated & downloaded Excel-compatible CSV log NextAI_Export_${currentAntiFake}.csv.`;
  });

  document.getElementById('btnGenKml').addEventListener('click', () => {
    const kml = `<?xml version="1.0" encoding="UTF-8"?>\n<kml xmlns="http://www.opengis.net/kml/2.2"><Document><name>NextAI Field Placemarks</name><Placemark><name>${camProjectInput.value}</name><description>AntiFakeCode: ${currentAntiFake}</description><Point><coordinates>106.7009,10.7769,14.2</coordinates></Point></Placemark></Document></kml>`;
    downloadBlob(`NextAI_Geotags_${currentAntiFake}.kml`, kml, 'application/vnd.google-earth.kml+xml');
    exportConsoleOut.textContent = `Generated & downloaded Google Earth KML/KMZ placemark file NextAI_Geotags_${currentAntiFake}.kml.`;
  });

  document.getElementById('btnRunJpegPacker').addEventListener('click', () => {
    const payload = {
      magic: 'NEXTAI_JPEG_PACK_END_2026',
      eoiMarkerFound: '0xFFD9',
      antiFakeCode: currentAntiFake,
      sha256OriginalSlice: '8f94d2c1b07e4a1190c42a8e7f1138bc',
      sha256CanvasRegion: '8f94d2c1b07e4a1190c42a8e7f1138bc',
      integrityVerdict: 'AUTHENTIC_UNMODIFIED_JPEG'
    };
    exportConsoleOut.textContent = `[JpegPacker Binary Pack/Unpack Simulation]\n` + JSON.stringify(payload, null, 2);
  });

  // =========================================================================
  // 8. TAB 2 — SCREEN 5, 6, 7: VERIFYAUTH, PAYWALL/ADMOB & EARN .MCAP
  // =========================================================================
  const verifyAuthConsole = document.getElementById('verifyAuthConsole');
  function runVerifyAudit() {
    const code = document.getElementById('verifyCodeInput').value.trim();
    const ntp = Number(document.getElementById('verifyNtpInput').value);
    const sys = Number(document.getElementById('verifySysInput').value);
    const driftSec = Math.abs(sys - ntp) / 1000;
    const valid = code.length === 12 && driftSec <= 30;

    verifyAuthConsole.textContent = JSON.stringify(
      {
        antiFakeCode: code,
        base62FormatValid: code.length === 12,
        ntpClockDriftSeconds: driftSec,
        maxAllowedDriftSeconds: 30,
        aegisEcdsaHeader: 'X-NextAI-Sign: VERIFIED',
        finalVerdict: valid ? 'AUTHENTIC FIELD PHOTO' : 'TAMPERED / CLOCK SPOOF DETECTED'
      },
      null,
      2
    );
  }
  document.getElementById('btnRunVerifyAuth').addEventListener('click', runVerifyAudit);
  document.getElementById('btnSimulateTamper').addEventListener('click', () => {
    document.getElementById('verifySysInput').value = '1792800000000';
    runVerifyAudit();
  });
  runVerifyAudit();

  const paywallAdmobConsole = document.getElementById('paywallAdmobConsole');
  document.getElementById('btnSimSubPlus').addEventListener('click', () => {
    paywallAdmobConsole.textContent = JSON.stringify(
      {
        plan: 'NextAI Plus (Individual VIP)',
        status: 'ACTIVE',
        entitlements: ['remove_watermark_logo', 'custom_logo', 'unlimited_steel_ai', 'no_ads'],
        admobSdkState: 'SUPPRESSED_FOR_VIP'
      },
      null,
      2
    );
  });
  document.getElementById('btnSimSubBiz').addEventListener('click', () => {
    paywallAdmobConsole.textContent = JSON.stringify(
      {
        plan: 'NextAI Business (Enterprise Teamspace)',
        status: 'ACTIVE_25_SEATS',
        entitlements: ['mandatory_template_lock', 'haversine_geofence_audit', 'batch_pdf_xlsx_kmz_api'],
        admobTestAppId: 'ca-app-pub-3940256099942544~3347511713'
      },
      null,
      2
    );
  });

  document.getElementById('btnJumpMcapGame').addEventListener('click', () => {
    document.querySelector('[data-tab="tab-games"]').click();
    launchGameInStage(GAMES_CATALOG[1]);
  });

  // =========================================================================
  // 9. TAB 3: 42-TABLE RELATIONAL + OLAP SCHEMA & LIVE BACKEND CONNECTOR
  // =========================================================================
  const ALL_42_TABLES = [
    // Identity & Access (5)
    { name: 'users', engine: 'PostgreSQL', domain: '1. Identity & Auth' },
    { name: 'user_devices', engine: 'PostgreSQL', domain: '1. Identity & Auth' },
    { name: 'user_sessions', engine: 'PostgreSQL', domain: '1. Identity & Auth' },
    { name: 'oauth_accounts', engine: 'PostgreSQL', domain: '1. Identity & Auth' },
    { name: 'aegis_device_nonces', engine: 'PostgreSQL', domain: '1. Identity & Auth' },
    // Watermark & Templates (5)
    { name: 'watermark_categories', engine: 'PostgreSQL', domain: '2. Watermark Engine' },
    { name: 'watermark_templates', engine: 'PostgreSQL', domain: '2. Watermark Engine' },
    { name: 'watermark_template_fields', engine: 'PostgreSQL', domain: '2. Watermark Engine' },
    { name: 'user_custom_watermarks', engine: 'PostgreSQL', domain: '2. Watermark Engine' },
    { name: 'template_share_codes', engine: 'PostgreSQL', domain: '2. Watermark Engine' },
    // Media & Forensics (5)
    { name: 'media_assets', engine: 'PostgreSQL', domain: '3. Media & VerifyAuth' },
    { name: 'media_forensic_proofs', engine: 'PostgreSQL', domain: '3. Media & VerifyAuth' },
    { name: 'jpeg_packer_manifests', engine: 'PostgreSQL', domain: '3. Media & VerifyAuth' },
    { name: 'verify_auth_audits', engine: 'PostgreSQL', domain: '3. Media & VerifyAuth' },
    { name: 'steel_ai_detections', engine: 'PostgreSQL', domain: '3. Media & VerifyAuth' },
    // Teamspace & Patrol (6)
    { name: 'workspaces', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    { name: 'workspace_members', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    { name: 'workspace_projects', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    { name: 'geofence_zones', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    { name: 'patrol_checkpoints', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    { name: 'attendance_checkins', engine: 'PostgreSQL', domain: '4. Teamspace & Patrol' },
    // Batch Export (4)
    { name: 'export_jobs', engine: 'PostgreSQL', domain: '5. Batch Deliverables' },
    { name: 'export_job_items', engine: 'PostgreSQL', domain: '5. Batch Deliverables' },
    { name: 'before_after_collages', engine: 'PostgreSQL', domain: '5. Batch Deliverables' },
    { name: 'kmz_placemark_layers', engine: 'PostgreSQL', domain: '5. Batch Deliverables' },
    // Billing & AdMob (4)
    { name: 'subscription_plans', engine: 'PostgreSQL', domain: '6. Billing & AdMob' },
    { name: 'user_subscriptions', engine: 'PostgreSQL', domain: '6. Billing & AdMob' },
    { name: 'payment_transactions', engine: 'PostgreSQL', domain: '6. Billing & AdMob' },
    { name: 'admob_test_placements', engine: 'PostgreSQL', domain: '6. Billing & AdMob' },
    // Embodied AI .MCAP (5)
    { name: 'embodied_campaigns', engine: 'PostgreSQL', domain: '7. Embodied .MCAP' },
    { name: 'embodied_episodes', engine: 'PostgreSQL', domain: '7. Embodied .MCAP' },
    { name: 'embodied_mcap_chunks', engine: 'PostgreSQL', domain: '7. Embodied .MCAP' },
    { name: 'embodied_qa_reviews', engine: 'PostgreSQL', domain: '7. Embodied .MCAP' },
    { name: 'embodied_payout_ledger', engine: 'PostgreSQL', domain: '7. Embodied .MCAP' },
    // OLAP ClickHouse (8)
    { name: 'olap_sensor_events_raw', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_camera_capture_metrics', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_verify_auth_logs', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_steel_inference_perf', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_mcap_imu_timeseries', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_geofence_audit_stream', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_admob_test_impressions', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' },
    { name: 'olap_daily_kpi_rollup', engine: 'ClickHouse OLAP', domain: '8. OLAP Telemetry' }
  ];

  const schemaTablesGrid = document.getElementById('schemaTablesGrid');
  const schemaFilterInput = document.getElementById('schemaFilterInput');

  function renderSchemaTables(filterText = '') {
    schemaTablesGrid.innerHTML = '';
    const q = filterText.toLowerCase();
    ALL_42_TABLES.filter(
      t => t.name.toLowerCase().includes(q) || t.domain.toLowerCase().includes(q) || t.engine.toLowerCase().includes(q)
    ).forEach((t, idx) => {
      const div = document.createElement('div');
      div.className = 'schema-item';
      div.style.borderLeftColor = t.engine.includes('OLAP') ? '#00e676' : '#00e5ff';
      div.innerHTML = `
        <strong>#${idx + 1} ${t.name}</strong>
        <span>${t.domain} • ${t.engine}</span>
      `;
      schemaTablesGrid.appendChild(div);
    });
  }
  schemaFilterInput.addEventListener('input', (e) => renderSchemaTables(e.target.value));
  renderSchemaTables();

  // Live API Tester with Offline Fallback
  const apiResponseConsole = document.getElementById('apiResponseConsole');
  const backendUrlInput = document.getElementById('backendUrlInput');
  const apiStatusPill = document.getElementById('apiStatusPill');

  const FALLBACK_RESPONSES = {
    '/health': {
      status: 'ok',
      brand: 'NextAI Global API',
      domain: 'nextai.global',
      relationalTables: 34,
      olapTables: 8,
      totalTables: 42
    },
    '/config/bootstrap': {
      brand: 'NextAI',
      domains: ['nextai.global', 'nextai.technology', 'nextai.solution'],
      admobTestConfig: {
        appId: 'ca-app-pub-3940256099942544~3347511713',
        banner: 'ca-app-pub-3940256099942544/6300978111',
        interstitial: 'ca-app-pub-3940256099942544/1033173712',
        rewarded: 'ca-app-pub-3940256099942544/5224354917'
      }
    },
    '/templates/catalog': { modules: WM_MODULES },
    '/teamspace/projects': {
      projects: [
        { id: 'PRJ-01', name: 'Metro Line 4 Viaduct Pier #18', geofenceRadiusMeters: 150, activeMembers: 28 },
        { id: 'PRJ-02', name: 'Solar Substation HV Transformer B', geofenceRadiusMeters: 200, activeMembers: 14 }
      ]
    },
    '/embodied/tasks': {
      campaigns: [
        { id: 'MCAP-101', title: '21-Keypoint Dexterous Gear Sorting', minVisibilityRatio: 0.85, rewardBrl: 18.5 },
        { id: 'MCAP-102', title: 'Bimanual Li-Ion Cell Insertion', minVisibilityRatio: 0.85, rewardMxn: 65.0 }
      ]
    },
    '/olap/dashboard': {
      totalPhotosVerified: 148920,
      steelPipesCountedAi: 982410,
      mcapEpisodesAccepted: 12480,
      admobTestImpressions: 42100
    }
  };

  async function callEndpoint(ep) {
    const base = backendUrlInput.value.replace(/\/+$/, '');
    try {
      const res = await fetch(`${base}${ep}`, { method: 'GET' });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      apiStatusPill.textContent = 'API: ONLINE (:4580)';
      apiResponseConsole.textContent = `[LIVE RESPONSE FROM ${base}${ep}]\n` + JSON.stringify(data, null, 2);
    } catch (_) {
      apiStatusPill.textContent = 'API: LOCAL FALLBACK READY';
      const fallback = FALLBACK_RESPONSES[ep] || { endpoint: ep, status: 'local_mock_ok' };
      apiResponseConsole.textContent =
        `[LOCAL FALLBACK PAYLOAD FOR ${ep}]\n` + JSON.stringify(fallback, null, 2);
    }
  }

  document.querySelectorAll('.api-test-btn').forEach(btn => {
    btn.addEventListener('click', () => callEndpoint(btn.dataset.ep));
  });
  document.getElementById('btnPingBackend').addEventListener('click', () => callEndpoint('/health'));
})();
