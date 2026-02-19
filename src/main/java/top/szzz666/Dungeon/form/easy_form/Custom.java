package top.szzz666.Dungeon.form.easy_form;

import cn.nukkit.Player;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.handler.FormResponseHandler;
import cn.nukkit.form.window.FormWindowCustom;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static top.szzz666.Dungeon.tools.taskUtil.Async;


@Data
public class Custom {
    private final List<String> elements = new ArrayList<>();
    private FormWindowCustom form;
    private Runnable close;
    private Runnable submit;
    private boolean async;

    public Custom(String title) {
        this.form = new FormWindowCustom(title);
    }

    public Custom(String title, boolean async) {
        this.form = new FormWindowCustom(title);
        this.async = async;
    }

    public String add(String Label) {
        String key = getRandKey();
        this.elements.add(key);
        this.form.addElement(new ElementLabel(Label));
        return key;
    }

    private String getRandKey() {
        return "custom_" + Math.random();
    }

    public void add(String key, Element element) {
        this.elements.add(key);
        this.form.addElement(element);
    }

    public void show(Player player) {
        if (this.async) {
            asyncShow(player);
            return;
        }
        this.form.addHandler(FormResponseHandler.withoutPlayer(ignored -> processReturns()));
        player.showFormWindow(this.form);
    }

    private void processReturns() {
        if (this.form.wasClosed()) {
            if (this.close != null) {
                this.close.run();
            }
            return;
        }
        if (this.submit != null) {
            this.submit.run();
        }
    }

    public void asyncShow(Player player) {
        this.form.addHandler(FormResponseHandler.withoutPlayer(ignored -> Async(this::processReturns)));
        player.showFormWindow(this.form);
    }

    @SuppressWarnings("unchecked")
    public <T> T getRes(String key) {
        return (T) this.form.getResponse().getResponse(elements.indexOf(key));
    }

    public String getInputRes(String key) {
        return this.form.getResponse().getInputResponse(elements.indexOf(key));
    }

    public String getDropdownRes(String key) {
        return this.form.getResponse().getDropdownResponse(elements.indexOf(key)).getElementContent();
    }

    public String getStepSliderRes(String key) {
        return this.form.getResponse().getStepSliderResponse(elements.indexOf(key)).getElementContent();
    }

    public int getStepSliderIdRes(String key) {
        return this.form.getResponse().getStepSliderResponse(elements.indexOf(key)).getElementID();
    }

    public int getDropdownIndexRes(String key) {
        return this.form.getResponse().getDropdownResponse(elements.indexOf(key)).getElementID();
    }

    public float getSliderRes(String key) {
        return this.form.getResponse().getSliderResponse(elements.indexOf(key));
    }

    public boolean getToggleRes(String key) {
        return this.form.getResponse().getToggleResponse(elements.indexOf(key));
    }

}
