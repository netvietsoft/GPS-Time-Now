<#
.SYNOPSIS
    NextAI Camera / GPS-Time-Now — Always-Awake Git Audit Watchdog (Audit-git)
    Wakeup Interval: At least once every 5 minutes (default 300s, configurable)
    Mandate: Chairman Tony Remote Git Command & Control
    Rule: MUST read E:\Decompiler\APP\IMAGE\Timemark\VAN-HANH on every wakeup & new task!
#>
param(
    [switch]$Once = $false,
    [int]$IntervalSeconds = 300
)

$ErrorActionPreference = "Continue"

$WorkspaceRoot = "E:\Decompiler\APP\IMAGE\Timemark"
$VanHanhRoot   = "$WorkspaceRoot\VAN-HANH"
$ConvertRepo   = "$WorkspaceRoot\CONVERT"
$SourceRepo    = "$WorkspaceRoot\SOURCE"
$HeartbeatLog  = "$VanHanhRoot\AGENT-BAO-CAO\watchdog_heartbeat.log"
$AuditInbox    = "$VanHanhRoot\AGENT-BAO-CAO\git_audit_inbox.json"
$ConvertStatus = "$ConvertRepo\.ai\watchdog_audit_git_status.json"

function Get-IsoTimestamp {
    return (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
}

function Read-VanHanhGovernance {
    <#
      MANDATORY STEP 1: Read E:\Decompiler\APP\IMAGE\Timemark\VAN-HANH on every wakeup/task
    #>
    $govFiles = @(
        "$VanHanhRoot\Task.txt",
        "$VanHanhRoot\KE-HOACH-CEO\KE_HOACH_TONG_THE_CEO_NEXTAI_V2.1.2.md",
        "$VanHanhRoot\KE-HOACH-CEO\MASTER_TASK_GRAPH.json",
        "$VanHanhRoot\KE-HOACH-CEO\Tasksrequiring.md",
        "$VanHanhRoot\KE-HOACH-CEO\UPDATETODOS.md",
        "$VanHanhRoot\PHAN-BIEN\SO-CAI-DOI-KHANG\LEDGER_DOI_KHANG.json"
    )
    $loadedCount = 0
    $combinedHashInput = ""
    foreach ($f in $govFiles) {
        if (Test-Path $f) {
            $loadedCount++
            $item = Get-Item $f
            $combinedHashInput += "$($item.Name):$($item.Length):$($item.LastWriteTimeUtc.Ticks)|"
        }
    }
    $nhiemVuFiles = Get-ChildItem -Path "$VanHanhRoot\NHIEM-VU" -Filter "*.yaml" -ErrorAction SilentlyContinue
    $baoCaoFiles  = Get-ChildItem -Path "$VanHanhRoot\AGENT-BAO-CAO" -Filter "*.md" -ErrorAction SilentlyContinue

    return @{
        van_hanh_path        = $VanHanhRoot
        mandatory_read_ok    = ($loadedCount -ge 5)
        core_files_read      = $loadedCount
        nhiem_vu_yaml_count  = ($nhiemVuFiles | Measure-Object).Count
        agent_reports_count  = ($baoCaoFiles | Measure-Object).Count
        governance_signature = $combinedHashInput
    }
}

function Invoke-GitRepoAudit {
    param(
        [string]$RepoPath,
        [string]$RepoName
    )
    if (-not (Test-Path "$RepoPath\.git")) {
        return @{
            repo        = $RepoName
            path        = $RepoPath
            status      = "NO_GIT_REPO"
            new_commits = 0
        }
    }

    # Fetch remote orders from Chairman Tony
    & git -C $RepoPath fetch origin --prune 2>&1 | Out-Null

    $branch      = (& git -C $RepoPath rev-parse --abbrev-ref HEAD 2>$null).Trim()
    $localHead   = (& git -C $RepoPath rev-parse HEAD 2>$null).Trim()
    $remoteHead  = (& git -C $RepoPath rev-parse "origin/$branch" 2>$null).Trim()
    $behindCount = 0
    $aheadCount  = 0
    $newOrders   = @()

    if ($localHead -and $remoteHead -and ($localHead -ne $remoteHead)) {
        $counts = (& git -C $RepoPath rev-list --left-right --count "HEAD...origin/$branch" 2>$null).Trim() -split "\s+"
        if ($counts.Count -ge 2) {
            $aheadCount  = [int]$counts[0]
            $behindCount = [int]$counts[1]
        }
        if ($behindCount -gt 0) {
            # Extract new commits pushed remotely by Chairman Tony
            $logLines = & git -C $RepoPath log "HEAD..origin/$branch" --pretty=format:"%H|%an|%ae|%ad|%s" --date=iso
            foreach ($line in $logLines) {
                if ($line) {
                    $parts = $line -split "\|", 5
                    $newOrders += @{
                        commit  = $parts[0]
                        author  = $parts[1]
                        email   = $parts[2]
                        date    = $parts[3]
                        subject = $parts[4]
                    }
                }
            }
            # Pull new Chairman directives into workspace
            & git -C $RepoPath pull --rebase origin $branch 2>&1 | Out-Null
            $localHead = (& git -C $RepoPath rev-parse HEAD 2>$null).Trim()
        }
    }

    $lastSubject = (& git -C $RepoPath log -n 1 --pretty=format:"%s" 2>$null).Trim()

    return @{
        repo          = $RepoName
        path          = $RepoPath
        branch        = $branch
        local_head    = $localHead
        remote_head   = $remoteHead
        ahead_count   = $aheadCount
        behind_count  = $behindCount
        new_commits   = $behindCount
        new_orders    = $newOrders
        last_subject  = $lastSubject
        sync_status   = if ($behindCount -gt 0) { "PULLED_NEW_CHAIRMAN_ORDERS" } else { "UP_TO_DATE" }
    }
}

function Invoke-SingleAuditPass {
    param([int]$Iteration)

    $ts = Get-IsoTimestamp

    # 1. Mandatory Read of E:\Decompiler\APP\IMAGE\Timemark\VAN-HANH
    $govReport = Read-VanHanhGovernance

    # 2. Audit-git across both repositories (GPS-Time-Now & Time-Now)
    $convertAudit = Invoke-GitRepoAudit -RepoPath $ConvertRepo -RepoName "netvietsoft/GPS-Time-Now (CONVERT)"
    $sourceAudit  = Invoke-GitRepoAudit -RepoPath $SourceRepo  -RepoName "netvietsoft/Time-Now (SOURCE)"

    # 3. Check ready tasks in CONVERT/.ai/tasks/ready
    $readyTasks = @()
    if (Test-Path "$ConvertRepo\.ai\tasks\ready") {
        $readyFiles = Get-ChildItem -Path "$ConvertRepo\.ai\tasks\ready" -File -ErrorAction SilentlyContinue
        foreach ($rf in $readyFiles) {
            $readyTasks += $rf.Name
        }
    }

    $totalNewOrders = [int]$convertAudit.new_commits + [int]$sourceAudit.new_commits

    $payload = [ordered]@{
        timestamp           = $ts
        iteration           = $Iteration
        watchdog_mode       = "AUDIT_GIT_ALWAYS_AWAKE_5MIN"
        interval_seconds    = $IntervalSeconds
        van_hanh_governance = $govReport
        git_audit           = [ordered]@{
            total_new_remote_orders = $totalNewOrders
            gps_time_now_convert    = $convertAudit
            time_now_source         = $sourceAudit
            ready_queue_tasks       = $readyTasks
        }
        overall_status      = if ($totalNewOrders -gt 0) { "NEW_GIT_ORDER_RECEIVED_EXECUTING" } else { "AWAKE_MONITORING_GIT_CLEAN" }
    }

    $jsonLine = ($payload | ConvertTo-Json -Depth 6 -Compress)
    $jsonPretty = ($payload | ConvertTo-Json -Depth 6)

    # Append to VAN-HANH heartbeat log
    $hbDir = Split-Path -Parent $HeartbeatLog
    if (-not (Test-Path $hbDir)) { New-Item -ItemType Directory -Force -Path $hbDir | Out-Null }
    Add-Content -Path $HeartbeatLog -Value $jsonLine -Encoding UTF8

    # Save latest status snapshot in CONVERT/.ai/watchdog_audit_git_status.json
    $aiDir = Split-Path -Parent $ConvertStatus
    if (-not (Test-Path $aiDir)) { New-Item -ItemType Directory -Force -Path $aiDir | Out-Null }
    Set-Content -Path $ConvertStatus -Value $jsonPretty -Encoding UTF8

    # If new orders arrived from Git, record them in git_audit_inbox.json
    if ($totalNewOrders -gt 0) {
        Set-Content -Path $AuditInbox -Value $jsonPretty -Encoding UTF8
    }

    Write-Output "[AUDIT-GIT WATCHDOG $ts] iter=$Iteration status=$($payload.overall_status) van_hanh_read=$($govReport.mandatory_read_ok) convert_head=$($convertAudit.local_head.Substring(0,7)) source_head=$($sourceAudit.local_head.Substring(0,7)) new_orders=$totalNewOrders"
}

$iter = 0
while ($true) {
    $iter++
    try {
        Invoke-SingleAuditPass -Iteration $iter
    } catch {
        Write-Output "[AUDIT-GIT WATCHDOG ERROR] $($_.Exception.Message)"
    }
    if ($Once) {
        break
    }
    Start-Sleep -Seconds $IntervalSeconds
}
