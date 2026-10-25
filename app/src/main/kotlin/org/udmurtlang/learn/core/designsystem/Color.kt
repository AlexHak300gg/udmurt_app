package org.udmurtlang.learn.core.designsystem

import androidx.compose.ui.graphics.Color

// ---- Палитра брендбука «Столетие Удмуртии»: коралл, бирюза, индиго, оранжевый, чёрный ----
val ZcCream = Color(0xFFFFF7EC)          // светлый фон страницы
val ZcCard = Color(0xFFFFFFFF)           // карточка на светлом
val ZcSubtle = Color(0xFFF8ECDD)         // вложенные плитки, chips
val ZcInk = Color(0xFF141414)            // основной тёмный текст
val ZcMuted = Color(0xFF6E6A66)          // вторичный текст на кремовом
val ZcHairline = Color(0xFFEADFD0)       // тонкие линии/бордеры
val ZcInkPill = Color(0xFF141414)        // чёрная кнопка-таблетка

// Тёмная тема
val ZcDarkBg = Color(0xFF15131B)
val ZcDarkCard = Color(0xFF24212C)
val ZcDarkSubtle = Color(0xFF2F2B38)
val ZcDarkInk = Color(0xFFF4F1F8)
val ZcDarkMuted = Color(0xFFA8A3B3)
val ZcDarkHairline = Color(0xFF3A3642)

// Фирменные цвета — плоские, без градиентов (Top/Bottom = основной цвет)
val ZcRed = Color(0xFFFF4647)            // коралл — звезда, слово дня, игры
val ZcRedTop = Color(0xFFFF4647)         // градиент сверху
val ZcRedBottom = Color(0xFFFF4647)      // градиент снизу
val ZcTeal = Color(0xFF00B5A3)           // бирюза — карточки слов
val ZcTealDeep = Color(0xFF00A896)
val ZcOrange = Color(0xFFF7821E)         // апельсин — результат, профиль
val ZcOrangeTop = Color(0xFFF7821E)
val ZcOrangeBottom = Color(0xFFF7821E)
val ZcPurple = Color(0xFF4E3AB5)         // фиолет — меню игр, викторина
val ZcPurpleDeep = Color(0xFF4E3AB5)     // слово дня, фон викторины
val ZcPurpleTile = Color(0xFF3B2A96)     // тёмно-фиолетовая плитка «Собери слово»
val ZcBerry = Color(0xFF1E1B24)          // чёрная плитка «Правда или ложь» (брендбук)
val ZcGold = Color(0xFFFFC233)           // звёзды (тёплый жёлтый, виден на оранжевом)
val ZcWhite = Color(0xFFFFFFFF)

// Совместимость со старыми ссылками (использовались вне дизайн-системы)
val UdmGreen = ZcTealDeep
val UdmGreenLight = ZcTeal
val UdmGreenDark = Color(0xFF0E7A3C)
val UdmBlack = ZcInkPill
val UdmWhite = ZcWhite
val OrnamentRed = ZcRed
val OrnamentOrange = ZcOrange
val SunYellow = ZcGold
val SkyBlue = Color(0xFF97B8FF)
val BerryPink = ZcBerry

val LightBackground = ZcCream
val LightSurface = ZcCard
val LightSurfaceVariant = ZcSubtle
val LightOnSurface = ZcInk
val LightOutline = ZcMuted
val LightPrimaryContainer = Color(0xFFFFD9D9)
val LightOnPrimaryContainer = Color(0xFF5A1A14)
val LightSecondaryContainer = Color(0xFFCFF1ED)
val LightOnSecondaryContainer = Color(0xFF0B5148)
val LightTertiaryContainer = Color(0xFFFDE3CC)
val LightOnTertiaryContainer = Color(0xFF6A3B00)

val DarkBackground = ZcDarkBg
val DarkSurface = ZcDarkCard
val DarkSurfaceVariant = ZcDarkSubtle
val DarkOnSurface = ZcDarkInk
val DarkOutline = ZcDarkMuted
val DarkPrimaryContainer = Color(0xFF3B2428)
val DarkOnPrimaryContainer = Color(0xFFFFD3CB)
val DarkSecondaryContainer = Color(0xFF183836)
val DarkOnSecondaryContainer = Color(0xFFC9F2EA)
val DarkTertiaryContainer = Color(0xFF3B2D1F)
val DarkOnTertiaryContainer = Color(0xFFFFE4C2)

val SuccessGreen = ZcTeal
val ErrorSoft = Color(0xFFE5484D)

// Насыщенные акценты для тёмной темы (чуть темнее, чтобы не «резали» глаз)
val ZcRedDark = Color(0xFFD63C42)
val ZcTealDark = Color(0xFF079486)
val ZcPurpleDark = Color(0xFF4A38B0)
val ZcOrangeDark = Color(0xFFDE7419)
// Цветной текст на тёмном фоне
val ZcRedOnDark = Color(0xFFFF6B6F)
val ZcTealOnDark = Color(0xFF2CC9B7)
val ZcPurpleOnDark = Color(0xFF9A8BF5)
val ZcOrangeOnDark = Color(0xFFFF9A45)
