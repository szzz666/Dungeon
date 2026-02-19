package top.szzz666.Dungeon.entity;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import top.szzz666.Dungeon.tools.ItemMap;


import java.util.ArrayList;
import java.util.HashMap;

import static top.szzz666.Dungeon.DungeonMain.teams;
import static top.szzz666.Dungeon.form.easy_form.Modal.confirmModal;
import static top.szzz666.Dungeon.form.easy_form.Modal.tipsModal;
import static top.szzz666.Dungeon.item.ItemTpMagicScroll.TMS_ID;
import static top.szzz666.Dungeon.tools.pluginUtil.getItemMapByPlayer;


public class Team {
    public HashMap<Player, Boolean> members;
    public Player leader;

    public Team(Player leader) {
        this.leader = leader;
        members = new HashMap<>();
    }

    public ArrayList<Player> getMembers() {
        return new ArrayList<>(members.keySet());
    }

    public void disband() {
        teams.remove(leader.getName());
        this.sendMessage("§c你所在的队伍已解散");
    }

    public String membersToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Player player : members.keySet()) {
            sb.append(" -").append(player.getName()).append(isReady(player) ? "(准备就绪)" : " (未准备好)");
        }
        return sb.toString();
    }

    public boolean hasMember(Player player) {
        return this.members.containsKey(player) || this.isLeader(player);
    }

    public boolean isMember(Player player) {
        return this.members.containsKey(player);
    }

    public boolean isReady(Player player) {
        Boolean b = members.get(player);
        if (b == null) {
            b = false;
        }
        return b;
    }

    public boolean isAllReady() {
        for (Boolean ready : members.values()) {
            if (!ready) {
                return false;
            }
        }
        return true;
    }

    public void ready(Player player) {
        ItemMap itemMap = getItemMapByPlayer(player);
        if (!itemMap.containsKey(Item.fromString(TMS_ID))) {
            player.sendMessage("§c你没有传送魔法卷轴，无法准备。");
            return;
        }
        Boolean b = members.get(player);
        members.put(player, !b);
        this.sendMessage(b ? String.format("§c玩家 %s 已取消准备", player.getName()) : String.format("§a玩家 %s 已准备就绪", player.getName()));
    }

    public void addMember(Player player) {
        members.put(player, false);
        this.sendMessage(String.format("§a玩家 %s 已加入队伍", player.getName()));
    }

    public void removeMember(Player player) {
        members.remove(player);
        this.sendMessage(String.format("§a玩家 %s 已离开队伍", player.getName()));
    }


    public boolean isLeader(Player player) {
        return leader.equals(player);
    }

    public static boolean isSameTeam(Player p1, Player p2) {
        Team team = getPlayerHasTeam(p1);
        return team != null && team.hasMember(p2);
    }

    public void sendMessage(String message) {
        this.leader.sendMessage(message);
        for (Player player : this.getMembers()) {
            if (!player.equals(this.leader)) {
                player.sendMessage(message);
            }
        }
    }

    public static Team getPlayerHasTeam(Player player) {
        for (String teamName : teams.keySet()) {
            if (teams.get(teamName).hasMember(player)) {
                return teams.get(teamName);
            }
        }
        return null;
    }
}
