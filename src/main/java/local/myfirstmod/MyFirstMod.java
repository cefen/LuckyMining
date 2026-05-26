package local.myfirstmod;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyFirstMod implements ModInitializer {
	public static final String MOD_ID = "myfirstmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    
    private record RewardConfig(net.minecraft.world.item.Item item, float chance) {}
    private static final java.util.Map<net.minecraft.world.level.block.Block, RewardConfig> ORE_REWARDS = java.util.Map.ofEntries(
        // --- 钻石类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DIAMOND_ORE, new RewardConfig(net.minecraft.world.item.Items.DIAMOND_BLOCK, 0.01f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE, new RewardConfig(net.minecraft.world.item.Items.DIAMOND_BLOCK, 0.01f)),
        
        // --- 绿宝石类 (中奖奖励为对应的方块本身) ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.EMERALD_ORE, new RewardConfig(net.minecraft.world.item.Items.EMERALD_ORE, 0.01f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE, new RewardConfig(net.minecraft.world.item.Items.DEEPSLATE_EMERALD_ORE, 0.01f)),
        
        // --- 红石类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.REDSTONE_ORE, new RewardConfig(net.minecraft.world.item.Items.REDSTONE_BLOCK, 0.001f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_REDSTONE_ORE, new RewardConfig(net.minecraft.world.item.Items.REDSTONE_BLOCK, 0.001f)),
        
        // --- 铁矿类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.IRON_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_IRON_BLOCK, 0.01f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_IRON_BLOCK, 0.01f)),
        
        // --- 金矿类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.GOLD_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_GOLD_BLOCK, 0.01f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_GOLD_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_GOLD_BLOCK, 0.01f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.NETHER_GOLD_ORE, new RewardConfig(net.minecraft.world.item.Items.GOLD_BLOCK, 0.001f)),
        
        // --- 铜矿类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.COPPER_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_COPPER_BLOCK, 0.001f)),
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE, new RewardConfig(net.minecraft.world.item.Items.RAW_COPPER_BLOCK, 0.001f)),
        
        // --- 下界石英类 ---
        java.util.Map.entry(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE, new RewardConfig(net.minecraft.world.item.Items.QUARTZ_BLOCK, 0.001f))
    );

    private void spawnLuckyEffects(net.minecraft.server.level.ServerLevel serverWorld, net.minecraft.core.BlockPos pos) {
        // 提醒玩家中奖的辅助函数
        // 🌌 发送粒子效果 (龙息效果)
        serverWorld.sendParticles(
            net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            30,
            0.3, 0.3, 0.3,
            0.5
        );

        // 🔊 播放音效 (挑战完成)
        serverWorld.playSound(
            null,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            net.minecraft.sounds.SoundSource.BLOCKS,
            1.0f,
            1.0f
        );
    }


	@Override
    public void onInitialize() {
        LOGGER.info("我的第一个模组已成功加载！");
    
        // 监听玩家加入
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { // （网络句柄、发送器、服务器实例）
             
            net.minecraft.server.level.ServerPlayer player = handler.getPlayer();
            String playerName = player.getName().getString();

            LOGGER.info("player " + playerName + " join in the server secretly!");
            
            //    player.sendMessage(net.minecraft.network.chat.Component.literal("§a[系统] 欢迎来到这个世界，" + playerName + "！"), false);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[系统] 欢迎来到这个世界，" + playerName + "！"));

            // 创建一个包含 1 个钻石的物品堆叠
            // new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND);
            // player.getInventory().add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));

        });

        // 监听玩家破坏方块
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            RewardConfig config = ORE_REWARDS.get(state.getBlock());
            if (config != null) { // 如果在奖励清单上找得到
                
                // 1. 获取玩家手上拿着的工具
                net.minecraft.world.item.ItemStack handItem = player.getMainHandItem();

                // 2. 检查这个工具是否带有精准采集附魔
                // 在 1.21.4 Mojang 映射表下，通过 RegistryLookup 获取附魔的 Key
                var enchantsLookup = world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> silkTouch = enchantsLookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);

                // 3. 判断如果【没有】精准采集，才给奖励
                if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(silkTouch, handItem) == 0) {
                    
                    // 如果挖到的是矿石，在矿石的位置按照概率随机生成一个掉落物
                    // 在 Java 的 record 语法中，括号里定义的变量名字叫什么，后面就用什么名字加括号来读取它。
                    if (world.getRandom().nextFloat() < config.chance()) {
                        // 准备一个“物品堆叠” ItemStack
                        net.minecraft.world.item.ItemStack gift = new net.minecraft.world.item.ItemStack(config.item());

                        // 在方块所处位置创建以一个掉落物实体 并召唤到世界中
                        // Level 代表的是维度世界， 包括："Overworld", "The Nether", "The End"
                        // 这里的world是监听得到的参数, 但底层类型不是Level, 要进行强制转换
                        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            (net.minecraft.world.level.Level) world, 
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, // 加上 0.5 是为了让物品从方块中心蹦出来
                            gift
                        );

                        // 让世界生成这个实体
                        world.addFreshEntity(itemEntity);

                        // 给玩家中奖提示
                        // 强制转换成服务器世界
                        net.minecraft.server.level.ServerLevel serverWorld = (net.minecraft.server.level.ServerLevel) world;
                        this.spawnLuckyEffects(serverWorld, pos);

                    }
                } else {
                    // 也可以温馨提示一下玩家
                    
                    // 强制类型转换，ServerPlayer类型才有sendSystemMessage方法
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        // 避免是假人，假人不是ServerPlayer类型，会报错崩溃
                        serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[系统] 带有精准采集的工具无法触发幸运掉落！"));
                    }
                }
                
            }

            return true; // true表示允许玩家正常挖掉这个方块；返回false会让方块变为无敌的
        });

        // 监听指令注册事件
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // dispatcher (调度器): 指令发布官
            // registryAccess : 用来访问游戏的注册表（比如获取方块、物品的数据）
            // environment：用来判断当前指令是在单人世界还是专属服务器运行。

            // 登记指令的固定开头
            dispatcher.register(net.minecraft.commands.Commands.literal("tpto")
                // 后面拼接指令的逻辑
                // 权限等级：0~4
                .requires(source -> source.hasPermission(0)) // 0权限是最普通的玩家
                .then(net.minecraft.commands.Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.player())
                    .executes(context -> {
                        // 接下来我们要在这里写执行传送的具体逻辑
                        
                        net.minecraft.server.level.ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
                        net.minecraft.server.level.ServerPlayer targetPlayer = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "target");

                        net.minecraft.world.level.portal.TeleportTransition target_transition = new net.minecraft.world.level.portal.TeleportTransition(
                            targetPlayer.serverLevel(), 
                            targetPlayer.position(), 
                            net.minecraft.world.phys.Vec3.ZERO, 
                            sourcePlayer.getYRot(), sourcePlayer.getXRot(), 
                            net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
                        );

                        sourcePlayer.teleport(target_transition);

                        return 1;
                    })
                )
            );

            // 暂时不实现：将别人tp到自己
            // dispatcher.register(net.minecraft.commands.Commands.literal("tphere") 
            //     // 后面拼接指令的逻辑
            //     // 权限等级：0~4
            //     .requires(source -> source.hasPermission(0)) // 0权限是最普通的玩家
            //     .then(net.minecraft.commands.Commands.argument("target", net.minecraft.commands.arguments.EntityArgument.player())
            //         .executes(context -> {
            //             // 接下来我们要在这里写执行传送的具体逻辑
                        
            //             net.minecraft.server.level.ServerPlayer sourcePlayer = context.getSource().getPlayer(); // 写指令的人
            //             net.minecraft.server.level.ServerPlayer targetPlayer = net.minecraft.commands.arguments.EntityArgument.getPlayer(context, "target");

            //             net.minecraft.world.level.portal.TeleportTransition target_transition = new net.minecraft.world.level.portal.TeleportTransition(
            //                 sourcePlayer.serverLevel(), 
            //                 sourcePlayer.position(), 
            //                 net.minecraft.world.phys.Vec3.ZERO, 
            //                 targetPlayer.getYRot(), targetPlayer.getXRot(), 
            //                 net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
            //             );

            //             targetPlayer.teleport(target_transition);

            //             return 1;
            //         })
            //     )
            // );




        });

    }

    
}
