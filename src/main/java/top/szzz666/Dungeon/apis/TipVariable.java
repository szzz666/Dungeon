package top.szzz666.Dungeon.apis;

import cn.nukkit.Player;
import tip.utils.Api;
import tip.utils.variables.BaseVariable;
import top.szzz666.Dungeon.entity.PermanentPlayerData;


import static top.szzz666.Dungeon.DungeonMain.*;
import static top.szzz666.Dungeon.tools.pluginUtil.getEntityLevel;


public class TipVariable extends BaseVariable {

    public TipVariable(Player player) {
        super(player);
    }

    public static void init() {
        Api.registerVariables(plugin.getName(), TipVariable.class);
    }

    public void strReplace() {
        PermanentPlayerData data = permanentPlayerDataMap.get(player);
        if (data == null) {
            return;
        }
        addStrReplaceString("{s_rpg_level}", String.valueOf(getEntityLevel(player)));
        addStrReplaceString("{s_rpg_exp}", String.valueOf(data.exp));
        addStrReplaceString("{s_rpg_exp_max}",
                String.valueOf((int)(ec.getInt("玩家升级需要经验") *
                        ec.getDouble("玩家升级经济倍率") * getEntityLevel(player))));
    }
}
