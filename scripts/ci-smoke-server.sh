#!/usr/bin/env bash
# Headless server smoke test for Minecraft 1.20.1 + Fabric.
# Checks that the mod actually loads and the server reaches the ready state.
# This does NOT test player interactions, pathfinding or NPC tasks.
set -Eeuo pipefail

log_file="${PWD}/server-smoke.log"
mkdir -p run

# Disposable CI world, listening on loopback only. The EULA is accepted
# for this isolated test server; see docs/CI-RELEASES.md.
printf 'eula=true\n' > run/eula.txt
cat > run/server.properties <<'EOF'
server-ip=127.0.0.1
server-port=25565
online-mode=false
max-players=1
motd=Minecraft AI NPC CI test
EOF

echo "Starting isolated Fabric server; output: ${log_file}"
timeout --signal=TERM --kill-after=10s 360s \
    gradle --no-daemon --console=plain runServer > "${log_file}" 2>&1 &
launcher_pid=$!

cleanup() {
    kill -TERM "${launcher_pid}" 2>/dev/null || true
    wait "${launcher_pid}" 2>/dev/null || true
}
trap cleanup EXIT

for attempt in $(seq 1 165); do
    if grep -Fq "Minecraft AI NPC initialized" "${log_file}" \
            && grep -Eq 'Done \([0-9.,]+s\)!' "${log_file}"; then
        echo "PASS: Fabric loaded Minecraft AI NPC and the server reached ready state."
        exit 0
    fi

    if ! kill -0 "${launcher_pid}" 2>/dev/null; then
        echo "::error::Fabric server process exited before becoming ready."
        tail -n 160 "${log_file}" || true
        exit 1
    fi

    sleep 2
done

echo "::error::Fabric server did not reach ready state within 330 seconds."
tail -n 160 "${log_file}" || true
exit 1
