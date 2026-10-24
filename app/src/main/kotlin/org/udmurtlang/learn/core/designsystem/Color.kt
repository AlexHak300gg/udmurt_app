package org.udmurtlang.learn.core.designsystem

import androidx.compose.ui.graphics.Color

// ---- Фирменная палитра макета (кремовый фон, коралл, бирюза, апельсин, фиолет) ----
val ZcCream = Color(0xFFFDF4E8)          // светлый фон страницы
val ZcCard = Color(0xFFFFFFFF)           // карточка на светлом
val ZcSubtle = Color(0xFFF4EADB)         // вложенные плитки, chips
val ZcInk = Color(0xFF251A18)            // основной тёмный текст
val ZcMuted = Color(0xFF9A8B80)          // вторичный текст на кремовом
val ZcHairline = Color(0xFFF0E3D2)       // тонкие линии/бордеры
val ZcInkPill = Color(0xFF26201F)        // чёрная кнопка-таблетка

// Тёмная тема
val ZcDarkBg = Color(0xFF1A1114)
val ZcDarkCard = Color(0xFF2B2126)
val ZcDarkSubtle = Color(0xFF392E33)
val ZcDarkInk = Color(0xFFF4ECE7)
val ZcDarkMuted = Color(0xFFAE9FA5)
val ZcDarkHairline = Color(0xFF443840)

// Яркие бутылки бренда
val ZcRed = Color(0xFFF4574B)            // коралл — звезда, слово дня, игры
val ZcRedTop = Color(0xFFF97A6C)         // градиент сверху
val ZcRedBottom = Color(0xFFF0433B)      // градиент снизу
val ZcTeal = Color(0xFF2BC3AE)           // бирюза — карточки слов
val ZcTealDeep = Color(0xFF1BAE9A)
val ZcOrange = Color(0xFFEF8B1C)         // апельсин — результат, профиль
val ZcOrangeTop = Color(0xFFF7A649)
val ZcOrangeBottom = Color(0xFFEC7E11)
val ZcPurple = Color(0xFF6647C9)         // фиолет — меню игр, викторина
val ZcPurpleDeep = Color(0xFF4F34AE)     // слово дня, фон викторины
val ZcPurpleTile = Color(0xFF5B3DB8)     // тёмно-фиолетовая плитка «Собери слово»
val ZcBerry = Color(0xFFEE5D6C)          // малиновая плитка «Правда или ложь»
val ZcGold = Color(0xFFF7B733)           // звёзды
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
val LightPrimaryContainer = Color(0xFFFFDBD5)
val LightOnPrimaryContainer = Color(0xFF5A1A14)
val LightSecondaryContainer = Color(0xFFCFF0EA)
val LightOnSecondaryContainer = Color(0xFF0B5148)
val LightTertiaryContainer = Color(0xFFFFE4C2)
val LightOnTertiaryContainer = Color(0xFF6A3B00)

val DarkBackground = ZcDarkBg
val DarkSurface = ZcDarkCard
val DarkSurfaceVariant = ZcDarkSubtle
val DarkOnSurface = ZcDarkInk
val DarkOutline = ZcDarkMuted
val DarkPrimaryContainer = Color(0xFF6B2B25)
val DarkOnPrimaryContainer = Color(0xFFFFD3CB)
val DarkSecondaryContainer = Color(0xFF1E5B51)
val DarkOnSecondaryContainer = Color(0xFFC9F2EA)
val DarkTertiaryContainer = Color(0xFF6A4406)
val DarkOnTertiaryContainer = Color(0xFFFFE4C2)

val SuccessGreen = ZcTeal
val ErrorSoft = Color(0xFFE2544E)
