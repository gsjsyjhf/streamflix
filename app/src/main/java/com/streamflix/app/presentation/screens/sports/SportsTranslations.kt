package com.streamflix.app.presentation.screens.sports

/**
 * ترجمة أسماء الفرق والبطولات إلى العربية + أكواد الدول لجلب الأعلام
 * الأعلام تُجلب من flagcdn.com: https://flagcdn.com/w80/{code}.png
 */

// ===== أسماء الفرق بالعربية =====
private val teamsAr = mapOf(
    // أمريكا الجنوبية
    "Argentina" to "الأرجنتين",
    "Brazil" to "البرازيل",
    "Uruguay" to "الأوروغواي",
    "Colombia" to "كولومبيا",
    "Peru" to "بيرو",
    "Chile" to "تشيلي",
    "Paraguay" to "باراغواي",
    "Ecuador" to "الإكوادور",
    "Bolivia" to "بوليفيا",
    "Venezuela" to "فنزويلا",
    // أوروبا
    "France" to "فرنسا",
    "Germany" to "ألمانيا",
    "Spain" to "إسبانيا",
    "England" to "إنجلترا",
    "Portugal" to "البرتغال",
    "Netherlands" to "هولندا",
    "Italy" to "إيطاليا",
    "Belgium" to "بلجيكا",
    "Croatia" to "كرواتيا",
    "Switzerland" to "سويسرا",
    "USA" to "الولايات المتحدة",
    "United States" to "الولايات المتحدة",
    "Mexico" to "المكسيك",
    "Japan" to "اليابان",
    "South Korea" to "كوريا الجنوبية",
    "Senegal" to "السنغال",
    "Serbia" to "صربيا",
    "Poland" to "بولندا",
    "Denmark" to "الدنمارك",
    "Australia" to "أستراليا",
    "Qatar" to "قطر",
    "Saudi Arabia" to "السعودية",
    "Tunisia" to "تونس",
    "Ghana" to "غانا",
    "Cameroon" to "الكاميرون",
    "Canada" to "كندا",
    "Costa Rica" to "كوستاريكا",
    "Wales" to "ويلز",
    "Iran" to "إيران",
    "Egypt" to "مصر",
    "Algeria" to "الجزائر",
    "Morocco" to "المغرب",
    "Nigeria" to "نيجيريا",
    "Ivory Coast" to "ساحل العاج",
    "Turkey" to "تركيا",
    "Russia" to "روسيا",
    "Ukraine" to "أوكرانيا",
    "Austria" to "النمسا",
    "Sweden" to "السويد",
    "Norway" to "النرويج",
    "Czech Republic" to "التشيك",
    "Romania" to "رومانيا",
    "Hungary" to "هنغاريا",
    "Greece" to "اليونان",
    "Scotland" to "اسكتلندا",
    "Ireland" to "أيرلندا",
    "Northern Ireland" to "أيرلندا الشمالية",
    "Slovakia" to "سلوفاكيا",
    "Slovenia" to "سلوفينيا",
    "Bosnia and Herzegovina" to "البوسنة والهرسك",
    "Albania" to "ألبانيا",
    "Georgia" to "جورجيا",
    "Iceland" to "آيسلندا",
    "Finland" to "فنلندا",
    "Montenegro" to "الجبل الأسود",
    "Israel" to "إسرائيل",
    // عربي
    "Iraq" to "العراق",
    "UAE" to "الإمارات",
    "United Arab Emirates" to "الإمارات",
    "Jordan" to "الأردن",
    "Lebanon" to "لبنان",
    "Syria" to "سوريا",
    "Oman" to "عُمان",
    "Bahrain" to "البحرين",
    "Kuwait" to "الكويت",
    // أفريقيا
    "South Africa" to "جنوب أفريقيا",
    "Mali" to "مالي",
    "Burkina Faso" to "بوركينا فاسو",
    "Cape Verde" to "الرأس الأخضر",
    "Mozambique" to "موزمبيق",
    "Angola" to "أنغولا",
    "DR Congo" to "الكونغو الديمقراطية",
    "Congo" to "الكونغو",
    // إنجلترا - أندية
    "Liverpool" to "ليفربول",
    "Manchester City" to "مانشستر سيتي",
    "Manchester United" to "مانشستر يونايتد",
    "Chelsea" to "تشيلسي",
    "Arsenal" to "أرسنال",
    "Tottenham Hotspur" to "توتنهام",
    "Tottenham" to "توتنهام",
    "Leicester City" to "ليستر سيتي",
    "West Ham United" to "ويست هام",
    "Newcastle United" to "نيوكاسل",
    "Everton" to "إيفرتون",
    "Aston Villa" to "أستون فيلا",
    "Brighton" to "برايتون",
    "Wolverhampton" to "ولفرهامبتون",
    "Crystal Palace" to "كريستال بالاس",
    "Leeds United" to "ليدز يونايتد",
    "Southampton" to "ساوثهامبتون",
    "Burnley" to "بيرنلي",
    "Fulham" to "فولهام",
    "Brentford" to "برينتفورد",
    "Nottingham Forest" to "نوتنغهام فورست",
    "Bournemouth" to "بورنموث",
    // إسبانيا - أندية
    "Real Madrid" to "ريال مدريد",
    "Barcelona" to "برشلونة",
    "Atletico Madrid" to "أتلتيكو مدريد",
    "Atletico" to "أتلتيكو",
    "Sevilla" to "إشبيلية",
    "Valencia" to "فالنسيا",
    "Villarreal" to "فياريال",
    "Real Sociedad" to "ريال سوسيداد",
    "Real Betis" to "ريال بيتيس",
    "Athletic Bilbao" to "أتلتيك بيلباو",
    "Athletic Club" to "أتلتيك بيلباو",
    "Celta Vigo" to "سيلتا فيغو",
    "Getafe" to "خيتافي",
    "Osasuna" to "أوساسونا",
    "Espanyol" to "إسبانيول",
    "Mallorca" to "مايوركا",
    "Rayo Vallecano" to "رايو فايكانو",
    "Girona" to "خيرونا",
    // إيطاليا - أندية
    "Juventus" to "يوفنتوس",
    "AC Milan" to "ميلان",
    "Inter Milan" to "إنتر ميلان",
    "Inter" to "إنتر ميلان",
    "Napoli" to "نابولي",
    "Roma" to "روما",
    "AS Roma" to "روما",
    "Lazio" to "لاتسيو",
    "Atalanta" to "أتالانتا",
    "Fiorentina" to "فيورنتينا",
    "Torino" to "تورينو",
    "Bologna" to "بولونيا",
    "Sampdoria" to "سامبدوريا",
    "Genoa" to "جنوة",
    "Cagliari" to "كالياري",
    "Udinese" to "أودينيزي",
    "Sassuolo" to "ساسولو",
    "Empoli" to "إمبولي",
    "Verona" to "فيرونا",
    "Hellas Verona" to "فيرونا",
    "Monza" to "مونزا",
    // ألمانيا - أندية
    "Bayern Munich" to "بايرن ميونخ",
    "Bayern" to "بايرن ميونخ",
    "Borussia Dortmund" to "دورتموند",
    "Dortmund" to "دورتموند",
    "RB Leipzig" to "لايبزيغ",
    "Leverkusen" to "ليفركوزن",
    "Bayer Leverkusen" to "ليفركوزن",
    "Eintracht Frankfurt" to "أينتراخت فرانكفورت",
    "Wolfsburg" to "فولفسبورغ",
    "Freiburg" to "فرايبورغ",
    "Hoffenheim" to "هوفنهايم",
    "Mainz" to "ماينتس",
    "Augsburg" to "أوغسبورغ",
    "Stuttgart" to "شتوتغارت",
    "Hertha Berlin" to "هيرتا برلين",
    "Werder Bremen" to "فيردر بريمن",
    "Schalke 04" to "شالكه",
    "Schalke" to "شالكه",
    "Bochum" to "بوخوم",
    "Mönchengladbach" to "مونشنغلادباخ",
    "Borussia Mönchengladbach" to "مونشنغلادباخ",
    "Union Berlin" to "يونيون برلين",
    // فرنسا - أندية
    "Paris Saint-Germain" to "باريس سان جيرمان",
    "PSG" to "باريس سان جيرمان",
    "Marseille" to "مرسيليا",
    "Monaco" to "موناكو",
    "Lyon" to "ليون",
    "Lille" to "ليل",
    "Rennes" to "رين",
    "Nice" to "نيس",
    "Lens" to "لون",
    "Nantes" to "نانت",
    "Strasbourg" to "ستراسبورغ",
    "Montpellier" to "مونبلييه",
    "Bordeaux" to "بوردو",
    "Saint-Etienne" to "سان إتيان",
    "Reims" to "رينز",
    "Toulouse" to "تولوز",
    "Brest" to "بريست",
    "Metz" to "ميتز",
    "Lorient" to "لوريان",
    // البرتغال - أندية
    "Benfica" to "بنفيكا",
    "Porto" to "بورتو",
    "Sporting CP" to "سبورتينغ لشبونة",
    "Sporting" to "سبورتينغ لشبونة",
    "Braga" to "براغا",
    // هولندا - أندية
    "Ajax" to "أياكس",
    "PSV Eindhoven" to "آيندهوفن",
    "PSV" to "آيندهوفن",
    "Feyenoord" to "فينورد",
    "AZ Alkmaar" to "ألكمار",
    "Twente" to "توينتي",
    // تركيا - أندية
    "Galatasaray" to "غلطة سراي",
    "Fenerbahce" to "فنربخشة",
    "Besiktas" to "بشكتاش",
    "Trabzonspor" to "طرابزون سبور",
    // السعودية - أندية
    "Al Hilal" to "الهلال",
    "Al Nassr" to "النصر",
    "Al Ahli" to "الأهلي",
    "Al Ittihad" to "الاتحاد",
    "Al Shabab" to "الشباب",
    "Al Taawon" to "التعاون",
    "Al Fateh" to "الفتح",
    "Al Khaleej" to "الخليج",
    "Al Riyadh" to "الرياض",
    "Al Wehda" to "الوحدة",
    "Al Fayha" to "الفيحاء",
    "Damac" to "الضمك",
    "Abha" to "أبها",
    "Al Raed" to "الرائد",
    "Al Akhdoud" to "الأخدود",
    "Al Okhdood" to "الأخدود",
    // مصر - أندية
    "Al Ahly SC" to "الأهلي المصري",
    "Zamalek" to "الزمالك",
    "Pyramids FC" to "بيراميدز",
    // الإمارات - أندية
    "Al Ain" to "العين",
    // قطر - أندية
    "Al Duhail" to "الدحيل",
    "Al Sadd" to "السد",
    "Al Rayyan" to "الريان",
    "Al Wakrah" to "الوكرة",
    "Al Gharafa" to "الغرافة",
    "Al Arabi" to "العربي",
    // المغرب - أندية
    "Wydad Casablanca" to "الوداد",
    "Raja Casablanca" to "الرجاء",
    "AS FAR" to "الجيش الملكي"
)

// ===== أكواد الدول لجلب الأعلام (ISO 3166-1 alpha-2) =====
// ملاحظة: إنجلترا، ويلز، اسكتلندا تستخدم رموز FIFA وليس ISO (eng, wal, sco)
private val teamCountryCode = mapOf(
    // أمريكا الجنوبية
    "Argentina" to "ar",
    "Brazil" to "br",
    "Uruguay" to "uy",
    "Colombia" to "co",
    "Peru" to "pe",
    "Chile" to "cl",
    "Paraguay" to "py",
    "Ecuador" to "ec",
    "Bolivia" to "bo",
    "Venezuela" to "ve",
    // أوروبا
    "France" to "fr",
    "Germany" to "de",
    "Spain" to "es",
    "England" to "gb-eng",   // إنجلترا (FIFA code)
    "Portugal" to "pt",
    "Netherlands" to "nl",
    "Italy" to "it",
    "Belgium" to "be",
    "Croatia" to "hr",
    "Switzerland" to "ch",
    "USA" to "us",
    "United States" to "us",
    "Mexico" to "mx",
    "Japan" to "jp",
    "South Korea" to "kr",
    "Senegal" to "sn",
    "Serbia" to "rs",
    "Poland" to "pl",
    "Denmark" to "dk",
    "Australia" to "au",
    "Qatar" to "qa",
    "Saudi Arabia" to "sa",
    "Tunisia" to "tn",
    "Ghana" to "gh",
    "Cameroon" to "cm",
    "Canada" to "ca",
    "Costa Rica" to "cr",
    "Wales" to "gb-wls",     // ويلز (FIFA code)
    "Iran" to "ir",
    "Egypt" to "eg",
    "Algeria" to "dz",
    "Morocco" to "ma",
    "Nigeria" to "ng",
    "Ivory Coast" to "ci",
    "Turkey" to "tr",
    "Russia" to "ru",
    "Ukraine" to "ua",
    "Austria" to "at",
    "Sweden" to "se",
    "Norway" to "no",
    "Czech Republic" to "cz",
    "Romania" to "ro",
    "Hungary" to "hu",
    "Greece" to "gr",
    "Scotland" to "gb-sct",  // اسكتلندا (FIFA code)
    "Ireland" to "ie",
    "Northern Ireland" to "gb-nir",
    "Slovakia" to "sk",
    "Slovenia" to "si",
    "Bosnia and Herzegovina" to "ba",
    "Albania" to "al",
    "Georgia" to "ge",
    "Iceland" to "is",
    "Finland" to "fi",
    "Montenegro" to "me",
    "Israel" to "il",
    "Iraq" to "iq",
    "UAE" to "ae",
    "United Arab Emirates" to "ae",
    "Jordan" to "jo",
    "Lebanon" to "lb",
    "Syria" to "sy",
    "Oman" to "om",
    "Bahrain" to "bh",
    "Kuwait" to "kw",
    "South Africa" to "za",
    "Mali" to "ml",
    "Burkina Faso" to "bf",
    "Cape Verde" to "cv",
    "Mozambique" to "mz",
    "Angola" to "ao",
    "DR Congo" to "cd",
    "Congo" to "cg"
)

// ===== البطولات بالعربية =====
private val leaguesArMap = mapOf(
    "FIFA World Cup" to "كأس العالم",
    "World Cup" to "كأس العالم",
    "UEFA Champions League" to "دوري أبطال أوروبا",
    "Champions League" to "دوري الأبطال",
    "UEFA Europa League" to "الدوري الأوروبي",
    "Europa League" to "الدوري الأوروبي",
    "UEFA Europa Conference League" to "دوري المؤتمرات",
    "Premier League" to "الدوري الإنجليزي",
    "English Premier League" to "الدوري الإنجليزي الممتاز",
    "La Liga" to "الدوري الإسباني",
    "Laliga" to "الدوري الإسباني",
    "Serie A" to "الدوري الإيطالي",
    "Bundesliga" to "الدوري الألماني",
    "Ligue 1" to "الدوري الفرنسي",
    "Primeira Liga" to "الدوري البرتغالي",
    "Eredivisie" to "الدوري الهولندي",
    "Süper Lig" to "الدوري التركي",
    "Super Lig" to "الدوري التركي",
    "Saudi Pro League" to "دوري روشن السعودي",
    "Roshn Saudi League" to "دوري روشن السعودي",
    "Botola" to "الدوري المغربي",
    "AFC Champions League" to "دوري أبطال آسيا",
    "CAF Champions League" to "دوري أبطال أفريقيا",
    "Copa Libertadores" to "كوبا ليبرتادوريس",
    "Copa America" to "كوبا أمريكا",
    "UEFA Euro" to "يورو",
    "European Championship" to "البطولة الأوروبية",
    "Nations League" to "دوري الأمم الأوروبية",
    "FA Cup" to "كأس الاتحاد الإنجليزي",
    "Copa del Rey" to "كأس ملك إسبانيا",
    "Coppa Italia" to "كأس إيطاليا",
    "DFB Pokal" to "كأس ألمانيا",
    "Coupe de France" to "كأس فرنسا",
    "King Cup" to "كأس الملك السعودي",
    "Club World Cup" to "كأس العالم للأندية"
)

fun teamNameAr(name: String?): String {
    if (name.isNullOrBlank()) return ""
    teamsAr[name]?.let { return it }
    val lower = name.lowercase()
    teamsAr.entries.firstOrNull { it.key.lowercase() == lower }?.let { return it.value }
    teamsAr.entries.firstOrNull { name.contains(it.key, ignoreCase = true) }?.let { return it.value }
    return name
}

fun leagueNameAr(name: String?): String {
    if (name.isNullOrBlank()) return ""
    leaguesArMap[name]?.let { return it }
    val lower = name.lowercase()
    leaguesArMap.entries.firstOrNull { it.key.lowercase() == lower }?.let { return it.value }
    leaguesArMap.entries.firstOrNull { name.contains(it.key, ignoreCase = true) }?.let { return it.value }
    return name
}

fun teamInitialsAr(name: String): String {
    if (name.isBlank()) return "?"
    val parts = name.trim().split(" ", "_", "-").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        else -> name.take(2).uppercase()
    }
}

/**
 * يرجع كود الدولة (ISO 3166-1 alpha-2) لفريق معين
 * يُستخدم لجلب علم الدولة من flagcdn.com
 * يرجع null للأندية (لا دول لها)
 */
fun teamCountryCode(name: String?): String? {
    if (name.isNullOrBlank()) return null
    teamCountryCode[name]?.let { return it }
    val lower = name.lowercase()
    teamCountryCode.entries.firstOrNull { it.key.lowercase() == lower }?.let { return it.value }
    return null
}

/**
 * يبني رابط علم الدولة من flagcdn.com
 * يرجع null إذا كان الفريق نادياً (لا دولة له)
 * مثال: "France" → "https://flagcdn.com/w80/fr.png"
 */
fun teamFlagUrl(name: String?): String? {
    val code = teamCountryCode(name) ?: return null
    // ملاحظة: flagcdn.com لا يدعم رموز FIFA الفرعية (gb-eng, gb-wls, gb-sct)
    // نعالجها بإستخدام gb للمملكة المتحدة كحل بديل
    val flagCode = when (code) {
        "gb-eng", "gb-wls", "gb-sct", "gb-nir" -> "gb"
        else -> code
    }
    return "https://flagcdn.com/w80/${flagCode}.png"
}
