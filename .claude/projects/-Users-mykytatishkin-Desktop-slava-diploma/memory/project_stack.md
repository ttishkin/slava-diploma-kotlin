---
name: Project stack decision
description: Tech stack chosen for diploma — Kotlin Ktor + SQLite backend, Android Jetpack Compose frontend
type: project
---

Целевой стек (решение от 2026-06-01):
- **Android:** Kotlin + Jetpack Compose + Material 3, архитектура MVVM
- **Бэкенд:** Kotlin + Ktor + SQLite (через Exposed ORM)
- **Платформы:** только Android (iOS — возможно в будущем через отдельный Swift-клиент к тому же API)

**Why:** Пользователь считает Kotlin стабильнее JS. Единый язык на клиенте и сервере. Нативный Android даёт лучший UX чем PWA. SQLite оставлен для простоты — хватает для диплома.

**How to apply:** Весь новый код пишем на Kotlin. Старый JS-проект (nevsky-konditer/) — только как референс для бизнес-логики и API-контрактов.
