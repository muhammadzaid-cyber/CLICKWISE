package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.data.EducationalContentRepository
import com.example.domain.model.AppLanguage
import com.example.domain.model.SampleScenario
import com.example.ui.components.AnimatedPulseBeacon
import com.example.ui.components.CyberRadarVisualizer
import com.example.ui.components.LessonTopicGlyph
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.theme.LocalClickwiseColors

@Composable
fun DashboardScreen(
    language: AppLanguage,
    reduceMotion: Boolean,
    completedLessonsCount: Int,
    sessionScansCount: Int,
    onStartAnalysis: () -> Unit,
    onExploreAcademy: () -> Unit,
    onOpenUrlInspector: () -> Unit,
    onOpenQuiz: () -> Unit,
    onLoadSampleInScanner: (SampleScenario) -> Unit,
    onOpenHighlightedLesson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val featuredLesson = EducationalContentRepository.lessons[2] // Fake Scholarship Offers
    val demoThreats = listOf(
        EducationalContentRepository.sampleScenarios[0], // Scholarship
        EducationalContentRepository.sampleScenarios[1], // Job/Internship
        EducationalContentRepository.sampleScenarios[2]  // Parcel/Payment
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Asymmetric Hero Command Center
        item {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1140.dp),
                borderColor = colors.borderCyan,
                containerColor = colors.bgSecondary,
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(0.dp),
                showCornerReticles = true
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isDesktop = maxWidth >= 840.dp
                    val isTablet = maxWidth >= 600.dp

                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isTablet) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (isDesktop) 32.dp else 24.dp,
                                        vertical = if (isDesktop) 28.dp else 22.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HeroTextColumn(
                                    language = language,
                                    onStartAnalysis = onStartAnalysis,
                                    onExploreAcademy = onExploreAcademy,
                                    isCompact = false,
                                    modifier = Modifier.weight(1.45f)
                                )
                                Spacer(Modifier.width(24.dp))
                                Box(
                                    modifier = Modifier.weight(0.85f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CyberRadarVisualizer(
                                        isScanning = false,
                                        reduceMotion = reduceMotion,
                                        size = if (isDesktop) 236.dp else 196.dp
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CyberRadarVisualizer(
                                    isScanning = false,
                                    reduceMotion = reduceMotion,
                                    size = 168.dp
                                )
                                Spacer(Modifier.height(18.dp))
                                HeroTextColumn(
                                    language = language,
                                    onStartAnalysis = onStartAnalysis,
                                    onExploreAcademy = onExploreAcademy,
                                    isCompact = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Integrated Telemetry Posture Strip
                        PostureTelemetryStrip(
                            language = language,
                            reduceMotion = reduceMotion,
                            sessionScansCount = sessionScansCount,
                            completedLessonsCount = completedLessonsCount
                        )
                    }
                }
            }
        }

        // 2. Asymmetric Console Pair (Primary Message Scanner 1.35x + Local URL Engine 1x on wide screens)
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1140.dp)
            ) {
                val isWide = maxWidth >= 660.dp
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickScannerModule(
                            language = language,
                            sessionScansCount = sessionScansCount,
                            onStartAnalysis = onStartAnalysis,
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight()
                        )
                        QuickUrlModule(
                            language = language,
                            onOpenUrlInspector = onOpenUrlInspector,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        QuickScannerModule(
                            language = language,
                            sessionScansCount = sessionScansCount,
                            onStartAnalysis = onStartAnalysis,
                            modifier = Modifier.fillMaxWidth()
                        )
                        QuickUrlModule(
                            language = language,
                            onOpenUrlInspector = onOpenUrlInspector,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 3. Threat Intelligence Test Bench (3-col Desktop, 2+1 Tablet, Stacked Mobile)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionEyebrow(
                        text = ClickwiseStrings.sampleThreatsTitle(language),
                        accentColor = colors.warnAmber
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.warnAmber.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = if (language == AppLanguage.UR) "محفوظ ڈیمو ٹیسٹ" else "SANDBOX SCENARIOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.warnAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = if (language == AppLanguage.UR)
                        "یہ فرضی تعلیمی مثالیں ہیں۔ کسی بھی مثال کو اسکینر میں لوڈ کر کے تجزیہ دیکھیں۔"
                    else
                        "Fictional educational threat patterns. Tap any scenario to prefill and inspect it inside the scanner.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    when {
                        maxWidth >= 820.dp -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                demoThreats.forEachIndexed { index, scenario ->
                                    SampleThreatCard(
                                        scenario = scenario,
                                        index = index,
                                        language = language,
                                        onClick = { onLoadSampleInScanner(scenario) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                        }

                        maxWidth >= 560.dp -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SampleThreatCard(
                                        scenario = demoThreats[0],
                                        index = 0,
                                        language = language,
                                        onClick = { onLoadSampleInScanner(demoThreats[0]) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    SampleThreatCard(
                                        scenario = demoThreats[1],
                                        index = 1,
                                        language = language,
                                        onClick = { onLoadSampleInScanner(demoThreats[1]) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                                SampleThreatCard(
                                    scenario = demoThreats[2],
                                    index = 2,
                                    language = language,
                                    onClick = { onLoadSampleInScanner(demoThreats[2]) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                demoThreats.forEachIndexed { index, scenario ->
                                    SampleThreatCard(
                                        scenario = scenario,
                                        index = index,
                                        language = language,
                                        onClick = { onLoadSampleInScanner(scenario) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Editorial Security Learning + Readiness Split
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1140.dp)
            ) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HighlightedLessonCard(
                            lessonId = featuredLesson.id,
                            lessonTitle = if (language == AppLanguage.UR) featuredLesson.titleUr else featuredLesson.title,
                            lessonIntro = if (language == AppLanguage.UR) featuredLesson.introUr else featuredLesson.intro,
                            completedCount = completedLessonsCount,
                            language = language,
                            onClick = { onOpenHighlightedLesson(featuredLesson.id) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        SecurityAwarenessPanel(
                            language = language,
                            onOpenQuiz = onOpenQuiz,
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        HighlightedLessonCard(
                            lessonId = featuredLesson.id,
                            lessonTitle = if (language == AppLanguage.UR) featuredLesson.titleUr else featuredLesson.title,
                            lessonIntro = if (language == AppLanguage.UR) featuredLesson.introUr else featuredLesson.intro,
                            completedCount = completedLessonsCount,
                            language = language,
                            onClick = { onOpenHighlightedLesson(featuredLesson.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SecurityAwarenessPanel(
                            language = language,
                            onOpenQuiz = onOpenQuiz,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostureTelemetryStrip(
    language: AppLanguage,
    reduceMotion: Boolean,
    sessionScansCount: Int,
    completedLessonsCount: Int
) {
    val colors = LocalClickwiseColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgPrimary.copy(alpha = 0.68f))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryChip(
                label = if (language == AppLanguage.UR) "App Check فعال" else "APP CHECK VERIFIED",
                color = colors.limeSignal,
                reduceMotion = reduceMotion
            )
            TelemetryChip(
                label = if (language == AppLanguage.UR) "مقامی URL انجن" else "LOCAL URL PARSER READY",
                color = colors.cyanPrimary,
                reduceMotion = reduceMotion
            )
            TelemetryChip(
                label = if (language == AppLanguage.UR) "زیرو اسٹوریج موڈ" else "ZERO PERSISTENT RETENTION",
                color = colors.indigoAccent,
                reduceMotion = reduceMotion
            )
            TelemetryChip(
                label = if (language == AppLanguage.UR) "اسکینز: $sessionScansCount • اسباق: $completedLessonsCount/8"
                else "SESSION: $sessionScansCount SCANS • $completedLessonsCount/8 MODULES",
                color = colors.textSecondary,
                reduceMotion = true
            )
        }
    }
}

@Composable
private fun TelemetryChip(
    label: String,
    color: Color,
    reduceMotion: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedPulseBeacon(color = color, reduceMotion = reduceMotion, size = 7.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroTextColumn(
    language: AppLanguage,
    onStartAnalysis: () -> Unit,
    onExploreAcademy: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    Column(modifier = modifier) {
        SectionEyebrow(
            text = ClickwiseStrings.tagline(language),
            accentColor = colors.cyanPrimary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = ClickwiseStrings.heroHeadline(language),
            style = if (isCompact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = ClickwiseStrings.heroSubtitle(language),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (language == AppLanguage.UR)
                "پیغام پر اعتماد کرنے سے پہلے خطرے کی علامات کو سمجھیں۔"
            else
                "Understand the warning signs before you trust the message.",
            style = MaterialTheme.typography.labelLarge,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(20.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onStartAnalysis,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(48.dp)
                    .testTag("dashboard_start_analysis_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanPrimary,
                    contentColor = colors.bgPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ClickwiseStrings.startAnalysis(language),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            OutlinedButton(
                onClick = onExploreAcademy,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .height(48.dp)
                    .testTag("dashboard_explore_academy_button"),
                border = BorderStroke(1.dp, colors.borderCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = ClickwiseStrings.exploreAcademy(language),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun QuickScannerModule(
    language: AppLanguage,
    sessionScansCount: Int,
    onStartAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier
            .clickable(onClick = onStartAnalysis)
            .testTag("dashboard_quick_scanner_card"),
        borderColor = colors.borderCyan,
        accentRailColor = colors.cyanPrimary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.cyanPrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = colors.cyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = ClickwiseStrings.quickScannerTitle(language),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.cyanPrimary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = if (language == AppLanguage.UR) "$sessionScansCount اس سیشن میں" else "$sessionScansCount SESSION",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.cyanPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = ClickwiseStrings.quickScannerDesc(language),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (language == AppLanguage.UR) "اسکینر کھولیں" else "OPEN MESSAGE SCANNER",
                style = MaterialTheme.typography.labelLarge,
                color = colors.cyanPrimary
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.cyanPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickUrlModule(
    language: AppLanguage,
    onOpenUrlInspector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier
            .clickable(onClick = onOpenUrlInspector)
            .testTag("dashboard_quick_url_card"),
        borderColor = colors.limeSignal.copy(alpha = 0.36f),
        accentRailColor = colors.limeSignal
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.limeSignal.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = colors.limeSignal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = ClickwiseStrings.navUrlInspector(language),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (language == AppLanguage.UR)
                "کسی بھی لنک کو کھولے بغیر اس کا ڈومین، سب ڈومین اور مشکوک ساخت مقامی طور پر جانچیں۔"
            else
                "Deconstruct hostname, subdomains, IP literals, and shorteners locally without visiting the link.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (language == AppLanguage.UR) "لنک جانچیں" else "INSPECT A LINK",
                style = MaterialTheme.typography.labelLarge,
                color = colors.limeSignal
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.limeSignal,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SampleThreatCard(
    scenario: SampleScenario,
    index: Int,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val railColor = when (index) {
        0 -> colors.alertRed
        1 -> colors.warnAmber
        else -> colors.indigoAccent
    }
    TechnicalPanel(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("dashboard_sample_${scenario.id}"),
        containerColor = colors.surfaceDeep,
        borderColor = railColor.copy(alpha = 0.35f),
        accentRailColor = railColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = railColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = if (language == AppLanguage.UR) scenario.categoryUr else scenario.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = railColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = colors.warnAmber,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (language == AppLanguage.UR) scenario.titleUr else scenario.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = (if (language == AppLanguage.UR) scenario.messageUr else scenario.message).take(112) + "…",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (language == AppLanguage.UR) "اسکینر میں جانچیں" else "LOAD IN SCANNER",
                style = MaterialTheme.typography.labelSmall,
                color = colors.cyanPrimary
            )
            Spacer(Modifier.width(5.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.cyanPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun HighlightedLessonCard(
    lessonId: String,
    lessonTitle: String,
    lessonIntro: String,
    completedCount: Int,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("dashboard_highlighted_lesson_card"),
        borderColor = colors.borderCyan
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionEyebrow(
                text = if (language == AppLanguage.UR) "نمایاں سبق" else "FEATURED MODULE",
                accentColor = colors.cyanPrimary
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.limeSignal.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "$completedCount / 8",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.limeSignal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LessonTopicGlyph(
                lessonId = lessonId,
                accentColor = colors.cyanPrimary,
                size = 42.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = lessonTitle,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = lessonIntro,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MenuBook, contentDescription = null, tint = colors.cyanPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.UR) "سبق پڑھیں" else "OPEN LESSON MODULE",
                style = MaterialTheme.typography.labelLarge,
                color = colors.cyanPrimary
            )
            Spacer(Modifier.width(5.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.cyanPrimary, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun SecurityAwarenessPanel(
    language: AppLanguage,
    onOpenQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        containerColor = colors.surfaceDeep,
        borderColor = colors.limeSignal.copy(alpha = 0.28f)
    ) {
        SectionEyebrow(
            text = ClickwiseStrings.awarenessTitle(language),
            accentColor = colors.limeSignal
        )
        Spacer(Modifier.height(12.dp))
        val tips = if (language == AppLanguage.UR) {
            listOf(
                "کبھی بھی کسی کالر، چیٹ یا غیر مصدقہ لنک پر او ٹی پی (OTP) یا پن شیئر نہ کریں۔",
                "اصلی اسکالرشپ یا انٹرن شپ کے لیے ذاتی موبائل والٹ میں فیس جمع نہیں کرائی جاتی۔",
                "مشتبہ پیغام میں موجود لنک کے بجائے سرکاری ویب سائٹ خود براؤزر میں ٹائپ کریں۔"
            )
        } else {
            listOf(
                "Never share OTPs, ATM PINs, or passwords with anyone who contacts you unexpectedly.",
                "Legitimate scholarships and internships do not ask for personal mobile-wallet registration fees.",
                "Always navigate to official university or bank portals manually instead of clicking message links."
            )
        }
        tips.forEach { tip ->
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = colors.limeSignal,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(14.dp))
        OutlinedButton(
            onClick = onOpenQuiz,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("dashboard_take_quiz_button"),
            border = BorderStroke(1.dp, colors.limeSignal.copy(alpha = 0.55f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colors.limeSignal.copy(alpha = 0.08f),
                contentColor = colors.limeSignal
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (language == AppLanguage.UR) "سیکیورٹی کوئز کھیلیں" else "TEST YOUR SKILLS IN QUIZ",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
        }
    }
}
