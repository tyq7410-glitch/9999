package dev.auroravisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.List;

/** Config manager: save under a name, load, delete, share through the clipboard. */
public class ConfigScreen extends Screen {
    private static final int ROWS = 5;
    private final Screen parent;
    private TextFieldWidget nameField;
    private String status = "", confirm = null, typed = "";
    private int page = 0;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Configs"));
        this.parent = parent;
    }

    private void btn(String label, String tip, int x, int y, int w, Runnable r) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), bb -> r.run()).dimensions(x, y, w, 20).build();
        if (tip != null) b.setTooltip(Tooltip.of(Text.literal(tip)));
        addDrawableChild(b);
    }

    @Override
    protected void init() {
        int L = width / 2 - 155;
        nameField = new TextFieldWidget(textRenderer, L, 34, 200, 20, Text.literal("Config name"));
        nameField.setMaxLength(24);
        nameField.setPlaceholder(Text.literal("config name"));
        nameField.setText(typed);
        addDrawableChild(nameField);
        btn("Save", "Сохранить текущие настройки под этим именем (буквы, цифры, _ и -).", L + 205, 34, 105, () -> {
            typed = nameField.getText();
            String n = Configs.clean(typed);
            if (n.isEmpty()) status = "Enter a name first";
            else { status = Configs.save(n) ? "Saved: " + n : "Could not save"; page = 0; }
            clearAndInit();
        });

        List<String> names = Configs.list();
        int pages = Math.max(1, (names.size() + ROWS - 1) / ROWS);
        page = Math.min(page, pages - 1);
        for (int r = 0; r < ROWS; r++) {
            int i = page * ROWS + r;
            if (i >= names.size()) break;
            final String n = names.get(i);
            int y = 62 + r * 24;
            btn("Load: " + n, "Загрузить этот конфиг. Текущие настройки будут заменены.", L, y, 205, () -> {
                typed = nameField.getText();
                status = Configs.load(n) ? "Loaded: " + n : "Could not load";
                confirm = null;
                clearAndInit();
            });
            btn(n.equals(confirm) ? "Sure?" : "Delete", "Удалить конфиг. Нажмите дважды для подтверждения.", L + 210, y, 100, () -> {
                typed = nameField.getText();
                if (n.equals(confirm)) { Configs.delete(n); status = "Deleted: " + n; confirm = null; }
                else confirm = n;
                clearAndInit();
            });
        }
        if (pages > 1) {
            btn("< Prev", null, L, 62 + ROWS * 24, 100, () -> { typed = nameField.getText(); page = Math.max(0, page - 1); clearAndInit(); });
            btn("Next >", null, L + 210, 62 + ROWS * 24, 100, () -> { typed = nameField.getText(); page = Math.min(pages - 1, page + 1); clearAndInit(); });
        }
        btn("Copy to clipboard", "Скопировать текущий конфиг одной строкой, чтобы отправить другу.", L, height - 52, 150, () -> {
            client.keyboard.setClipboard(Configs.export());
            status = "Copied! Send this text to a friend";
        });
        btn("Paste from clipboard", "Вставить конфиг, скопированный из буфера обмена (заменит текущие настройки).", L + 160, height - 52, 150, () -> {
            status = Configs.importFrom(client.keyboard.getClipboard()) ? "Imported from clipboard" : "Clipboard has no valid config";
        });
        btn("Back", null, width / 2 - 75, height - 28, 150, this::close);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        String t = "CONFIG MANAGER";
        ctx.drawText(textRenderer, t, (width - textRenderer.getWidth(t)) / 2, 14, Config.I.accent(), true);
        if (!status.isEmpty())
            ctx.drawText(textRenderer, status, (width - textRenderer.getWidth(status)) / 2, height - 66, 0xFFFFFFFF, true);
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.setScreen(parent);
    }
}
