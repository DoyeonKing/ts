// Neo4j 扩展版知识图谱导入脚本
// 使用方式：在 Neo4j Browser 中直接执行整个文件内容

MATCH (n) DETACH DELETE n;

CREATE
(:Play {name:'哈姆雷特', description:'莎士比亚四大悲剧之一。', genre:'悲剧', rating:9.5}),
(:Play {name:'李尔王', description:'权力、亲情与背叛。', genre:'悲剧', rating:9.1}),
(:Play {name:'麦克白', description:'野心与预言的悲剧。', genre:'悲剧', rating:9.2}),
(:Play {name:'奥赛罗', description:'嫉妒引发的毁灭。', genre:'悲剧', rating:9.0}),
(:Play {name:'罗密欧与朱丽叶', description:'经典爱情悲剧。', genre:'爱情悲剧', rating:9.3}),
(:Play {name:'仲夏夜之梦', description:'奇幻浪漫喜剧。', genre:'喜剧', rating:8.8}),
(:Play {name:'茶馆', description:'老舍三幕话剧代表作。', genre:'话剧', rating:9.6}),
(:Play {name:'雷雨', description:'曹禺现实主义话剧经典。', genre:'话剧', rating:9.4}),
(:Play {name:'天下第一楼', description:'老字号兴衰与群像人生。', genre:'京味话剧', rating:9.2}),
(:Play {name:'天鹅湖', description:'古典芭蕾代表作。', genre:'芭蕾', rating:9.0}),
(:Play {name:'茶花女', description:'威尔第经典歌剧。', genre:'歌剧', rating:9.4}),
(:Play {name:'歌剧魅影', description:'全球知名音乐剧。', genre:'音乐剧', rating:9.5}),
(:Play {name:'悲惨世界', description:'改编自雨果名著的音乐剧。', genre:'音乐剧', rating:9.6}),
(:Actor {name:'濮存昕', specialty:'话剧、莎剧'}),
(:Actor {name:'何冰', specialty:'话剧、京味戏'}),
(:Actor {name:'袁泉', specialty:'话剧、音乐剧'}),
(:Actor {name:'冯远征', specialty:'现实主义话剧'}),
(:Actor {name:'廖昌永', specialty:'歌剧'}),
(:Actor {name:'和慧', specialty:'歌剧'}),
(:Tag {name:'莎士比亚'}),
(:Tag {name:'曹禺'}),
(:Tag {name:'老舍'}),
(:Tag {name:'悲剧'}),
(:Tag {name:'喜剧'}),
(:Tag {name:'爱情'}),
(:Tag {name:'经典'}),
(:Tag {name:'京味'}),
(:Tag {name:'芭蕾'}),
(:Tag {name:'歌剧'}),
(:Tag {name:'音乐剧'}),
(:Terminology {name:'独白'}),
(:Terminology {name:'旁白'}),
(:Terminology {name:'台步'}),
(:Terminology {name:'谢幕'}),
(:Terminology {name:'幕间'}),
(:Terminology {name:'返场'}),
(:Terminology {name:'卡司'}),
(:Terminology {name:'首演'}),
(:Terminology {name:'末场'}),
(:Terminology {name:'走位'}),
(:Terminology {name:'调度'}),
(:Terminology {name:'咏叹调'}),
(:Terminology {name:'宣叙调'}),
(:Terminology {name:'足尖'}),
(:Terminology {name:'双人舞'}),
(:Venue {name:'国家大剧院', city:'北京'}),
(:Venue {name:'北京人民艺术剧院', city:'北京'}),
(:Venue {name:'上海大剧院', city:'上海'}),
(:Venue {name:'中央歌剧院', city:'北京'}),
(:Venue {name:'天桥艺术中心', city:'北京'});

MATCH (a:Actor {name:'濮存昕'}),(p:Play {name:'哈姆雷特'}) CREATE (a)-[:PERFORMS_IN {label:'饰 哈姆雷特'}]->(p);
MATCH (a:Actor {name:'濮存昕'}),(p:Play {name:'李尔王'}) CREATE (a)-[:PERFORMS_IN {label:'饰 李尔王'}]->(p);
MATCH (a:Actor {name:'何冰'}),(p:Play {name:'茶馆'}) CREATE (a)-[:PERFORMS_IN {label:'饰 松二爷'}]->(p);
MATCH (a:Actor {name:'何冰'}),(p:Play {name:'天下第一楼'}) CREATE (a)-[:PERFORMS_IN {label:'饰 卢孟实'}]->(p);
MATCH (a:Actor {name:'袁泉'}),(p:Play {name:'哈姆雷特'}) CREATE (a)-[:PERFORMS_IN {label:'饰 奥菲利亚'}]->(p);
MATCH (a:Actor {name:'冯远征'}),(p:Play {name:'雷雨'}) CREATE (a)-[:PERFORMS_IN {label:'饰 周朴园'}]->(p);
MATCH (a:Actor {name:'廖昌永'}),(p:Play {name:'茶花女'}) CREATE (a)-[:PERFORMS_IN {label:'主演'}]->(p);
MATCH (a:Actor {name:'和慧'}),(p:Play {name:'茶花女'}) CREATE (a)-[:PERFORMS_IN {label:'主演'}]->(p);

MATCH (p:Play {name:'哈姆雷特'}),(t:Tag {name:'莎士比亚'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'哈姆雷特'}),(t:Tag {name:'悲剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'哈姆雷特'}),(t:Tag {name:'经典'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'李尔王'}),(t:Tag {name:'莎士比亚'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'李尔王'}),(t:Tag {name:'悲剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'麦克白'}),(t:Tag {name:'莎士比亚'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'麦克白'}),(t:Tag {name:'悲剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'奥赛罗'}),(t:Tag {name:'莎士比亚'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'奥赛罗'}),(t:Tag {name:'悲剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'罗密欧与朱丽叶'}),(t:Tag {name:'爱情'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'茶馆'}),(t:Tag {name:'老舍'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'茶馆'}),(t:Tag {name:'京味'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'雷雨'}),(t:Tag {name:'曹禺'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'天鹅湖'}),(t:Tag {name:'芭蕾'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'茶花女'}),(t:Tag {name:'歌剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'歌剧魅影'}),(t:Tag {name:'音乐剧'}) CREATE (p)-[:HAS_TAG]->(t);
MATCH (p:Play {name:'悲惨世界'}),(t:Tag {name:'音乐剧'}) CREATE (p)-[:HAS_TAG]->(t);

MATCH (p:Play {name:'哈姆雷特'}),(x:Terminology {name:'独白'}) CREATE (p)-[:HAS_TERMINOLOGY {label:'生存还是毁灭'}]->(x);
MATCH (p:Play {name:'哈姆雷特'}),(x:Terminology {name:'旁白'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'哈姆雷特'}),(x:Terminology {name:'卡司'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'茶馆'}),(x:Terminology {name:'幕间'}) CREATE (p)-[:HAS_TERMINOLOGY {label:'三幕结构'}]->(x);
MATCH (p:Play {name:'雷雨'}),(x:Terminology {name:'调度'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'雷雨'}),(x:Terminology {name:'走位'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'天鹅湖'}),(x:Terminology {name:'足尖'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'天鹅湖'}),(x:Terminology {name:'双人舞'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'茶花女'}),(x:Terminology {name:'咏叹调'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'茶花女'}),(x:Terminology {name:'宣叙调'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);
MATCH (p:Play {name:'茶花女'}),(x:Terminology {name:'返场'}) CREATE (p)-[:HAS_TERMINOLOGY]->(x);

MATCH (p:Play {name:'哈姆雷特'}),(v:Venue {name:'国家大剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'李尔王'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'茶馆'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (p)-[:PERFORMED_AT {label:'驻场代表作'}]->(v);
MATCH (p:Play {name:'雷雨'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'天鹅湖'}),(v:Venue {name:'国家大剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'茶花女'}),(v:Venue {name:'中央歌剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'歌剧魅影'}),(v:Venue {name:'天桥艺术中心'}) CREATE (p)-[:PERFORMED_AT]->(v);
MATCH (p:Play {name:'悲惨世界'}),(v:Venue {name:'上海大剧院'}) CREATE (p)-[:PERFORMED_AT]->(v);

MATCH (a:Actor {name:'濮存昕'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (a)-[:WORKS_AT]->(v);
MATCH (a:Actor {name:'何冰'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (a)-[:WORKS_AT]->(v);
MATCH (a:Actor {name:'冯远征'}),(v:Venue {name:'北京人民艺术剧院'}) CREATE (a)-[:WORKS_AT]->(v);
MATCH (a:Actor {name:'廖昌永'}),(v:Venue {name:'中央歌剧院'}) CREATE (a)-[:WORKS_AT]->(v);
MATCH (a:Actor {name:'和慧'}),(v:Venue {name:'国家大剧院'}) CREATE (a)-[:WORKS_AT]->(v);

MATCH (p1:Play {name:'哈姆雷特'}),(p2:Play {name:'李尔王'}) CREATE (p1)-[:SIMILAR_TO {label:'同为莎翁悲剧'}]->(p2);
MATCH (p1:Play {name:'哈姆雷特'}),(p2:Play {name:'麦克白'}) CREATE (p1)-[:SIMILAR_TO {label:'同为莎翁悲剧'}]->(p2);
MATCH (p1:Play {name:'哈姆雷特'}),(p2:Play {name:'奥赛罗'}) CREATE (p1)-[:SIMILAR_TO {label:'同为莎翁悲剧'}]->(p2);
MATCH (p1:Play {name:'茶馆'}),(p2:Play {name:'天下第一楼'}) CREATE (p1)-[:SIMILAR_TO {label:'同具京味群像气质'}]->(p2);
MATCH (p1:Play {name:'歌剧魅影'}),(p2:Play {name:'悲惨世界'}) CREATE (p1)-[:SIMILAR_TO {label:'同为国际经典音乐剧'}]->(p2);

MATCH (t1:Terminology {name:'独白'}),(t2:Terminology {name:'旁白'}) CREATE (t1)-[:RELATED_TO {label:'均为角色独立发声方式'}]->(t2);
MATCH (t1:Terminology {name:'走位'}),(t2:Terminology {name:'调度'}) CREATE (t1)-[:RELATED_TO {label:'导演排练核心概念'}]->(t2);
MATCH (t1:Terminology {name:'谢幕'}),(t2:Terminology {name:'返场'}) CREATE (t1)-[:RELATED_TO {label:'演后互动流程'}]->(t2);
MATCH (t1:Terminology {name:'咏叹调'}),(t2:Terminology {name:'宣叙调'}) CREATE (t1)-[:RELATED_TO {label:'歌剧两类重要唱段'}]->(t2);
MATCH (t1:Terminology {name:'足尖'}),(t2:Terminology {name:'双人舞'}) CREATE (t1)-[:RELATED_TO {label:'芭蕾高频术语'}]->(t2);
