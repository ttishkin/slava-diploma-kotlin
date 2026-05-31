# Невский Кондитер — ЗОЖ (Дипломный проект)

## Обзор проекта

Мобильное Android-приложение + бэкенд для кондитерской фабрики «Невский Кондитер».
Совмещает e-commerce (каталог, корзина, заказы, бонусы) с ЗОЖ-функционалом (расчёт калорийной нормы, «сладкий бюджет», дневник питания с КБЖУ).

**Текущее состояние:** существующее PWA (client/ + server/) на JS/Express/SQLite — служит как референс.
**Цель:** полный перенос функционала на нативный стек.

## Целевой стек

### Android-приложение (`android/`)
- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Архитектура:** MVVM (ViewModel + StateFlow + Repository pattern)
- **Навигация:** Compose Navigation
- **Сеть:** Ktor Client или Retrofit + kotlinx.serialization
- **DI:** Hilt (Dagger)
- **Хранение:** Room (локальный кеш) + DataStore (настройки)
- **Изображения:** Coil

### Бэкенд (`backend/`)
- **Язык:** Kotlin
- **Фреймворк:** Ktor (Server)
- **БД:** SQLite (через Exposed ORM)
- **Аутентификация:** JWT (ktor-auth + ktor-auth-jwt)
- **Сериализация:** kotlinx.serialization
- **Документация API:** OpenAPI / Swagger (ktor-swagger)

## Структура репозитория

```
slava-diploma/
├─ android/                    # Android-приложение (Kotlin + Jetpack Compose)
│  ├─ app/src/main/
│  │  ├─ java/.../             # Kotlin-код
│  │  │  ├─ data/              #   Repository, API-клиент, Room DAO
│  │  │  ├─ domain/            #   Use cases, модели
│  │  │  ├─ ui/                #   Screens (Compose), ViewModels, theme
│  │  │  └─ di/                #   Hilt-модули
│  │  └─ res/                  #   Ресурсы
│  └─ build.gradle.kts
├─ backend/                    # Ktor-бэкенд (Kotlin)
│  ├─ src/main/kotlin/
│  │  ├─ routes/               #   API-эндпоинты
│  │  ├─ services/             #   Бизнес-логика
│  │  ├─ repositories/         #   SQL-запросы (Exposed)
│  │  ├─ models/               #   DTO и сущности
│  │  ├─ plugins/              #   Ktor-плагины (auth, CORS, сериализация)
│  │  └─ Application.kt       #   Точка входа
│  └─ build.gradle.kts
├─ nevsky-konditer/            # Старый PWA-проект (референс)
├─ docs/                       # Документация
└─ CLAUDE.md                   # <- этот файл
```

## Функционал (полный перенос из PWA)

### Пользовательские экраны
1. **Онбординг** — пол, возраст, рост, вес, активность, цель → расчёт КБЖУ (Mifflin-St Jeor)
2. **Каталог** — поиск, фильтры (категория, теги), сортировка, «продукт дня»
3. **Карточка продукта** — КБЖУ, состав, отзывы (1-5 звёзд)
4. **Дневник питания** — приёмы пищи, прогресс-кольцо калорий, синхронизация
5. **Корзина и оформление заказа** — доставка (199₽ / бесплатно от 1000₽), промокоды, бонусы
6. **Профиль** — данные, история заказов, баланс бонусов, тема (светлая/тёмная)
7. **Избранное**

### Админ-панель
- Управление заказами (статусы: new → processing → shipped → delivered → cancelled)
- Статистика продаж (дневная), список пользователей
- CSV-экспорт

### Бизнес-логика (важно!)
- Бонусы: 200 при регистрации, 5% кэшбэк, трата до 30% от суммы
- `total = max(0, subtotal + delivery - promoDiscount - bonusSpent)`
- Доставка: бесплатно от 1000₽, иначе 199₽
- TDEE: формула Mifflin-St Jeor с коэффициентами активности
- «Сладкий бюджет» ≈ 20% от TDEE

## Правила разработки

### Общие
- Весь код на Kotlin (и Android, и бэкенд)
- Комментарии в коде — на русском
- Commit messages — на русском
- UI-текст — на русском

### Android
- Минимальный SDK: 26 (Android 8.0)
- Target SDK: 35
- Compose BOM для единых версий
- Каждый экран = отдельный Composable + ViewModel
- State через StateFlow, не LiveData
- Навигация через sealed class для маршрутов

### Бэкенд
- Слоистая архитектура: Routes → Services → Repositories → DB
- Все SQL через Exposed (не raw SQL)
- Параметризованные запросы (без конкатенации)
- JWT с настраиваемым временем жизни
- Валидация входных данных на уровне сервисов
- Транзакции для составных операций (создание заказа)

### Безопасность
- Пароли: bcrypt (или Argon2)
- JWT секрет из env-переменной
- Rate limiting на API
- CORS настроен на домен
- Нет секретов в коде

## API Reference (переносим из PWA)

Базовый путь: `/api`

| Метод | Путь | Описание | Auth |
|-------|------|----------|------|
| POST | /auth/register | Регистрация | - |
| POST | /auth/login | Вход | - |
| GET | /auth/me | Текущий пользователь | JWT |
| GET | /products | Каталог (?q, ?category, ?tag, ?sort) | - |
| GET | /products/:id | Карточка продукта | - |
| GET | /categories | Категории | - |
| POST | /orders | Создать заказ | JWT |
| GET | /orders | Мои заказы | JWT |
| GET/PUT | /cart | Корзина | JWT |
| GET/PUT | /favorites | Избранное | JWT |
| POST | /reviews | Оставить отзыв | opt |
| GET | /bonuses | Баланс и история бонусов | JWT |
| GET/POST/PATCH/DELETE | /diary | Дневник питания | JWT |
| GET/POST/DELETE | /addresses | Адреса доставки | JWT |
| GET | /pickup-points | Пункты самовывоза | - |
| GET | /promo/:code | Проверить промокод | - |
| GET | /notifications | Уведомления | JWT |
| * | /admin/* | Админ-эндпоинты | admin |

## Демо-данные

- Пользователь: demo@nk.ru / demo1234
- Администратор: admin@nk.ru / admin1234
- Промокод: ЗОЖ10 (скидка 10%)

## Команды

```bash
# Старый PWA (референс)
cd nevsky-konditer && npm start          # http://localhost:3000

# Бэкенд (Ktor) — будет
cd backend && ./gradlew run              # http://localhost:8080

# Android — будет
cd android && ./gradlew assembleDebug    # собрать APK
```
