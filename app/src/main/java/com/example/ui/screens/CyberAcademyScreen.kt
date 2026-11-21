package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.data.EducationalContentRepository
import com.example.domain.model.AppLanguage
import com.example.domain.model.CyberLesson
import com.example.ui.components.LessonTopicGlyph
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.theme.LocalClickwiseColors

@Composable
fun CyberAcademyScreen(
    language: AppLanguage,
    completedLessonIds: Set<String>,
    initialSelectedLessonId: String?,
    onClearInitialLesson: () -> Unit,
    onMarkLessonCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val lessons = EducationalContentRepository.lessons
    var selectedLesson by remember { mutableStateOf<CyberLesson?>(null) }

    LaunchedEffect(initialSelectedLessonId) {
        if (initialSelectedLessonId != null) {
            selectedLesson = lessons.firstOrNull { it.id == initialSelectedLessonId }
            onClearInitialLesson()
        }
    }

    val activeLesson = selectedLesson
    if (activeLesson != null) {
        BackHandler {
            selectedLesson = null
            onClearInitialLesson()
        }
        LessonDetailView(
            lesson = activeLesson,
            language = language,
            isCompleted = activeLesson.id in completedLessonIds,
            onBack = {
                selectedLesson = null
                onClearInitialLesson()
            },
            onCompleteLesson = { onMarkLessonCompleted(activeLesson.id) },
            onNextLesson = {
                val next = lessons.getOrNull(lessons.indexOf(activeLesson) + 1)
                selectedLesson = next
                if (next == null) onClearInitialLesson()
            },
            hasNext = lessons.indexOf(activeLesson) < lessons.lastIndex,
            modifier = modifier
        )
        return
    }

    val total = lessons.size
    val completed = completedLessonIds.size
    val progressRatio = if (total > 0) completed.toFloat() / total else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("academy_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Curriculum Mastery Hero
        item {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1080.dp),
                borderColor = colors.borderCyan,
                containerColor = colors.bgSecondary,
                showCornerReticles = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SectionEyebrow(
                            text = if (language == AppLanguage.UR) "سائبر سیکیورٹی تعلیمی مرکز" else "CYBERSECURITY LEARNING HUB"
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = ClickwiseStrings.academyHeadline(language),
                            style = MaterialTheme.typography.displayMedium,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.UR)
                                "8 مختصر عملی اسباق میں فشنگ، جعلی اسکالرشپ، او ٹی پی اور لنک دھوکہ دہی کی پہچان سیکھیں۔"
                            else
                                "Master practical scam defense across 8 bilingual modules covering phishing, fake scholarships, OTP traps, and URL spoofing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier.size(72.dp),
                            color = colors.limeSignal,
                            trackColor = colors.surfaceDeep,
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$completed / $total",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.limeSignal
                            )
                            Text(
                                text = if (language == AppLanguage.UR) "سیشن" else "DONE",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = colors.limeSignal,
                    trackColor = colors.bgPrimary
                )
            }
        }

        // Responsive 2-Column Lesson Grid on Wide Screens / Single Column on Mobile
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1080.dp)
            ) {
                val isWide = maxWidth >= 700.dp
                if (isWide) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        lessons.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                pair.forEach { lesson ->
                                    LessonGridCard(
                                        lesson = lesson,
                                        isDone = lesson.id in completedLessonIds,
                                        language = language,
                                        onClick = { selectedLesson = lesson },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        lessons.forEach { lesson ->
                            LessonGridCard(
                                lesson = lesson,
                                isDone = lesson.id in completedLessonIds,
                                language = language,
                                onClick = { selectedLesson = lesson },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LessonGridCard(
    lesson: CyberLesson,
    isDone: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val accent = if (isDone) colors.limeSignal else colors.cyanPrimary

    TechnicalPanel(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("academy_lesson_${lesson.id}"),
        borderColor = if (isDone) colors.limeSignal.copy(alpha = 0.48f) else colors.borderMuted,
        accentRailColor = if (isDone) colors.limeSignal else colors.cyanPrimary.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LessonTopicGlyph(
                    lessonId = lesson.id,
                    accentColor = accent,
                    size = 38.dp
                )
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (language == AppLanguage.UR) "سبق 0${lesson.number}" else "MODULE 0${lesson.number}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            if (isDone) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = colors.limeSignal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.UR) "مکمل" else "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.limeSignal
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (language == AppLanguage.UR) lesson.titleUr else lesson.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (language == AppLanguage.UR) lesson.introUr else lesson.intro,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (language == AppLanguage.UR) "سبق کھولیں" else "OPEN MODULE",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Spacer(Modifier.width(5.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun LessonDetailView(
    lesson: CyberLesson,
    language: AppLanguage,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onCompleteLesson: () -> Unit,
    onNextLesson: () -> Unit,
    hasNext: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    var selectedOption by remember(lesson.id) { mutableStateOf<Int?>(null) }
    var checked by remember(lesson.id) { mutableStateOf(false) }

    val kc = lesson.knowledgeCheck
    val options = if (language == AppLanguage.UR) kc.optionsUr else kc.options
    val warningSigns = if (language == AppLanguage.UR) lesson.warningSignsUr else lesson.warningSigns
    val tips = if (language == AppLanguage.UR) lesson.protectionTipsUr else lesson.protectionTips

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lesson_detail_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("lesson_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (language == AppLanguage.UR) "تمام اسباق" else "ALL LESSONS")
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.limeSignal.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = if (language == AppLanguage.UR) "مکمل" else "COMPLETED",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.limeSignal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.UR) "سبق 0${lesson.number}" else "LESSON 0${lesson.number}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.cyanPrimary
                    )
                }
            }
        }

        // Concept & Warning Signs Split on Wide Screens
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
            ) {
                val isWide = maxWidth >= 780.dp
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LessonConceptPanel(
                            lesson = lesson,
                            language = language,
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                        )
                        LessonSignalsAndTipsPanel(
                            warningSigns = warningSigns,
                            tips = tips,
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LessonConceptPanel(
                            lesson = lesson,
                            language = language,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LessonSignalsAndTipsPanel(
                            warningSigns = warningSigns,
                            tips = tips,
                            language = language,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Interactive Knowledge Check
        item {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                borderColor = colors.borderCyan,
                showCornerReticles = true
            ) {
                SectionEyebrow(
                    text = if (language == AppLanguage.UR) "مختصر علمی جانچ" else "KNOWLEDGE CHECK"
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.UR) kc.promptUr else kc.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(10.dp))
                options.forEachIndexed { idx, opt ->
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == kc.correctIndex
                    val border = when {
                        checked && isCorrect -> colors.limeSignal
                        checked && isSelected && !isCorrect -> colors.alertRed
                        isSelected -> colors.cyanPrimary
                        else -> colors.borderMuted
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !checked) { selectedOption = idx }
                            .testTag("lesson_kc_option_$idx"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) colors.surfaceDeep else colors.bgPrimary.copy(alpha = 0.60f),
                        border = BorderStroke(1.dp, border)
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (!checked) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                checked = true
                                onCompleteLesson()
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("lesson_check_answer_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cyanPrimary,
                            contentColor = colors.bgPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (language == AppLanguage.UR) "جواب جانچیں" else "CHECK ANSWER")
                    }
                } else {
                    val correct = selectedOption == kc.correctIndex
                    Text(
                        text = if (correct) {
                            if (language == AppLanguage.UR) "درست! " else "Correct! "
                        } else {
                            if (language == AppLanguage.UR) "درست جواب کی وضاحت: " else "Explanation: "
                        } + (if (language == AppLanguage.UR) kc.explanationUr else kc.explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (correct) colors.limeSignal else colors.warnAmber
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (hasNext) {
                            Button(
                                onClick = onNextLesson,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.cyanPrimary,
                                    contentColor = colors.bgPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (language == AppLanguage.UR) "اگلا سبق" else "NEXT LESSON")
                            }
                        }
                        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(10.dp)) {
                            Text(if (language == AppLanguage.UR) "فہرست پر واپس" else "BACK TO ACADEMY")
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LessonConceptPanel(
    lesson: CyberLesson,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        borderColor = colors.borderCyan,
        showCornerReticles = true
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LessonTopicGlyph(
                lessonId = lesson.id,
                accentColor = colors.cyanPrimary,
                size = 44.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (language == AppLanguage.UR) lesson.titleUr else lesson.title,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (language == AppLanguage.UR) lesson.introUr else lesson.intro,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (language == AppLanguage.UR) lesson.explanationUr else lesson.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(14.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.bgPrimary.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.4f))
        ) {
            Text(
                text = if (language == AppLanguage.UR) lesson.illustrativeExampleUr else lesson.illustrativeExample,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.warnAmber,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun LessonSignalsAndTipsPanel(
    warningSigns: List<String>,
    tips: List<String>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        containerColor = colors.surfaceDeep
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "خطرناک علامات" else "WARNING SIGNS",
            accentColor = colors.warnAmber
        )
        Spacer(Modifier.height(10.dp))
        warningSigns.forEach { sign ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = colors.warnAmber, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(sign, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "عملی حفاظتی نکات" else "PRACTICAL PROTECTION TIPS",
            accentColor = colors.limeSignal
        )
        Spacer(Modifier.height(10.dp))
        tips.forEach { tip ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = colors.limeSignal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(tip, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            }
        }
    }
}
