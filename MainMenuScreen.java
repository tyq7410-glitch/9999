package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class MainMenuScreen extends Screen {
    private final Screen parent;
    private String status = "";

    public MainMenuScreen(Screen parent) {
        super(Text.literal("Aurora Visuals"));
        this.parent = parent;
    }

    private void btn(String label, String tip, int x, int y, int w, Runnable r) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), bb -> r.run()).dimensions(x, y, w, 20).build();
        if (tip != null) b.setTooltip(Tooltip.of(Text.literal(tip)));
        addDrawableChild(b);
    }

    @Override
    protected void init() {
        int L = width / 2 - 155, y = 38;
        String[] names = {"HUD", "Info", "Crosshair", "Effects", "Cosmetics", "Theme", "PvP", "Helper",
                          "Cosm. +", "Widgets +", "Cosm. ++", "Widgets ++", "PvP +", "PvE +", "Comfort"};
        int[] pages = {0, 1, 2, 3, 4, 5, 6, 7, 12, 14, 18, 20, 24, 25, 27};
        String[] tips = {
            "Панели HUD: FPS, CPS, пинг, координаты, клавиши, масштаб и стиль.",
            "Дополнительные виджеты: скорость, броня, зелья, таргет и др.",
            "Свой прицел: форма, цвет, размер, хитмаркер.",
            "Эффекты: след, шляпа, частицы при ударе, зум, звук удара.",
            "Косметика: нимб, аура, крылья, следы, кольца.",
            "Цвета темы: пресеты, радуга, палитра, прозрачность.",
            "PvP-визуалы: предупреждение о HP, таймер боя, Target HUD.",
            "Помощники: перезарядки предметов, счётчики, FPS guard.",
            "Ещё 28 косметических эффектов с палитрой, размером и скоростью.",
            "16 виджетов у прицела, экранные эффекты и палитры цветов.",
            "Ещё 26 косметических эффектов: фигуры, орбиты, плащ, лента и другое.",
            "14 виджетов статистики, 10 экранных эффектов, палитры цветов.",
            "10 новых PvP-функций: крит, щит, вспышка убийства, предупреждения.",
            "10 новых PvE-функций: инструмент, высота, таймер дня, точка смерти.",
            "Комфорт и доступность: режим для дальтоников, без мигания, высокий контраст, режим стримера и др."};
        for (int i = 0; i < 15; i++) {
            final int page = pages[i];
            btn(names[i], tips[i], L + (i % 3) * 105, y + (i / 3) * 22, 100, () -> client.setScreen(new SettingsScreen(this, page)));
        }
        y += 5 * 22 + 6;
        btn("HUD editor", "Редактор: перетаскивайте блоки HUD мышкой.", L, y, 100, () -> client.setScreen(new HudEditorScreen(this)));
        btn("Configs", "Менеджер конфигов: сохранить под именем, загрузить, удалить, поделиться через буфер обмена.", L + 105, y, 100,
            () -> client.setScreen(new ConfigScreen(this)));
        btn("Reset all", "Сбросить ВСЕ настройки на стандартные.", L + 210, y, 100, () -> { Config.reset(); status = "Reset to defaults"; });
        y += 26;
        btn("Minimal", "Быстрая настройка: только FPS и CPS, всё остальное выключено. Тема и позиции сохраняются.", L, y, 74,
            () -> { Config.applyProfile(0); status = "Profile: Minimal"; });
        btn("PvP", "Быстрая настройка: PvP-функции, счётчики, звук удара. Тема и позиции сохраняются.", L + 78, y, 74,
            () -> { Config.applyProfile(1); status = "Profile: PvP"; });
        btn("Cosmetic", "Быстрая настройка: крылья, нимб, аура, след, лепестки. Тема и позиции сохраняются.", L + 156, y, 74,
            () -> { Config.applyProfile(2); status = "Profile: Cosmetic"; });
        btn("Comfort", "Удобные настройки: спокойный режим, высокий контраст, меньше движения, компактный HUD.", L + 234, y, 74,
            () -> { Config.applyComfort(); status = "Profile: Comfort"; });
        y += 26;
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            btn("Save " + i, "Сохранить текущие настройки в быстрый слот " + i + ".", L + (i - 1) * 52, y, 50,
                () -> { Config.saveSlot(n); status = "Saved to slot " + n; });
            btn("Load " + i, "Загрузить настройки из быстрого слота " + i + ".", L + 160 + (i - 1) * 52, y, 50,
                () -> { status = Config.loadSlot(n) ? "Loaded slot " + n : "Slot " + n + " is empty"; });
        }
        btn("Close", null, width / 2 - 75, height - 28, 150, this::close);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        String t = "AURORA VISUALS";
        ctx.drawText(textRenderer, t, (width - textRenderer.getWidth(t)) / 2, 18, Config.I.accent(), true);
        ctx.fill(width / 2 - 60, 29, width / 2 + 60, 30, Config.I.accent());
        if (!status.isEmpty())
            ctx.drawText(textRenderer, status, (width - textRenderer.getWidth(status)) / 2, height - 42, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
