package com.github.timmyovo.pixelbedwars.utils;

import org.bukkit.command.CommandSender;

public class CommandsHelp {
    public static void help(CommandSender commandSender) {
        commandSender.sendMessage("/pb sws/setWorldSpawn - 设置玩家重生等待地点");
        commandSender.sendMessage("/pb dm/deathMatch - 手动启用绝杀模式末影龙");
        commandSender.sendMessage("/pb srrs/setResourceRefreshSpeed [资源名称] [资源刷新乘数] - 设置全局资源刷新点速率乘数");
        commandSender.sendMessage("/pb strrs/setTeamResourceRefreshSpeed <队伍名称(可选填,不填应用到全部队伍)> [资源名称] [资源刷新乘数] - 设置资源刷新队伍速率乘数");
        commandSender.sendMessage("/pb destroyAllBed/dab - 强制自毁所有床.");
        commandSender.sendMessage("/pb setWaitLoc/swl - 设置等待地点.");
        commandSender.sendMessage("/pb setTeamGameLoc/stgl [队伍名称] - 设置队伍重生基地地点");
        commandSender.sendMessage("/pb addTeam/at [队伍名字] [队伍颜色] [最小人数] [最大人数] [队伍颜色] - 添加一个队伍");
        commandSender.sendMessage("/pb setTeamBed/stb [队伍名称] - 设置队伍床");
        commandSender.sendMessage("/pb addSpawner/as [资源名称] [刷新间隔(秒)] - 添加一个全局资源刷新点");
        commandSender.sendMessage("/pb addSpawnerTeam/ast [队伍名称] [资源名称] [刷新间隔(秒)] - 添加一个队伍资源刷新点");
        commandSender.sendMessage("/pb si/saveitem - 输出手上物品配置文具");
        commandSender.sendMessage("/pb aps/addPlayerShop - 添加玩家购买商店");
        commandSender.sendMessage("/pb ats/addTeamShop - 添加队伍商店");
        commandSender.sendMessage("/pb setShopItemIcon 分类ID 格子ID - 设置玩家购买商店展示物品为手上物品");
        commandSender.sendMessage("/pb setShopItemRequire 分类ID 格子ID - 设置玩家购买商店购买所需物品为手上物品");
        commandSender.sendMessage("/pb setShopItem 分类ID 格子ID - 设置玩家购买商店购买该物品后所得到的物品为手上物品");
        commandSender.sendMessage("/pb startGame - 强制开始游戏");
        commandSender.sendMessage("/pb trap - 强制开启陷阱商店");
    }
}
