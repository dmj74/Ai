package com.titanali.app.data.learn

/**
 * Built-in lesson content for the language-learning section.
 * Each language pack contains lessons grouped by CEFR level,
 * each with grammar notes (bilingual) and flashcard vocabulary.
 * Adding a new language = adding a new LanguagePack here.
 */
data class VocabItem(
    val word: String,
    val meaning: String,
    val example: String = "",
    val exampleFa: String = "",
)

data class Lesson(
    val id: String,
    val level: String,
    val title: String,
    val titleFa: String,
    val grammar: String,
    val vocab: List<VocabItem>,
)

data class LanguagePack(
    val code: String,
    val name: String,
    val nameFa: String,
    val flag: String,
    /** First message sent to the AI tutor to kick off a practice session. */
    val starter: String,
    val lessons: List<Lesson>,
)

private val english = LanguagePack(
    code = "en",
    name = "English",
    nameFa = "انگلیسی",
    flag = "🇬🇧",
    starter = "Hi! I'm a Persian student. Please start our English practice conversation with me.",
    lessons = listOf(
        Lesson(
            id = "en_a1_hello",
            level = "A1",
            title = "Greetings & Introduction",
            titleFa = "سلام‌آشنانی و معرفی",
            grammar = """
                Use "Hello / Hi" to greet people.
                Ask: "What is your name?"  →  Answer: "My name is Ali."
                گرامر: با to be سوال و جواب می‌سازیم:
                What is your name? = اسمت چیه؟
            """.trimIndent(),
            vocab = listOf(
                VocabItem("hello", "سلام", "Hello! How are you?", "سلام! حالت چطوره؟"),
                VocabItem("goodbye", "خداحافظ", "Goodbye, see you tomorrow.", "خداحافظ، فردا می‌بینمت."),
                VocabItem("my name is", "اسم من … است", "My name is Ali.", "اسم من علی است."),
                VocabItem("how are you", "حالت چطور است؟", "How are you today?", "امروز حالت چطوره؟"),
                VocabItem("fine, thank you", "خوبم، ممنون", "I am fine, thank you.", "خوبم، ممنون."),
                VocabItem("nice to meet you", "از آشناییت خوشحالم", "Nice to meet you, Sara!", "از آشناییت خوشحالم، سارا!"),
                VocabItem("thank you", "ممنون", "Thank you for your help.", "از کمکت ممنونم."),
                VocabItem("please", "لطفاً", "Please sit down.", "لطفاً بنشین."),
                VocabItem("sorry", "ببخشید", "Sorry, I am late.", "ببخشید، دیر کردم."),
                VocabItem("what is your name", "اسمت چیه؟", "What is your name, please?", "اسمت چیه؟"),
            ),
        ),
        Lesson(
            id = "en_a1_tobe",
            level = "A1",
            title = "The verb \"to be\"",
            titleFa = "فعل to be (هست)",
            grammar = """
                I am / You are / He is / She is / We are / They are
                «من هستم / تو هستی / او هست / ما هستیم / آن‌ها هستند»
                For nationality, job and feelings:
                I am from Iran.  She is a teacher.  I am happy.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("am", "هستم (I)", "I am a student.", "من دانشجو هستم."),
                VocabItem("is", "هست (he/she/it)", "He is my friend.", "او دوست من است."),
                VocabItem("are", "هستید / هستند", "They are at home.", "آن‌ها در خانه هستند."),
                VocabItem("teacher", "معلم", "My mother is a teacher.", "مادرم معلم است."),
                VocabItem("student", "دانشجو", "I am a new student.", "من یک دانشجوی جدیدم."),
                VocabItem("happy", "خوشحال", "I am very happy today.", "امروز خیلی خوشحالم."),
                VocabItem("tired", "خسته", "You look tired.", "خسته به نظر می‌رسی."),
                VocabItem("from Iran", "از ایران", "I am from Iran.", "من از ایران هستم."),
            ),
        ),
        Lesson(
            id = "en_a1_numbers",
            level = "A1",
            title = "Numbers & Time",
            titleFa = "اعداد و ساعت",
            grammar = """
                one, two, three, four, five, six, seven, eight, nine, ten
                Time: "It is three o'clock."  "It is ten thirty." (10:30)
                ساعت می‌گوییم با It is + number.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("one", "یک", "I have one cat.", "من یک گربه دارم."),
                VocabItem("two", "دو", "Two cups of tea, please.", "دو فنجون چای لطفاً."),
                VocabItem("three", "سه", "We have three books.", "ما سه کتاب داریم."),
                VocabItem("five", "پنج", "Five minutes, please.", "پنج دقیقه لطفاً."),
                VocabItem("ten", "ده", "I am ten years old.", "من ده سالم است."),
                VocabItem("o'clock", "ساعت (دقیق)", "It is five o'clock.", "ساعت پنج است."),
                VocabItem("morning", "صبح", "Good morning, Mr. Reza!", "صبح بخیر، آقای رضا!"),
                VocabItem("night", "شب", "Good night, sleep well!", "شب بخیر، خوب بخواب!"),
            ),
        ),
        Lesson(
            id = "en_a1_food",
            level = "A1",
            title = "Food & Drinks",
            titleFa = "غذا و نوشیدنی",
            grammar = """
                I like / I don't like + food
                In a restaurant: "Can I have some rice?"
                "Can I have …?" = یک … می‌شه داشته باشم؟
            """.trimIndent(),
            vocab = listOf(
                VocabItem("rice", "برنج", "I like rice with chicken.", "برنج با مرغ دوست دارم."),
                VocabItem("bread", "نان", "We buy bread every morning.", "ما هر صبح نان می‌خریم."),
                VocabItem("tea", "چای", "A cup of tea, please.", "یک فنجون چای لطفاً."),
                VocabItem("water", "آب", "Can I have some water?", "یکم آب می‌شه داشته باشم؟"),
                VocabItem("chicken", "مرغ", "I don't like chicken.", "مرغ دوست ندارم."),
                VocabItem("sugar", "شکر", "No sugar, thank you.", "شکر نمی‌خوام، ممنون."),
                VocabItem("delicious", "خوشمزه", "This food is delicious!", "این غذا خیلی خوشمزست!"),
                VocabItem("hungry", "گرسنه", "I am hungry. Let's eat!", "گرسنم. بیا غذا بخوریم!"),
            ),
        ),
        Lesson(
            id = "en_a2_past",
            level = "A2",
            title = "Past Simple",
            titleFa = "گذشته ساده",
            grammar = """
                Regular: verb + ed  →  I worked, I visited
                Irregular: go → went, see → saw, eat → ate, buy → bought
                Negative: "I didn't go."  Question: "Did you go?"
                «didn't» برای منفی و «did» برای سوال.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("went (go)", "رفتم", "I went to Tehran last week.", "هفته پیش به تهران رفتم."),
                VocabItem("saw (see)", "دیدم", "I saw a great movie.", "یک فیلم عالی دیدم."),
                VocabItem("ate (eat)", "خوردم", "We ate breakfast at eight.", "صبحانه را ساعت هشت خوردیم."),
                VocabItem("bought (buy)", "خریدم", "She bought a new phone.", "او یک گوشی جدید خرید."),
                VocabItem("visited", "دیدن کردم", "I visited my grandparents.", "به دیدن پدربزرگ و مادربزرگم رفتم."),
                VocabItem("stayed (stay)", "ماندم / اقامت داشتم", "We stayed in a small hotel.", "در یک هتل کوچک اقامت داشتیم."),
                VocabItem("yesterday", "دیروز", "Yesterday was a busy day.", "دیروز یک روز شلوغ بود."),
                VocabItem("last week", "هفته پیش", "Last week I was sick.", "هفته پیش مریض بودم."),
            ),
        ),
        Lesson(
            id = "en_a2_future",
            level = "A2",
            title = "Future: will & going to",
            titleFa = "آینده: will و going to",
            grammar = """
                will + verb (predictions): "It will rain tomorrow."
                going to + verb (plans): "I am going to study English."
                «will» بیشتر برای پیش‌بینی، «going to» برای برنامه و تصمیم.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("will", "خواهد / می‌کنم", "I will call you tonight.", "امشب بهت زنگ می‌زنم."),
                VocabItem("going to", "قرار است", "I am going to learn English.", "قرار است انگلیسی یاد بگیرم."),
                VocabItem("tomorrow", "فردا", "See you tomorrow!", "فردا می‌بینمت!"),
                VocabItem("next month", "ماه بعد", "We will travel next month.", "ماه بعد سفر می‌کنیم."),
                VocabItem("plan", "برنامه", "What is your plan for the weekend?", "برنامه‌ات برای آخر هفته چیه؟"),
                VocabItem("maybe", "شاید", "Maybe I will come.", "شاید بیایم."),
            ),
        ),
    ),
)

private val german = LanguagePack(
    code = "de",
    name = "German",
    nameFa = "آلمانی",
    flag = "🇩🇪",
    starter = "Hallo! Ich lerne Deutsch. Bitte fang unsere Konversation auf Deutsch an.",
    lessons = listOf(
        Lesson(
            id = "de_a1_hallo",
            level = "A1",
            title = "Begrüßungen",
            titleFa = "سلام و احوال‌پرسی",
            grammar = """
                Hallo / Guten Tag = سلام
                "Wie geht's?" = حالت چطوره؟  →  "Gut, danke!" = خوبم، ممنون!
                Genus (جنسیت اسم‌ها): der (مذکر), die (مؤنث), das (خنثی)
            """.trimIndent(),
            vocab = listOf(
                VocabItem("Hallo", "سلام", "Hallo, wie geht's?", "سلام، حالت چطوره؟"),
                VocabItem("Tschüss", "خداحافظ (دوستانه)", "Tschüss, bis morgen!", "خداحافظ، تا فردا!"),
                VocabItem("danke", "ممنون", "Danke für deine Hilfe.", "از کمکت ممنون."),
                VocabItem("bitte", "لطفاً / خواهش می‌کنم", "Ein Wasser, bitte.", "یک آب لطفاً."),
                VocabItem("ja", "بله", "Ja, ich verstehe.", "بله، متوجه می‌شم."),
                VocabItem("nein", "خیر", "Nein, danke.", "نه، ممنون."),
                VocabItem("mein Name ist …", "اسم من … است", "Mein Name ist Hassan.", "اسم من حسن است."),
                VocabItem("wie geht's?", "حالت چطوره؟", "Hallo Anna, wie geht's?", "سلام آنا، حالت چطوره؟"),
                VocabItem("gut", "خوب", "Gut, danke. Und dir?", "خوبم، ممنون. تو چطور؟"),
            ),
        ),
        Lesson(
            id = "de_a1_zahlen",
            level = "A1",
            title = "Zahlen 1–10",
            titleFa = "اعداد ۱ تا ۰",
            grammar = """
                eins, zwei, drei, vier, fünf, sechs, sieben, acht, neun, zehn
                "Wie viel kostet das?" = این چنده؟
                "Das kostet zehn Euro." = این ده یورو است.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("eins", "یک", "Ich habe eins.", "من یکی دارم."),
                VocabItem("zwei", "دو", "Zwei Kaffee, bitte.", "دو قهوه لطفاً."),
                VocabItem("drei", "سه", "Wir sind zu dritt.", "ما سه نفریم."),
                VocabItem("vier", "چهار", "Das Haus hat vier Zimmer.", "این خانه چهار اتاق دارد."),
                VocabItem("fünf", "پنج", "Fünf Minuten, bitte.", "پنج دقیقه لطفاً."),
                VocabItem("zehn", "ده", "Ich habe zehn Euro.", "من ده یورو دارم."),
                VocabItem("hundert", "صد", "Das kostet hundert Euro.", "این صد یورو است."),
                VocabItem("wie viel", "چقدر / چند", "Wie viel kostet das?", "این چنده؟"),
            ),
        ),
        Lesson(
            id = "de_a1_verben",
            level = "A1",
            title = "Wichtige Verben",
            titleFa = "فعل‌های مهم",
            grammar = """
                Present tense: ich gehe, du gehst, er geht
                "sein" (هست): ich bin, du bist, er ist
                "haben" (دارد): ich habe, du hast, er hat
            """.trimIndent(),
            vocab = listOf(
                VocabItem("sein (ich bin)", "هستم", "Ich bin Student.", "من دانشجویم."),
                VocabItem("haben (ich habe)", "دارم", "Ich habe einen Hund.", "من یک سگ دارم."),
                VocabItem("gehen (ich gehe)", "می‌روم", "Ich gehe zur Arbeit.", "به کار می‌روم."),
                VocabItem("kommen (ich komme)", "می‌آیم", "Ich komme aus Iran.", "من از ایران می‌آیم."),
                VocabItem("heißen (ich heiße)", "اسم من … است", "Ich heiße Maryam.", "اسم من مریم است."),
                VocabItem("mögen (ich mag)", "دوست دارم", "Ich mag Kaffee.", "قهوه دوست دارم."),
                VocabItem("verstehen", "متوجه می‌شوم", "Ich verstehe nicht.", "متوجه نمی‌شم."),
                VocabItem("sprechen", "حرف می‌زنم", "Ich spreche ein bisschen Deutsch.", "یکم آلمانی حرف می‌زنم."),
            ),
        ),
        Lesson(
            id = "de_a1_familie",
            level = "A1",
            title = "Die Familie",
            titleFa = "خانواده",
            grammar = """
                die Mutter (مادر), der Vater (پدر), das Kind (کودک)
                "Ich habe eine Schwester." = یک خواهر دارم.
                possessive: mein / meine = من (مالکیت)
            """.trimIndent(),
            vocab = listOf(
                VocabItem("die Mutter", "مادر", "Meine Mutter ist Ärztin.", "مادرم پزشک است."),
                VocabItem("der Vater", "پدر", "Mein Vater ist Ingenieur.", "پدرم مهندس است."),
                VocabItem("die Schwester", "خواهر", "Ich habe eine Schwester.", "یک خواهر دارم."),
                VocabItem("der Bruder", "برادر", "Mein Bruder studiert in Berlin.", "برادرم در برلین درس می‌خواند."),
                VocabItem("das Kind", "کودک", "Das Kind schläft.", "کودک خوابیده است."),
                VocabItem("die Familie", "خانواده", "Meine Familie ist groß.", "خانواده‌ام بزرگ است."),
                VocabItem("der Freund", "دوست (مذکر)", "Das ist mein Freund Ali.", "این دوست من، علی است."),
                VocabItem("die Freundin", "دوست (مؤنث)", "Meine Freundin heißt Lena.", "دوست دخترم لنای نام دارد."),
            ),
        ),
    ),
)

private val arabic = LanguagePack(
    code = "ar",
    name = "Arabic",
    nameFa = "عربی",
    flag = "🇸🇦",
    starter = "مرحباً! أنا أتعلم العربية. من فضلك ابدأ محادثتنا باللغة العربية.",
    lessons = listOf(
        Lesson(
            id = "ar_a1_hello",
            level = "A1",
            title = "التحيات",
            titleFa = "سلام و احوال‌پرسی",
            grammar = """
                مرحبا = سلام
                "كيف حالك؟" = حالت چطوره؟  →  "بخير، شكراً" = خوبم، ممنون
                عربی از راست به چپ نوشته می‌شود.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("مرحبا", "سلام", "مرحباً، كيف حالك؟", "سلام، حالت چطوره؟"),
                VocabItem("مع السلامة", "خداحافظ", "مع السلامة، إلى اللقاء!", "خداحافظ، به امید دیدار!"),
                VocabItem("شكراً", "ممنون", "شكراً على مساعدتك.", "از کمکت ممنون."),
                VocabItem("عفواً", "خواهش می‌کنم / ببخشید", "عفواً على التأخير.", "به خاطر تأخیر عذر می‌خواهم."),
                VocabItem("نعم", "بله", "نعم، أفهم.", "بله، متوجه می‌شم."),
                VocabItem("لا", "نه", "لا، شكراً.", "نه، ممنون."),
                VocabItem("كيف حالك", "حالت چطوره؟", "مرحباً أحمد، كيف حالك؟", "سلام احمد، حالت چطوره؟"),
                VocabItem("اسمي", "اسم من", "اسمي رضا.", "اسم من رضاست."),
            ),
        ),
        Lesson(
            id = "ar_a1_numbers",
            level = "A1",
            title = "الأعداد",
            titleFa = "اعداد",
            grammar = """
                واحد، اثنان، ثلاثة، أربعة، خمسة، ستة، سبعة، ثمانية، تسعة، عشرة
                "كم عمرك؟" = چند سالته؟
            """.trimIndent(),
            vocab = listOf(
                VocabItem("واحد", "یک", "عندي واحد.", "من یکی دارم."),
                VocabItem("اثنان", "دو", "عندي اثنان.", "من دو تا دارم."),
                VocabItem("ثلاثة", "سه", "لدي ثلاثة إخوة.", "من سه برادر دارم."),
                VocabItem("أربعة", "چهار", "في البيت أربعة غرف.", "در خانه چهار اتاق هست."),
                VocabItem("خمسة", "پنج", "خمسة دقائق من فضلك.", "پنج دقیقه لطفاً."),
                VocabItem("عشرة", "ده", "عندي عشرة ريالات.", "من ده ریال دارم."),
                VocabItem("كم", "چند / چقدر", "كم عمرك؟", "چند سالته؟"),
            ),
        ),
        Lesson(
            id = "ar_a1_alphabet",
            level = "A1",
            title = "الحروف",
            titleFa = "حروف الفبا",
            grammar = """
                الفبا عربی ۲۸ حرف دارد:
                ا ب ت ث ج ح خ د ذ ر ز س ش ص ض ط ظ ع غ ف ق ك ل م ن ه و ي
                حروف به هم وصل می‌شوند و از راست به چپ نوشته می‌شوند.
            """.trimIndent(),
            vocab = listOf(
                VocabItem("ألف", "حرف ا", "أ ب ت", "الف، باء، تاء"),
                VocabItem("باء", "حرف ب", "بسم الله", "بسم الله"),
                VocabItem("تاء", "حرف ت", "تفاح", "سیب"),
                VocabItem("ثاء", "حرف ث", "ثلاثة", "سه"),
                VocabItem("جيم", "حرف ج", "جبل", "کوه"),
                VocabItem("حاء", "حرف ح", "حديقة", "باغ"),
                VocabItem("خاء", "حرف خ", "خبز", "نان"),
            ),
        ),
        Lesson(
            id = "ar_a1_family",
            level = "A1",
            title = "العائلة",
            titleFa = "خانواده",
            grammar = """
                أم (مادر)، أب (پدر)، أخ (برادر)، أخت (خواهر)
                "أبي" = پدر من  —  "أمي" = مادر من
            """.trimIndent(),
            vocab = listOf(
                VocabItem("أمي", "مادر من", "أمي طبيبة.", "مادرم پزشک است."),
                VocabItem("أبي", "پدر من", "أبي مهندس.", "پدرم مهندس است."),
                VocabItem("أخ", "برادر", "لي أخ واحد.", "من یک برادر دارم."),
                VocabItem("أخت", "خواهر", "أختي صغيرة.", "خواهرم کوچک است."),
                VocabItem("جدّي", "پدربزرگم", "جدّي كبير في السن.", "پدربزرگم سال‌دار است."),
                VocabItem("جدة", "مادربزرگ", "جدتي في البيت.", "مادربزرگم در خانه است."),
                VocabItem("طفل", "کودک", "الطفل يلعب.", "کودک بازی می‌کند."),
            ),
        ),
    ),
)

object LessonData {

    val languages: List<LanguagePack> = listOf(english, german, arabic)

    fun pack(code: String): LanguagePack =
        languages.firstOrNull { it.code == code } ?: languages.first()
}
