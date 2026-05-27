package com.example.data.local

import com.example.data.model.Question

object OfflineQuestions {
    val level1 = listOf(
        Question(
            id = 101,
            level = 1,
            questionText = "a、o、( )、i、u、ü。括号里漏掉了哪个单韵母？",
            options = listOf("e", "p", "f", "u"),
            correctAnswer = "e",
            explanation = "单韵母口诀：a o e i u ü，这里漏掉了 e 哦！"
        ),
        Question(
            id = 102,
            level = 1,
            questionText = "b、p、( )、f、d、t、n、l。括号里漏掉了哪个声母？",
            options = listOf("m", "n", "x", "h"),
            correctAnswer = "m",
            explanation = "声母表开头：b p m f d t n l，括号里应该是 m 呢！"
        ),
        Question(
            id = 103,
            level = 1,
            questionText = "d、t、n、l、( )、k、h。口诀中漏掉了哪一个声母？",
            options = listOf("g", "q", "z", "s"),
            correctAnswer = "g",
            explanation = "d t n l 后面紧跟着的就是 g k h 兄弟啦！"
        ),
        Question(
            id = 104,
            level = 1,
            questionText = "ai、ei、( )、ao、ou、iu。这里漏掉了哪个复韵母？",
            options = listOf("ui", "ie", "er", "an"),
            correctAnswer = "ui",
            explanation = "复韵母常考排位：ai ei ui ao ou iu，这里是 ui 呀！"
        ),
        Question(
            id = 105,
            level = 1,
            questionText = "ie、( )、er。中间戴耳机漏掉的特殊复韵母是？",
            options = listOf("üe", "un", "ing", "ou"),
            correctAnswer = "üe",
            explanation = "ie üe er，中间空下的复韵母是 üe 哦，两点别忘啦！"
        ),
        Question(
            id = 106,
            level = 1,
            questionText = "an、en、in、( )、ün。前鼻韵母里谁溜走了？",
            options = listOf("un", "ing", "on", "ai"),
            correctAnswer = "un",
            explanation = "五个前鼻韵母是 an en in un ün，这里漏掉了 un 喔。"
        ),
        Question(
            id = 107,
            level = 1,
            questionText = "ang、eng、ing、( )。后鼻韵母最后一位是？",
            options = listOf("ong", "un", "on", "ie"),
            correctAnswer = "ong",
            explanation = "四个后鼻韵母是 ang eng ing ong，少写了声音响亮的 ong！"
        ),
        Question(
            id = 108,
            level = 1,
            questionText = "zhi、chi、shi、ri、zi、ci、( )。整体认读音节中还差谁？",
            options = listOf("si", "yi", "wu", "yu"),
            correctAnswer = "si",
            explanation = "z c s 对应的整体认读音节是 zi ci si，差的就是 si 啦。"
        )
    )

    val level2 = listOf(
        Question(
            id = 201,
            level = 2,
            questionText = "三年级词语【准备】的‘准’，读音是哪一个？",
            options = listOf("A. zhǔn (翘舌音)", "B. zǔn (平舌音)"),
            correctAnswer = "A. zhǔn (翘舌音)",
            explanation = "‘准’是翘舌音 zhǔn，可不能读成平舌音 zǔn 呀。"
        ),
        Question(
            id = 202,
            level = 2,
            questionText = "三年级词语【晨光】的‘晨’，读音是哪一个？",
            options = listOf("A. chén (前鼻音)", "B. chéng (后鼻音)"),
            correctAnswer = "A. chén (前鼻音)",
            explanation = "‘晨’是前鼻音 chén，清晨的早晨，收音在门齿。"
        ),
        Question(
            id = 203,
            level = 2,
            questionText = "三年级词语【旅行】的‘旅’，读音是哪一个？",
            options = listOf("A. lǚ (带两点音)", "B. lǔ (不带点音)"),
            correctAnswer = "A. lǚ (带两点音)",
            explanation = "‘旅’的拼音是 lǚ，绿色的绿、旅行的旅，嘴巴吹小哨。"
        ),
        Question(
            id = 204,
            level = 2,
            questionText = "三年级词语【赠送】的‘赠’，读音是哪一个？",
            options = listOf("A. zèng (平舌音)", "B. zhèng (翘舌音)"),
            correctAnswer = "A. zèng (平舌音)",
            explanation = "‘赠’是平舌音 zèng 哟，赠送卡片，牙齿咬合发声。"
        ),
        Question(
            id = 205,
            level = 2,
            questionText = "三年级词语【虽然】的‘虽’，读音是哪一个？",
            options = listOf("A. suī (平舌音)", "B. shuī (翘舌音)"),
            correctAnswer = "A. suī (平舌音)",
            explanation = "‘虽’是平舌音 suī，读‘虽然’的时候舌头放平平。"
        ),
        Question(
            id = 206,
            level = 2,
            questionText = "三年级词语【敬礼】的‘敬’，读音是哪一个？",
            options = listOf("A. jìng (后鼻音)", "B. jìn (前鼻音)"),
            correctAnswer = "A. jìng (后鼻音)",
            explanation = "‘敬’是后鼻音 jìng，尊敬的敬，后鼻腔要共鸣声哦。"
        ),
        Question(
            id = 207,
            level = 2,
            questionText = "三年级词语【穿戴】的‘戴’，音节声调应该标在哪个字母上？",
            options = listOf("A. dái (在 a 上)", "B. dài (在 a 上)"),
            correctAnswer = "B. dài (在 a 上)",
            explanation = "‘戴’读四声 dài，声调戴在 a 的头上！"
        )
    )

    val level3 = listOf(
        Question(
            id = 301,
            level = 3,
            questionText = "【优秀】的‘秀’写成了 xíù，这个拼音帽子戴得对吗？",
            options = listOf("A. 对", "B. 不对（i u并列标在后）"),
            correctAnswer = "B. 不对（i u并列标在后）",
            explanation = "拼音戴帽口诀：i u 并列标在后，xiu 的声调应当标在 u 头上 (xiù)。"
        ),
        Question(
            id = 302,
            level = 3,
            questionText = "【圆圈】的‘圈’写成 qūan，这个拼音帽子戴得对吗？",
            options = listOf("A. 对", "B. 不对（ü与j q x y相拼去两点）"),
            correctAnswer = "B. 不对（ü与j q x y相拼去两点）",
            explanation = "ü 遇见 j q x y 必须摘掉两点小帽子，所以拼音应是 quān。"
        ),
        Question(
            id = 303,
            level = 3,
            questionText = "【绿叶】的‘绿’写成 lù，这个拼音帽子戴得对吗？",
            options = listOf("A. 对", "B. 不对（l n相拼 ü 不能省去两点）"),
            correctAnswer = "B. 不对（l n相拼 ü 不能省去两点）",
            explanation = "因为 l、n 和 ü 相拼时不会省略两点，‘绿’必须拼成 lǜ。"
        ),
        Question(
            id = 304,
            level = 3,
            questionText = "【皮鞋】的‘鞋’写成 xié，声调标在字母 e 上，这顶帽子戴得对吗？",
            options = listOf("A. 对 (o e 后面 o 在前，但有 e 标在 e)", "B. 不对"),
            correctAnswer = "A. 对 (o e 后面 o 在前，但有 e 标在 e)",
            explanation = "口诀是：有 a 不放过，没 a 找 o e。所以 xié 的声调确实在 e 上。"
        ),
        Question(
            id = 305,
            level = 3,
            questionText = "【开会】的‘会’写成 hùī，戴得对吗？",
            options = listOf("A. 对", "B. 不对（i u并列标在后，应标在i上）"),
            correctAnswer = "B. 不对（i u并列标在后，应标在i上）",
            explanation = "i u 并列按顺序，谁在后面给谁戴，hui 中 i 在后所以是 huì。"
        ),
        Question(
            id = 306,
            level = 3,
            questionText = "【月亮】的‘亮’写成 liang，拼音在这里不标调号（轻声），对吗？",
            options = listOf("A. 对 (月亮中亮读轻声)", "B. 不对 (必须标四声)"),
            correctAnswer = "A. 对 (月亮中亮读轻声)",
            explanation = "‘月亮’的‘亮’读轻声，不戴调号帽子是完全对的！"
        )
    )

    val level4 = listOf(
        Question(
            id = 401,
            level = 4,
            questionText = "请看拼音写出词语：【 lǚ xíng 】",
            correctAnswer = "旅行",
            hint = "提示：小动物们结伴出门，最喜欢到处去走走看看……",
            explanation = "‘旅行’注意两点哦，‘旅’是左边方字旁加一个斜右下，‘行’是前行。"
        ),
        Question(
            id = 402,
            level = 4,
            questionText = "请看拼音写出词语：【 chén guāng 】",
            correctAnswer = "晨光",
            hint = "提示：早晨从东方透出来的，温暖明亮的金色阳光……",
            explanation = "‘晨光’的‘晨’底下是‘辰’，可不要忘了写呀，‘光’是光明。"
        ),
        Question(
            id = 403,
            level = 4,
            questionText = "请看拼音写出词语：【 zhǔn bèi 】",
            correctAnswer = "准备",
            hint = "提示：出发去做一件好玩事情之前，做好各种资料和物资的……",
            explanation = "‘准备’的‘准’是两点水旁，‘备’上面是折文，拼音是 zhǔn bèi。"
        ),
        Question(
            id = 404,
            level = 4,
            questionText = "请看拼音写出词语：【 yóu xì 】",
            correctAnswer = "游戏",
            hint = "提示：下课了！灵儿姐和好朋友在操场上开心地玩老鹰捉小鸡……",
            explanation = "‘游戏’这两个字要写好，‘游’是三点水，‘戏’右边是个‘戈’。"
        ),
        Question(
            id = 405,
            level = 4,
            questionText = "请看拼音写出词语：【 mì fēng 】",
            correctAnswer = "蜜蜂",
            hint = "提示：嗡嗡嗡，飞在花丛里忙着采花粉酿蜜的高产小昆虫……",
            explanation = "‘蜜蜂’都是虫字底，别写成蜂蜜的密，是蜜蜂的‘蜜’字哟！"
        )
    )

    fun getQuestionsForLevel(level: Int): List<Question> {
        return when (level) {
            1 -> level1
            2 -> level2
            3 -> level3
            4 -> level4
            else -> level1
        }
    }
}
