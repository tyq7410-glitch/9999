# Інструкція: як отримати мод через GitHub (без програм на комп'ютері)

GitHub сам збере jar-файл мода. Тобі потрібен лише браузер.

## 1. Підготовка
1. Зареєструйся на https://github.com (безкоштовно) і підтверди пошту.
2. **Розпакуй** архів `aurora-visuals-github.zip` на комп'ютері. Тобі потрібні файли *всередині* (папка `src`, файли `build.gradle`, `settings.gradle`, `gradle.properties`, `README.md`, а також прихована папка `.github`).
   - Не завантажуй сам zip на GitHub: він не розпакується.

## 2. Створи НОВИЙ репозиторій
1. Натисни **+** (праворуч угорі) -> **New repository**.
2. Name: `aurora-visuals`. Став **Public**.
3. Нічого не відмічай (без README, без .gitignore) -> **Create repository**.

## 3. Завантаж файли
1. На сторінці нового репозиторію натисни **uploading an existing file**.
2. Перетягни в вікно ВСЕ вміст розпакованої папки (папки і файли).
3. Внизу натисни **Commit changes**.

### Якщо не завантажилась папка `.github` (вона прихована)
Перевір: у репозиторії має бути шлях `.github/workflows/build.yml`. Якщо його немає:
1. **Add file -> Create new file**.
2. У полі імені введи: `.github/workflows/build.yml` (слеші створять папки).
3. Встав такий вміст і натисни **Commit changes**:

```yaml
name: build
on:
  push:
    branches: [main, master]
    tags: ['v*']
  pull_request:
  workflow_dispatch:
permissions:
  contents: write
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Fix folder layout
        shell: bash
        run: |
          J=src/main/java/dev/auroravisuals
          R=src/main/resources
          mkdir -p "$J" "$R/assets/auroravisuals/lang"
          find . -name '*.java' -not -path './.git/*' -not -path './build/*' | while read -r f; do
            [ "$f" -ef "$J/$(basename "$f")" ] || cp -n "$f" "$J/"
          done
          [ -f "$R/fabric.mod.json" ] || cp "$(find . -name fabric.mod.json -not -path './.git/*' -not -path './build/*' | head -1)" "$R/"
          [ -f "$R/assets/auroravisuals/lang/en_us.json" ] || cp "$(find . -name en_us.json -not -path './.git/*' -not -path './build/*' | head -1)" "$R/assets/auroravisuals/lang/"
          test "$(ls $J/*.java | wc -l)" -gt 0 || { echo "NO JAVA FILES FOUND"; exit 1; }
          { echo '## Repository files'; echo '```'; find . -type f -not -path './.git/*' | sort; echo '```'; } >> "$GITHUB_STEP_SUMMARY"
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: 9.2.0
      - name: Build
        shell: bash
        run: |
          set -o pipefail
          gradle build --no-daemon --stacktrace 2>&1 | tee build.log
      - name: Check jar
        shell: bash
        run: |
          JAR=$(ls build/libs/*.jar | grep -v sources | head -1)
          N=$(unzip -l "$JAR" | grep -c '\.class' || true)
          { echo '## Jar check'; echo "$JAR: $(stat -c%s "$JAR") bytes, $N classes"; } >> "$GITHUB_STEP_SUMMARY"
          [ "$N" -gt 0 ] || { echo "JAR HAS NO CLASSES"; exit 1; }
      - name: Show errors in summary
        if: failure()
        shell: bash
        run: |
          { echo '## Build errors (send this to Claude)'; echo '```'; grep -E "error:|What went wrong|Could not|FAILED|Caused by" -A4 build.log | head -80 || true; echo '```'; } >> "$GITHUB_STEP_SUMMARY"
      - uses: actions/upload-artifact@v4
        with:
          name: aurora-visuals
          path: |
            build/libs/*.jar
            !build/libs/*-sources.jar
      - name: Release on tag
        if: startsWith(github.ref, 'refs/tags/v')
        uses: softprops/action-gh-release@v2
        with:
          files: build/libs/*.jar
```'
            grep -E "error:|What went wrong|Could not|FAILED|Caused by" -A4 build.log | head -80 || true
            echo '```'
          } >> "$GITHUB_STEP_SUMMARY"
      - name: Upload full log on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: build-log
          path: build.log
      - uses: actions/upload-artifact@v4
        with:
          name: aurora-visuals
          path: build/libs/*.jar
      - name: Release on tag
        if: startsWith(github.ref, 'refs/tags/v')
        uses: softprops/action-gh-release@v2
        with:
          files: build/libs/*.jar
```

## 4. Запусти збірку
1. Відкрий вкладку **Actions**. Якщо просить, натисни **I understand my workflows, go ahead and enable them**.
2. Зліва вибери **build** -> **Run workflow** -> **Run workflow** (гілка main).
3. Зачекай 3-6 хвилин. Зелена галочка = успіх.

## 5. Забери jar
1. Відкрий завершений запуск (клік на назву).
2. Унизу, в розділі **Artifacts**, натисни `aurora-visuals` — завантажиться zip.
3. Розпакуй його. Тобі потрібен `aurora-visuals-1.0.0.jar` (файл із `-sources` не потрібен).

## 6. Встанови в гру
1. Встанови **Fabric Loader** для Minecraft **1.21.11**: https://fabricmc.net/use/installer/
2. Завантаж **Fabric API** для 1.21.11 (Modrinth: сторінка "Fabric API").
3. Поклади обидва jar-файли в папку `.minecraft/mods` (Win+R -> `%appdata%\.minecraft\mods`).
4. Запусти профіль Fabric 1.21.11. У грі натисни **Right Shift**.

## 6.5. Перевір, що все завантажилось
У репозиторії, вкладка **Code**: має бути папка `src` -> `main` -> `java` -> `dev` -> `auroravisuals` з 16 файлами `.java`, а також `build.gradle`, `settings.gradle`, `gradle.properties` і папка `.github`. Якщо чогось нема, докинь через **Add file -> Upload files**.

## 7. Якщо збірка червона (X)
Найпростіше: відкрий червоний запуск у Actions і прокрути сторінку вниз до блоку **Build errors**. Зроби його скріншот або скопіюй текст і надішли мені.

1. Відкрий запуск -> натисни на крок з червоним хрестиком (зазвичай **Run gradle build**).
2. Знайди рядки зі словом `error:` — скопіюй перші 10-20 рядків і надішли мені. Я виправлю.
3. Часті причини:
   - Версії в `gradle.properties` застаріли: відкрий https://fabricmc.net/develop , обери 1.21.11 і заміни значення `yarn_mappings`, `loader_version`, `fabric_version`, `loom_version`.
   - У коді змінилась назва методу Minecraft — це покаже `cannot find symbol` з назвою методу.

## 8. Як оновити мод
1. Відкрий потрібний файл у GitHub -> значок олівця -> змін -> **Commit changes**.
2. Збірка запуститься сама (вкладка Actions), новий jar буде в Artifacts.
3. Для публічного релізу: **Releases -> Create a new release -> Tag `v1.0.1` -> Publish**. Jar додасться до релізу автоматично.

## 9. Безпека і продуктивність
- Користуйся лише jar, зібраним у твоєму репозиторії. Не качай моди з випадкових сайтів.
- Мод тільки візуальний. Деякі сервери забороняють клієнтські моди — дивись правила сервера.
- Багато частинкових ефектів одночасно знижують FPS. Якщо просідає FPS, зменш Cosmetic intensity, вимкни зайві ефекти або перемкни форму частинок.
