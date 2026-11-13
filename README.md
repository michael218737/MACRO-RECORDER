# Macro Recorder 1.0 — Minecraft Java 1.21.11 / Fabric

Niezależny mod klienta. Domyślne klawisze:
- F6 — rozpocznij nagrywanie
- F7 — zakończ i zapisz nagranie
- F8 — odtwórz ostatnie nagranie
- F9 — zatrzymaj odtwarzanie

Gdy nagrywanie lub odtwarzanie jest aktywne, w prawym górnym rogu HUD pokazuje stan makra i bindy F6–F9.

Ta wersja nagrywa przejścia klawiszy ruchu (W/A/S/D, skok, kucanie, sprint) oraz wysłane komendy. Komendy są odtwarzane jako zwykłe komendy gracza — serwer nadal sprawdza uprawnienia. Kliknięcia myszy i zwykłe wiadomości czatu nie są jeszcze obsługiwane.

Nagrania są zapisywane w `config/macro-recorder/macro-1.mcr`.

## Budowanie
Projekt buduje się automatycznie przez GitHub Actions po każdym pushu. Gotowy JAR znajdziesz w zakładce Actions → najnowszy workflow → Artifacts. Możesz też pobrać go z Releases, jeśli workflow opublikuje release.
