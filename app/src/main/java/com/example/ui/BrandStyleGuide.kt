package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandCream
import com.example.ui.theme.BrandWhite
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.MutedCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandStyleGuideDialog(
    onDismiss: () -> Unit,
    dynamicPrimary: Color = PrimaryGreen,
    dynamicGold: Color = BrandGold
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandCream),
            containerColor = BrandCream,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.lms_launcher_fg),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "Brand Style Guide",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_close_brand_guide")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Guide", tint = dynamicPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BrandWhite
                    )
                )
            }
        ) { paddingValues ->
            var activeTab by remember { mutableStateOf(0) }
            val tabs = listOf(
                Triple("Overview", Icons.Outlined.Info, "btn_brand_tab_overview"),
                Triple("Logo", Icons.Outlined.FilterFrames, "btn_brand_tab_logo"),
                Triple("Colors", Icons.Outlined.Palette, "btn_brand_tab_colors"),
                Triple("Typography", Icons.Outlined.TextFields, "btn_brand_tab_typo"),
                Triple("Visual Style", Icons.Outlined.GridOn, "btn_brand_tab_visual")
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = BrandWhite,
                    contentColor = dynamicPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = dynamicPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, (title, icon, tag) ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp)) },
                            selectedContentColor = dynamicPrimary,
                            unselectedContentColor = MutedCharcoal,
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (activeTab) {
                        0 -> BrandOverviewSection(dynamicPrimary, dynamicGold)
                        1 -> BrandLogoSection(dynamicPrimary, dynamicGold)
                        2 -> BrandColorsSection(dynamicPrimary, dynamicGold)
                        3 -> BrandTypographySection(dynamicPrimary, dynamicGold)
                        4 -> BrandVisualSection(dynamicPrimary, dynamicGold)
                    }
                }
            }
        }
    }
}

@Composable
fun BrandOverviewSection(primary: Color, gold: Color) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACADEMY IDENTITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rooted & Ready Training & Development",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Growing Roses from Concrete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCharcoal,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = gold.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Motto: Rooted in Knowledge. Ready for Opportunity.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = gold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We are an elite training and educational institute centered on transforming talent through structured growth, meticulous compliance, and financial leadership. Our visual and structural identity stems from professional strength, trustworthiness, and natural nature-inspired evolution.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = DarkCharcoal
                    )
                }
            }
        }

        item {
            Text(
                text = "BRAND PERSONALITY PILLARS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        val pillars = listOf(
            Triple("Professional", "Corporate-grade excellence, rigorous adherence to compliance, and world-class educational standards.", Icons.Outlined.Shield),
            Triple("Trustworthy", "Rooted in absolute honesty, premium credentials, and verified corporate transparency.", Icons.Outlined.VerifiedUser),
            Triple("Growth", "A focus on incremental and developmental progress, moving individuals 'from concrete' to thriving roles.", Icons.Outlined.TrendingUp),
            Triple("Opportunity", "Bridging the gap between active student syllabus learning and industry-grade recruitment placement.", Icons.Outlined.Lightbulb),
            Triple("Integrity", "Complying with standard educational guidelines and financial accountability with transparent, secure models.", Icons.Outlined.Balance),
            Triple("Transformation", "Providing systemic structural advancement through executive-level mentorship and career elevation.", Icons.Outlined.AutoAwesome)
        )

        items(pillars.size) { index ->
            val (title, description, icon) = pillars[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primary)
                        Text(description, fontSize = 11.sp, color = MutedCharcoal, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BrandLogoSection(primary: Color, gold: Color) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE OFFICIAL APPROVED EMBLEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.lms_launcher_fg),
                        contentDescription = "Official Emblem",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "The Tree of Knowledge and Professional Growth",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = primary,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A robust tree rooted deep in compliance with an elegant canopy, paired with the timeless 'Rooted & Ready' wordmark.",
                        fontSize = 11.sp,
                        color = MutedCharcoal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, gold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = "Strict Policy", tint = gold)
                        Text(
                            text = "STRICT BRAND POLICY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val rules = listOf(
                        "Always use the uploaded logo as the only approved brand asset.",
                        "Never redesign or alter the layout, colors, or components.",
                        "Never redraw any element of the icon or wordmark.",
                        "Never stylize, filter, apply outer shadows or artificial gradients.",
                        "Use the original file as the logo on every single page."
                    )
                    rules.forEach { rule ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text("•", color = gold, fontWeight = FontWeight.Bold)
                            Text(rule, fontSize = 11.sp, color = DarkCharcoal, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandColorsSection(primary: Color, gold: Color) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "PRIMARY BRAND COLORS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primary,
                letterSpacing = 0.5.sp
            )
        }

        val colorsList = listOf(
            ColorSpec("Forest Green", "#1E5631", PrimaryGreen, "Primary identity color. Represents nature, professional growth, stability, and high educational standards. Used for headers, primary actions, and key structural points.", BrandWhite),
            ColorSpec("Classic Gold", "#C89B3C", BrandGold, "The refinement color. Indicates premium quality, high-grade transformation, and career opportunity. Used for highlights, accents, borders, and academic achievements.", DarkCharcoal),
            ColorSpec("Soft Cream", "#F8F4E9", BrandCream, "The core canvas background. Soft, eye-safe environment designed to prevent fatigue during continuous corporate learning sessions.", DarkCharcoal),
            ColorSpec("Pure White", "#FFFFFF", BrandWhite, "Interactive white contrast. Applied to cards, list panels, and text input boxes to offer stark and modern visual division.", DarkCharcoal),
            ColorSpec("Dark Text", "#2B2B2B", Color(0xFF2B2B2B), "Primary typographical shade. Highly legible neutral charcoal to maximize readability across mobile and expanded layouts.", BrandWhite)
        )

        items(colorsList.size) { index ->
            val colorSpec = colorsList[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp))
                            .background(colorSpec.color, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = colorSpec.hex,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorSpec.onColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(colorSpec.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primary)
                            Box(
                                modifier = Modifier
                                    .background(primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(colorSpec.hex, fontSize = 10.sp, color = gold, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(colorSpec.description, fontSize = 11.sp, color = MutedCharcoal, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

data class ColorSpec(
    val name: String,
    val hex: String,
    val color: Color,
    val description: String,
    val onColor: Color
)

@Composable
fun BrandTypographySection(primary: Color, gold: Color) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TYPOGRAPHY SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Headings: Modern Professional Serif",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Text(
                        text = "Use serif typefaces for main screen titles, headers, brand mottoes, and core landing headings to deliver a premium, academic, and highly professional corporate tone.",
                        fontSize = 11.sp,
                        color = MutedCharcoal,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCream, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Serif H1 Display", fontSize = 24.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = primary)
                            Text("Serif H2 Subtitle", fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, color = DarkCharcoal)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Body Text: Clean Sans-Serif",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    Text(
                        text = "Apply clean sans-serif typefaces for user logs, client forms, list items, course builder workspaces, and standard dashboard grids. This balances elegancy with razor-sharp mobile legibility.",
                        fontSize = 11.sp,
                        color = MutedCharcoal,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandCream, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sans-Serif Standard Body Text", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, color = DarkCharcoal)
                            Text("Sans-Serif Secondary Metadata", fontSize = 11.sp, fontFamily = FontFamily.SansSerif, color = MutedCharcoal)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ICONOGRAPHY STANDARDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val iconRules = listOf(
                        "Outline Style: Favor outlines (e.g. Icons.Outlined) rather than heavy default blocks to preserve visual minimalism.",
                        "Rounded Corners: Apply smooth rounded frames to interactive icons and status cells (minimum 8dp/12dp).",
                        "Soft Shadows: Give items minor depth, avoiding flat-color overlapping interfaces.",
                        "Organic Accent Tones: Strictly avoid bright neon, neon pinks, or synthetic gradients. Rely exclusively on Forest Green, Classic Gold, and Slate Charcoal."
                    )
                    iconRules.forEach { rule ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Approved", tint = primary, modifier = Modifier.size(16.dp))
                            Text(rule, fontSize = 11.sp, color = DarkCharcoal, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandVisualSection(primary: Color, gold: Color) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DESIGN STYLE PRINCIPLES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val designPrinciples = listOf(
                        Pair("Corporate & Premium", "Strictly aligned components, high contrast text ratios, clean professional layout frameworks."),
                        Pair("Minimal & Elegant", "Generous margins, negative space, avoiding crowded screens or colorful clutter."),
                        Pair("Education Focused", "Intuitive cohort filters, clean lesson checklists, explicit grading metrics, and visual learning aids."),
                        Pair("Nature Inspired", "Relying heavily on Forest Green (#1E5631), Warm Gold (#C89B3C), and Deep Earthy Charcoal to provide an organic, growth-focused environment.")
                    )
                    designPrinciples.forEach { (title, description) ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primary)
                            Text(description, fontSize = 11.sp, color = MutedCharcoal, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPROVED PHOTOGRAPHY MOTIFS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val motifs = listOf(
                        Pair(Icons.Outlined.Group, "Professional business people in executive settings."),
                        Pair(Icons.Outlined.School, "Corporate learning & modern training environments."),
                        Pair(Icons.Outlined.AccountBalance, "Financial services, accounting, and compliance focus."),
                        Pair(Icons.Outlined.TrendingUp, "Individual growth, mentorship, and career paths."),
                        Pair(Icons.Outlined.MilitaryTech, "Compliance, skills certifications, and corporate leadership.")
                    )
                    motifs.forEach { (icon, desc) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = gold, modifier = Modifier.size(18.dp))
                            Text(desc, fontSize = 11.sp, color = DarkCharcoal)
                        }
                    }
                }
            }
        }
    }
}
