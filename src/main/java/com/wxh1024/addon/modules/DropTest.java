package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DropTest extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> targetItems = sgGeneral.add(new StringListSetting.Builder()
            .name("追踪物品")
            .description("要追踪的物品列表（如 'minecraft:iron_ingot', 'gold_ingot'）。留空则不追踪任何物品，除非开启'追踪所有物品'。")
            .defaultValue(List.of("minecraft:iron_ingot"))
            .build()
    );

    private final Setting<Integer> testDuration = sgGeneral.add(new IntSetting.Builder()
            .name("测试时长(分钟)")
            .description("测试持续时长（分钟）")
            .defaultValue(5)
            .min(1)
            .sliderMax(120)
            .build()
    );

    private final Setting<Boolean> trackAllItems = sgGeneral.add(new BoolSetting.Builder()
            .name("追踪所有物品")
            .description("追踪所有物品类型，而非仅追踪目标物品")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> scanInterval = sgGeneral.add(new IntSetting.Builder()
            .name("扫描间隔")
            .description("实体扫描间隔（tick），值越大性能越好")
            .defaultValue(5)
            .min(1)
            .sliderMax(20)
            .build()
    );

    private final Setting<Boolean> verboseLogging = sgGeneral.add(new BoolSetting.Builder()
            .name("详细日志")
            .description("测试过程中输出详细信息")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> itemStackSize = sgGeneral.add(new IntSetting.Builder()
            .name("物品堆叠上限")
            .description("追踪物品的最大堆叠数（1, 16, 或 64）")
            .defaultValue(64)
            .min(1)
            .max(64)
            .sliderMin(1)
            .sliderMax(64)
            .build()
    );

    private final Setting<Boolean> clearLogsButton = sgGeneral.add(new BoolSetting.Builder()
            .name("清除日志")
            .description("点击清除 DropTest.txt 中的所有历史日志")
            .defaultValue(false)
            .onChanged(value -> {
                if (value) {
                    clearLogFile();
                }
            })
            .build()
    );

    private volatile boolean testActive = false;
    private volatile long testStartTime;
    private volatile long testEndTime;
    private final Map<String, Integer> itemCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> trackedEntityStacks = new ConcurrentHashMap<>();
    private final Set<UUID> trackedEntities = ConcurrentHashMap.newKeySet();
    private volatile int totalTicks = 0;
    private volatile int checksPerformed = 0;
    private int tickCounter = 0;
    private int scanCounter = 0;

    private final DecimalFormat df2 = new DecimalFormat("0.00");
    private final DecimalFormat df1 = new DecimalFormat("0.0");

    public DropTest() {
        super(Wxh1024Addon.WXH1024, "掉落测试", "追踪实体掉落物，统计掉落率并输出到文件。");
    }

    @Override
    public void onActivate() {
        startTest();
    }

    @Override
    public void onDeactivate() {
        if (testActive) {
            endTest();
        }
    }

    private void startTest() {
        testActive = true;
        testStartTime = System.currentTimeMillis();
        itemCounts.clear();
        trackedEntities.clear();
        trackedEntityStacks.clear();
        totalTicks = 0;
        checksPerformed = 0;
        tickCounter = 0;
        scanCounter = 0;

        info("掉落测试已开始，持续 " + testDuration.get() + " 分钟");
        if (!targetItems.get().isEmpty()) {
            info("追踪物品: " + String.join(", ", targetItems.get()));
        }
        info("扫描间隔: " + scanInterval.get() + " tick");
        if (trackAllItems.get()) {
            info("正在追踪渲染距离内所有物品");
        }
    }

    private void endTest() {
        testActive = false;
        testEndTime = System.currentTimeMillis();

        info("掉落测试已完成！");

        calculateAndDisplayStats();

        info("正在保存结果...");
        saveTestResults();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!testActive) return;

        if (mc == null || mc.world == null || mc.player == null) {
            return;
        }

        totalTicks++;
        tickCounter++;
        scanCounter++;

        long elapsedMillis = System.currentTimeMillis() - testStartTime;
        long targetMillis = testDuration.get() * 60L * 1000L;

        if (elapsedMillis >= targetMillis) {
            toggle();
            return;
        }

        if (verboseLogging.get() && tickCounter >= 20) {
            tickCounter = 0;
            double elapsedMinutes = elapsedMillis / 60000.0;
            int totalItems = getTotalItemCount();
            info("进度: " + df1.format(elapsedMinutes) + " / " + testDuration.get() + " 分钟 | 实体: " + trackedEntities.size() + " | 物品: " + totalItems);
        }

        if (scanCounter < scanInterval.get()) {
            return;
        }

        scanCounter = 0;
        checksPerformed++;

        try {
            int renderDistance = mc.options.getViewDistance().getValue() * 16;

            List<ItemEntity> entities = mc.world.getEntitiesByClass(
                    ItemEntity.class,
                    mc.player.getBoundingBox().expand(renderDistance),
                    entity -> entity != null && !entity.isRemoved()
            );

            for (ItemEntity itemEntity : entities) {
                try {
                    if (itemEntity == null) continue;

                    UUID entityId = itemEntity.getUuid();
                    if (entityId == null) continue;

                    if (trackedEntities.contains(entityId)) {
                        continue;
                    }

                    ItemStack stack = itemEntity.getStack();
                    if (stack == null || stack.isEmpty()) continue;

                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    if (itemId == null) continue;

                    boolean shouldTrack = trackAllItems.get();

                    if (!shouldTrack && !targetItems.get().isEmpty()) {
                        String normalizedItemId = itemId;
                        for (String targetItem : targetItems.get()) {
                            if (normalizedItemId.equals(normalizeItemId(targetItem))) {
                                shouldTrack = true;
                                break;
                            }
                        }
                    }

                    if (shouldTrack) {
                        if (trackedEntities.contains(entityId)) {
                            Integer previousCount = trackedEntityStacks.get(entityId);
                            if (previousCount != null && stack.getCount() > previousCount) {
                                int addedItems = stack.getCount() - previousCount;
                                itemCounts.merge(itemId, addedItems, Integer::sum);
                                trackedEntityStacks.put(entityId, stack.getCount());

                                if (verboseLogging.get()) {
                                    info("检测到堆叠合并: " + addedItems + "x " + itemId + " (新总数: " + stack.getCount() + ")");
                                }
                            }
                        } else {
                            trackedEntities.add(entityId);
                            trackedEntityStacks.put(entityId, stack.getCount());
                            itemCounts.merge(itemId, stack.getCount(), Integer::sum);

                            if (verboseLogging.get()) {
                                info("发现: " + stack.getCount() + "x " + itemId);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (verboseLogging.get()) {
                        warning("处理实体时出错: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            if (verboseLogging.get()) {
                warning("实体扫描时出错: " + e.getMessage());
            }
        }
    }

    private String normalizeItemId(String id) {
        if (id == null || id.isEmpty()) {
            return "minecraft:air";
        }
        if (!id.contains(":")) {
            return "minecraft:" + id;
        }
        return id;
    }

    private int getTotalItemCount() {
        try {
            return itemCounts.values().stream().mapToInt(Integer::intValue).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    private void calculateAndDisplayStats() {
        try {
            long durationMillis = testEndTime - testStartTime;
            double durationMinutes = durationMillis / 60000.0;
            double durationSeconds = durationMillis / 1000.0;

            info("=== 掉落测试结果 ===");
            info("时长: " + df2.format(durationMinutes) + "分 (" + df1.format(durationSeconds) + "秒) | Tick: " + totalTicks + " | 扫描: " + checksPerformed);
            info("发现实体: " + trackedEntities.size());

            if (itemCounts.isEmpty()) {
                warning("测试期间未发现任何物品！");
                return;
            }

            List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemCounts.entrySet());
            sortedItems.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            int totalItems = getTotalItemCount();

            info("");
            info("=== 物品统计 ===");
            for (Map.Entry<String, Integer> entry : sortedItems) {
                String itemId = entry.getKey();
                int count = entry.getValue();

                double perSecond = count / durationSeconds;
                double perMinute = count / durationMinutes;
                double perHour = perMinute * 60;
                double percentage = (count * 100.0) / totalItems;

                info(itemId + ": " + count + " 个 (" + df1.format(percentage) + "%)");
                info("  " + df2.format(perSecond) + "/秒 | " + df2.format(perMinute) + "/分 | " + df2.format(perHour) + "/时");
            }

            info("");
            info("=== 总览 ===");
            info("总计: " + totalItems + " 个物品 | " + itemCounts.size() + " 种类型");
            double itemsPerMinute = totalItems / durationMinutes;
            double itemsPerHour = itemsPerMinute * 60;
            info("速率: " + df2.format(totalItems / durationSeconds) + "/秒 | " + df2.format(itemsPerMinute) + "/分 | " + df2.format(itemsPerHour) + "/时");

            info("");
            info("=== 潜影盒计算 (堆叠: " + itemStackSize.get() + ") ===");
            int stackSize = itemStackSize.get();
            int itemsPerShulker = 27 * stackSize;
            int itemsPerDub = 54 * itemsPerShulker;

            if (itemsPerMinute > 0) {
                double minutesPerShulker = itemsPerShulker / itemsPerMinute;
                double hoursPerShulker = minutesPerShulker / 60;
                double hoursPerDub = (itemsPerDub / itemsPerMinute) / 60;
                double daysPerDub = hoursPerDub / 24;

                info("潜影盒 (" + itemsPerShulker + " 个): " + df2.format(minutesPerShulker) + "分 (" + df2.format(hoursPerShulker) + "时)");
                info("大箱子 (" + itemsPerDub + " 个): " + df2.format(hoursPerDub) + "时 (" + df2.format(daysPerDub) + " 天)");

                double shulkersPerHour = 60 / minutesPerShulker;
                double shulkersPerDay = shulkersPerHour * 24;
                double dubsPerDay = 24 / hoursPerDub;

                info("产量: " + df2.format(shulkersPerHour) + " 盒/时 | " + df2.format(shulkersPerDay) + " 盒/天 | " + df2.format(dubsPerDay) + " 大箱子/天");
            } else {
                info("数据不足，无法计算产量");
            }

            if (sortedItems.size() > 1) {
                Map.Entry<String, Integer> mostCommon = sortedItems.get(0);
                Map.Entry<String, Integer> leastCommon = sortedItems.get(sortedItems.size() - 1);
                info("");
                info("=== 分布 ===");
                info("最多: " + mostCommon.getKey() + " (" + mostCommon.getValue() + ") | 最少: " + leastCommon.getKey() + " (" + leastCommon.getValue() + ")");
                info("稀有度比例: " + df2.format((double) mostCommon.getValue() / leastCommon.getValue()) + ":1 | 平均每类: " + df2.format((double) totalItems / itemCounts.size()));
            }
        } catch (Exception e) {
            error("计算统计数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearLogFile() {
        new Thread(() -> {
            try {
                String appData = System.getenv("APPDATA");
                if (appData == null) {
                    String userHome = System.getProperty("user.home");
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("mac")) {
                        appData = userHome + "/Library/Application Support";
                    } else if (os.contains("linux")) {
                        appData = userHome + "/.local/share";
                    } else {
                        appData = userHome;
                    }
                }

                File baseDir = new File(appData, ".minecraft" + File.separator + "meteor-client" + File.separator + "wxh1024-addon");
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }

                File resultsFile = new File(baseDir, "DropTest.txt");
                if (resultsFile.exists()) {
                    if (resultsFile.delete()) {
                        info("已清除历史日志文件");
                    } else {
                        warning("清除日志文件失败");
                    }
                } else {
                    info("没有可清除的历史日志");
                }

                try {
                    Thread.sleep(100);
                    clearLogsButton.set(false);
                } catch (InterruptedException e) {
                    // Ignore
                }

            } catch (Exception e) {
                error("清除日志文件时出错: " + e.getMessage());
                try {
                    Thread.sleep(100);
                    clearLogsButton.set(false);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
        }, "DropTest-LogClearer").start();
    }

    private void saveTestResults() {
        final Map<String, Integer> itemCountsCopy = new HashMap<>(itemCounts);
        final int trackedEntitiesCount = trackedEntities.size();
        final long startTime = testStartTime;
        final long endTime = testEndTime;
        final int ticks = totalTicks;
        final int checks = checksPerformed;
        final List<String> targets = new ArrayList<>(targetItems.get());
        final boolean trackAll = trackAllItems.get();
        final int stackSize = itemStackSize.get();

        Thread saveThread = new Thread(() -> {
            BufferedWriter writer = null;
            try {
                String appData = System.getenv("APPDATA");
                if (appData == null) {
                    String userHome = System.getProperty("user.home");
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("mac")) {
                        appData = userHome + "/Library/Application Support";
                    } else if (os.contains("linux")) {
                        appData = userHome + "/.local/share";
                    } else {
                        appData = userHome;
                    }
                }

                File baseDir = new File(appData, ".minecraft" + File.separator + "meteor-client" + File.separator + "wxh1024-addon");
                if (!baseDir.exists()) {
                    baseDir.mkdirs();
                }

                File resultsFile = new File(baseDir, "DropTest.txt");
                writer = new BufferedWriter(new FileWriter(resultsFile, true));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DecimalFormat df2 = new DecimalFormat("0.00");
                DecimalFormat df1 = new DecimalFormat("0.0");

                writer.write("\n");
                writer.write("=================================================================\n");
                writer.write("                    掉落测试结果\n");
                writer.write("=================================================================\n\n");

                // 测试配置
                writer.write("测试配置:\n");
                writer.write("-----------------------------------------------------------------\n");
                if (!targets.isEmpty()) {
                    writer.write("  追踪物品:\n");
                    for (String target : targets) {
                        writer.write("    - " + target + "\n");
                    }
                } else {
                    writer.write("  追踪物品: 未指定\n");
                }
                writer.write("  追踪所有物品: " + (trackAll ? "是" : "否") + "\n");
                writer.write("  设定时长: " + testDuration.get() + " 分钟\n");
                writer.write("  扫描间隔: " + scanInterval.get() + " tick\n");
                writer.write("  物品堆叠上限: " + stackSize + "\n");
                writer.write("\n");

                // 测试时间线
                writer.write("测试时间线:\n");
                writer.write("-----------------------------------------------------------------\n");
                writer.write("  开始时间: " + dateFormat.format(new Date(startTime)) + "\n");
                writer.write("  结束时间: " + dateFormat.format(new Date(endTime)) + "\n");

                long durationMillis = endTime - startTime;
                double durationMinutes = durationMillis / 60000.0;
                double durationSeconds = durationMillis / 1000.0;

                writer.write("  实际时长: " + df2.format(durationMinutes) + " 分钟 (" + df1.format(durationSeconds) + " 秒)\n");
                writer.write("\n");

                // 测试指标
                writer.write("测试指标:\n");
                writer.write("-----------------------------------------------------------------\n");
                writer.write("  总 Tick 数: " + ticks + "\n");
                writer.write("  扫描次数: " + checks + "\n");
                writer.write("  发现独立实体: " + trackedEntitiesCount + "\n");
                writer.write("\n");

                writer.write("=================================================================\n");
                writer.write("                    物品掉落统计\n");
                writer.write("=================================================================\n\n");

                if (itemCountsCopy.isEmpty()) {
                    writer.write("本次测试未发现任何物品。\n");
                } else {
                    List<Map.Entry<String, Integer>> sortedItems = new ArrayList<>(itemCountsCopy.entrySet());
                    sortedItems.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                    int totalItems = 0;
                    for (Integer count : itemCountsCopy.values()) {
                        totalItems += count;
                    }

                    int rank = 1;
                    for (Map.Entry<String, Integer> entry : sortedItems) {
                        String itemId = entry.getKey();
                        int count = entry.getValue();

                        double perSecond = count / durationSeconds;
                        double perMinute = count / durationMinutes;
                        double perHour = perMinute * 60;
                        double percentage = (count * 100.0) / totalItems;

                        writer.write("#" + rank + " - " + itemId + "\n");
                        writer.write("-----------------------------------------------------------------\n");
                        writer.write("  总数: " + count + " 个\n");
                        writer.write("  占比: " + df2.format(percentage) + "%\n");
                        writer.write("  掉落速率 (每秒): " + df2.format(perSecond) + " 个/秒\n");
                        writer.write("  掉落速率 (每分钟): " + df2.format(perMinute) + " 个/分\n");
                        writer.write("  掉落速率 (每小时): " + df2.format(perHour) + " 个/时\n");
                        writer.write("\n");
                        rank++;
                    }

                    writer.write("=================================================================\n");
                    writer.write("                          总览\n");
                    writer.write("=================================================================\n\n");
                    writer.write("  收集物品总数: " + totalItems + " 个\n");
                    writer.write("  每秒: " + df2.format(totalItems / durationSeconds) + " 个\n");
                    writer.write("  每分: " + df2.format(totalItems / durationMinutes) + " 个\n");
                    writer.write("  每时: " + df2.format((totalItems / durationMinutes) * 60) + " 个\n");
                    writer.write("  物品类型数: " + itemCountsCopy.size() + " 种\n");
                    writer.write("\n");

                    // 潜影盒计算
                    int itemsPerShulker = 27 * stackSize;
                    int itemsPerDoubleChest = 54 * itemsPerShulker;

                    double itemsPerMinute = totalItems / durationMinutes;
                    double itemsPerHour = itemsPerMinute * 60;

                    if (itemsPerMinute > 0) {
                        double minutesPerShulker = itemsPerShulker / itemsPerMinute;
                        double hoursPerShulker = minutesPerShulker / 60;
                        double minutesPerDub = itemsPerDoubleChest / itemsPerMinute;
                        double hoursPerDub = minutesPerDub / 60;
                        double daysPerDub = hoursPerDub / 24;

                        double shulkersPerHour = 60 / minutesPerShulker;
                        double shulkersPerDay = shulkersPerHour * 24;
                        double dubsPerDay = 24 / hoursPerDub;

                        writer.write("=================================================================\n");
                        writer.write("                    潜影盒计算\n");
                        writer.write("=================================================================\n\n");
                        writer.write("存储容量:\n");
                        writer.write("-----------------------------------------------------------------\n");
                        writer.write("  堆叠上限: " + stackSize + " 个/格\n");
                        writer.write("  每潜影盒: " + itemsPerShulker + " 个 (27 格)\n");
                        writer.write("  每大箱子: " + itemsPerDoubleChest + " 个 (54 个潜影盒)\n");
                        writer.write("\n");

                        writer.write("填满所需时间:\n");
                        writer.write("-----------------------------------------------------------------\n");
                        writer.write("  一个潜影盒:\n");
                        writer.write("    " + df2.format(minutesPerShulker) + " 分钟\n");
                        writer.write("    " + df2.format(hoursPerShulker) + " 小时\n");
                        writer.write("\n");
                        writer.write("  一个大箱子:\n");
                        writer.write("    " + df2.format(minutesPerDub) + " 分钟\n");
                        writer.write("    " + df2.format(hoursPerDub) + " 小时\n");
                        writer.write("    " + df2.format(daysPerDub) + " 天\n");
                        writer.write("\n");

                        writer.write("产量:\n");
                        writer.write("-----------------------------------------------------------------\n");
                        writer.write("  潜影盒/小时: " + df2.format(shulkersPerHour) + "\n");
                        writer.write("  潜影盒/天: " + df2.format(shulkersPerDay) + "\n");
                        writer.write("  大箱子/天: " + df2.format(dubsPerDay) + "\n");
                        writer.write("\n");

                        writer.write("批量存储估算:\n");
                        writer.write("-----------------------------------------------------------------\n");
                        writer.write("  1 小时: " + df2.format(shulkersPerHour) + " 盒 (" + df2.format(itemsPerHour) + " 个)\n");
                        writer.write("  24 小时: " + df2.format(shulkersPerDay) + " 盒 (" + df2.format(itemsPerHour * 24) + " 个)\n");
                        writer.write("  7 天: " + df2.format(shulkersPerDay * 7) + " 盒 (" + df2.format(itemsPerHour * 24 * 7) + " 个)\n");
                        writer.write("  30 天: " + df2.format(shulkersPerDay * 30) + " 盒 (" + df2.format(itemsPerHour * 24 * 30) + " 个)\n");
                        writer.write("\n");
                    } else {
                        writer.write("=================================================================\n");
                        writer.write("                    潜影盒计算\n");
                        writer.write("=================================================================\n\n");
                        writer.write("数据不足，无法计算潜影盒产量。\n\n");
                    }

                    // 分布分析
                    if (sortedItems.size() > 1) {
                        Map.Entry<String, Integer> mostCommon = sortedItems.get(0);
                        Map.Entry<String, Integer> leastCommon = sortedItems.get(sortedItems.size() - 1);

                        writer.write("=================================================================\n");
                        writer.write("                     分布分析\n");
                        writer.write("=================================================================\n\n");
                        writer.write("  最常见物品: " + mostCommon.getKey() + "\n");
                        writer.write("    数量: " + mostCommon.getValue() + " 个\n");
                        writer.write("    占比: " + df2.format((mostCommon.getValue() * 100.0) / totalItems) + "%\n");
                        writer.write("\n");
                        writer.write("  最稀有物品: " + leastCommon.getKey() + "\n");
                        writer.write("    数量: " + leastCommon.getValue() + " 个\n");
                        writer.write("    占比: " + df2.format((leastCommon.getValue() * 100.0) / totalItems) + "%\n");
                        writer.write("\n");
                        writer.write("  稀有度比例: " + df2.format((double) mostCommon.getValue() / leastCommon.getValue()) + ":1\n");
                        writer.write("  平均每类: " + df2.format((double) totalItems / itemCountsCopy.size()) + " 个\n");
                        writer.write("\n");
                    }

                    // 性能指标
                    writer.write("=================================================================\n");
                    writer.write("                     性能指标\n");
                    writer.write("=================================================================\n\n");
                    writer.write("  发现实体总数: " + trackedEntitiesCount + "\n");
                    writer.write("  总扫描次数: " + checks + " 次\n");
                    writer.write("  每秒扫描: " + df2.format((double) checks / durationSeconds) + " 次\n");
                    writer.write("  每次扫描发现物品: " + df2.format((double) totalItems / checks) + " 个\n");
                    writer.write("  总 Tick 数: " + ticks + "\n");
                    writer.write("\n");
                }

                writer.write("=================================================================\n");
                writer.write("                      测试结束\n");
                writer.write("=================================================================\n\n\n");

                writer.flush();

                info("测试结果已保存！");
                info("位置: " + resultsFile.getAbsolutePath());

            } catch (Exception e) {
                error("保存测试结果失败: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }, "DropTest-FileSaver");

        saveThread.setDaemon(true);
        saveThread.start();
    }
}
