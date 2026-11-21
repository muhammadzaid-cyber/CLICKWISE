package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.SentinelRankCrest
import com.example.ui.components.TechnicalPanel
import com.example.ui.theme.LocalClickwiseColors
import com.example.ui.viewmodel.QuizScreenState

@Composable
fun SecurityQuizScreen(
    state: QuizScreenState,
    language: AppLanguage,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val total = state.questions.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quiz_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 900.dp)
            ) {
                SectionEyebrow(text = if (language == AppLanguage.UR) "سائبر سیکیورٹی آزمائش" else "INTERACTIVE SECURITY QUIZ")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ClickwiseStrings.quizHeadline(language),
                    style = MaterialTheme.typography.displayMedium,
                    color = colors.textPrimary
                )
            }
        }

        if (state.isCompleted) {
            item {
                val ratio = if (total > 0) state.score.toFloat() / total else 0f
                val pct = (ratio * 100).toInt()
                val category = when {
                    pct >= 85 -> if (language == AppLanguage.UR) "ماہر ڈیجیٹل محافظ (Sentinel Level)" else "Sentinel Level — Sharp Threat Awareness"
                    pct >= 65 -> if (language == AppLanguage.UR) "محتاط صارف (Aware Defender)" else "Aware Defender — Solid Security Instincts"
                    pct >= 40 -> if (language == AppLanguage.UR) "ترقی پذیر سیکھنے والا (Developing)" else "Developing — Review Cyber Academy Lessons"
                    else -> if (language == AppLanguage.UR) "ابتدائی سطح (Needs Practice)" else "Foundational — Practice Recommended"
                }

                TechnicalPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 900.dp),
                    borderColor = colors.borderCyan,
                    containerColor = colors.bgSecondary,
                    showCornerReticles = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SentinelRankCrest(scoreRatio = ratio, size = 92.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "${state.score} / $total",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.limeSignal
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.UR)
                                "آپ نے 12 بنیادی سائبر سیکیورٹی منظرناموں کا جائزہ مکمل کر لیا ہے۔"
                            else
                                "You completed all $total cybersecurity scenarios covering phishing, OTP safety, URLs, and digital privacy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(22.dp))
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .height(48.dp)
                                .testTag("quiz_restart_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyanPrimary,
                                contentColor = colors.bgPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.UR) "کوئز دوبارہ شروع کریں" else "RESTART QUIZ",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        } else {
            val question = state.questions.getOrNull(state.currentIndex)
            if (question != null) {
                item {
                    TechnicalPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 900.dp),
                        borderColor = colors.borderCyan,
                        showCornerReticles = true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.UR)
                                    "سوال ${state.currentIndex + 1} از $total"
                                else
                                    "QUESTION ${state.currentIndex + 1} OF $total",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.cyanPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.limeSignal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.UR) question.topicUr else question.topic.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.limeSignal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        // 12-segment diagnostic progress strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(total) { segIdx ->
                                val segColor = when {
                                    segIdx < state.currentIndex -> colors.limeSignal
                                    segIdx == state.currentIndex -> colors.cyanPrimary
                                    else -> colors.surfaceDeep
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(segColor)
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = if (language == AppLanguage.UR) question.questionUr else question.question,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )

                        Spacer(Modifier.height(14.dp))
                        val options = if (language == AppLanguage.UR) question.optionsUr else question.options
                        options.forEachIndexed { idx, opt ->
                            val isSelected = state.selectedOptionIndex == idx
                            val isCorrect = idx == question.correctIndex
                            val borderColor = when {
                                state.isAnswerSubmitted && isCorrect -> colors.limeSignal
                                state.isAnswerSubmitted && isSelected && !isCorrect -> colors.alertRed
                                isSelected -> colors.cyanPrimary
                                else -> colors.borderMuted
                            }
                            val badgeColor = when {
                                state.isAnswerSubmitted && isCorrect -> colors.limeSignal
                                state.isAnswerSubmitted && isSelected && !isCorrect -> colors.alertRed
                                isSelected -> colors.cyanPrimary
                                else -> colors.textSecondary
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable(enabled = !state.isAnswerSubmitted) { onSelectOption(idx) }
                                    .testTag("quiz_option_$idx"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colors.surfaceDeep else colors.bgPrimary.copy(alpha = 0.60f),
                                border = BorderStroke(if (isSelected) 1.4.dp else 1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${('A' + idx)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = badgeColor
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.isAnswerSubmitted && isCorrect) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Correct option",
                                            tint = colors.limeSignal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = state.isAnswerSubmitted) {
                            val correct = state.selectedOptionIndex == question.correctIndex
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = (if (correct) colors.limeSignal else colors.warnAmber).copy(alpha = 0.10f),
                                border = BorderStroke(
                                    1.dp,
                                    (if (correct) colors.limeSignal else colors.warnAmber).copy(alpha = 0.45f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (correct) colors.limeSignal else colors.warnAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.UR) question.explanationUr else question.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        if (!state.isAnswerSubmitted) {
                            Button(
                                onClick = onSubmitAnswer,
                                enabled = state.selectedOptionIndex != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("quiz_submit_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.cyanPrimary,
                                    contentColor = colors.bgPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.UR) "جواب جمع کرائیں" else "SUBMIT ANSWER",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("quiz_next_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.cyanPrimary,
                                    contentColor = colors.bgPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (state.currentIndex == total - 1) {
                                        if (language == AppLanguage.UR) "نتیجہ دیکھیں" else "VIEW FINAL SCORE"
                                    } else {
                                        if (language == AppLanguage.UR) "اگلا سوال" else "NEXT QUESTION"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
