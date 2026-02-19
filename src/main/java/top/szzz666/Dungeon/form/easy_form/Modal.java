package top.szzz666.Dungeon.form.easy_form;

import cn.nukkit.Player;
import cn.nukkit.form.handler.FormResponseHandler;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowModal;
import lombok.Data;

import static top.szzz666.Dungeon.tools.taskUtil.Async;


@Data
public class Modal {
    private final FormWindowModal form;

    private Runnable truer;
    private Runnable falser;
    private Runnable close;
    private boolean async;


    public Modal(String title, String content, String trueButtonText, String falseButtonText) {
        this.form = new FormWindowModal(title, content, trueButtonText, falseButtonText);
    }

    public Modal(String title, String content, String trueButtonText, String falseButtonText, boolean async) {
        this.form = new FormWindowModal(title, content, trueButtonText, falseButtonText);
        this.async = async;
    }

    public static void tipsModal(Player player, String content, FormWindow form) {
        Modal modal = new Modal("提示", content, "返回", "关闭");
        modal.setTruer(() -> player.showFormWindow(form));
        modal.asyncShow(player);
    }

    public static void tipsModal(Player player, String content, Runnable r) {
        Modal modal = new Modal("提示", content, "返回", "关闭");
        modal.setTruer(r);
        modal.asyncShow(player);
    }

    public static void confirmModal(Player player, String content, FormWindow form, Runnable truer) {
        Modal form1 = new Modal("需要确认", content, "确认", "返回");
        form1.setTruer(truer);
        form1.setFalser(() -> player.showFormWindow(form));
        form1.asyncShow(player);
    }
    public static void confirmModal(Player player, String content, Runnable falser, Runnable truer) {
        Modal form1 = new Modal("需要确认", content, "确认", "返回");
        form1.setTruer(truer);
        form1.setFalser(falser);
        form1.asyncShow(player);
    }
    public static void confirmModal(Player player, String content, Runnable truer) {
        Modal form1 = new Modal("需要确认", content, "确认", "取消");
        form1.setTruer(truer);
        form1.asyncShow(player);
    }

    public void show(Player player) {
        if (this.async){
            asyncShow(player);
            return;
        }
        this.form.addHandler(FormResponseHandler.withoutPlayer(ignored -> processReturns()));
        player.showFormWindow(this.form);
    }


    public void asyncShow(Player player) {
        this.form.addHandler(FormResponseHandler.withoutPlayer(ignored -> Async(this::processReturns)));
        player.showFormWindow(this.form);
    }

    private void processReturns() {
        if (this.form.wasClosed()) {
            if (this.close != null) {
                this.close.run();
            }
            return;
        }
        if (this.form.getResponse().getClickedButtonId() == 0) {
            if (this.truer != null) {
                this.truer.run();
            }
        } else {
            if (this.falser != null) {
                this.falser.run();
            }
        }
    }
}
