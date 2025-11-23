# Avito Assignment App

Приложение для загрузки, хранения и чтения книг (PDF, EPUB, TXT) с синхронизацией через облако.
Реализовано в рамках тестового задания на позицию Android Intern (Autumn 2025).

## Демонстрация работы

▶️ **[Видео-демонстрация функционала на Google Drive](https://drive.google.com/drive/folders/1my_Em5OkjZlMXHDRO_8K0AyMYx0MpDik?usp=sharing)**

### Скриншоты

| Экран 1 | Экран 2 | Экран 3 | Экран 4 |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/8c0d483f-c9bc-46e0-8b1b-554304625ec9" width="200"/> | <img src="https://github.com/user-attachments/assets/b51ddc9f-c44c-499e-9cb2-0d6a81564c3a" width="200"/> | <img src="https://github.com/user-attachments/assets/1d7ba0ca-9b7b-4eed-b141-13fa26f16367" width="200"/> | <img src="https://github.com/user-attachments/assets/b5aa69fd-2891-4231-a50e-f634d4dc44d0" width="200"/> |

| Экран 5 | Экран 6 | Экран 7 | Экран 8 |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/44f744c6-9fef-4773-84e5-07cc44c5bd6e" width="200"/> | <img src="https://github.com/user-attachments/assets/4d769da7-3a1f-4f7b-af25-09cb6e4dd292" width="200"/> | <img src="https://github.com/user-attachments/assets/f4efdef0-7d1b-4d3f-9f6f-b52530fa7b28" width="200"/> | <img src="https://github.com/user-attachments/assets/eaed3a6f-ef21-4af7-b216-4f7a0d00307b" width="200"/> |

*(Примечание: Скриншоты расположены в порядке прохождения пользовательского сценария)*

## Технологический стек

Проект написан с использованием современных практик Android-разработки:

*   **Язык:** Kotlin
*   **UI:** Jetpack Compose 
*   **Архитектура:** Clean Architecture + MVVM
*   **DI:** Hilt
*   **Асинхронность:** Coroutines + Flow
*   **Сеть и Облако:**
    *   **Firebase Auth:** Регистрация и авторизация.
    *   **Firebase Firestore:** База данных для метаинформации о книгах.
    *   **Yandex Object Storage (S3):** Хранение бинарных файлов книг и аватарок (реализовано через Amazon S3 SDK).
*   **Локальные данные:**
    *   **Room:** Кэширование списка книг и прогресса чтения.
    *   **DataStore:** Хранение настроек (размер шрифта, тема).
*   **Парсинг:**
    *   **PdfBox-Android:** Рендеринг PDF.
    *   **Jsoup:** Парсинг EPUB/HTML.

## Функциональность

### 1. Авторизация и Профиль
*   Вход и регистрация (Email/Password).
*   Валидация полей с отображением ошибок.
*   Редактирование профиля: смена имени и загрузка аватара в Yandex Object Storage.
*   Автоматический вход (сохранение сессии).

### 2. Библиотека
*   Синхронизация списка с Firestore.
*   Локальный поиск и фильтрация книг.
*   Индикация статуса: "В облаке" / "Скачано".
*   Два типа удаления:
    *   Удалить с устройства: удаляет файл локально, экономит место.
    *   Удалить навсегда: удаляет файл из облачного хранилища и запись из БД.

### 3. Чтение
*   Поддержка форматов: .txt, .pdf, .epub.
*   Кастомизация: Изменение размера шрифта, переключение темы (Светлая/Тёмная).
*   Прогресс: Автоматическое сохранение позиции чтения.
*   Обработка ошибок при открытии поврежденных файлов.

## Сборка и запуск

В целях безопасности ключи доступа к облачным сервисам (S3, Firebase) не включены в репозиторий.
Для запуска проекта вам потребуется настроить собственное окружение.

### Шаг 1. Настройка ключей S3 (Yandex Object Storage)
Создайте файл `keystore.properties` в корне проекта и добавьте ключи:

```properties
S3_ENDPOINT=https://storage.yandexcloud.net
S3_BUCKET_NAME=your_bucket_name
S3_ACCESS_KEY=your_access_key
S3_SECRET_KEY=your_secret_key
```

### Шаг 2. Настройка Firebase
1.  Создайте проект в Firebase Console.
2.  Включите Authentication (Email/Password) и Firestore.
3.  Скачайте файл `google-services.json` и поместите его в папку `app/` внутри проекта.

**Примечание:** Без выполнения этих шагов приложение не соберется или будет падать при запуске сетевых функций.

### Тестовое окружение
Приложение протестировано на:
*   **Устройство:** Pixel 7 Pro
*   **ОС:** Android 16
