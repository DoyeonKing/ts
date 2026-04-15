package com.example.springboot.service;

import com.example.springboot.dto.MockPlayProfile;
import com.example.springboot.dto.MockUserPreference;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Day3: 推荐系统 Mock 数据源
 * - 20 条剧目画像
 * - 50 条用户偏好记录
 */
@Service
public class MockRecommendationDataService {

    private static final int TARGET_PLAY_COUNT = 220;
    private static final int TARGET_USER_PREF_COUNT = 1200;
    private static final long DATA_SEED = 20260323L;

    private final List<MockPlayProfile> plays = new ArrayList<>();
    private final List<MockUserPreference> userPreferences = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (!plays.isEmpty() || !userPreferences.isEmpty()) {
            return;
        }
        initPlayProfiles();
        initUserPreferences();
    }

    public List<MockPlayProfile> getPlays() {
        return Collections.unmodifiableList(plays);
    }

    public List<MockUserPreference> getUserPreferences() {
        return Collections.unmodifiableList(userPreferences);
    }

    public Map<String, Object> getSummary() {
        Map<String, Long> genreDistribution = plays.stream()
                .collect(Collectors.groupingBy(MockPlayProfile::getGenre, LinkedHashMap::new, Collectors.counting()));

        Set<String> allTags = plays.stream()
                .flatMap(p -> p.getTags().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Map.of(
                "playCount", plays.size(),
                "userPreferenceCount", userPreferences.size(),
                "genres", genreDistribution,
                "tagCount", allTags.size(),
                "samplePlay", plays.get(0),
                "sampleUserPreference", userPreferences.get(0)
        );
    }

    private void initPlayProfiles() {
        plays.add(new MockPlayProfile(1L, "《哈姆雷特》", "戏剧", List.of("经典", "悲剧", "哲思"), "张文远", 9.2, 96, 180, 680));
        plays.add(new MockPlayProfile(2L, "《天鹅湖》", "舞剧", List.of("芭蕾", "浪漫", "古典"), "林雪宁", 9.0, 91, 220, 980));
        plays.add(new MockPlayProfile(3L, "《茶花女》", "歌剧", List.of("爱情", "抒情", "古典"), "王语安", 8.8, 85, 200, 880));
        plays.add(new MockPlayProfile(4L, "《罗密欧与朱丽叶》", "戏剧", List.of("爱情", "青春", "悲剧"), "陈子昂", 8.9, 89, 160, 620));
        plays.add(new MockPlayProfile(5L, "《雷雨》", "话剧", List.of("现实主义", "家庭", "冲突"), "周以诚", 9.1, 92, 120, 520));
        plays.add(new MockPlayProfile(6L, "《白蛇》", "国风舞剧", List.of("国风", "神话", "视觉"), "许清妍", 9.3, 97, 280, 1080));
        plays.add(new MockPlayProfile(7L, "《牡丹亭》", "昆曲", List.of("传统", "爱情", "意境"), "沈知夏", 8.7, 78, 100, 460));
        plays.add(new MockPlayProfile(8L, "《霸王别姬》", "京剧", List.of("传统", "经典", "悲剧美学"), "顾承泽", 9.4, 98, 260, 1180));
        plays.add(new MockPlayProfile(9L, "《猫》", "音乐剧", List.of("音乐剧", "活力", "舞台特效"), "韩若彤", 8.6, 82, 240, 980));
        plays.add(new MockPlayProfile(10L, "《剧院魅影》", "音乐剧", List.of("音乐剧", "爱情", "悬疑"), "陆景曜", 9.1, 94, 320, 1380));
        plays.add(new MockPlayProfile(11L, "《悲惨世界》", "音乐剧", List.of("史诗", "励志", "经典"), "谢书言", 9.0, 90, 300, 1280));
        plays.add(new MockPlayProfile(12L, "《李尔王》", "戏剧", List.of("莎士比亚", "权力", "悲剧"), "贺明川", 8.8, 83, 160, 640));
        plays.add(new MockPlayProfile(13L, "《仲夏夜之梦》", "戏剧", List.of("喜剧", "奇幻", "轻松"), "唐沐晴", 8.5, 75, 120, 520));
        plays.add(new MockPlayProfile(14L, "《胡桃夹子》", "芭蕾舞剧", List.of("节日", "梦幻", "亲子"), "苏以沫", 8.9, 87, 180, 860));
        plays.add(new MockPlayProfile(15L, "《茶馆》", "话剧", List.of("现实主义", "社会", "群像"), "方启明", 9.2, 93, 100, 480));
        plays.add(new MockPlayProfile(16L, "《巴黎圣母院》", "音乐剧", List.of("音乐剧", "史诗", "法语"), "邵霖", 8.7, 80, 280, 1180));
        plays.add(new MockPlayProfile(17L, "《红楼梦》", "舞剧", List.of("国风", "古典", "诗意"), "叶清歌", 9.0, 88, 240, 980));
        plays.add(new MockPlayProfile(18L, "《魔法学院》", "原创话剧", List.of("奇幻", "青春", "魔法"), "江屿", 8.4, 74, 130, 560));
        plays.add(new MockPlayProfile(19L, "《时空旅人》", "原创音乐剧", List.of("科幻", "冒险", "情感"), "周栩", 8.6, 79, 180, 760));
        plays.add(new MockPlayProfile(20L, "《长安十二时辰》", "沉浸式戏剧", List.of("沉浸式", "历史", "互动"), "秦观澜", 9.1, 95, 260, 1280));

        // 基于固定种子扩容，保证本地/队友机器上结果一致，便于联调。
        expandPlayProfiles(TARGET_PLAY_COUNT, new Random(DATA_SEED));
    }

    private void initUserPreferences() {
        String[] genrePool = plays.stream().map(MockPlayProfile::getGenre).distinct().toArray(String[]::new);
        String[] tagPool = plays.stream().flatMap(p -> p.getTags().stream()).distinct().toArray(String[]::new);
        String[] actorPool = plays.stream().map(MockPlayProfile::getLeadActor).distinct().toArray(String[]::new);

        Random random = new Random(DATA_SEED + 99);
        for (long i = 1; i <= TARGET_USER_PREF_COUNT; i++) {
            List<String> likedGenres = pickDistinct(genrePool, 2 + random.nextInt(2), random);
            List<String> likedTags = pickDistinct(tagPool, 3, random);
            String favoriteActor = actorPool[random.nextInt(actorPool.length)];
            int budgetMin = 80 + random.nextInt(120);
            int budgetMax = budgetMin + 300 + random.nextInt(700);
            List<Long> recent = pickDistinctPlayIds(3, random);

            userPreferences.add(new MockUserPreference(
                    i,
                    likedGenres,
                    likedTags,
                    favoriteActor,
                    budgetMin,
                    budgetMax,
                    recent
            ));
        }
    }

    private List<String> pickDistinct(String[] pool, int n, Random random) {
        List<String> all = new ArrayList<>(Arrays.asList(pool));
        Collections.shuffle(all, random);
        return all.subList(0, Math.min(n, all.size()));
    }

    private List<Long> pickDistinctPlayIds(int n, Random random) {
        List<Long> ids = plays.stream().map(MockPlayProfile::getPlayId).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(ids, random);
        return ids.subList(0, Math.min(n, ids.size()));
    }

    private void expandPlayProfiles(int targetCount, Random random) {
        if (plays.size() >= targetCount) return;

        String[] genrePool = {"戏剧", "话剧", "音乐剧", "舞剧", "歌剧", "沉浸式戏剧", "京剧", "昆曲", "儿童剧", "实验戏剧"};
        String[] tagPool = {
                "经典", "爱情", "奇幻", "悲剧", "国风", "视觉", "互动", "悬疑", "励志", "青春",
                "治愈", "科幻", "历史", "家庭", "现实主义", "诗意", "冒险", "热血", "成长", "轻喜剧"
        };
        String[] namePrefix = {"星河", "月影", "长夜", "黎明", "风暴", "微光", "镜花", "海岸", "焰火", "旅途"};
        String[] nameSuffix = {"计划", "物语", "之门", "纪事", "回声", "来信", "密语", "终章", "未眠夜", "记忆体"};

        long nextId = plays.stream().mapToLong(MockPlayProfile::getPlayId).max().orElse(0L) + 1;
        int actorCounter = 1;
        while (plays.size() < targetCount) {
            String genre = genrePool[random.nextInt(genrePool.length)];
            List<String> tags = pickDistinct(tagPool, 3, random);
            String playName = "《" + namePrefix[random.nextInt(namePrefix.length)] + nameSuffix[random.nextInt(nameSuffix.length)] + "·" + nextId + "》";
            String actor = "演员" + String.format("%03d", actorCounter++);

            double rating = 7.8 + random.nextDouble() * 1.8; // 7.8-9.6
            rating = Math.round(rating * 10.0) / 10.0;
            int heat = 65 + random.nextInt(36); // 65-100
            int priceMin = 80 + random.nextInt(280);
            int priceMax = priceMin + 260 + random.nextInt(980);

            plays.add(new MockPlayProfile(
                    nextId++,
                    playName,
                    genre,
                    tags,
                    actor,
                    rating,
                    heat,
                    priceMin,
                    priceMax
            ));
        }
    }
}
